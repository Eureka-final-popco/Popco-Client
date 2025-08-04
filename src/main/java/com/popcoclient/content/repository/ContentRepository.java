package com.popcoclient.content.repository;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.key.ContentId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, ContentId> {

    Page<Content> findAllByOrderByReleaseDateDesc(Pageable pageable);

    @Query(value = "SELECT c FROM Content c ORDER BY c.contentId.id ASC",
            countQuery = "SELECT COUNT(c) FROM Content c")
    Page<Content> findAllByContentIdAsc(Pageable pageable);

    @Query(
            value = "SELECT c FROM Content c " +
                    "LEFT JOIN ContentReaction cr ON c.contentId.id = cr.content.contentId.id " +
                    "  AND c.contentId.type = cr.content.contentId.type " +
                    "GROUP BY c " +
                    "ORDER BY " +
                    "  SUM(CASE WHEN cr.reaction = 'LIKE' THEN 1 ELSE 0 END) - " +
                    "  SUM(CASE WHEN cr.reaction = 'DISLIKE' THEN 1 ELSE 0 END) DESC, " +
                    "  (c.ratingAverage * 100 + c.ratingCount * 0.1) DESC, " +
                    "  c.releaseDate DESC",
            countQuery = "SELECT COUNT(DISTINCT c) FROM Content c " +
                    "LEFT JOIN ContentReaction cr ON c.contentId.id = cr.content.contentId.id " +
                    "  AND c.contentId.type = cr.content.contentId.type"
    )
    Page<Content> findAllOrderByPopularity(Pageable pageable);

    @Query("SELECT DISTINCT c FROM Content c " +
            "LEFT JOIN FETCH c.genreIds " +
            "WHERE c.contentId = :contentId")
    Optional<Content> findByIdWithGenres(@Param("contentId") ContentId contentId);

    @Query("SELECT DISTINCT c FROM Content c " +
            "LEFT JOIN FETCH c.casts cast " +
            "LEFT JOIN FETCH cast.actor " +
            "WHERE c.contentId = :contentId")
    Optional<Content> findByIdWithCasts(@Param("contentId") ContentId contentId);

    @Query("SELECT DISTINCT c FROM Content c " +
            "LEFT JOIN FETCH c.crews crew " +
            "LEFT JOIN FETCH crew.crewMember " +
            "WHERE c.contentId = :contentId")
    Optional<Content> findByIdWithCrews(@Param("contentId") ContentId contentId);

    @Query("SELECT DISTINCT c FROM Content c " +
            "LEFT JOIN FETCH c.videos " +
            "WHERE c.contentId = :contentId")
    Optional<Content> findByIdWithVideos(@Param("contentId") ContentId contentId);

    @Query("SELECT DISTINCT c FROM Content c " +
            "LEFT JOIN FETCH c.watchProviders wp " +
            "LEFT JOIN FETCH wp.provider " +
            "WHERE c.contentId = :contentId")
    Optional<Content> findByIdWithWatchProviders(@Param("contentId") ContentId contentId);

    List<Content> findAllByTitleIn(List<String> titles);
}
