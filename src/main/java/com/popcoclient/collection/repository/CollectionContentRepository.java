package com.popcoclient.collection.repository;

import com.popcoclient.collection.entity.Collection;
import com.popcoclient.collection.entity.CollectionContent;
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
public interface CollectionContentRepository extends JpaRepository<CollectionContent, Long> {

    List<CollectionContent> findAllByCollection(Collection collection);

    Page<CollectionContent> findByCollection(Collection collection, Pageable pageable);

    Page<CollectionContent> findByCollectionCollectionId(Long collectionId, Pageable pageable);

    @Query("SELECT cc FROM CollectionContent cc WHERE cc.collection = :collection AND cc.content.contentId = :contentId")
    Optional<CollectionContent> findByCollectionAndContentId(@Param("collection") Collection collection, @Param("contentId") ContentId contentId);

    boolean existsByCollectionAndContent(Collection collection, Content content);

    @Query("SELECT COUNT(cc) FROM CollectionContent cc WHERE cc.collection = :collection")
    long countByCollection(@Param("collection") Collection collection);

    @Query("SELECT cc FROM CollectionContent cc JOIN FETCH cc.content WHERE cc.collection.collectionId = :collectionId")
    List<CollectionContent> findByCollectionIdWithContent(@Param("collectionId") Long collectionId);

    void deleteByCollectionAndContent(Collection collection, Content content);

    // 컬렉션의 최신 6개 컨텐츠 포스터 정보 가져오기
    @Query("SELECT cc FROM CollectionContent cc " +
            "JOIN FETCH cc.content c " +
            "WHERE cc.collection.collectionId = :collectionId " +
            "ORDER BY cc.createdAt DESC")
    List<CollectionContent> findTop6ByCollectionIdOrderByCreatedAtDesc(@Param("collectionId") Long collectionId, Pageable pageable);

    // 여러 컬렉션의 컨텐츠를 한번에 가져오기 (N+1 문제 해결)
    @Query("SELECT cc FROM CollectionContent cc " +
            "JOIN FETCH cc.content c " +
            "WHERE cc.collection.collectionId IN :collectionIds " +
            "ORDER BY cc.collection.collectionId, cc.createdAt DESC")
    List<CollectionContent> findByCollectionIdsWithContent(@Param("collectionIds") List<Long> collectionIds);
}
