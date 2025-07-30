package com.popcoclient.review.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.exception.business.auth.UnauthorizedUserException;
import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.dto.response.*;
import com.popcoclient.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Review API", description = "리뷰와 관련된 api 요청이 모두 포함되어 있습니다.")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "콘텐츠의 리뷰 목록 조회", description = "contentId로 콘텐츠에 포함된 모든 리뷰 조회")
    @GetMapping("/contents/{contentId}/types/{type}")
    public ResponseEntity<ApiResponse<ReviewPageResponseDto>> getReviewPage(
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sort", defaultValue = "recent") String sort,
            @PathVariable("contentId") Long contentId, @PathVariable("type") String type) {
        Long userId = jwtProvider.getNullableUserId();
        ReviewPageResponseDto response = reviewService.getReviewPage(pageNumber, pageSize, sort, userId, contentId, type);
        return ResponseEntity.ok(ApiResponse.success("get review page success", response));
    }

    @Operation(summary = "콘텐츠에 리뷰 작성", description = "콘텐츠에 리뷰 작성")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/contents/{contentId}/types/{type}")
    public ResponseEntity<ApiResponse<ReviewCreateResponseDto>> createReview(
            @RequestBody ReviewCreateRequestDto request, @PathVariable("contentId") Long contentId, @PathVariable("type") String type) {
        Long userId = jwtProvider.getRequiredUserId();
        ReviewCreateResponseDto response = reviewService.insertReview(request, contentId, userId, type);
        return ResponseEntity.ok(ApiResponse.success("create review success", response));
    }

    @Operation(summary = "리뷰 수정", description = "리뷰 작성자만 리뷰를 수정 가능")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> updateReview(
            @RequestBody ReviewUpdateRequestDto request, @PathVariable("reviewId") Long reviewId){
        Long userId = jwtProvider.getRequiredUserId();
        reviewService.updateReview(reviewId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("update review success", null));
    }

    @Operation(summary = "리뷰 삭제", description = "리뷰 작성자만 리뷰를 삭제 가능")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable("reviewId") Long reviewId) {
        Long userId = jwtProvider.getRequiredUserId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.success("delete review success", null));
    }

    @Operation(summary = "리뷰 좋아요/취소", description = "리뷰 좋아요/취소")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{reviewId}/reaction")
    public ResponseEntity<ApiResponse<ReviewLikeResponseDto>> reactionReview(@PathVariable("reviewId") Long reviewId) {
        Long userId = jwtProvider.getRequiredUserId();
        ReviewLikeResponseDto response = reviewService.reactionReview(reviewId, userId);

        if (response.getIsLiked()) {
            return ResponseEntity.ok(ApiResponse.success("liked review success", response));
        } else {
            return ResponseEntity.ok(ApiResponse.success("liked review delete ", response));
        }
    }

    @Operation(summary = "최근 뜨고 있는 리뷰 목록 조회", description = "최근 일주일간 좋아요를 많이 받은 리뷰 목록을 조회합니다.")
    @GetMapping("/weekly-trend")
    public ResponseEntity<ApiResponse<List<TrendingReviewResponseDto>>> getTrendingReviews(
            @RequestParam(defaultValue = "20") int limit
    ) {
        List<TrendingReviewResponseDto> trendingReviews = reviewService.getTrendingReviews(limit);
        return ResponseEntity.ok(ApiResponse.success("인기 리뷰 목록 조회", trendingReviews));
    }

    @Operation(summary = "콘텐츠 리뷰 요약 조회", description = "리뷰가 5개 이상 쌓이면 리뷰 요약본을 제공합니다.")
    @GetMapping("/summary/contents/{contentId}/types/{type}")
    public ResponseEntity<ApiResponse<ReviewSummaryResponseDto>> getContentReviewSummary(
            @PathVariable("contentId") Long contentId, @PathVariable("type") String type
    ) {
        ReviewSummaryResponseDto reviewSummary = reviewService.getContentReviewSummary(contentId, type);
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 리뷰 요약 조회", reviewSummary));
    }

    @Operation(summary = "나의 리뷰 목록 월별 조회", description = "나의 리뷰 목록을 조회합니다. 마이페이지에 달력이랑 나의 리뷰에 사용 가능. (month 값 예시 : 2025-07")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my/monthly")
    public ResponseEntity<ApiResponse<List<MyReviewResponseDto>>> getMyReviewsByMonth(@RequestParam String month) {
        Long userId = jwtProvider.getRequiredUserId();
        return ResponseEntity.ok(ApiResponse.success(reviewService.getMyReviewsByMonth(userId, month)));
    }

    @Operation(summary = "나의 리뷰 별점 분포", description = "나의 리뷰 별점 분포를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my/score-distribution")
    public ResponseEntity<ApiResponse<ScoreDistributionResponseDto>> getScoreDistribution() {
        Long userId = jwtProvider.getRequiredUserId();
        return ResponseEntity.ok(ApiResponse.success(reviewService.getScoreDistribution(userId)));
    }
}
