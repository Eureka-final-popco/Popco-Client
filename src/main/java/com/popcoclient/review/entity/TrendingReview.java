package com.popcoclient.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "trending_reviews")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TrendingReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "weekly_like_count", nullable = false)
    private Integer weeklyLikeCount;

    @Column(name = "ranking", nullable = false)
    private Integer ranking;

    @CreatedDate
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    public static TrendingReview of(Review review, Integer weeklyLikeCount, Integer ranking) {
        return TrendingReview.builder()
                .review(review)
                .weeklyLikeCount(weeklyLikeCount)
                .ranking(ranking)
                .build();
    }
}
