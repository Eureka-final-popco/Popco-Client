package com.popcoclient.collection.service;

import com.popcoclient.collection.dto.response.CollectionListResponseDto;
import com.popcoclient.collection.dto.response.CollectionResponseDto;

import java.util.List;

public interface MarkedCollectionService {

    // 컬렉션 마크/언마크 토글
    boolean toggleMarkCollection(Long userId, Long collectionId);

    // 컬렉션 마크
    void markCollection(Long userId, Long collectionId);

    // 컬렉션 언마크
    void unmarkCollection(Long userId, Long collectionId);

    // 사용자가 마크한 컬렉션인지 확인
    boolean isMarkedByUser(Long userId, Long collectionId);

    // 사용자가 마크한 컬렉션 목록 조회
    CollectionListResponseDto getUserMarkedCollections(Long userId, Integer pageNumber, Integer pageSize);

    // 최근 일주일간 인기 컬렉션 목록
    List<CollectionResponseDto> getWeeklyPopularCollections(Long userId, Integer limit);

    // 특정 컨텐츠를 포함한 컬렉션 목록 조회
    CollectionListResponseDto getCollectionsByContent(Long contentId, String contentType,
                                                      String sortType, Long userId,
                                                      Integer pageNumber, Integer pageSize);
}
