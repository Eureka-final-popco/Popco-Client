package com.popcoclient.review.repository;

import com.popcoclient.review.entity.Review;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsReviewByContentIdAndUserId(long contentId, long userId);
}
