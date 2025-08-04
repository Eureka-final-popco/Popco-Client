package com.popcoclient.collection.repository;

import com.popcoclient.collection.entity.Collection;
import com.popcoclient.collection.entity.MarkedCollection;
import com.popcoclient.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarkedCollectionRepository extends JpaRepository<MarkedCollection, Long> {

    // 사용자와 컬렉션으로 마크 찾기
    Optional<MarkedCollection> findByUserAndCollection(User user, Collection collection);

    // 사용자와 컬렉션으로 존재 여부 확인
    boolean existsByUserAndCollection(User user, Collection collection);

    // 사용자가 마크한 컬렉션 목록 조회
    @Query("SELECT mc FROM MarkedCollection mc " +
            "JOIN FETCH mc.collection c " +
            "JOIN FETCH c.user u " +
            "LEFT JOIN FETCH u.userDetail " +
            "WHERE mc.user.userId = :userId " +
            "ORDER BY mc.createdAt ASC")
    Page<MarkedCollection> findByUserIdWithCollection(@Param("userId") Long userId, Pageable pageable);

    // 최근 일주일간 마크된 컬렉션 중 인기 컬렉션 (saveCount 높은 순)
    @Query("SELECT DISTINCT c FROM Collection c " +
            "JOIN FETCH c.user u " +
            "LEFT JOIN FETCH u.userDetail " +
            "WHERE c.collectionId IN (" +
            "  SELECT DISTINCT mc.collection.collectionId FROM MarkedCollection mc " +
            "  WHERE mc.createdAt >= :weekAgo" +
            ") " +
            "ORDER BY c.saveCount DESC")
    List<Collection> findPopularCollectionsLastWeek(@Param("weekAgo") LocalDateTime weekAgo, Pageable pageable);

    // 사용자가 마크한 컬렉션 ID 목록
    @Query("SELECT mc.collection.collectionId FROM MarkedCollection mc WHERE mc.user.userId = :userId")
    List<Long> findMarkedCollectionIdsByUserId(@Param("userId") Long userId);

    // 특정 컬렉션들에 대한 사용자의 마크 여부 확인
    @Query("SELECT mc.collection.collectionId FROM MarkedCollection mc " +
            "WHERE mc.user.userId = :userId AND mc.collection.collectionId IN :collectionIds")
    List<Long> findMarkedCollectionIdsByUserIdAndCollectionIds(@Param("userId") Long userId,
                                                               @Param("collectionIds") List<Long> collectionIds);

    // 사용자와 컬렉션으로 삭제
    void deleteByUserAndCollection(User user, Collection collection);

    // 특정 컬렉션의 모든 마크 조회
    List<MarkedCollection> findAllByCollection(Collection collection);

}
