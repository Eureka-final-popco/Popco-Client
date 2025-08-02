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
public class PopularContentResponseDto {
    @JsonProperty("contentId")
    private String contentId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("type")
    private String type;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("genres")
    private List<String> genres;

    @JsonProperty("platforms")
    private List<String> platforms;

    @JsonProperty("popularity_score")
    private Float popularityScore;
}
