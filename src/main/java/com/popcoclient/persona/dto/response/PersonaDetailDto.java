package com.popcoclient.persona.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonaDetailDto {
    private String name;
    private BigDecimal score;
    private String description;
    private String tag;
    private String personaGenre;
}
