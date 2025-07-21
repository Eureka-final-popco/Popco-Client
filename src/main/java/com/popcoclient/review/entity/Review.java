package com.popcoclient.review.entity;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.enums.ContentTypes;
import com.popcoclient.review.dto.request.ReviewCreateRequestDto;
import com.popcoclient.review.dto.request.ReviewUpdateRequestDto;
import com.popcoclient.review.entity.enums.ReviewStatus;
import com.popcoclient.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "content_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "type", referencedColumnName = "type", nullable = false)
    })
    private Content content;

    @Column(name = "review_content")
    private String text;
    private BigDecimal score;
    private Integer likeCount;
    @Column(name = "report_count")
    private Integer report;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    private ReviewStatus status;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void updateFrom(ReviewUpdateRequestDto request) {
        this.score = request.getScore();
        this.text = request.getText();
        this.status = request.getStatus();
    }

    public static Review of(ReviewCreateRequestDto request, User user, Content content) {
        return Review.builder()
                .user(user)
                .content(content)
                .score(request.getScore())
                .text(request.getText())
                .status(request.getStatus())
                .likeCount(0)
                .report(0)
                .build();
    }

}
