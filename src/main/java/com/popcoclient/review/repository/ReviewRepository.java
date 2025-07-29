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

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    Boolean existsReviewByContentAndUser(Content content, User user);

    @Modifying
    @Query("update Review SET likeCount = likeCount + 1 WHERE reviewId = :reviewId")
    void updateReviewLikeCount(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("update Review SET likeCount = likeCount - 1 WHERE reviewId = :reviewId")
    void decrementReviewLikeCount(@Param("reviewId") Long reviewId);

    Integer countByContent(Content content);

    @Query("""
       select r
         from Review r
    join fetch r.content c
        where r.user      = :user
          and r.createdAt >= :start
          and r.createdAt <  :end
     order by r.createdAt desc
    """)
    List<Review> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            @Param("user")  User user,
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end);

    @Query("select avg(r.score) from Review r where r.user = :user")
    Double findAverageScoreByUser(@Param("user") User user);

    @Query("select count(r) from Review r where r.user = :user")
    Long countByUser(@Param("user") User user);

    @Query("select r.score, count(r) from Review r " +
            " where r.user = :user " +
            " group by r.score")
    List<Object[]> findScoreCountByUser(@Param("user") User user);
}
