package com.popcoclient.content.dto.response;

import com.popcoclient.content.document.ContentDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentSearchResponse {
    private String id;
    private String title;
    private String overview;
    private String contentType;
    private Long contentId;
    private BigDecimal ratingAverage;
    private LocalDate releaseDate;
    private String posterPath;
    private List<ContentDocument.CastInfo> cast;
    private List<ContentDocument.CrewInfo> crew;

    private Boolean isLiked;

    public static ContentSearchResponse from(ContentDocument document) {
        return ContentSearchResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .overview(document.getOverview())
                .contentType(document.getContentType())
                .contentId(document.getContentId())
                .ratingAverage(document.getRatingAverage())
                .releaseDate(document.getReleaseDate())
                .posterPath(document.getPosterPath())
                .cast(document.getCast())
                .crew(document.getCrew())
                .isLiked(false) // 기본값은 null
                .build();
    }

    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }
}