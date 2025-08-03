package com.popcoclient.persona.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonaDetailDto {
    private String name;
    private BigDecimal score;
    private String description;
    private String tag;
    private String personaGenre; // List<String> → String으로 변경
}
