package com.popcoclient.event.dto.response;

import com.popcoclient.event.entity.enums.QuizStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 퀴즈 상태 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizStatusResponseDto {
    private Long quizId;
    private Long questionId;
    private int currentSurvivors;
    private int maxSurvivors;
    private boolean isActive;
    private int remainingTime;
    private boolean isTimerRunning;
    private long timerStartedAt;
    private QuizStatus status;
}