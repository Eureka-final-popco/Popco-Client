package com.popcoclient.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PopcorithmRecommendationApiResponseDto {
    private int userId;
    private List<PopularContentResponseDto> recommendations;
}
