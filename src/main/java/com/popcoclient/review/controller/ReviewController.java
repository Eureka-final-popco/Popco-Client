package com.popcoclient.review.controller;

import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.dto.response.ReviewCreateResponseDto;
import com.popcoclient.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/{contentId}")
    public ResponseEntity<ApiResponse<ReviewCreateResponseDto>> createReview(
            @RequestBody ReviewCreateRequestDto request, @PathVariable("contentId") long contentId, @RequestParam("userId") long userId) {
        ReviewCreateResponseDto response = reviewService.insertReview(request, contentId, userId);
        return ResponseEntity.ok(ApiResponse.success("create review success", response));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> updateReview(
            @RequestBody ReviewUpdateRequestDto request, @PathVariable("reviewId") long reviewId, @RequestParam("userId") long userId){
        return ResponseEntity.ok(ApiResponse.success("update review success", reviewService.updateReview(reviewId, request, userId)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable("reviewId") long reviewId, @RequestParam("userId") long userId) {
        return ResponseEntity.ok(ApiResponse.success("delete review success", reviewService.deleteReview(reviewId, userId)));
    }
}
