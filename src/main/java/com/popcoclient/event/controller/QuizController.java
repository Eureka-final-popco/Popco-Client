package com.popcoclient.event.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.event.dto.response.QuizAlarmResponseDto;
import com.popcoclient.event.dto.response.QuizResponseDto;
import com.popcoclient.event.entity.Quiz;
import com.popcoclient.event.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "퀴즈", description = "퀴즈 관련 API")
@RestController
@RequestMapping("/quizs")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;
    private final JwtProvider jwtProvider;

    @Operation(
            summary = "퀴즈 알람 조회",
            description = "오늘 퀴즈가 있는지 확인하여 퀴즈 정보를 반환합니다."
    )
    @GetMapping("/alarm")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<QuizAlarmResponseDto>> getQuizAlarm() {
        Long userId = jwtProvider.getRequiredUserId();

        QuizAlarmResponseDto response = quizService.getQuizAlarm(userId);
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.success("오늘은 퀴즈가 없습니다.", null));
        }
        return ResponseEntity.ok(ApiResponse.success("퀴즈 알람 요청 성공", response));
    }


    @Operation(
            summary = "퀴즈 조회",
            description = "오늘의 퀴즈를 조회합니다."
    )
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<QuizResponseDto>> getQuiz() {
        Long userId = jwtProvider.getRequiredUserId();

        QuizResponseDto response = quizService.getQuiz();
        if (!response.isQuizPageAccess()) {
            return ResponseEntity.ok(ApiResponse.success("오늘은 퀴즈가 없습니다.", response));
        }
        return ResponseEntity.ok(ApiResponse.success("퀴즈 조회 성공", response));
    }
}
