package com.popcoclient.review.service;

import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.response.ReviewCreateResponseDto;

public interface ReviewService {
    ReviewCreateResponseDto insertReview(ReviewCreateRequestDto request, long contentId, long userId);
    Void deleteReview(long reviewId, long userId);
}
