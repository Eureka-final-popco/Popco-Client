package com.popcoclient.review.dto.request;

import com.popcoclient.review.entity.enums.ReviewStatus;
import lombok.Getter;

@Getter
public class ReviewUpdateRequestDto {
    private int score;
    private String text;
    private ReviewStatus status;
}
