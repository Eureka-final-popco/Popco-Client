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
    private int currentSurvivors;       // 현재 생존자 수
    private int maxSurvivors;           // 최대 생존자 수
    private boolean isActive;           // 퀴즈 활성 상태
    private int remainingTime;          // 남은 시간 (초)
    private boolean isTimerRunning;     // 타이머가 실행중인지
    private long timerStartedAt;        // 타이머 시작 시간 (밀리초)
    private QuizStatus status;              // 문제 상태 (대기 중/ 진행 중/ 종료)
}