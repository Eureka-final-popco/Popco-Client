package com.popcoclient.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 생존자 순위 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurvivorRankingResponse {
    private Long quizId;
    private Long questionId;
    private int totalSurvivors;
    private int currentPage;
    private int totalPages;
    private java.util.List<SurvivorInfo> survivors;

    @Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurvivorInfo {
        private Long userId;
        private String username;
        private int rank;
        private long submissionTime;
        private long responseTimeMs;
    }
}