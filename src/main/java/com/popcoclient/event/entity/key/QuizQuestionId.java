package com.popcoclient.event.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
public class QuizQuestionId implements Serializable {

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QuizQuestionId quizQuestionId)) return false;
        return Objects.equals(questionId, quizQuestionId.questionId) &&
                Objects.equals(quizId, quizQuestionId.quizId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, quizId);
    }

    // 편의 메서드
    public static QuizQuestionId of(Long questionId, Long quizId) {
        QuizQuestionId id = new QuizQuestionId();
        id.questionId = questionId;
        id.quizId = quizId;
        return id;
    }
}
