package com.popcoclient.event.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseNotificationService {
    
    private final ObjectMapper objectMapper;
    
    // 활성 SSE 연결들을 저장
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<String> emitterIds = new CopyOnWriteArrayList<>();
    
    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = emitters.computeIfAbsent(clientId, id -> {
            log.info("🆕 새로운 SSE 연결을 생성합니다 - clientId: {}", id);

            SseEmitter newEmitter = new SseEmitter(30 * 60 * 1000L); // 30분 타임아웃

            // emitterIds에도 추가 (중복 체크)
            if (!emitterIds.contains(id)) {
                emitterIds.add(id);
            }

            // 연결 종료 처리
            newEmitter.onCompletion(() -> {
                log.info("SSE connection completed for client: {}", id);
                removeEmitter(id);
            });

            newEmitter.onTimeout(() -> {
                log.info("SSE connection timed out for client: {}", id);
                removeEmitter(id);
            });

            newEmitter.onError((ex) -> {
                log.error("SSE connection error for client: {}", id, ex);
                removeEmitter(id);
            });

            // 연결 확인 메시지 전송
            try {
                newEmitter.send(SseEmitter.event()
                        .name("connect")
                        .data(Map.of(
                                "type", "CONNECTION_SUCCESS",
                                "message", "SSE 연결 성공!",
                                "clientId", id,
                                "timestamp", System.currentTimeMillis()
                        )));

                // 🎯 추가로 환영 메시지도 전송
                newEmitter.send(SseEmitter.event()
                        .name("welcome")
                        .data("환영합니다! 실시간 알림을 받을 준비가 되었습니다."));
            } catch (IOException e) {
                log.error("Failed to send connection message to client: {}", id, e);
                newEmitter.completeWithError(e);
                // computeIfAbsent 내부에서 예외 발생 시 null 반환하여 맵에 저장되지 않도록
                emitterIds.remove(id);
                throw new RuntimeException("SSE 연결 초기화 실패", e);
            }

            return newEmitter;
        });

        // 이미 존재하는 연결인 경우
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("reconnect")
                        .data("기존 연결 재사용됨"));
            } catch (IOException e) {
                log.warn("기존 연결에 메시지 전송 실패: {}", clientId);
            }
        }

        return emitter;
    }
    
    private void removeEmitter(String clientId) {
        emitters.remove(clientId);
        emitterIds.remove(clientId);
        log.info("Removed SSE emitter for client: {}. Active connections: {}", clientId, emitters.size());
    }
    
    public void broadcastNotification(String messageJson) {
        try {
            // JSON을 그대로 파싱해서 Object로 전송
            Object notification = objectMapper.readValue(messageJson, Object.class);
            broadcastNotification(notification);
        } catch (Exception e) {
            log.error("Failed to parse notification message: {}", messageJson, e);
        }
    }
    
    public void broadcastNotification(Object notification) {
        if (emitters.isEmpty()) {
            log.info("No active SSE connections to broadcast notification");
            return;
        }
        
        log.info("Broadcasting notification to {} clients", emitters.size());
        
        // 모든 연결된 클라이언트에게 알림 전송
        emitterIds.forEach(clientId -> {
            SseEmitter emitter = emitters.get(clientId);
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
                    log.debug("Notification sent to client: {}", clientId);
                } catch (IOException e) {
                    log.error("Failed to send notification to client: {}", clientId, e);
                    removeEmitter(clientId);
                }
            }
        });
    }
    
    public void sendHeartbeat() {
        emitterIds.forEach(clientId -> {
            SseEmitter emitter = emitters.get(clientId);
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data("ping"));
                } catch (IOException e) {
                    log.error("Failed to send heartbeat to client: {}", clientId, e);
                    removeEmitter(clientId);
                }
            }
        });
    }
    
    public int getActiveConnectionCount() {
        return emitters.size();
    }
}