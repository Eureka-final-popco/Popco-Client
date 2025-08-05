package com.popcoclient.content.dto.response;

import com.popcoclient.content.document.ContentFilterDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FilteredContentItemResponseDto {
    private Long contentId;
    private String title;
    private String contentType;
    private List<String> genres;
    private BigDecimal ratingAverage;
    private LocalDate releaseDate;
    private String posterPath;
    private List<String> platforms;
    private Boolean isLiked;
    private Boolean isDisliked;

    public FilteredContentItemResponseDto(ContentFilterDocument doc, boolean isLiked, boolean isDisliked) {
        this.contentId = doc.getContentId();
        this.title = doc.getTitle();
        this.contentType = doc.getContentType();
        this.genres = doc.getGenres();
        this.ratingAverage = doc.getRatingAverage();
        this.releaseDate = doc.getReleaseDate();
        this.posterPath = doc.getPosterPath();
        this.platforms = doc.getPlatforms();
        this.isLiked = isLiked;
        this.isDisliked = isDisliked;
    }
}
