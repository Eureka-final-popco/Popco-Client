package com.popcoclient.review.repository;

import com.popcoclient.content.entity.Content;

import com.popcoclient.review.entity.Review;
import com.popcoclient.review.repository.custom.ReviewRepositoryCustom;
import com.popcoclient.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    Boolean existsReviewByContentAndUser(Content content, User user);
}
