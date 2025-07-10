package com.popcoclient.review.service.impl;

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
import com.popcoclient.review.entity.Review;
import com.popcoclient.review.service.ReviewService;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.repository.UserRepository;
import com.popcoclient.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    @Override
    public ReviewCreateResponseDto insertReview(ReviewCreateRequestDto request, long contentId, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException("콘텐츠를 찾을 수 없습니다. userId: " + contentId));

        if(reviewRepository.existsReviewByContentIdAndUser_UserId(userId, contentId)){
            throw new AlreadyReviewedException();
        }

        Review review = Review.of(request, user, contentId);
        reviewRepository.save(review);

        return ReviewCreateResponseDto.builder()
                .reviewId(review.getReviewId())
                .build();
    }

    @Override
    public Void updateReview(long reviewId, ReviewUpdateRequestDto request, long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다. reviewId: " + reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(review.getUser().getUserId() != userId){
            throw new NotMyReviewException();
        }

        review.updateFrom(request);
        reviewRepository.save(review);
        return null;
    }


    @Override
    public Void deleteReview(long reviewId, long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다. reviewId: " + reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(review.getUser().getUserId() != userId){
            throw new NotMyReviewException();
        }

        reviewRepository.delete(review);
        return null;
    }
}
