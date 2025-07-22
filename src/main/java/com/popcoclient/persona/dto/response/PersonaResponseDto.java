package com.popcoclient.persona.dto.response;

import com.popcoclient.persona.entity.Persona;
import com.popcoclient.review.dto.response.ReviewLikeResponseDto;
import com.popcoclient.review.entity.ReviewReaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaResponseDto {
    private Long id;
    private String name;
    private String description;
    private String babyImg;
    private String adultImg;

    public static PersonaResponseDto from(Persona persona) {
        return PersonaResponseDto.builder()
                .id(persona.getPersonaId())
                .name(persona.getName())
                .description(persona.getMainDescription())
                .babyImg(persona.getBabyImgPath())
                .adultImg(persona.getAdultImgPath())
                .build();
    }
}
