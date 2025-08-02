package com.popcoclient.common.interceptor;

import com.popcoclient.auth.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * 🔐 퀴즈 WebSocket 인터셉터
 * 
 * WebSocket 연결 시 JWT 인증, 로깅, 세션 관리를 담당합니다.
 */
@Component
@Slf4j
public class QuizWebSocketInterceptor implements ChannelInterceptor {

//     JWT 관련 서비스 (기존에 있는 것 활용)
     @Autowired
     private JwtProvider jwtTokenProvider;

    /**
     * 📨 메시지 전송 전 처리 (인증, 검증)
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            
            // 💬 명령어별 처리
            switch (command) {
                case CONNECT:
                    handleConnect(accessor);
                    break;
                case SUBSCRIBE:
                    handleSubscribe(accessor);
                    break;
                case DISCONNECT:
                    handleDisconnect(accessor);
                    break;
                default:
                    // 기타 명령어는 로깅만
                    log.debug("WebSocket command: {} from session: {}", 
                             command, accessor.getSessionId());
            }
        }
        
        return message;
    }

    /**
     * 🔌 WebSocket 연결 처리
     */
    private void handleConnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        
        try {
            // 🔐 JWT 토큰 검증
            String token = extractToken(accessor);
            
            if (token != null && jwtTokenProvider.validateToken(token,"ACCESS")) {
                // ✅ 인증 성공
                Long userId = Long.valueOf(jwtTokenProvider.getUserIdFromToken(token));
                
                // 세션에 사용자 정보 저장
                accessor.getSessionAttributes().put("userId", userId);
                accessor.getSessionAttributes().put("authenticated", true);
                
                log.info("WebSocket connected - sessionId: {}, userId: {}", sessionId, userId);
                
            } else {
                // ❌ 인증 실패
                log.warn("WebSocket connection failed - invalid token, sessionId: {}", sessionId);
                
                // 인증 실패 시에도 연결은 허용 (비회원도 구경 가능하게 할 수 있음)
                accessor.getSessionAttributes().put("authenticated", false);
            }
            
        } catch (Exception e) {
            log.error("Error during WebSocket connection - sessionId: {}", sessionId, e);
            accessor.getSessionAttributes().put("authenticated", false);
        }
    }

    /**
     * 📡 토픽 구독 처리 (권한 검증)
     */
    private void handleSubscribe(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();
        
        log.debug("WebSocket subscribe - sessionId: {}, destination: {}", sessionId, destination);
        
        // 🔍 구독 권한 검증
        if (destination != null) {
            
            // 퀴즈 관련 토픽 구독 시 권한 체크
            if (destination.startsWith("/topic/quiz/")) {
                
                // 인증된 사용자만 구독 가능하도록 설정 (선택적)
                Boolean authenticated = (Boolean) accessor.getSessionAttributes().get("authenticated");
                
                if (Boolean.TRUE.equals(authenticated)) {
                    Long userId = (Long) accessor.getSessionAttributes().get("userId");
                    
                    // 📝 특정 문제 토픽 구독 시 권한 검증
                    if (destination.contains("/question/")) {
                        validateQuestionSubscription(userId, destination, sessionId);
                    }
                    
                    log.info("Quiz topic subscribed - userId: {}, destination: {}", userId, destination);
                    
                } else {
                    log.warn("Unauthenticated user trying to subscribe - sessionId: {}, destination: {}", 
                            sessionId, destination);
                    // 구독 차단은 하지 않음 (관전 모드 허용)
                }
            }
        }
    }

    /**
     * 🔌 WebSocket 연결 해제 처리
     */
    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        
        log.info("WebSocket disconnected - sessionId: {}, userId: {}", sessionId, userId);
        
        // 🧹 세션 정리 작업 (필요 시)
        // - 진행 중인 퀴즈에서 사용자 상태 업데이트
        // - 재입장 방지를 위한 Redis 상태 변경 등
        
        if (userId != null) {
            // TODO: 퀴즈 세션 매니저에 연결 해제 알림
            // quizSessionManager.handleUserDisconnect(userId, sessionId);
        }
    }

    /**
     * 🎯 특정 문제 구독 권한 검증
     */
    private void validateQuestionSubscription(Long userId, String destination, String sessionId) {
        try {
            // destination 예시: "/topic/quiz/123/question/2"
            String[] parts = destination.split("/");
            
            if (parts.length >= 5) {
                Long quizId = Long.parseLong(parts[3]);
                Long questionId = Long.parseLong(parts[5]);
                
                // 🔍 이전 문제 통과 여부 확인 로직
                // boolean canSubscribe = quizProgressService.canAccessQuestion(userId, quizId, questionId);
                
                // if (!canSubscribe) {
                //     log.warn("User cannot access question - userId: {}, quizId: {}, questionId: {}", 
                //              userId, quizId, questionId);
                //     // 구독 차단 로직 (필요 시)
                // }
                
                log.debug("Question subscription validated - userId: {}, quizId: {}, questionId: {}", 
                         userId, quizId, questionId);
            }
            
        } catch (Exception e) {
            log.error("Error validating question subscription - userId: {}, destination: {}", 
                     userId, destination, e);
        }
    }

    /**
     * 🔐 헤더에서 JWT 토큰 추출
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // Authorization 헤더에서 토큰 추출
        String authorization = accessor.getFirstNativeHeader("Authorization");
        
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        
        // URL 파라미터에서도 토큰 추출 시도 (WebSocket 연결 시 헤더 제한 때문)
        String token = accessor.getFirstNativeHeader("token");
        return token;
    }

//    /**
//     * 🔍 JWT 토큰 유효성 검증
//     */
//    private boolean isValidToken(String token) {
//        try {
//             return jwtTokenProvider.validateToken(token, "ACCESS");
//
//        } catch (Exception e) {
//            log.error("JWT validation error", e);
//            return false;
//        }
//    }

//    /**
//     * 👤 JWT 토큰에서 사용자 ID 추출
//     */
//    private Long extractUserIdFromToken(String token) {
//        try {
//            // TODO: 기존 JWT 서비스 활용
//             String userIdStr = jwtTokenProvider.getUserIdFromToken(token);
//             return Long.parseLong(userIdStr);
//
//        } catch (Exception e) {
//            log.error("Error extracting user ID from token", e);
//            return null;
//        }
//    }
}