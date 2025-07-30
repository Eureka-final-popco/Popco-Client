package com.popcoclient.event.repository;

import com.popcoclient.event.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findFirstByStartAtBetween(LocalDateTime start, LocalDateTime end);
}
