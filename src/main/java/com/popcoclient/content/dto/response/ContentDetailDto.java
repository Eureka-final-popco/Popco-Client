package com.popcoclient.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDetailDto {
    private Long id;
    private String type;
    private String title;
    private String overview;
    private BigDecimal ratingAverage;
    private LocalDate releaseDate;
    private Integer ratingCount;
    private String backdropPath;
    private String posterPath;
    private Integer runtime;
    private List<GenreDto> genres;
    private List<CastMemberDto> casts;
    private List<CrewDto> crews;
    private List<VideoDto> videos;
    private List<WatchProviderDto> watchProviders;
}