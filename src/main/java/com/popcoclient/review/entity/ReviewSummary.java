package com.popcoclient.review.entity;

import com.popcoclient.content.entity.Content;
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
@Table(name = "content_review_summary")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ReviewSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "content_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "content_type", referencedColumnName = "type", nullable = false)
    })
    private Content content;

    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summaryText;

    @Column(name = "evaluation_type")
    private String evaluationType;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static ReviewSummary of(Content content, String summaryText, String evaluationType) {
        return ReviewSummary.builder()
                .content(content)
                .summaryText(summaryText)
                .evaluationType(evaluationType)
                .build();
    }

}