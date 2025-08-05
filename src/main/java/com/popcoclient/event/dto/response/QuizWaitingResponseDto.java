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
    private Long remainingHour;          // 남은 시간 (시)
    private Long remainingMin;   // 남은 시간 (분)
    private Long remainingSec; // 남은 시간 (초)
    private Long remainingTime; // 남은 시간 (전체 초)
    private QuizStatus quizStatus; // 퀴즈 활성화 상태,     WAITING("대기 중"), ACTIVE("진행 중"), FINISHED("종료");
}
