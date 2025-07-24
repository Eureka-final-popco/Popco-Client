package com.popcoclient.review.repository;

import com.popcoclient.review.entity.TrendingReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrendingReviewRepository extends JpaRepository<TrendingReview, Long> {

    // 최신 계산된 인기 리뷰 목록 조회 (순위대로) - UserDetail 함께 조회
    @Query("""
        SELECT tr FROM TrendingReview tr
        JOIN FETCH tr.review r
        JOIN FETCH r.user u
        LEFT JOIN FETCH u.userDetail
        JOIN FETCH r.content
        ORDER BY tr.ranking ASC
        """)
    List<TrendingReview> findAllByOrderByRankingAsc();

    // 특정 개수만큼 인기 리뷰 조회 - UserDetail 함께 조회
    @Query("""
        SELECT tr FROM TrendingReview tr
        JOIN FETCH tr.review r
        JOIN FETCH r.user u
        LEFT JOIN FETCH u.userDetail
        JOIN FETCH r.content
        WHERE tr.ranking <= :limit
        ORDER BY tr.ranking ASC
        """)
    List<TrendingReview> findTopNByOrderByRankingAsc(@Param("limit") int limit);

}
