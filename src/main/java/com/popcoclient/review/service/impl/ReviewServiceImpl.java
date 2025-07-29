package com.popcoclient.review.service.impl;

import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.content.repository.ContentRepository;
import com.popcoclient.exception.business.ContentNotFoundException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.exception.business.auth.UnauthorizedUserException;
import com.popcoclient.exception.business.review.AlreadyReviewedException;
import com.popcoclient.exception.business.review.NotMyReviewException;
import com.popcoclient.exception.business.review.ReviewNotFoundException;
import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.dto.response.*;
import com.popcoclient.content.entity.Content;
import com.popcoclient.review.entity.Review;
import com.popcoclient.review.entity.ReviewReaction;
import com.popcoclient.review.entity.ReviewSummary;
import com.popcoclient.review.entity.TrendingReview;
import com.popcoclient.review.entity.enums.ReviewStatus;
import com.popcoclient.review.repository.ReviewReactionRepository;
import com.popcoclient.review.repository.ReviewSummaryRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewReactionRepository reviewReactionRepository;
    private final TrendingReviewRepository trendingReviewRepository;
    private final ReviewSummaryRepository reviewSummaryRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    private static final int DISPLAY_LIMIT = 20; // 화면에 표시할 리뷰 개수

    @Override
    @Transactional
    public ReviewCreateResponseDto insertReview(ReviewCreateRequestDto request, Long contentId, Long userId, String type) {

        if (userId == null) {
            throw new UnauthorizedUserException("로그인이 필요한 기능입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        ContentId id = new ContentId(contentId, type);
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("콘텐츠를 찾을 수 없습니다. contentId: " + contentId + "content Type : " + type));

        if(reviewRepository.existsReviewByContentAndUser(content, user)){
            throw new AlreadyReviewedException();
        }

        Review review = Review.of(request, user, content);
        reviewRepository.save(review);

        updateContentStats(content);

        return ReviewCreateResponseDto.builder()
                .reviewId(review.getReviewId())
                .build();
    }

    @Override
    public ReviewPageResponseDto getReviewPage(Integer pageNumber, Integer pageSize, String sort, Long userId, Long contentId, String type) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // login status check
        Boolean loginStatus;
        Page<ReviewResponseDto> reviewPage;
        ContentId id = new ContentId(contentId, type);

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("콘텐츠를 찾을 수 없습니다. contentId: " + contentId + "content Type : " + type));

        if(userId != null){
            loginStatus = true;
            reviewPage = reviewRepository.findReviewList(userId, content, pageable, sort);
        } else {
            loginStatus = false;
            reviewPage = reviewRepository.findReviewList(null, content, pageable, sort);
        }

        Double avgScore = reviewRepository.avgStar(content);

        return ReviewPageResponseDto.of(reviewPage, avgScore, loginStatus);
    }


    @Override
    @Transactional
    public void updateReview(Long reviewId, ReviewUpdateRequestDto request, Long userId) {

        if (userId == null) {
            throw new UnauthorizedUserException("로그인이 필요한 기능입니다.");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다. reviewId: " + reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(review.getUser().getUserId() != userId){
            throw new NotMyReviewException("리뷰 작성자가 아닙니다. userId: " + userId);
        }

        review.updateFrom(request);
        reviewRepository.save(review);

        updateContentStats(review.getContent());
    }


    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {

        if (userId == null) {
            throw new UnauthorizedUserException("로그인이 필요한 기능입니다.");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다. reviewId: " + reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(review.getUser().getUserId() != userId){
            throw new NotMyReviewException("리뷰 작성자가 아닙니다. userId: " + userId);
        }

        reviewRepository.delete(review);
        updateContentStats(review.getContent());
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

    @Override
    public ReviewSummaryResponseDto getContentReviewSummary(Long contentId, String type) {
        ContentId id = new ContentId(contentId, type);

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("콘텐츠를 찾을 수 없습니다. contentId: " + contentId + "content Type : " + type));

        Optional<ReviewSummary> optReviewSummary = reviewSummaryRepository.findByContent(content);
        String summary = "아직 충분한 리뷰가 모이지 않았어요. 더 많은 이용자들의 후기가 쌓이면, 추천 리뷰를 보여드릴게요!";

        if (optReviewSummary.isEmpty()) {
            return ReviewSummaryResponseDto.of(summary, null, false);
        }

        ReviewSummary reviewSummary = optReviewSummary.get();

        return ReviewSummaryResponseDto.of(reviewSummary.getSummaryText(), reviewSummary.getEvaluationType(), true);
    }

    private void updateContentStats(Content content) {
        Integer reviewCount = reviewRepository.countByContent(content);
        Double avgScore = reviewRepository.avgStar(content);

        content.updateOf(BigDecimal.valueOf(avgScore), reviewCount);
        contentRepository.save(content);
    }

    @Override
    public List<MyReviewResponseDto> getMyReviews(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Review> reviews = reviewRepository.findByUserOrderByCreatedAtDesc(user);

        return reviews.stream()
                .map(MyReviewResponseDto::of)
                .collect(Collectors.toList());
    }

    @Override
    public ScoreDistributionResponseDto getScoreDistribution(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Double avg = reviewRepository.findAverageScoreByUser(user);
        Long total = reviewRepository.countByUser(user);

        List<Object[]> raw = reviewRepository.findScoreCountByUser(user);

        Map<BigDecimal, Long> dist = new LinkedHashMap<>();
        for (int i = 1; i <= 10; i++) {
            BigDecimal score = BigDecimal.valueOf(i).divide(BigDecimal.valueOf(2));
            dist.put(score, 0L);
        }

        raw.forEach(row -> {
            BigDecimal rawScore = (BigDecimal) row[0];
            BigDecimal scoreKey = rawScore.stripTrailingZeros();
            Long cnt        = (Long)      row[1];
            dist.put(scoreKey, cnt);
        });

        BigDecimal mostFreq = raw.stream()
                .map(r -> Map.entry((BigDecimal) r[0], (Long) r[1]))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return new ScoreDistributionResponseDto(avg, total, mostFreq, dist);
    }
}
