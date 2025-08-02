package com.popcoclient.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrentQuestionResponseDto {
    private Long quizId;
    private Long questionId;
    private String content;
    private Long quizQuestionId;
    private Integer firstCapacity;
    private List<QuizQuestionsOptionsResponseDto> options;
}
