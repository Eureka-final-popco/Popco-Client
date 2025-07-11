package com.popcoclient.review.dto.response;

import com.popcoclient.review.entity.Review;
import com.popcoclient.user.entity.UserDetail;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.persistence.criteria.CriteriaBuilder;
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
public class ReviewListResponseDto {
    private Long reviewId;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerProfile;
    private String reviewerPersona;
    private LocalDateTime reviewDate;
    private BigDecimal score;
    private String text;
    private Integer likeCount;
    private Boolean isLiked;
    private Boolean isAuthor;
}
