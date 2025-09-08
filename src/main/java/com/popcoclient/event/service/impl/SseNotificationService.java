package com.popcoclient.event.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseNotificationService {
    
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<String> emitterIds = new CopyOnWriteArrayList<>();

    public SseEmitter createEmitter(String clientId) {
        return emitters.compute(clientId, (id, existingEmitter) -> {
            if (existingEmitter != null) {
                    return existingEmitter;
            }

            SseEmitter newEmitter = new SseEmitter(30 * 60 * 1000L);

            if (!emitterIds.contains(id)) {
                emitterIds.add(id);
            }

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

            try {
                newEmitter.send(SseEmitter.event()
                        .name("connect")
                        .data(Map.of(
                                "type", "CONNECTION_SUCCESS",
                                "message", "SSE 연결 성공!",
                                "clientId", id,
                                "timestamp", System.currentTimeMillis()
                        )));

                newEmitter.send(SseEmitter.event()
                        .name("welcome")
                        .data("환영합니다! 실시간 알림을 받을 준비가 되었습니다."));

                log.info("SSE connection established for client: {} (전체 {}개)", id, emitters.size());
            } catch (IOException e) {
                log.error("Failed to send connection message to client: {}", id, e);
                emitterIds.remove(id);
                newEmitter.completeWithError(e);
                throw new RuntimeException("SSE 연결 초기화 실패", e);
            }

            return newEmitter;
        });
    }
    
    private void removeEmitter(String clientId) {
        SseEmitter removedEmitter = emitters.remove(clientId);
        boolean removed = emitterIds.remove(clientId);

        if (removedEmitter != null || removed) {
            log.info("SSE emitter 제거 완료 - clientId: {} (남은 연결: {}개)", clientId, emitters.size());
        }
    }
    
    public void broadcastNotification(String messageJson) {
        try {
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

        List<String> deadConnections = new ArrayList<>();

        emitterIds.forEach(clientId -> {
            SseEmitter emitter = emitters.get(clientId);
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));

                } catch (IOException e) {
                    log.error("Failed to send notification to client: {}", clientId, e);
                    deadConnections.add(clientId);
                }
            }
        });

        // 죽은 연결들 일괄 정리
        deadConnections.forEach(this::removeEmitter);
        if (!deadConnections.isEmpty()) {
            log.info("죽은 연결 {}개 정리 완료", deadConnections.size());
        }
    }

    public void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

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