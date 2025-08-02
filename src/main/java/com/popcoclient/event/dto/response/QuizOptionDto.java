package com.popcoclient.event.dto.response;

import com.popcoclient.event.entity.QuizOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizOptionDto {
    private Long optionId;
    private String optionContent;
    private boolean isCorrect;

    public static QuizOptionDto from(QuizOption quizOption) {
        return QuizOptionDto.builder()
                .optionId(quizOption.getOptionId().getOptionId())
                .optionContent(quizOption.getContent())
                .isCorrect(quizOption.getIsCorrect())
                .build();
    }
}
