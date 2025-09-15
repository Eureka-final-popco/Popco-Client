package com.popcoclient.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryResponseDto {
    private boolean hasSummary;
    private String summary;
    private String evaluationType;

    public static ReviewSummaryResponseDto of(String reviewSummary, String evaluationType, boolean hasSummary) {
        return ReviewSummaryResponseDto.builder()
                .hasSummary(hasSummary)
                .summary(reviewSummary)
                .evaluationType(evaluationType)
                .build();
    }
}

