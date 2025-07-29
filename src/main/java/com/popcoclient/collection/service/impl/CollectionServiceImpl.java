package com.popcoclient.collection.service.impl;

import com.popcoclient.collection.dto.ContentPosterDto;
import com.popcoclient.collection.dto.request.CollectionRequestDto;
import com.popcoclient.collection.dto.request.CollectionUpdateRequestDto;
import com.popcoclient.collection.dto.response.CollectionListResponseDto;
import com.popcoclient.collection.dto.response.CollectionResponseDto;
import com.popcoclient.collection.entity.Collection;
import com.popcoclient.collection.entity.CollectionContent;
import com.popcoclient.collection.entity.MarkedCollection;
import com.popcoclient.collection.repository.CollectionContentRepository;
import com.popcoclient.collection.repository.CollectionRepository;
import com.popcoclient.collection.repository.MarkedCollectionRepository;
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
    private final MarkedCollectionRepository markedCollectionRepository;

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

        List<ContentPosterDto> posters = getCollectionPosters(collectionId);
        return CollectionResponseDto.from(collection, posters);
    }

    @Override
    public CollectionResponseDto getCollection(Long collectionId, Long userId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없습니다. id : " + collectionId));

        List<ContentPosterDto> posters = getCollectionPosters(collectionId);
        boolean isMarked = false;
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                isMarked = markedCollectionRepository.existsByUserAndCollection(user, collection);
            }
        }

        return CollectionResponseDto.from(collection, posters, isMarked);
    }

    @Override
    public CollectionListResponseDto getUserCollections(Long userId, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Collection> collections = collectionRepository.findByUserUserId(userId, pageable);

        Page<CollectionResponseDto> responseDtos = collections.map(collection -> {
            List<ContentPosterDto> posters = getCollectionPosters(collection.getCollectionId());
            return CollectionResponseDto.from(collection, posters);
        });

        return CollectionListResponseDto.from(responseDtos);
    }

    @Override
    public CollectionListResponseDto getUserCollections(Long targetUserId, Long currentUserId, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Collection> collections = collectionRepository.findByUserUserId(targetUserId, pageable);

        Page<CollectionResponseDto> responseDtos = mapCollectionsWithPostersAndMarks(collections, currentUserId);
        return CollectionListResponseDto.from(responseDtos);
    }

    @Override
    public CollectionListResponseDto searchCollections(String keyword, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Collection> collections = collectionRepository.searchByKeyword(keyword, pageable);

        Page<CollectionResponseDto> responseDtos = mapCollectionsWithPosters(collections);
        return CollectionListResponseDto.from(responseDtos);
    }

    @Override
    public CollectionListResponseDto searchCollections(String keyword, Long userId, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Collection> collections = collectionRepository.searchByKeyword(keyword, pageable);

        Page<CollectionResponseDto> responseDtos = mapCollectionsWithPostersAndMarks(collections, userId);
        return CollectionListResponseDto.from(responseDtos);
    }

    @Override
    public List<CollectionResponseDto> getCollections(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Collection> collections = collectionRepository.findAllByOrderByCreatedAtDesc(pageable);

        return mapCollectionsWithPostersList(collections.getContent());
    }

    @Override
    public List<CollectionResponseDto> getCollections(Long userId, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Collection> collections = collectionRepository.findAllByOrderByCreatedAtDesc(pageable);

        return mapCollectionsWithPostersAndMarksList(collections.getContent(), userId);
    }

    @Override
    public List<CollectionResponseDto> getPopularCollections() {
        List<Collection> collections = collectionRepository.findTop10ByOrderBySaveCountDesc();
        return mapCollectionsWithPostersList(collections);
    }

    @Override
    public List<CollectionResponseDto> getPopularCollections(Long userId) {
        List<Collection> collections = collectionRepository.findTop10ByOrderBySaveCountDesc();
        return mapCollectionsWithPostersAndMarksList(collections, userId);
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

        // 컬렉션의 모든 컨텐츠 삭제
        List<CollectionContent> collectionContents = collectionContentRepository.findAllByCollection(collection);
        collectionContentRepository.deleteAll(collectionContents);

        // 컬렉션의 모든 마크 삭제
        List<MarkedCollection> markedCollections = markedCollectionRepository.findAllByCollection(collection);
        markedCollectionRepository.deleteAll(markedCollections);

        // 컬렉션 삭제
        collectionRepository.delete(collection);
    }

    // Helper methods
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

    private Page<CollectionResponseDto> mapCollectionsWithPosters(Page<Collection> collections) {
        if (collections.isEmpty()) {
            return collections.map(CollectionResponseDto::from);
        }

        List<Long> collectionIds = collections.getContent().stream()
                .map(Collection::getCollectionId)
                .collect(Collectors.toList());

        Map<Long, List<ContentPosterDto>> postersMap = getPostersMap(collectionIds);

        return collections.map(collection ->
                CollectionResponseDto.from(collection,
                        postersMap.getOrDefault(collection.getCollectionId(), new ArrayList<>()))
        );
    }

    private Page<CollectionResponseDto> mapCollectionsWithPostersAndMarks(Page<Collection> collections, Long userId) {
        if (collections.isEmpty()) {
            return collections.map(CollectionResponseDto::from);
        }

        List<Long> collectionIds = collections.getContent().stream()
                .map(Collection::getCollectionId)
                .collect(Collectors.toList());

        Map<Long, List<ContentPosterDto>> postersMap = getPostersMap(collectionIds);

        Map<Long, Boolean> markedMap = new HashMap<>();

        if (userId != null) {
            List<Long> markedCollectionIds = markedCollectionRepository
                    .findMarkedCollectionIdsByUserIdAndCollectionIds(userId, collectionIds);
            markedCollectionIds.forEach(id -> markedMap.put(id, true));
        }

        return collections.map(collection -> {
            List<ContentPosterDto> posters = postersMap.getOrDefault(collection.getCollectionId(), new ArrayList<>());
            boolean isMarked = markedMap.getOrDefault(collection.getCollectionId(), false);
            return CollectionResponseDto.from(collection, posters, isMarked);
        });
    }

    private List<CollectionResponseDto> mapCollectionsWithPostersList(List<Collection> collections) {
        if (collections.isEmpty()) {
            return collections.stream()
                    .map(CollectionResponseDto::from)
                    .collect(Collectors.toList());
        }

        List<Long> collectionIds = collections.stream()
                .map(Collection::getCollectionId)
                .collect(Collectors.toList());

        Map<Long, List<ContentPosterDto>> postersMap = getPostersMap(collectionIds);

        return collections.stream()
                .map(collection -> CollectionResponseDto.from(collection,
                        postersMap.getOrDefault(collection.getCollectionId(), new ArrayList<>())))
                .collect(Collectors.toList());
    }

    private List<CollectionResponseDto> mapCollectionsWithPostersAndMarksList(List<Collection> collections, Long userId) {
        if (collections.isEmpty()) {
            return collections.stream()
                    .map(CollectionResponseDto::from)
                    .collect(Collectors.toList());
        }

        List<Long> collectionIds = collections.stream()
                .map(Collection::getCollectionId)
                .collect(Collectors.toList());

        Map<Long, List<ContentPosterDto>> postersMap = getPostersMap(collectionIds);

        Map<Long, Boolean> markedMap = new HashMap<>();

        if (userId != null) {
            List<Long> markedCollectionIds = markedCollectionRepository
                    .findMarkedCollectionIdsByUserIdAndCollectionIds(userId, collectionIds);
            markedCollectionIds.forEach(id -> markedMap.put(id, true));
        }

        return collections.stream()
                .map(collection -> {
                    List<ContentPosterDto> posters = postersMap.getOrDefault(collection.getCollectionId(), new ArrayList<>());
                    boolean isMarked = markedMap.getOrDefault(collection.getCollectionId(), false);
                    return CollectionResponseDto.from(collection, posters, isMarked);
                })
                .collect(Collectors.toList());
    }

    private Map<Long, List<ContentPosterDto>> getPostersMap(List<Long> collectionIds) {
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
        return postersMap;
    }

}
