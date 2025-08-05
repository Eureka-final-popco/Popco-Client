package com.popcoclient.event.component;

import com.popcoclient.event.service.impl.SseNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeartbeatScheduler {
    
    private final SseNotificationService sseNotificationService;

    @Scheduled(fixedRate = 60000)
    public void sendHeartbeat() {
        if (sseNotificationService.getActiveConnectionCount() > 0) {
            sseNotificationService.sendHeartbeat();
            log.debug("Heartbeat sent to {} active connections", 
                     sseNotificationService.getActiveConnectionCount());
        }
    }
}