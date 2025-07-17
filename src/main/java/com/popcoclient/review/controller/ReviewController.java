package com.popcoclient.review.controller;

import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.dto.response.ReviewCreateResponseDto;
import com.popcoclient.review.dto.response.ReviewLikeResponseDto;
import com.popcoclient.review.dto.response.ReviewPageResponseDto;
import com.popcoclient.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Review API", description = "리뷰와 관련된 api 요청이 모두 포함되어 있습니다.")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(summary = "콘텐츠의 리뷰 목록 조회", description = "contentId로 콘텐츠에 포함된 모든 리뷰 조회")
    @GetMapping("/contents/{contentId}")
    public ResponseEntity<ApiResponse<ReviewPageResponseDto>> getReviewPage(
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @PathVariable("contentId") Long contentId, @RequestParam("userId") Long userId) {
        ReviewPageResponseDto response = reviewService.getReviewPage(pageNumber, pageSize, userId, contentId);
        return ResponseEntity.ok(ApiResponse.success("create review success", response));
    }

    @Operation(summary = "콘텐츠에 리뷰 작성", description = "콘텐츠에 리뷰 작성")
    @PostMapping("/contents/{contentId}")
    public ResponseEntity<ApiResponse<ReviewCreateResponseDto>> createReview(
            @RequestBody ReviewCreateRequestDto request, @PathVariable("contentId") Long contentId, @RequestParam("userId") Long userId) {
        ReviewCreateResponseDto response = reviewService.insertReview(request, contentId, userId);
        return ResponseEntity.ok(ApiResponse.success("create review success", response));
    }

    @Operation(summary = "리뷰 수정", description = "리뷰 작성자만 리뷰를 수정 가능")
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> updateReview(
            @RequestBody ReviewUpdateRequestDto request, @PathVariable("reviewId") Long reviewId, @RequestParam("userId") Long userId){
        return ResponseEntity.ok(ApiResponse.success("update review success", reviewService.updateReview(reviewId, request, userId)));
    }

    @Operation(summary = "리뷰 삭제", description = "리뷰 작성자만 리뷰를 삭제 가능")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable("reviewId") Long reviewId, @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("delete review success", reviewService.deleteReview(reviewId, userId)));
    }

    @Operation(summary = "리뷰 좋아요/취소", description = "리뷰 좋아요/취소")
    @PostMapping("/{reviewId}/reaction")
    public ResponseEntity<ApiResponse<ReviewLikeResponseDto>> reactionReview(@PathVariable("reviewId") Long reviewId, @RequestParam("userId") Long userId) {
        ReviewLikeResponseDto response = reviewService.reactionReview(reviewId, userId);

        if (response.getIsLiked()) {
            return ResponseEntity.ok(ApiResponse.success("liked review success", response));
        } else {
            return ResponseEntity.ok(ApiResponse.success("liked review delete ", response));
        }
    }

}
