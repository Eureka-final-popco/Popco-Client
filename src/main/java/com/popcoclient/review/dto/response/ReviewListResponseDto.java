package com.popcoclient.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter

// 닉네임, 프로필 이미지, 생성일, 페르소나 이름, 로그인 좋아요, 싫어요 상태
public class ReviewListResponseDto {
    private long reviewId;
    private long reviewerId;
    private String reviewerName;
    private String reviewerProfile;
    private String reviewerPersona;
    private String reviewDate;
    private int score;
    private String content;
    private int likeCount;

    private boolean isLiked;
    private boolean isAuthor;
}
