package com.popcoclient.event.entity;

import com.popcoclient.content.entity.Content;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_quiz_answers")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private UserQuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "option_id", referencedColumnName = "option_id", nullable = false),
            @JoinColumn(name = "question_id", referencedColumnName = "question_id", nullable = false),
            @JoinColumn(name = "quiz_id", referencedColumnName = "quiz_id", nullable = false)
    })
    private QuizOption quizOption;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @Column(name = "qualification_order")
    private Integer qualificationOrder;

    @Column(name = "advanced")
    private Boolean advanced;

    public void setAttempt(UserQuizAttempt attempt) {
        this.attempt = attempt;
    }
}
