package com.popcoclient.persona.dto.response;

import com.popcoclient.persona.entity.PersonaQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonaQuestionResponseDto {
    private Long questionId;
    private String content;
    private List<OptionResponseDto> options;

    public static PersonaQuestionResponseDto from(PersonaQuestion question, List<OptionResponseDto> options) {
        return PersonaQuestionResponseDto.builder()
                .questionId(question.getQuestionId())
                .content(question.getContent())
                .options(options)
                .build();
    }
}
