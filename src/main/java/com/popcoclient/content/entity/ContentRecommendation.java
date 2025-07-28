package com.popcoclient.content.entity;

import com.popcoclient.content.entity.key.ContentRecommendationId;
import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_recommendations")
@EntityListeners(AuditingEntityListener.class)
public class ContentRecommendation {

    @EmbeddedId
    private ContentRecommendationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "source_content_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "source_content_type", referencedColumnName = "type", insertable = false, updatable = false)
    })
    private Content sourceContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "recommended_content_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "recommended_content_type", referencedColumnName = "type", insertable = false, updatable = false)
    })
    private Content recommendedContent;

    @Column(nullable = false)
    private Integer ranking;

    private BigDecimal score;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
