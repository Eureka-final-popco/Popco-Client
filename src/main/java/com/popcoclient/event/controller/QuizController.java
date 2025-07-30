package com.popcoclient.event.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.event.dto.response.QuizAlarmResponseDto;
import com.popcoclient.event.entity.Quiz;
import com.popcoclient.event.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;
    private final JwtProvider jwtProvider;

    @GetMapping("/alarm")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<QuizAlarmResponseDto>> getQuizAlarm() {
        Long userId = jwtProvider.getNullableUserId();

        QuizAlarmResponseDto response = quizService.getQuizAlarm(userId);
        return ResponseEntity.ok(ApiResponse.success("퀴즈 알람 요청 성공", response));
    }
}
