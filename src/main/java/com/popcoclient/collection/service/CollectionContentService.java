package com.popcoclient.collection.service;

import com.popcoclient.collection.dto.request.CollectionContentRequestDto;
import com.popcoclient.collection.dto.response.CollectionContentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CollectionContentService {
    CollectionContentResponseDto addContentToCollection(Long userId, Long collectionId, CollectionContentRequestDto request);
    Page<CollectionContentResponseDto> getCollectionContents(Long collectionId, Integer pageNumber, Integer pageSize);
    List<CollectionContentResponseDto> getAllCollectionContents(Long collectionId);
    void removeContentFromCollection(Long userId, Long collectionId, Long contentId, String contentType);
    long getCollectionContentCount(Long collectionId);
    boolean isContentInCollection(Long collectionId, Long contentId, String contentType);
}
