package com.popcoclient.declaration.repository;

import com.popcoclient.declaration.entity.Declaration;
import com.popcoclient.review.entity.Review;
import com.popcoclient.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeclarationRepository extends JpaRepository<Declaration, Long> {
    boolean existsByReviewAndUser(Review review, User user);
}
