package com.popcoclient.review.controller;

import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.dto.response.ReviewCreateResponseDto;
import com.popcoclient.review.dto.response.ReviewLikeResponseDto;
import com.popcoclient.review.dto.response.ReviewPageResponseDto;
import com.popcoclient.review.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@Tag(name = "review", description = "리뷰와 관련된 api 요청이 모두 포함되어 있습니다.")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/content/{contentId}")
    public ResponseEntity<ApiResponse<ReviewPageResponseDto>> getReviewPage(
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @PathVariable("contentId") Long contentId, @RequestParam("userId") Long userId) {
        ReviewPageResponseDto response = reviewService.getReviewPage(pageNumber, pageSize, userId, contentId);
        return ResponseEntity.ok(ApiResponse.success("create review success", response));
    }

    @PostMapping("/{contentId}")
    public ResponseEntity<ApiResponse<ReviewCreateResponseDto>> createReview(
            @RequestBody ReviewCreateRequestDto request, @PathVariable("contentId") Long contentId, @RequestParam("userId") Long userId) {
        ReviewCreateResponseDto response = reviewService.insertReview(request, contentId, userId);
        return ResponseEntity.ok(ApiResponse.success("create review success", response));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> updateReview(
            @RequestBody ReviewUpdateRequestDto request, @PathVariable("reviewId") Long reviewId, @RequestParam("userId") Long userId){
        return ResponseEntity.ok(ApiResponse.success("update review success", reviewService.updateReview(reviewId, request, userId)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable("reviewId") Long reviewId, @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("delete review success", reviewService.deleteReview(reviewId, userId)));
    }

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
