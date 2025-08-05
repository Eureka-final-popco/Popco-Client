package com.popcoclient.review.dto.response;

import com.popcoclient.review.entity.Review;
import com.popcoclient.review.entity.enums.ReviewStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyReviewResponseDto {
    private Long reviewId;
    private Long contentId;
    private String contentType;
    private String title;
    private String posterPath;
    private BigDecimal score;
    private String text;
    private LocalDateTime createdAt;
    private ReviewStatus status;
    private Integer likeCount;
    private boolean isLiked;

    public static MyReviewResponseDto from(Review r, boolean isLiked) {
        return MyReviewResponseDto.builder()
                .reviewId(r.getReviewId())
                .contentId(r.getContent().getContentId().getId())
                .contentType(r.getContent().getContentId().getType())
                .title(r.getContent().getTitle())
                .posterPath(r.getContent().getPosterPath())
                .score(r.getScore())
                .text(r.getText())
                .createdAt(r.getCreatedAt())
                .likeCount(r.getLikeCount())
                .status(r.getStatus())
                .isLiked(isLiked)
                .build();
    }
}
