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
    private Long quizId;
    private String quizName;
    private LocalDateTime quizStartTime;
    private LocalDateTime serverTime;

    public static QuizAlarmResponseDto from(Quiz quiz, LocalDateTime serverTime) {
        return QuizAlarmResponseDto.builder()
                .quizId(quiz.getQuizId())
                .quizName(quiz.getName())
                .quizStartTime(quiz.getStartAt())
                .serverTime(serverTime)
                .build();
    }
}
