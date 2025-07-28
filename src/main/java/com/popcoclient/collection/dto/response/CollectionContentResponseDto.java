package com.popcoclient.collection.dto.response;

import com.popcoclient.collection.entity.CollectionContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionContentResponseDto {

    private Long collectionContentId;
    private Long contentId;
    private String contentType;
    private String title;
    private String overview;
    private BigDecimal ratingAverage;
    private LocalDate releaseDate;
    private String posterPath;
    private LocalDateTime addedAt;

    public static CollectionContentResponseDto from(CollectionContent collectionContent) {
        return CollectionContentResponseDto.builder()
                .collectionContentId(collectionContent.getCollectionContentId())
                .contentId(collectionContent.getContent().getContentId().getId())
                .contentType(collectionContent.getContent().getContentId().getType())
                .title(collectionContent.getContent().getTitle())
                .overview(collectionContent.getContent().getOverview())
                .ratingAverage(collectionContent.getContent().getRatingAverage())
                .releaseDate(collectionContent.getContent().getReleaseDate())
                .posterPath(collectionContent.getContent().getPosterPath())
                .addedAt(collectionContent.getCreatedAt())
                .build();
    }
}
