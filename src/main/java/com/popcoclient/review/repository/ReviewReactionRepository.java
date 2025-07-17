package com.popcoclient.review.repository;

import com.popcoclient.review.entity.Review;
import com.popcoclient.review.entity.ReviewReaction;
import com.popcoclient.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, Long> {
    ReviewReaction findByReviewAndUser(Review review, User user);
    Integer countByReview(Review review);
}
