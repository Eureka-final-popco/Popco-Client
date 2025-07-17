package com.popcoclient.review.repository;

import com.popcoclient.content.entity.Content;

import com.popcoclient.review.entity.Review;
import com.popcoclient.review.repository.custom.ReviewRepositoryCustom;
import com.popcoclient.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    Boolean existsReviewByContentAndUser(Content content, User user);

    @Modifying
    @Query("update Review SET likeCount = likeCount + 1 WHERE reviewId = :reviewId")
    void updateReviewLikeCount(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("update Review SET likeCount = likeCount - 1 WHERE reviewId = :reviewId")
    void decrementReviewLikeCount(@Param("reviewId") Long reviewId);
}
