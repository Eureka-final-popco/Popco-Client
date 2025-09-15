package com.popcoclient.event.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.event.dto.response.ConnectionStatusResponseDto;
import com.popcoclient.event.service.impl.SseNotificationManagementService;
import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "SSE", description = "알림 관련 API")
@Slf4j
@CrossOrigin(origins = "*")
public class SseController {

    private final SseNotificationManagementService sseManagementService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "SSE 채널 구독 API", description = "프론트에서 new EventSource('/notifications/stream') 으로 이벤트 서버 연결, 이후 송신은 자동처리됨")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(
            @RequestParam String token,
            HttpServletRequest request) {
        Long userId = Long.parseLong(jwtProvider.getUserIdFromToken(token));
        SseEmitter emitter = sseManagementService.createSseConnection(userId.toString(), request);

        if (emitter == null) {
            log.error("SseEmitter is null after creation for user: {}. Throwing exception to be handled globally.", userId);

            throw new BusinessException(ErrorCode.SSE_CONNECTION_ERROR);
        }

        log.info("Returning SseEmitter object directly for user {}", userId);

        return emitter;
    }

    @Operation(summary = "SSE 테스트 및 모니터링", description = "SSE 상태를 간단히 테스트해볼 수 있는 Api, 상태 파악 가능, healthCheck")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<ConnectionStatusResponseDto>> getConnectionStatus() {
        ConnectionStatusResponseDto status = sseManagementService.getConnectionStatus();
        return ResponseEntity.ok(ApiResponse.success("SSE 연결 상태 조회 성공", status));
    }

    @GetMapping(value = "/test-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testStream() {
        log.info("### ENTERING /test-stream ENDPOINT ###");
        SseEmitter emitter = new SseEmitter(60_000L);
        try {
            emitter.send(SseEmitter.event().name("test-connect").data("Connection test successful!"));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }
}
