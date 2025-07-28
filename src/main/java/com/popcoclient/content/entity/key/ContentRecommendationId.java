package com.popcoclient.content.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ContentRecommendationId implements Serializable {

    @Column(name = "source_content_id")
    private Long sourceContentId;

    @Column(name = "source_content_type")
    private String sourceContentType;

    @Column(name = "recommended_content_id")
    private Long recommendedContentId;

    @Column(name = "recommended_content_type")
    private String recommendedContentType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // 참조 비교 먼저
        if (o == null || getClass() != o.getClass()) return false;
        ContentRecommendationId that = (ContentRecommendationId) o;
        return Objects.equals(sourceContentId, that.sourceContentId) &&
                Objects.equals(sourceContentType, that.sourceContentType) &&
                Objects.equals(recommendedContentId, that.recommendedContentId) &&
                Objects.equals(recommendedContentType, that.recommendedContentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceContentId, sourceContentType, recommendedContentId, recommendedContentType);
    }

}
