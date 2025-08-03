package com.popcoclient.event.service.impl;

import com.popcoclient.event.dto.response.ConnectionStatusResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseNotificationManagementService {
    
    private final SseNotificationService sseNotificationService;
    
    public SseEmitter createSseConnection(String clientId, HttpServletRequest request) {
        if (clientId == null || clientId.trim().isEmpty()) {
            clientId = UUID.randomUUID().toString();
        }
        
        String userAgent = request.getHeader("User-Agent");
        String remoteAddr = request.getRemoteAddr();
        
        log.info("New SSE connection request - ClientId: {}, IP: {}, UserAgent: {}", 
                clientId, remoteAddr, userAgent);
        
        return sseNotificationService.createEmitter(clientId);
    }
    
    public ConnectionStatusResponseDto getConnectionStatus() {
        return new ConnectionStatusResponseDto(
            sseNotificationService.getActiveConnectionCount(),
            "SSE notification service is running"
        );
    }
}