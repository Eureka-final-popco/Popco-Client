package com.popcoclient.review.repository;

import com.popcoclient.review.entity.Content;
import com.popcoclient.review.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
}
