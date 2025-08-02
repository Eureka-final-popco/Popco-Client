package com.popcoclient.persona.dto.response;

import com.popcoclient.content.entity.Genre;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPersonaResponseDto {
    private String myPersonaName;
    private String myPersonaImgPath;
    private String myPersonaTags;
    private List<String> myPersonaGenres;
    private String myPersonaDescription;

    private String mainPersonaName;
    private Double mainPersonaPercent; // % 로 계산 후 숫자 전달 예시 (58)
    private String mainPersonaImgPath;

    private String subPersonaName;
    private Double subPersonaPercent; // % 로 계산 후 숫자 전달 예시 (42)
    private String subPersonaImgPath;

}
