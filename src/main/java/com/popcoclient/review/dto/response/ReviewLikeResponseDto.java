package com.popcoclient.review.dto.response;

import com.popcoclient.review.entity.ReviewReaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewLikeResponseDto {
    Long reviewLikeId;
    Boolean isLiked;

    public static ReviewLikeResponseDto of(ReviewReaction reviewReaction, Boolean isLiked) {
        return ReviewLikeResponseDto.builder()
                .reviewLikeId(reviewReaction.getReviewReactionId())
                .isLiked(isLiked)
                .build();
    }
}
