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
    private String aiResponse;
    private String myPersonaTags;
    private List<Genre> myPersonaGenres;
    private String myPersonaDescription;

    private String mainPersonaName;
    private Integer mainPersonaPercent; // % 로 계산 후 숫자 전달 예시 (58)
    private String mainPersonaImgPath;

    private String subPersonaName;
    private Integer subPersonaPercent; // % 로 계산 후 숫자 전달 예시 (42)
    private String subPersonaImgPath;
}
