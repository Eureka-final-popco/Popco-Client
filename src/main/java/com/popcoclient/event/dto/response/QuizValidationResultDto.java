package com.popcoclient.event.dto.response;

import com.popcoclient.event.dto.request.QuizSubmissionResultDto;
import com.popcoclient.event.entity.QuizOption;
import com.popcoclient.event.entity.QuizQuestion;
import com.popcoclient.event.entity.UserQuizAttempt;
import com.popcoclient.user.entity.User;
import lombok.Builder;
import lombok.Data;

/**
 * 서버 내에서만 전닫되는 Dto
 */
@Data
@Builder
public class QuizValidationResultDto {

    // 검증 성공/실패 여부
    private boolean valid;

    // 실패 시 에러 정보
    private QuizSubmissionResultDto.SubmissionStatus errorStatus;
    private String errorMessage;

    // 성공 시 로드된 엔티티들
    private User user;
    private QuizQuestion question;
    private QuizOption selectedOption;
    private UserQuizAttempt attempt;

    /**
     * ✅ 검증 성공 시 생성
     */
    public static QuizValidationResultDto valid(QuizQuestion question, QuizOption selectedOption,
                                             UserQuizAttempt attempt, User user) {
        return QuizValidationResultDto.builder()
                .valid(true)
                .user(user)
                .question(question)
                .selectedOption(selectedOption)
                .attempt(attempt)
                .build();
    }

    /**
     * ❌ 검증 실패 시 생성
     */
    public static QuizValidationResultDto invalid(QuizSubmissionResultDto.SubmissionStatus status, String message) {
        return QuizValidationResultDto.builder()
                .valid(false)
                .errorStatus(status)
                .errorMessage(message)
                .build();
    }
}
