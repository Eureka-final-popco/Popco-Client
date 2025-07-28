package com.popcoclient.collection.service.impl;

import com.popcoclient.collection.dto.ContentPosterDto;
import com.popcoclient.collection.dto.request.CollectionRequestDto;
import com.popcoclient.collection.dto.request.CollectionUpdateRequestDto;
import com.popcoclient.collection.dto.response.CollectionListResponseDto;
import com.popcoclient.collection.dto.response.CollectionResponseDto;
import com.popcoclient.collection.entity.Collection;
import com.popcoclient.collection.entity.CollectionContent;
import com.popcoclient.collection.repository.CollectionContentRepository;
import com.popcoclient.collection.repository.CollectionRepository;
import com.popcoclient.collection.service.CollectionService;
import com.popcoclient.exception.business.CollectionAlreadyExistsException;
import com.popcoclient.exception.business.CollectionNotFoundException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final CollectionContentRepository collectionContentRepository;

    @Override
    @Transactional
    public CollectionResponseDto createCollection(Long userId, CollectionRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. id : " + userId));

        if (collectionRepository.existsByUserAndTitle(user, request.getTitle())) {
            throw new CollectionAlreadyExistsException("이미 동일한 제목의 컬렉션이 존재합니다. title : " + request.getTitle());
        }

        Collection collection = Collection.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .saveCount(0)
                .contentCount(0)
                .build();

        Collection savedCollection = collectionRepository.save(collection);
        return CollectionResponseDto.from(savedCollection);
    }

    @Override
    public CollectionResponseDto getCollection(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없습니다. id : " + collectionId));

        // 단일 컬렉션 조회시에도 포스터 정보 포함
        List<ContentPosterDto> posters = getCollectionPosters(collectionId);
        return CollectionResponseDto.from(collection, posters);
    }

    @Override
    public CollectionListResponseDto getUserCollections(Long userId, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Collection> collections = collectionRepository.findByUserUserId(userId, pageable);

        // 컬렉션들의 포스터 정보를 가져오기
        Page<CollectionResponseDto> responseDtos = collections.map(collection -> {
            List<ContentPosterDto> posters = getCollectionPosters(collection.getCollectionId());
            return CollectionResponseDto.from(collection, posters);
        });

        return CollectionListResponseDto.from(responseDtos);
    }

    @Override
    public CollectionListResponseDto searchCollections(String keyword, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Collection> collections = collectionRepository.searchByKeyword(keyword, pageable);

        // 컬렉션들의 포스터 정보를 배치로 가져오기
        Page<CollectionResponseDto> responseDtos = mapCollectionsWithPosters(collections);

        return CollectionListResponseDto.from(responseDtos);
    }

    @Override
    public List<CollectionResponseDto> getCollections(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Collection> collections = collectionRepository.findAll(pageable);

        // 배치로 포스터 정보 가져오기
        return mapCollectionsWithPostersList(collections.getContent());
    }

    @Override
    public List<CollectionResponseDto> getPopularCollections() {
        List<Collection> collections = collectionRepository.findTop10ByOrderBySaveCountDesc();

        // 배치로 포스터 정보 가져오기
        return mapCollectionsWithPostersList(collections);
    }

    @Override
    @Transactional
    public CollectionResponseDto updateCollection(Long userId, Long collectionId, CollectionUpdateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. id : " + userId));

        Collection collection = collectionRepository.findByUserAndCollectionId(user, collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없거나 권한이 없습니다. id : " + collectionId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            collection.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            collection.setDescription(request.getDescription());
        }

        collectionRepository.save(collection);

        List<ContentPosterDto> posters = getCollectionPosters(collectionId);
        return CollectionResponseDto.from(collection, posters);
    }

    @Override
    @Transactional
    public void deleteCollection(Long userId, Long collectionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. id : " + userId));

        Collection collection = collectionRepository.findByUserAndCollectionId(user, collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없거나 권한이 없습니다. id : " + collectionId));

        List<CollectionContent> collectionContents = collectionContentRepository.findByCollection(collection);
        collectionContentRepository.deleteAll(collectionContents);
        collectionRepository.delete(collection);
    }

    @Override
    @Transactional
    public void incrementSaveCount(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없거나 권한이 없습니다. id : " + collectionId));
        collection.setSaveCount(collection.getSaveCount() + 1);
        collectionRepository.save(collection);
    }

    @Override
    @Transactional
    public void decrementSaveCount(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없거나 권한이 없습니다. id : " + collectionId));
        if (collection.getSaveCount() > 0) {
            collection.setSaveCount(collection.getSaveCount() - 1);
            collectionRepository.save(collection);
        }
    }

    // 단일 컬렉션의 포스터 정보 가져오기
    private List<ContentPosterDto> getCollectionPosters(Long collectionId) {
        Pageable topSix = PageRequest.of(0, 6);
        List<CollectionContent> contents = collectionContentRepository
                .findTop6ByCollectionIdOrderByCreatedAtDesc(collectionId, topSix);

        return contents.stream()
                .map(cc -> ContentPosterDto.builder()
                        .contentId(cc.getContent().getContentId().getId())
                        .contentType(cc.getContent().getContentId().getType())
                        .posterPath(cc.getContent().getPosterPath())
                        .title(cc.getContent().getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    // 여러 컬렉션의 포스터 정보를 배치로 가져오기 (N+1 문제 해결)
    private Page<CollectionResponseDto> mapCollectionsWithPosters(Page<Collection> collections) {
        if (collections.isEmpty()) {
            return collections.map(CollectionResponseDto::from);
        }

        // 모든 컬렉션 ID 수집
        List<Long> collectionIds = collections.getContent().stream()
                .map(Collection::getCollectionId)
                .collect(Collectors.toList());

        // 한 번의 쿼리로 모든 컬렉션의 컨텐츠 가져오기
        List<CollectionContent> allContents = collectionContentRepository
                .findByCollectionIdsWithContent(collectionIds);

        // 컬렉션별로 컨텐츠 그룹화 (최대 6개씩)
        Map<Long, List<ContentPosterDto>> postersMap = new HashMap<>();
        for (CollectionContent cc : allContents) {
            Long collectionId = cc.getCollection().getCollectionId();

            postersMap.computeIfAbsent(collectionId, k -> new ArrayList<>());

            if (postersMap.get(collectionId).size() < 6) {
                ContentPosterDto poster = ContentPosterDto.builder()
                        .contentId(cc.getContent().getContentId().getId())
                        .contentType(cc.getContent().getContentId().getType())
                        .posterPath(cc.getContent().getPosterPath())
                        .title(cc.getContent().getTitle())
                        .build();
                postersMap.get(collectionId).add(poster);
            }
        }

        // 컬렉션과 포스터 정보 매핑
        return collections.map(collection ->
                CollectionResponseDto.from(collection,
                        postersMap.getOrDefault(collection.getCollectionId(), new ArrayList<>()))
        );
    }

    // List 버전
    private List<CollectionResponseDto> mapCollectionsWithPostersList(List<Collection> collections) {
        if (collections.isEmpty()) {
            return collections.stream()
                    .map(CollectionResponseDto::from)
                    .collect(Collectors.toList());
        }

        List<Long> collectionIds = collections.stream()
                .map(Collection::getCollectionId)
                .collect(Collectors.toList());

        List<CollectionContent> allContents = collectionContentRepository
                .findByCollectionIdsWithContent(collectionIds);

        Map<Long, List<ContentPosterDto>> postersMap = new HashMap<>();
        for (CollectionContent cc : allContents) {
            Long collectionId = cc.getCollection().getCollectionId();

            postersMap.computeIfAbsent(collectionId, k -> new ArrayList<>());

            if (postersMap.get(collectionId).size() < 6) {
                ContentPosterDto poster = ContentPosterDto.builder()
                        .contentId(cc.getContent().getContentId().getId())
                        .contentType(cc.getContent().getContentId().getType())
                        .posterPath(cc.getContent().getPosterPath())
                        .title(cc.getContent().getTitle())
                        .build();
                postersMap.get(collectionId).add(poster);
            }
        }

        return collections.stream()
                .map(collection -> CollectionResponseDto.from(collection,
                        postersMap.getOrDefault(collection.getCollectionId(), new ArrayList<>())))
                .collect(Collectors.toList());
    }

}
