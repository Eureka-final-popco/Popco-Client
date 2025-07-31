package com.popcoclient.content.repository;

import com.popcoclient.content.entity.ContentReaction;
import com.popcoclient.content.entity.enums.ReactionType;
import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentReactionRepository extends JpaRepository<ContentReaction, Long> {

    @Query("SELECT cr FROM ContentReaction cr " +
            "JOIN FETCH cr.content c " +
            "WHERE cr.user = :user " +
            "AND cr.reaction = :reaction " +
            "ORDER BY cr.updatedAt DESC")
    List<ContentReaction> findByUserAndReactionWithContent(@Param("user") User user, @Param("reaction") ReactionType reaction);

    Integer countByUser_UserIdAndReaction(Long userId, ReactionType reactionType);
}
