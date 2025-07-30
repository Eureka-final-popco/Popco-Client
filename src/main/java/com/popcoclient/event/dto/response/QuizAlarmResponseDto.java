package com.popcoclient.event.dto.response;

import com.popcoclient.event.entity.Quiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizAlarmResponseDto {
    private Long QuizId;
    private String QuizName;
    private boolean showAlert;

    public static QuizAlarmResponseDto from(Quiz quiz, boolean showAlert) {
        return QuizAlarmResponseDto.builder()
                .QuizId(quiz.getQuizId())
                .QuizName(quiz.getName())
                .showAlert(showAlert)
                .build();
    }
}
