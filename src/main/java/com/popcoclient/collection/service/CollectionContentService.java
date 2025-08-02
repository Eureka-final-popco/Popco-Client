package com.popcoclient.collection.service;

import com.popcoclient.collection.dto.request.CollectionContentBatchRequestDto;
import com.popcoclient.collection.dto.request.CollectionContentRequestDto;
import com.popcoclient.collection.dto.response.CollectionContentBatchResponseDto;
import com.popcoclient.collection.dto.response.CollectionContentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CollectionContentService {
    // 컬렉션에 컨텐츠 추가
    CollectionContentResponseDto addContentToCollection(Long userId, Long collectionId, CollectionContentRequestDto request);

    // 컬렉션에 여러 컨텐츠 한번에 추가
    CollectionContentBatchResponseDto addMultipleContentsToCollection(Long userId, Long collectionId, CollectionContentBatchRequestDto request);

    // 컬렉션의 컨텐츠 목록 조회 (페이징)
    Page<CollectionContentResponseDto> getCollectionContents(Long collectionId, Integer pageNumber, Integer pageSize);

    // 컬렉션의 모든 컨텐츠 조회
    List<CollectionContentResponseDto> getAllCollectionContents(Long collectionId);

    // 컬렉션에서 컨텐츠 제거
    void removeContentFromCollection(Long userId, Long collectionId, Long contentId, String contentType);

    // 컬렉션의 컨텐츠 개수 조회
    long getCollectionContentCount(Long collectionId);

    // 특정 컨텐츠가 컬렉션에 포함되어 있는지 확인
    boolean isContentInCollection(Long collectionId, Long contentId, String contentType);
}
