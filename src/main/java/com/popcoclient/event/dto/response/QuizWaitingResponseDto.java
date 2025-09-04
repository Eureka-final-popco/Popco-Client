package com.popcoclient.event.dto.response;

import com.popcoclient.event.entity.enums.QuizStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizWaitingResponseDto {
    private Long quizId;
    private Long remainingHour;
    private Long remainingMin;
    private Long remainingSec;
    private Long remainingTime;
    private QuizStatus quizStatus; // 퀴즈 활성화 상태,     WAITING("대기 중"), ACTIVE("진행 중"), FINISHED("종료");
}
