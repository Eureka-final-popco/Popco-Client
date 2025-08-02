package com.popcoclient.event.dto.response;

import com.popcoclient.event.entity.Quiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizAlarmResponseDto {
    private QuizAlarmDto quizAlarmDto;
    private boolean existTodayQuiz;
    private boolean isAlarm;

    public static QuizAlarmResponseDto from(QuizAlarmDto quizAlarmDto, boolean existTodayQuiz, boolean isAlarm) {
        return QuizAlarmResponseDto.builder()
                .quizAlarmDto(quizAlarmDto)
                .existTodayQuiz(existTodayQuiz)
                .isAlarm(isAlarm)
                .build();
    }
}
