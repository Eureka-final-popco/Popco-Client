package com.popcoclient.review.dto.request;

import com.popcoclient.review.entity.enums.ReviewStatus;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ReviewUpdateRequestDto {
    private BigDecimal score;
    private String text;
    private ReviewStatus status;
}
