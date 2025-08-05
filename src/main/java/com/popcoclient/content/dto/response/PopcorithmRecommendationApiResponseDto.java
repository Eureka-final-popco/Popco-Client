package com.popcoclient.content.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("recommendations")
    private List<PopcorithmMovieRecommendationDto> recommendations;

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("generated_at")
    private String generatedAt;
}
