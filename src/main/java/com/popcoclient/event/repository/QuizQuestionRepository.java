package com.popcoclient.event.repository;

import com.popcoclient.event.entity.QuizQuestion;
import com.popcoclient.event.entity.key.QuizQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, QuizQuestionId> {
}
