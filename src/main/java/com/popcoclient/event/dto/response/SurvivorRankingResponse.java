package com.popcoclient.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🏆 생존자 순위 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurvivorRankingResponse {
    private Long quizId;
    private Long questionId;
    private int totalSurvivors;         // 총 생존자 수
    private int currentPage;            // 현재 페이지
    private int totalPages;             // 전체 페이지 수
    private java.util.List<SurvivorInfo> survivors;  // 생존자 목록

    @Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurvivorInfo {
        private Long userId;
        private String username;
        private int rank;               // 순위
        private long submissionTime;    // 제출 시간
        private long responseTimeMs;    // 응답 시간 (밀리초)
    }
}