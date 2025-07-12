package com.popcoclient.review.repository;

import com.popcoclient.review.entity.Review;
import com.popcoclient.review.entity.ReviewReaction;
import com.popcoclient.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, Long> {
    boolean existsByReviewAndUser(Review review, User user);
}
