package com.popcoclient.event.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.event.dto.response.ConnectionStatusResponseDto;
import com.popcoclient.event.service.impl.SseNotificationManagementService;
import com.popcoclient.event.service.impl.SseNotificationService;
import com.popcoclient.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "SSE", description = "알림 관련 API")
@Slf4j
@CrossOrigin(origins = "*") // 개발용, 운영에서는 구체적인 도메인 지정
public class SseController {

    private final SseNotificationManagementService sseManagementService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "SSE 채널 구독 API", description = "프론트에서 new EventSource('/notifications/stream') 으로 이벤트 서버 연결, 이후 송신은 자동처리됨")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<?> streamNotifications(
            HttpServletRequest request
    ) {
        try {
            Long userId = jwtProvider.getRequiredUserId();
            SseEmitter emitter = sseManagementService.createSseConnection(userId.toString(), request);
            return ResponseEntity.ok(emitter);
        } catch (AuthenticationException ex) { // 혹은 JWT 관련 특정 예외 클래스
            log.error("SSE connection authentication failed: {}", ex.getMessage());
            // 예외 발생 시 SseEmitter가 아닌 ApiResponse를 반환
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON) // Content-Type을 JSON으로 명시
                    .body(ApiResponse.fail(ErrorCode.INVALID_TOKEN));
        }
    }

    @Operation(summary = "SSE 테스트 및 모니터링", description = "SSE 상태를 간단히 테스트해볼 수 있는 Api, 상태 파악 가능, healthCheck")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<ConnectionStatusResponseDto>> getConnectionStatus() {
        ConnectionStatusResponseDto status = sseManagementService.getConnectionStatus();
        return ResponseEntity.ok(ApiResponse.success("SSE 연결 상태 조회 성공", status));
    }
}
