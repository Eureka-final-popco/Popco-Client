package com.popcoclient.persona.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PersonaAnalysisResponseDto {
    private List<Integer> genderPercent; // [남, 여] -> 숫자만
    private List<Integer> agePercent; // [10대, 20대, 30대, 40대, 50대, 60대]
    private Double myRatingPercent;
    private Double personaRatingPercent;
    private List<Integer> eventPercent; // [내 참여 횟수, 페르소나 평균 참여 횟수]
    private List<Integer> reviewPercent; // [내가 남긴 리뷰 수, 페르소나 평균 리뷰 수]
    private List<Integer> myLikePercent; // [나의 좋아요 수 %, 싫어요 수 %]
    // 장르 선호도 오각형 그래프 보류
}
