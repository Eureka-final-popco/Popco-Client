package com.popcoclient.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DailyPopularContentResponseDto {
    private Long contentId;
    private String type;
    private String title;
    private String overview;
    private String backdropPath;
    private Integer ratingCount;
    private List<String> genres;
}
