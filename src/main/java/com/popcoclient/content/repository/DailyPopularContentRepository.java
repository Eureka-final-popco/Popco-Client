package com.popcoclient.content.repository;

import com.popcoclient.content.entity.DailyPopularContent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyPopularContentRepository extends JpaRepository<DailyPopularContent, Long> {

    @EntityGraph(attributePaths = {"content"})
    List<DailyPopularContent> findByBatchContentTypeAndRankedDate(String batchType, LocalDate rankedDate);

    @Query("SELECT d FROM DailyPopularContent d " +
            "WHERE d.batchContentType = :batchType AND d.rankedDate = :rankedDate AND d.ranking = 1")
    DailyPopularContent findFirstRanked(@Param("batchType") String batchType,
                                           @Param("rankedDate") LocalDate rankedDate);

}
