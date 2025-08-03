package com.popcoclient.review.repository.custom;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.review.dto.response.MyReviewResponseDto;
import com.popcoclient.review.dto.response.ReviewResponseDto;
import com.popcoclient.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ReviewRepositoryCustom {
    Page<ReviewResponseDto> findReviewList(Long userId, Content content, Pageable pageable, String sort);
    List<MyReviewResponseDto> findReviewListByUserIdAndMonth(User user, LocalDateTime start, LocalDateTime end);
    Double avgStar(Content content);
}
