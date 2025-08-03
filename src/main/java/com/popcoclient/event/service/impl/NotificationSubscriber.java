package com.popcoclient.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSubscriber implements MessageListener {
    
    private final SseNotificationService sseNotificationService;
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern);
            String messageBody = new String(message.getBody());
            
            log.info("Received message from channel {}: {}", channel, messageBody);

            sseNotificationService.broadcastNotification(messageBody);
            
        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }
}