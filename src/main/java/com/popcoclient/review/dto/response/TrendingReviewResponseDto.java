package com.popcoclient.review.dto.response;

import com.popcoclient.review.entity.TrendingReview;
import com.popcoclient.review.entity.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingReviewResponseDto {

    // 리뷰 정보
    private Long reviewId;
    private String reviewText;
    private BigDecimal score;
    private ReviewStatus status;
    private Integer likeCount;

    // 사용자 정보
    private Long userId;
    private String userNickname;

    // 콘텐츠 정보
    private Long contentId;
    private String contentType;
    private String contentTitle;

    // 인기 정보
    private Integer ranking;

    public static TrendingReviewResponseDto from(TrendingReview trendingReview) {
        var review = trendingReview.getReview();
        var user = review.getUser();
        var content = review.getContent();

        return TrendingReviewResponseDto.builder()
                // 리뷰 정보
                .reviewId(review.getReviewId())
                .reviewText(review.getText())
                .score(review.getScore())
                .status(review.getStatus())
                .likeCount(review.getLikeCount())

                // 사용자 정보
                .userId(user.getUserId())
                .userNickname(user.getNickname())

                // 콘텐츠 정보
                .contentId(content.getContentId().getId())
                .contentType(content.getContentId().getType())
                .contentTitle(content.getTitle())

                // 인기 정보
                .ranking(trendingReview.getRanking())

                .build();
    }
}
