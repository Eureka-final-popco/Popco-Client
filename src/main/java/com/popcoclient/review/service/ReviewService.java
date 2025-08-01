package com.popcoclient.review.service;

import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.dto.response.*;

import java.util.List;

public interface ReviewService {
    ReviewCreateResponseDto insertReview(ReviewCreateRequestDto request, Long contentId, Long userId, String type);
    ReviewPageResponseDto getReviewPage(
            Integer pageNumber, Integer pageSize, String sort, Long userId, Long contentId, String type);
    void updateReview(Long reviewId, ReviewUpdateRequestDto request, Long userId);
    void deleteReview(Long reviewId, Long userId);

    ReviewLikeResponseDto reactionReview(Long reviewId, Long userId);

    List<TrendingReviewResponseDto> getTrendingReviews(int limit);

    ReviewSummaryResponseDto getContentReviewSummary(Long contentId, String type);

    List<MyReviewResponseDto> getMyReviewsByMonth(Long userId, String month);

    MyContentReviewResponseDto getMyReviewsByContent(Long userId, Long contentId, String type);

    ScoreDistributionResponseDto getScoreDistribution(Long userId);
}
