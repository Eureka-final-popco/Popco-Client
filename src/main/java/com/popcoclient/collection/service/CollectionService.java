package com.popcoclient.collection.service;

import com.popcoclient.collection.dto.request.CollectionRequestDto;
import com.popcoclient.collection.dto.request.CollectionUpdateRequestDto;
import com.popcoclient.collection.dto.response.CollectionListResponseDto;
import com.popcoclient.collection.dto.response.CollectionResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CollectionService {

    // 컬렉션 생성
    CollectionResponseDto createCollection(Long userId, CollectionRequestDto request);

    // 특정 컬렉션 조회 (로그인하지 않은 경우)
    CollectionResponseDto getCollection(Long collectionId);

    // 특정 컬렉션 조회 (로그인한 경우 - 마크 여부 포함)
    CollectionResponseDto getCollection(Long collectionId, Long userId);

    // 사용자의 컬렉션 목록 조회 (로그인하지 않은 경우)
    CollectionListResponseDto getUserCollections(Long userId, Integer pageNumber, Integer pageSize);

    // 사용자의 컬렉션 목록 조회 (로그인한 경우 - 마크 여부 포함)
    CollectionListResponseDto getUserCollections(Long targetUserId, Long currentUserId, Integer pageNumber, Integer pageSize);

    // 컬렉션 검색 (로그인하지 않은 경우)
    CollectionListResponseDto searchCollections(String keyword, Integer pageNumber, Integer pageSize);

    // 컬렉션 검색 (로그인한 경우 - 마크 여부 포함)
    CollectionListResponseDto searchCollections(String keyword, Long userId, Integer pageNumber, Integer pageSize);

    // 전체 컬렉션 목록 (로그인하지 않은 경우)
    List<CollectionResponseDto> getCollections(Integer pageNumber, Integer pageSize);

    // 전체 컬렉션 목록 (로그인한 경우 - 마크 여부 포함)
    List<CollectionResponseDto> getCollections(Long userId, Integer pageNumber, Integer pageSize);

    // 인기 컬렉션 목록 (로그인하지 않은 경우)
    List<CollectionResponseDto> getPopularCollections();

    // 인기 컬렉션 목록 (로그인한 경우 - 마크 여부 포함)
    List<CollectionResponseDto> getPopularCollections(Long userId);

    // 컬렉션 수정
    CollectionResponseDto updateCollection(Long userId, Long collectionId, CollectionUpdateRequestDto request);

    // 컬렉션 삭제
    void deleteCollection(Long userId, Long collectionId);

}
