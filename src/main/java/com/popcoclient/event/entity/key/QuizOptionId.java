package com.popcoclient.event.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
public class QuizOptionId implements Serializable {
    @Column(name = "option_id", nullable = false)
    private Long optionId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QuizOptionId quizOptionId)) return false;
        return Objects.equals(optionId, quizOptionId.optionId) &&
                Objects.equals(questionId, quizOptionId.questionId) &&
                Objects.equals(quizId, quizOptionId.quizId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionId, questionId, quizId);
    }

    public static QuizOptionId of(Long optionId, Long questionId, Long quizId) {
        QuizOptionId id = new QuizOptionId();
        id.optionId = optionId;
        id.questionId = questionId;
        id.quizId = quizId;
        return id;
    }
}
