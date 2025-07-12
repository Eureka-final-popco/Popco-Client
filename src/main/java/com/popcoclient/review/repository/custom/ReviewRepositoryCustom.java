package com.popcoclient.review.repository.custom;

import com.popcoclient.review.dto.response.ReviewResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {
    Page<ReviewResponseDto> findReviewList(Long userId, Long contentId, Pageable pageable);
    Double avgStar(Long contentId);
}
