package com.popcoclient.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResponseDto {
    private Long quizId;
    private Long questionId;
    private String content;
    private Integer firstCapacity;
    private List<QuizQuestionsOptionsResponseDto> options;
}
