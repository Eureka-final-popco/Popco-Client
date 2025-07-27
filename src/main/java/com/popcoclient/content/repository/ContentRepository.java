package com.popcoclient.content.repository;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.key.ContentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, ContentId> {

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
}
