package com.popcoclient.persona.dto.response;

import com.popcoclient.persona.entity.Persona;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonaListResponseDto {
    private Long count;
    private List<PersonaResponseDto> persona;

    public static PersonaListResponseDto of(List<PersonaResponseDto> persona, Long count) {
        return PersonaListResponseDto.builder()
                .persona(persona)
                .count(count)
                .build();
    }
}
