package com.popcoclient.event.repository;

import com.popcoclient.event.entity.UserQuizAnswer;
import com.popcoclient.event.entity.UserQuizAttempt;
import com.popcoclient.event.entity.key.QuizQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserQuizAnswerRepository extends JpaRepository<UserQuizAnswer, Long> {
    // 🔍 특정 참가 기록에서 특정 문제의 답안이 이미 있는지 확인
    @Query("SELECT COUNT(ua) > 0 FROM UserQuizAnswer ua " +
            "WHERE ua.attempt = :attempt AND ua.quizOption.optionId.questionId = :#{#questionId.questionId} " +
            "AND ua.quizOption.optionId.quizId = :#{#questionId.quizId}")
    boolean existsByAttemptAndQuestionId(@Param("attempt") UserQuizAttempt attempt, @Param("questionId") QuizQuestionId questionId);


    // 📊 선택적: 특정 문제의 정답자 수 조회
    @Query("SELECT COUNT(ua) FROM UserQuizAnswer ua " +
            "WHERE ua.quizOption.optionId.questionId = :questionId AND ua.quizOption.isCorrect = true")
    long countCorrectAnswersForQuestion(@Param("questionId") QuizQuestionId questionId);
}
