package com.popcoclient.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyContentReviewResponseDto {
    private boolean isLogin;
    private boolean existUserReview;
    private MyReviewResponseDto myReview;

    public static MyContentReviewResponseDto from(MyReviewResponseDto myReview, boolean isLogin, boolean existUserReview) {
        return MyContentReviewResponseDto.builder()
                .isLogin(isLogin)
                .existUserReview(existUserReview)
                .myReview(myReview)
                .build();
    }
}
