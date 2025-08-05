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
import java.util.Set;

@Repository
public interface ContentReactionRepository extends JpaRepository<ContentReaction, Long> {

    @Query("SELECT cr FROM ContentReaction cr " +
            "JOIN FETCH cr.content c " +
            "WHERE cr.user = :user " +
            "AND cr.reaction = :reaction " +
            "ORDER BY cr.updatedAt DESC")
    List<ContentReaction> findByUserAndReactionWithContent(@Param("user") User user, @Param("reaction") ReactionType reaction);

    Integer countByUser_UserIdAndReaction(Long userId, ReactionType reactionType);

    @Query("SELECT cr FROM ContentReaction cr " +
            "WHERE cr.user.userId = :userId AND cr.content.contentId IN :contentIds")
    List<ContentReaction> findByUserIdAndContentIds(@Param("userId") Long userId,
                                                    @Param("contentIds") Set<ContentId> contentIds);

    @Query("SELECT new com.popcoclient.content.entity.key.ContentId(cr.content.contentId.id, cr.content.contentId.type) " +
            "FROM ContentReaction cr WHERE cr.user.userId = :userId AND cr.reaction = :reactionType")
    Set<ContentId> findContentIdsByUserAndReaction(@Param("userId") Long userId, @Param("reactionType") ReactionType reactionType);

    @Query("SELECT cr FROM ContentReaction cr " +
            "WHERE cr.user.userId = :userId " +
            "AND cr.content.contentId.id = :contentId " +
            "AND cr.content.contentId.type = :contentType " +
            "AND cr.reaction = :reactionType")
    Optional<ContentReaction> findByUserAndContentAndReaction(
            @Param("userId") Long userId,
            @Param("contentId") Long contentId,
            @Param("contentType") String contentType,
            @Param("reactionType") ReactionType reactionType);
}
