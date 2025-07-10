package com.popcoclient.review.entity;

import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.entity.enums.ReviewStatus;
import com.popcoclient.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;
    private Long userId;
    private Long contentId;
    private String text;
    private Double score;
    private Integer likeCount;
    private String report;
    private ReviewStatus status;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

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
                .build();
    }

}
