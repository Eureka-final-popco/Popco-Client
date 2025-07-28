package com.popcoclient.content.repository;

import com.popcoclient.content.dto.response.ContentRecommendResponseDto;
import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.ContentRecommendation;
import com.popcoclient.content.entity.key.ContentRecommendationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRecommendationRepository extends JpaRepository<ContentRecommendation, ContentRecommendationId> {

    @Query(
            "select new com.popcoclient.content.dto.response.ContentRecommendResponseDto(" +
                    "cr.recommendedContent.contentId.id, " +
                    "cr.recommendedContent.contentId.type, " +
                    "cr.recommendedContent.title, " +
                    "cr.ranking, " +
                    "cr.recommendedContent.posterPath, " +
                    "null" +
                    ") " +
                    "from ContentRecommendation cr " +
                    "where cr.sourceContent = :sourceContent"
    )
    List<ContentRecommendResponseDto> findWithoutUserReactions(@Param("sourceContent") Content sourceContent);

    @Query(
            "select new com.popcoclient.content.dto.response.ContentRecommendResponseDto(" +
                    "cr.recommendedContent.contentId.id, " +
                    "cr.recommendedContent.contentId.type, " +
                    "cr.recommendedContent.title, " +
                    "cr.ranking, " +
                    "cr.recommendedContent.posterPath, " +
                    "r.reaction" +
                    ") " +
                    "from ContentRecommendation cr " +
                    "left join ContentReaction r " +
                    "on r.content = cr.recommendedContent " +
                    "and r.user.userId = :userId " +
                    "where cr.sourceContent = :sourceContent"
    )
    List<ContentRecommendResponseDto> findWithUserReactions(
            @Param("sourceContent") Content sourceContent,
            @Param("userId") Long userId
    );

}
