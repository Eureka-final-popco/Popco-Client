package com.popcoclient.event.controller;

import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.event.dto.response.ConnectionStatusResponseDto;
import com.popcoclient.event.service.impl.SseNotificationManagementService;
import com.popcoclient.event.service.impl.SseNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // 개발용, 운영에서는 구체적인 도메인 지정
public class SseController {

    private final SseNotificationManagementService sseManagementService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(
            @RequestParam(required = false) String clientId,
            HttpServletRequest request
    ) {
        return sseManagementService.createSseConnection(clientId, request);
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<ConnectionStatusResponseDto>> getConnectionStatus() {
        ConnectionStatusResponseDto status = sseManagementService.getConnectionStatus();
        return ResponseEntity.ok(ApiResponse.success("SSE 연결 상태 조회 성공", status));
    }
}
