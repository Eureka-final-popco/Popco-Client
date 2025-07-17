package com.popcoclient.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReviewPageResponseDto {
    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Long totalReviews;
    private Double reviewAvgScore;
    private List<ReviewResponseDto> reviewList;
    private Boolean isLogin;

    public static ReviewPageResponseDto of(Page<ReviewResponseDto> reviewPage,
                                           Double avgScore,
                                           Boolean loginStatus) {
        return ReviewPageResponseDto.builder()
                .reviewList(reviewPage.getContent())
                .page(reviewPage.getNumber() + 1)
                .size(reviewPage.getSize())
                .totalPages(reviewPage.getTotalPages())
                .reviewAvgScore(avgScore)
                .totalReviews(reviewPage.getTotalElements())
                .isLogin(loginStatus)
                .build();
    }
}
