package com.popcoclient.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReviewPageResponseDto {
    private int page;
    private int size;
    private int totalPages;
    private int totalReviews;
    private Double reviewAvgScore;
    private List<ReviewListResponseDto> reviewList;
    private boolean isLogin;
}
