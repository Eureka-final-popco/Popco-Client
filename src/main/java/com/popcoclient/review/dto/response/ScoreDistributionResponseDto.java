package com.popcoclient.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ScoreDistributionResponseDto {

    private final Double averageScore;

    private final Long totalCount;

    private final BigDecimal mostFrequentScore;

    private final Map<BigDecimal, Long> distribution;
}
