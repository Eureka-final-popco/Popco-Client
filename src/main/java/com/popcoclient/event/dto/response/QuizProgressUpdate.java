package com.popcoclient.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 서버 내부 데이터 전달용 Dto
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizProgressUpdate {

    private Long quizId;
    private Long questionId;

    private int currentSurvivors;
    private int maxSurvivors;
    private Integer latestRank;

    private long timestamp;
    private ProgressType type;
    private String message;

    private Integer remainingTime;
    private Double progressPercentage;

    /**
     * 진행 상황 업데이트
     */
    public enum ProgressType {
        PROGRESS_UPDATE("진행 상황 업데이트"),
        QUESTION_COMPLETED("문제가 완료되었습니다"),
        TIME_WARNING("시간이 얼마 남지 않았습니다"),
        CAPACITY_FULL("정원이 가득 찼습니다"),
        NEXT_QUESTION_READY("다음 문제 준비 완료");

        private final String defaultMessage;

        ProgressType(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    /**
     * 프론트엔드에서 프로그레스 바만 업데이트
     */
    public static QuizProgressUpdate progressUpdate(Long quizId, Long questionId,
                                                    int currentSurvivors, int maxSurvivors) {

        double progressPercentage = (double) currentSurvivors / maxSurvivors * 100.0;

        return QuizProgressUpdate.builder()
                .quizId(quizId)
                .questionId(questionId)
                .currentSurvivors(currentSurvivors)
                .maxSurvivors(maxSurvivors)
                .timestamp(System.currentTimeMillis())
                .type(ProgressType.PROGRESS_UPDATE)
                .message(null)
                .progressPercentage(progressPercentage)
                .build();
    }

    /**
     * 문제 완료 알림 (정원 충족)
     */
    public static QuizProgressUpdate questionCompleted(Long quizId, Long questionId,
                                                       int finalSurvivors) {

        String message = String.format("문제가 완료되었습니다! 총 %d명이 통과했습니다.", finalSurvivors);

        return QuizProgressUpdate.builder()
                .quizId(quizId)
                .questionId(questionId)
                .currentSurvivors(finalSurvivors)
                .maxSurvivors(finalSurvivors)
                .timestamp(System.currentTimeMillis())
                .type(ProgressType.QUESTION_COMPLETED)
                .message(message)
                .progressPercentage(100.0)
                .build();
    }

    /**
     * 시간 경고 알림
     */
    public static QuizProgressUpdate timeWarning(Long quizId, Long questionId,
                                                 int remainingSeconds,
                                                 int currentSurvivors, int maxSurvivors) {

        String message = String.format("⚠️ %d초 후 문제가 종료됩니다!", remainingSeconds);

        return QuizProgressUpdate.builder()
                .quizId(quizId)
                .questionId(questionId)
                .currentSurvivors(currentSurvivors)
                .maxSurvivors(maxSurvivors)
                .remainingTime(remainingSeconds)
                .timestamp(System.currentTimeMillis())
                .type(ProgressType.TIME_WARNING)
                .message(message)
                .build();
    }

    /**
     * 정원 가득참 알림
     */
    public static QuizProgressUpdate capacityFull(Long quizId, Long questionId, int maxSurvivors) {

        String message = String.format("정원이 가득 찼습니다! (%d명)", maxSurvivors);

        return QuizProgressUpdate.builder()
                .quizId(quizId)
                .questionId(questionId)
                .currentSurvivors(maxSurvivors)
                .maxSurvivors(maxSurvivors)
                .timestamp(System.currentTimeMillis())
                .type(ProgressType.CAPACITY_FULL)
                .message(message)
                .progressPercentage(100.0)
                .build();
    }
}
