package com.popcoclient.review.repository.custom;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.review.dto.response.ReviewResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {
    Page<ReviewResponseDto> findReviewList(Long userId, Content content, Pageable pageable);
    Double avgStar(Content content);
}
