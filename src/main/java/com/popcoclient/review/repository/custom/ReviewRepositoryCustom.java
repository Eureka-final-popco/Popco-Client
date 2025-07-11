package com.popcoclient.review.repository.custom;

import com.popcoclient.review.dto.response.ReviewListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {
    Page<ReviewListResponseDto> findReviewList(Long userId, Long contentId, Pageable pageable);
    Double avgStar(Long contentId);
}
