package com.popcoclient.review.repository.custom;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.review.dto.response.ReviewResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public interface ReviewRepositoryCustom {
    Page<ReviewResponseDto> findReviewList(Long userId, Content content, Pageable pageable, String sort);
    Double avgStar(Content content);
    Map<ContentId, Double> findAverageScoreByContents(Set<ContentId> contentIds);
}
