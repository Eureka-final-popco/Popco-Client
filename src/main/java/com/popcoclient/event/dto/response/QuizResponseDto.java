package com.popcoclient.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizResponseDto {
    private boolean isQuizPageAccess;
    private QuizDetailDto quizDetail;

    public static QuizResponseDto from(QuizDetailDto quizDetail, boolean isQuizPageAccess) {
        return QuizResponseDto.builder()
                .quizDetail(quizDetail)
                .isQuizPageAccess(isQuizPageAccess)
                .build();
    }
}
