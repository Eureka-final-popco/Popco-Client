package com.popcoclient.content.dto.response;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.ContentReaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikedContentResponseDto {

    private Long contentId;
    private String contentType;
    private String title;
    private String overview;
    private BigDecimal ratingAverage;
    private LocalDate releaseDate;
    private Integer ratingCount;
    private String backdropPath;
    private String posterPath;
    private Integer runtime;
//    private Set<Integer> genreIds;
    private LocalDateTime likedAt;

    public static LikedContentResponseDto from(ContentReaction reaction) {
        Content content = reaction.getContent();
        return LikedContentResponseDto.builder()
                .contentId(content.getContentId().getId())
                .contentType(content.getContentId().getType())
                .title(content.getTitle())
                .overview(content.getOverview())
                .ratingAverage(content.getRatingAverage())
                .releaseDate(content.getReleaseDate())
                .ratingCount(content.getRatingCount())
                .backdropPath(content.getBackdropPath())
                .posterPath(content.getPosterPath())
                .runtime(content.getRuntime())
//                .genreIds(content.getGenreIds())
                .likedAt(reaction.getCreatedAt())
                .build();
    }
}
