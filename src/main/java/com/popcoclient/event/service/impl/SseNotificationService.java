package com.popcoclient.event.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30분 타임아웃
        
        emitters.put(clientId, emitter);
        emitterIds.add(clientId);
        
        // 연결 종료 처리
        emitter.onCompletion(() -> {
            log.info("SSE connection completed for client: {}", clientId);
            removeEmitter(clientId);
        });
        
        emitter.onTimeout(() -> {
            log.info("SSE connection timed out for client: {}", clientId);
            removeEmitter(clientId);
        });
        
        emitter.onError((ex) -> {
            log.error("SSE connection error for client: {}", clientId, ex);
            removeEmitter(clientId);
        });
        
        // 연결 확인 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                .name("connect")
                .data("Connected to notification service"));
            log.info("SSE connection established for client: {}", clientId);
        } catch (IOException e) {
            log.error("Failed to send connection message to client: {}", clientId, e);
            emitter.completeWithError(e);
            removeEmitter(clientId);
            return null;
        }

        emitters.put(clientId, emitter);
        emitterIds.add(clientId);

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