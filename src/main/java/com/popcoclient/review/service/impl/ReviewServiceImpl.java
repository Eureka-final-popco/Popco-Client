package com.popcoclient.review.service.impl;

import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.content.repository.ContentRepository;
import com.popcoclient.exception.business.ContentNotFoundException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.exception.business.review.AlreadyReviewedException;
import com.popcoclient.exception.business.review.NotMyReviewException;
import com.popcoclient.exception.business.review.ReviewNotFoundException;
import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.dto.response.*;
import com.popcoclient.content.entity.Content;
import com.popcoclient.review.entity.Review;
import com.popcoclient.review.entity.ReviewReaction;
import com.popcoclient.review.entity.TrendingReview;
import com.popcoclient.review.entity.enums.ReviewStatus;
import com.popcoclient.review.repository.ReviewReactionRepository;
import com.popcoclient.review.repository.TrendingReviewRepository;
import com.popcoclient.review.service.ReviewService;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.repository.UserDetailRepository;
import com.popcoclient.user.repository.UserRepository;
import com.popcoclient.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewReactionRepository reviewReactionRepository;
    private final TrendingReviewRepository trendingReviewRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    private static final int DISPLAY_LIMIT = 20; // 화면에 표시할 리뷰 개수

    @Override
    public ReviewCreateResponseDto insertReview(ReviewCreateRequestDto request, Long contentId, Long userId, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        ContentId contentIds = new ContentId(contentId, type);
        Content content = contentRepository.findById(contentIds)
                .orElseThrow(() -> new ContentNotFoundException("콘텐츠를 찾을 수 없습니다. contentId: " + contentId + "content Type : " + type));

        if(reviewRepository.existsReviewByContentAndUser(content, user)){
            throw new AlreadyReviewedException();
        }

        Review review = Review.of(request, user, content);
        reviewRepository.save(review);

        return ReviewCreateResponseDto.builder()
                .reviewId(review.getReviewId())
                .build();
    }

    @Override
    public ReviewPageResponseDto getReviewPage(Integer pageNumber, Integer pageSize, Long userId, Long contentId, String type) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // login status check
        Boolean loginStatus;
        Page<ReviewResponseDto> reviewPage;
        ContentId contentComplex = new ContentId(contentId, type);

        Content content = contentRepository.findById(contentComplex)
                .orElseThrow(() -> new ContentNotFoundException("콘텐츠를 찾을 수 없습니다. contentId: " + contentId + "content Type : " + type));

        if(userId != null){
            loginStatus = true;
            reviewPage = reviewRepository.findReviewList(userId, content, pageable);
        } else {
            loginStatus = false;
            reviewPage = reviewRepository.findReviewList(null, content, pageable);
        }

        Double avgScore = reviewRepository.avgStar(content);

        return ReviewPageResponseDto.of(reviewPage, avgScore, loginStatus);
    }


    @Override
    public void updateReview(Long reviewId, ReviewUpdateRequestDto request, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다. reviewId: " + reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(review.getUser().getUserId() != userId){
            throw new NotMyReviewException("리뷰 작성자가 아닙니다. userId: " + userId);
        }

        review.updateFrom(request);
        reviewRepository.save(review);
    }


    @Override
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다. reviewId: " + reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(review.getUser().getUserId() != userId){
            throw new NotMyReviewException("리뷰 작성자가 아닙니다. userId: " + userId);
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional
    public ReviewLikeResponseDto reactionReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다. reviewId: " + reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        Optional<ReviewReaction> optionalReaction = reviewReactionRepository.findByReviewAndUser(review, user);

        if (optionalReaction.isPresent()) {
            reviewRepository.decrementReviewLikeCount(reviewId);
            reviewReactionRepository.delete(optionalReaction.get());
            return ReviewLikeResponseDto.of(optionalReaction.get(), false);
        } else {
            reviewRepository.updateReviewLikeCount(reviewId);
            ReviewReaction saveReaction = ReviewReaction.of(user, review);
            reviewReactionRepository.save(saveReaction);
            return ReviewLikeResponseDto.of(saveReaction, true);
        }

    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<TrendingReviewResponseDto> getTrendingReviews(int limit) {
        List<TrendingReview> trendingReviews = trendingReviewRepository.findTopNByOrderByRankingAsc(
                Math.min(limit, DISPLAY_LIMIT)
        );

        return trendingReviews.stream()
                .map(TrendingReviewResponseDto::from)
                .collect(Collectors.toList());
    }

}
