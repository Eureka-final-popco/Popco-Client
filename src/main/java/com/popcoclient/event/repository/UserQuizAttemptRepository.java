package com.popcoclient.event.repository;

import com.popcoclient.event.entity.UserQuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.popcoclient.event.entity.Quiz;
import com.popcoclient.user.entity.User;

import java.util.Optional;

@Repository
public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {
    // 🔍 사용자의 특정 퀴즈 참가 기록 조회
    Optional<UserQuizAttempt> findByQuizAndUser(Quiz quiz, User user);

    // 📊 퀴즈별 참가자 수 카운트 (선택적)
    long countByQuiz(Quiz quiz);
}
