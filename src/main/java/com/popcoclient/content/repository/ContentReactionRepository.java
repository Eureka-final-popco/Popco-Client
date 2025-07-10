package com.popcoclient.content.repository;

import com.popcoclient.content.entity.ContentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentReactionRepository extends JpaRepository<ContentReaction, Long> {
}
