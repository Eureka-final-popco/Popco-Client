package com.popcoclient.event.entity;

import com.popcoclient.event.entity.key.QuizQuestionId;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class QuizQuestion {

    @EmbeddedId
    private QuizQuestionId questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("quizId") // 복합 ID 클래스(QuizQuestionId)의 'quizId' 필드와 매핑
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(nullable = false)
    private Integer questionOrder;

    @Column(name = "img_path")
    private String imgPath;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "first_capacity")
    private Integer firstCapacity;

    @Column(name = "content")
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
            @JoinColumn(name = "question_id", referencedColumnName = "question_id"),
            @JoinColumn(name = "quiz_id", referencedColumnName = "quiz_id")
    })
    private List<QuizOption> options = new ArrayList<>();
}
