package com.popcoclient.event.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionResultDto {

    private SubmissionStatus status;        // 제출 결과 상태
    private String message;                 // 결과 메시지
    private Integer rank;                   // 생존 시 순위 (선착순)
    private Integer totalSurvivors;         // 현재 총 생존자 수
    private Long submissionTime;            // 제출 시간
    private boolean survived;               // 생존 여부

    /**
     * 📊 답안 제출 결과 상태
     */
    public enum SubmissionStatus {
        SURVIVED("정답! 생존하셨습니다."),
        TOO_LATE("정답이지만 선착순에서 탈락하셨습니다."),
        WRONG_ANSWER("오답입니다. 탈락하셨습니다."),
        ALREADY_SUBMITTED("이미 답안을 제출하셨습니다."),
        DUPLICATE_SUBMISSION("중복 제출입니다."),
        QUIZ_NOT_ACTIVE("퀴즈가 진행 중이 아닙니다."),
        SYSTEM_ERROR("시스템 오류가 발생했습니다.");

        private final String defaultMessage;

        SubmissionStatus(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    // ===== 정적 팩토리 메서드들 =====

    /**
     * 🏆 생존 성공
     */
    public static QuizSubmissionResultDto survived(int rank, int totalSurvivors) {
        return QuizSubmissionResultDto.builder()
                .status(SubmissionStatus.SURVIVED)
                .message(SubmissionStatus.SURVIVED.getDefaultMessage() + " (순위: " + rank + "등)")
                .rank(rank)
                .totalSurvivors(totalSurvivors)
                .submissionTime(System.currentTimeMillis())
                .survived(true)
                .build();
    }

    /**
     * ⏰ 정답이지만 늦음 (선착순 탈락)
     */
    public static QuizSubmissionResultDto tooLate(int totalSurvivors) {
        return QuizSubmissionResultDto.builder()
                .status(SubmissionStatus.TOO_LATE)
                .message(SubmissionStatus.TOO_LATE.getDefaultMessage())
                .totalSurvivors(totalSurvivors)
                .submissionTime(System.currentTimeMillis())
                .survived(false)
                .build();
    }

    /**
     * ❌ 오답
     */
    public static QuizSubmissionResultDto wrongAnswer() {
        return QuizSubmissionResultDto.builder()
                .status(SubmissionStatus.WRONG_ANSWER)
                .message(SubmissionStatus.WRONG_ANSWER.getDefaultMessage())
                .submissionTime(System.currentTimeMillis())
                .survived(false)
                .build();
    }

    /**
     * 🔄 이미 제출됨
     */
    public static QuizSubmissionResultDto alreadySubmitted() {
        return QuizSubmissionResultDto.builder()
                .status(SubmissionStatus.ALREADY_SUBMITTED)
                .message(SubmissionStatus.ALREADY_SUBMITTED.getDefaultMessage())
                .submissionTime(System.currentTimeMillis())
                .survived(false)
                .build();
    }

    /**
     * 🔄 중복 제출
     */
    public static QuizSubmissionResultDto duplicate() {
        return QuizSubmissionResultDto.builder()
                .status(SubmissionStatus.DUPLICATE_SUBMISSION)
                .message(SubmissionStatus.DUPLICATE_SUBMISSION.getDefaultMessage())
                .submissionTime(System.currentTimeMillis())
                .survived(false)
                .build();
    }

    /**
     * ⚠️ 시스템 에러
     */
    public static QuizSubmissionResultDto error() {
        return QuizSubmissionResultDto.builder()
                .status(SubmissionStatus.SYSTEM_ERROR)
                .message(SubmissionStatus.SYSTEM_ERROR.getDefaultMessage())
                .submissionTime(System.currentTimeMillis())
                .survived(false)
                .build();
    }

    /**
     * 🛠️ 커스텀 에러 메시지
     */
    public static QuizSubmissionResultDto error(String customMessage) {
        return QuizSubmissionResultDto.builder()
                .status(SubmissionStatus.SYSTEM_ERROR)
                .message(customMessage)
                .submissionTime(System.currentTimeMillis())
                .survived(false)
                .build();
    }
}
