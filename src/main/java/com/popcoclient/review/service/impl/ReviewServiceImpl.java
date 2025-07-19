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
import com.popcoclient.review.dto.response.ReviewCreateResponseDto;
import com.popcoclient.content.entity.Content;
import com.popcoclient.review.dto.response.ReviewLikeResponseDto;
import com.popcoclient.review.dto.response.ReviewResponseDto;
import com.popcoclient.review.dto.response.ReviewPageResponseDto;
import com.popcoclient.review.entity.Review;
import com.popcoclient.review.entity.ReviewReaction;
import com.popcoclient.review.repository.ReviewReactionRepository;
import com.popcoclient.review.service.ReviewService;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.repository.UserDetailRepository;
import com.popcoclient.user.repository.UserRepository;
import com.popcoclient.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewReactionRepository reviewReactionRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    @Override
    public ReviewCreateResponseDto insertReview(ReviewCreateRequestDto request, Long contentId, Long userId, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        ContentId contentIds = new ContentId(contentId, type);
        Content content = contentRepository.findById(contentIds)
                .orElseThrow(() -> new ContentNotFoundException("콘텐츠를 찾을 수 없습니다. userId: " + contentId));

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
        Boolean loginStatus = true;
        ContentId contentComplex = new ContentId(contentId, type);

        Content content = contentRepository.findById(contentComplex)
                .orElseThrow(() -> new ContentNotFoundException("콘텐츠를 찾을 수 없습니다. contentId: " + contentId + "content Type : " + type));

        if(loginStatus){
            Page<ReviewResponseDto> reviewPage = reviewRepository.findReviewList(userId, content, pageable);
            Double avgScore = reviewRepository.avgStar(content);

            return ReviewPageResponseDto.of(reviewPage, avgScore, loginStatus);
        }

        return null;
    }


    @Override
    public Void updateReview(Long reviewId, ReviewUpdateRequestDto request, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다. reviewId: " + reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(review.getUser().getUserId() != userId){
            throw new NotMyReviewException("리뷰 작성자가 아닙니다. userId: " + userId);
        }

        review.updateFrom(request);
        reviewRepository.save(review);
        return null;
    }


    @Override
    public Void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다. reviewId: " + reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(review.getUser().getUserId() != userId){
            throw new NotMyReviewException("리뷰 작성자가 아닙니다. userId: " + userId);
        }

        reviewRepository.delete(review);
        return null;
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
}
