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
public class PopcorithmMovieRecommendationDto {
    @JsonProperty("content_id")
    private Long contentId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("score")
    private Float score;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("genres")
    private List<String> genres;

    @JsonProperty("main_actors")
    private List<String> mainActors;

    @JsonProperty("directors")
    private List<String> directors;

     @JsonProperty("platforms")
     private List<String> platforms;
}