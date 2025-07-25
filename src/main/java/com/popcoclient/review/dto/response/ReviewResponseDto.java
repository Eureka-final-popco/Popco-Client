package com.popcoclient.review.dto.response;

import com.popcoclient.review.entity.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReviewResponseDto {
    private Long reviewId;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerProfile;
    private LocalDateTime reviewDate;
    private ReviewStatus reviewStatus;
    private BigDecimal score;
    private String text;
    private Integer likeCount;
    private Boolean isLiked;
    private Boolean isAuthor;
}
