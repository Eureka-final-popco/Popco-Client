package com.popcoclient.persona.entity.key;

import com.popcoclient.event.entity.key.QuizQuestionId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
public class OptionsId implements Serializable {
    @Column(name = "option_id", nullable = false)
    private Long optionId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OptionsId optionsId)) return false;
        return Objects.equals(optionId, optionsId.optionId) &&
                Objects.equals(questionId, optionsId.questionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionId, questionId);
    }

    public static OptionsId of(Long optionId, Long questionId) {
        OptionsId id = new OptionsId();
        id.optionId = optionId;
        id.questionId = questionId;
        return id;
    }

}
