package com.popcoclient.event.repository;

import com.popcoclient.event.entity.UserQuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {
    Integer countByUser_UserId(Long userId);
    Integer countByUser_UserIdIn(List<Long> samePerUserIds);
}
