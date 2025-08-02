package com.popcoclient.content.dto.response;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.DailyPopularContent;
import com.popcoclient.content.entity.enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyPopularContentResponseDto {
    private Long contentId;
    private String type;
    private int rank;
    private String title;
    private String overview;
    private String posterPath;
    private BigDecimal reviewRatingAvg;
    private String genres;
    private ReactionType userReaction;

    public static DailyPopularContentResponseDto of(
            DailyPopularContent popularContent, String genres, ReactionType userReaction) {
        return DailyPopularContentResponseDto.builder()
                .contentId(popularContent.getContent().getContentId().getId())
                .type(popularContent.getContent().getContentId().getType())
                .title(popularContent.getContent().getTitle())
                .rank(popularContent.getRanking())
                .overview(popularContent.getContent().getOverview())
                .posterPath(popularContent.getContent().getPosterPath())
                .reviewRatingAvg(popularContent.getContent().getRatingAverage())
                .genres(genres)
                .userReaction(userReaction)
                .build();
    }
}
