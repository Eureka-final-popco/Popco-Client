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
public class QuizDetailDto {
    private Long quizId;
    private String quizName;
    private String quizContentPosterUrl;
    private String quizReward;
    private LocalDateTime quizStartTime;
    private LocalDateTime serverTime;

    public static QuizDetailDto from(Quiz quiz, LocalDateTime serverTime) {
        return QuizDetailDto.builder()
                .quizId(quiz.getQuizId())
                .quizName(quiz.getName())
                .quizContentPosterUrl(quiz.getQuizPosterPath())
                .quizReward(quiz.getQuizReward())
                .quizStartTime(quiz.getStartAt())
                .serverTime(serverTime)
                .build();
    }
}
