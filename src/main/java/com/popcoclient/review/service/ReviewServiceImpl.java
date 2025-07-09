package com.popcoclient.review.service;

import com.popcoclient.exception.business.ContentNotFoundException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.exception.business.review.AlreadyReviewedException;
import com.popcoclient.exception.business.review.NotMyReviewException;
import com.popcoclient.exception.business.review.ReviewNotFoundException;
import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.dto.response.ReviewCreateResponseDto;
import com.popcoclient.review.entity.Content;
import com.popcoclient.review.entity.Review;
import com.popcoclient.review.entity.User;
import com.popcoclient.review.repository.ContentRepository;
import com.popcoclient.review.repository.ReviewRepository;
import com.popcoclient.review.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService{
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    @Override
    public ReviewCreateResponseDto insertReview(ReviewCreateRequestDto request, long contentId, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(ContentNotFoundException::new);

        if(reviewRepository.existsReviewByContentIdAndUserId(userId, contentId)){
            throw new AlreadyReviewedException();
        }

        Review review = Review.from(request, contentId, userId);
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

        if(review.getUserId() != userId){
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

        if(review.getUserId() != userId){
            throw new NotMyReviewException();
        }

        reviewRepository.delete(review);
        return null;
    }
}
