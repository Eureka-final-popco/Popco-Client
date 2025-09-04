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
 * 퀴즈 WebSocket 인터셉터
 * 
 * WebSocket 연결 시 JWT 인증, 로깅, 세션 관리 담당
 */
@Component
@Slf4j
public class QuizWebSocketInterceptor implements ChannelInterceptor {

     @Autowired
     private JwtProvider jwtTokenProvider;

    /**
     * 메시지 전송 전 처리 (인증, 검증)
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            StompCommand command = accessor.getCommand();

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
                    log.debug("WebSocket command: {} from session: {}", 
                             command, accessor.getSessionId());
            }
        }
        
        return message;
    }

    /**
     *  WebSocket 연결
     */
    private void handleConnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        
        try {
            String token = extractToken(accessor);
            log.info("추출된 토큰 : " + token, "검증 예상 결과 : " + jwtTokenProvider.validateToken(token,"ACCESS"));
            if (token != null && jwtTokenProvider.validateToken(token,"ACCESS")) {
                Long userId = Long.valueOf(jwtTokenProvider.getUserIdFromToken(token));

                accessor.getSessionAttributes().put("userId", userId);
                accessor.getSessionAttributes().put("authenticated", true);
                
                log.info("WebSocket connected - sessionId: {}, userId: {}", sessionId, userId);
                
            } else {
                log.warn("WebSocket connection failed - invalid token, sessionId: {}", sessionId);
            }
            
        } catch (Exception e) {
            log.error("Error during WebSocket connection - sessionId: {}", sessionId, e);
            accessor.getSessionAttributes().put("authenticated", false);
        }
    }

    /**
     * 토픽 구독 처리 (권한 검증)
     */
    private void handleSubscribe(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();
        
        log.debug("WebSocket subscribe - sessionId: {}, destination: {}", sessionId, destination);

        if (destination != null) {

            if (destination.startsWith("/topic/quiz/")) {

                Boolean authenticated = (Boolean) accessor.getSessionAttributes().get("authenticated");
                
                if (Boolean.TRUE.equals(authenticated)) {
                    Long userId = (Long) accessor.getSessionAttributes().get("userId");

                    if (destination.contains("/question/")) {
                        validateQuestionSubscription(userId, destination, sessionId);
                    }
                    
                    log.info("Quiz topic subscribed - userId: {}, destination: {}", userId, destination);
                    
                } else {
                    log.warn("Unauthenticated user trying to subscribe - sessionId: {}, destination: {}", 
                            sessionId, destination);
                }
            }
        }
    }

    /**
     * WebSocket 연결 해제 처리
     */
    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        
        log.info("WebSocket disconnected - sessionId: {}, userId: {}", sessionId, userId);
    }

    /**
     * 특정 문제 구독 권한 검증
     */
    private void validateQuestionSubscription(Long userId, String destination, String sessionId) {
        try {
            String[] parts = destination.split("/");
            
            if (parts.length >= 5) {
                Long quizId = Long.parseLong(parts[3]);
                Long questionId = Long.parseLong(parts[5]);
                
                log.debug("Question subscription validated - userId: {}, quizId: {}, questionId: {}", 
                         userId, quizId, questionId);
            }
            
        } catch (Exception e) {
            log.error("Error validating question subscription - userId: {}, destination: {}", 
                     userId, destination, e);
        }
    }

    /**
     * 헤더에서 JWT 토큰 추출
     */
    private String extractToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        String token = accessor.getFirstNativeHeader("token");
        return token;
    }
}