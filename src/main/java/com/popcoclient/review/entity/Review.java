package com.popcoclient.review.entity;

import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.entity.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "score")
    private Integer score;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "dislike_count")
    private Integer dislikeCount;

    @Column(name = "report")
    private String report;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private ReviewStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateFrom(ReviewUpdateRequestDto request) {
        this.score = request.getScore();
        this.text = request.getText();
        this.status = request.getStatus();
    }

    public static Review from(ReviewCreateRequestDto request, long contentId, long userId) {
        return Review.builder()
                .userId(userId)
                .contentId(contentId)
                .score(request.getScore())
                .text(request.getText())
                .status(request.getStatus())
                .likeCount(0)
                .dislikeCount(0)
                .build();
    }

}
