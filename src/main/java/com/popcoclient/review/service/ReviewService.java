package com.popcoclient.review.service;

import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.dto.response.ReviewCreateResponseDto;
import com.popcoclient.review.dto.response.ReviewLikeResponseDto;
import com.popcoclient.review.dto.response.ReviewPageResponseDto;

public interface ReviewService {
    ReviewCreateResponseDto insertReview(ReviewCreateRequestDto request, Long contentId, Long userId);
    ReviewPageResponseDto getReviewPage(
            Integer pageNumber, Integer pageSize, Long userId, Long contentId);
    Void updateReview(Long reviewId, ReviewUpdateRequestDto request, Long userId);
    Void deleteReview(Long reviewId, Long userId);

    ReviewLikeResponseDto reactionReview(Long reviewId, Long userId);
}
