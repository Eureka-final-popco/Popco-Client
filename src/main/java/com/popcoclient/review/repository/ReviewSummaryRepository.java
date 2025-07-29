package com.popcoclient.review.repository;

import com.popcoclient.content.entity.Content;
import com.popcoclient.review.entity.ReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewSummaryRepository extends JpaRepository<ReviewSummary, Long> {
    Optional<ReviewSummary> findByContent(Content content);
}
