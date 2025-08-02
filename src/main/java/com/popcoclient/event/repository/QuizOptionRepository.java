package com.popcoclient.event.repository;

import com.popcoclient.event.dto.response.QuizOptionDto;
import com.popcoclient.event.entity.QuizOption;
import com.popcoclient.event.entity.QuizQuestion;
import com.popcoclient.event.entity.key.QuizOptionId;
import com.popcoclient.event.entity.key.QuizQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizOptionRepository extends JpaRepository<QuizOption, QuizOptionId> {
    @Query("SELECT new com.popcoclient.event.dto.response.QuizOptionDto( " +
            "qo.optionId.optionId, " +
            "qo.content, " +
            "qo.isCorrect) " +
            "FROM QuizOption qo " +
            "WHERE qo.optionId.quizId = :quizId AND qo.optionId.questionId = :questionId")
    List<QuizOptionDto> findByQuizIdAndQuestionId(long quizId, long questionId);
    List<QuizOption> findAllByQuestion(QuizQuestion quizQuestion);
}
