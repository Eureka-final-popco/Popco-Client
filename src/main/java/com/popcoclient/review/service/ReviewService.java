package com.popcoclient.review.service;

import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.dto.response.ReviewCreateResponseDto;
import com.popcoclient.review.dto.response.ReviewLikeResponseDto;
import com.popcoclient.review.dto.response.ReviewPageResponseDto;
import com.popcoclient.review.dto.response.TrendingReviewResponseDto;

import java.util.List;

public interface ReviewService {
    ReviewCreateResponseDto insertReview(ReviewCreateRequestDto request, Long contentId, Long userId, String type);
    ReviewPageResponseDto getReviewPage(
            Integer pageNumber, Integer pageSize, Long userId, Long contentId, String type);
    void updateReview(Long reviewId, ReviewUpdateRequestDto request, Long userId);
    void deleteReview(Long reviewId, Long userId);

    ReviewLikeResponseDto reactionReview(Long reviewId, Long userId);

    List<TrendingReviewResponseDto> getTrendingReviews(int limit);
}
