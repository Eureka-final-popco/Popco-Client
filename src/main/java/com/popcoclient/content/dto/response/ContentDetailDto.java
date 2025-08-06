package com.popcoclient.content.dto.response;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.Genre;
import com.popcoclient.content.entity.enums.ReactionType;
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
    private ReactionType userReaction;

    public static ContentDetailDto of(Content content, List<Genre> genres, ReactionType userReaction) {
        return ContentDetailDto.builder()
                .id(content.getContentId().getId())
                .type(content.getContentId().getType())
                .title(content.getTitle())
                .overview(content.getOverview())
                .ratingAverage(content.getRatingAverage())
                .releaseDate(content.getReleaseDate())
                .ratingCount(content.getRatingCount())
                .backdropPath(content.getBackdropPath())
                .posterPath(content.getPosterPath())
                .runtime(content.getRuntime())
                .genres(GenreDto.from(genres))
                .casts(CastMemberDto.from(content.getCasts()))
                .crews(CrewDto.from(content.getCrews()))
                .videos(VideoDto.from(content.getVideos()))
                .watchProviders(WatchProviderDto.from(content.getWatchProviders()))
                .userReaction(userReaction)
                .build();
    }
}