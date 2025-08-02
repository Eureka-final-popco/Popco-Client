package com.popcoclient.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MonthlyReviewResponseDto {
    private String month;
    private List<MyReviewResponseDto> reviews;
}
