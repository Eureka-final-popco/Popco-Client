package com.popcoclient.review.dto.response;

import com.popcoclient.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class MyReviewResponseDto {
    private Long reviewId;
    private Long contentId;
    private String contentType;
    private String title;
    private String posterPath;
    private BigDecimal score;
    private String text;
    private LocalDateTime createdAt;

    public static MyReviewResponseDto of(Review r) {
        return MyReviewResponseDto.builder()
                .reviewId(r.getReviewId())
                .contentId(r.getContent().getContentId().getId())
                .contentType(r.getContent().getContentId().getType())
                .title(r.getContent().getTitle())
                .posterPath(r.getContent().getPosterPath())
                .score(r.getScore())
                .text(r.getText())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
