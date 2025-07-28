package com.popcoclient.collection.service;

import com.popcoclient.collection.dto.request.CollectionRequestDto;
import com.popcoclient.collection.dto.request.CollectionUpdateRequestDto;
import com.popcoclient.collection.dto.response.CollectionListResponseDto;
import com.popcoclient.collection.dto.response.CollectionResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CollectionService {
    CollectionResponseDto createCollection(Long userId, CollectionRequestDto request);
    CollectionResponseDto getCollection(Long collectionId);
    CollectionListResponseDto getUserCollections(Long userId, Integer pageNumber, Integer pageSize);
    CollectionListResponseDto searchCollections(String keyword, Integer pageNumber, Integer pageSize);
    List<CollectionResponseDto> getCollections(Integer pageNumber, Integer pageSize);
    List<CollectionResponseDto> getPopularCollections();
    CollectionResponseDto updateCollection(Long userId, Long collectionId, CollectionUpdateRequestDto request);
    void deleteCollection(Long userId, Long collectionId);
    void incrementSaveCount(Long collectionId);
    void decrementSaveCount(Long collectionId);
}
