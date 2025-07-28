package com.popcoclient.collection.repository;

import com.popcoclient.collection.entity.Collection;
import com.popcoclient.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Page<Collection> findByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.userDetail"})
    Page<Collection> findByUserUserId(Long userId, Pageable pageable);

    @Query("SELECT c FROM Collection c WHERE c.user = :user AND c.collectionId = :collectionId")
    Optional<Collection> findByUserAndCollectionId(@Param("user") User user, @Param("collectionId") Long collectionId);

    @EntityGraph(attributePaths = {"user", "user.userDetail"})
    @Query("SELECT c FROM Collection c WHERE c.title LIKE %:keyword% OR c.description LIKE %:keyword%")
    Page<Collection> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.userDetail"})
    List<Collection> findTop10ByOrderBySaveCountDesc();

    boolean existsByUserAndTitle(User user, String title);

    // 모든 컬렉션 조회시 User 정보도 함께 가져오기
    @EntityGraph(attributePaths = {"user", "user.userDetail"})
    Page<Collection> findAll(Pageable pageable);
}
