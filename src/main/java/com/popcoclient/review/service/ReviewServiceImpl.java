package com.popcoclient.review.service;

import com.popcoclient.exception.business.ContentNotFoundException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.exception.business.review.AlreadyReviewedException;
import com.popcoclient.exception.business.review.NotMyReviewException;
import com.popcoclient.exception.business.review.ReviewNotFoundException;
import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
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
                .orElseThrow(UserNotFoundException::new);

        Content content = contentRepository.findById(contentId)
                .orElseThrow(ContentNotFoundException::new);

        if(reviewRepository.existsReviewByContentIdAndUserId(userId, contentId)){
            throw new AlreadyReviewedException();
        }

        Review review = Review.createReview(request, contentId, userId);
        reviewRepository.save(review);

        return ReviewCreateResponseDto.builder()
                .reviewId(review.getReviewId())
                .build();
    }

    @Override
    public Void deleteReview(long reviewId, long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if(review.getUserId() != userId){
            throw new NotMyReviewException();
        }

        reviewRepository.delete(review);
        return null;
    }
}
