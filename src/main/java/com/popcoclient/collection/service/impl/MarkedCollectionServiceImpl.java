package com.popcoclient.collection.service.impl;

import com.popcoclient.collection.dto.response.ContentPosterDto;
import com.popcoclient.collection.dto.response.CollectionListResponseDto;
import com.popcoclient.collection.dto.response.CollectionResponseDto;
import com.popcoclient.collection.entity.Collection;
import com.popcoclient.collection.entity.CollectionContent;
import com.popcoclient.collection.entity.MarkedCollection;
import com.popcoclient.collection.repository.CollectionContentRepository;
import com.popcoclient.collection.repository.CollectionRepository;
import com.popcoclient.collection.repository.MarkedCollectionRepository;
import com.popcoclient.collection.service.MarkedCollectionService;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarkedCollectionServiceImpl implements MarkedCollectionService {

    private final MarkedCollectionRepository markedCollectionRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionContentRepository collectionContentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public boolean toggleMarkCollection(Long userId, Long collectionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. id : " + userId));

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없습니다. id : " + collectionId));

        Optional<MarkedCollection> existingMark = markedCollectionRepository.findByUserAndCollection(user, collection);

        if (existingMark.isPresent()) {
            // 언마크: saveCount 감소
            markedCollectionRepository.delete(existingMark.get());
            if (collection.getSaveCount() > 0) {
                collection.setSaveCount(collection.getSaveCount() - 1);
                collectionRepository.save(collection);
            }
            return false; // 언마크됨
        } else {
            // 마크: saveCount 증가
            MarkedCollection markedCollection = MarkedCollection.builder()
                    .user(user)
                    .collection(collection)
                    .build();
            markedCollectionRepository.save(markedCollection);
            collection.setSaveCount(collection.getSaveCount() + 1);
            collectionRepository.save(collection);
            return true; // 마크됨
        }
    }

    @Override
    @Transactional
    public void markCollection(Long userId, Long collectionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. id : " + userId));

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없습니다. id : " + collectionId));

        if (!markedCollectionRepository.existsByUserAndCollection(user, collection)) {
            MarkedCollection markedCollection = MarkedCollection.builder()
                    .user(user)
                    .collection(collection)
                    .build();
            markedCollectionRepository.save(markedCollection);

            // saveCount 증가
            collection.setSaveCount(collection.getSaveCount() + 1);
            collectionRepository.save(collection);
        }
    }

    @Override
    @Transactional
    public void unmarkCollection(Long userId, Long collectionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. id : " + userId));

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없습니다. id : " + collectionId));

        if (markedCollectionRepository.existsByUserAndCollection(user, collection)) {
            markedCollectionRepository.deleteByUserAndCollection(user, collection);

            // saveCount 감소
            if (collection.getSaveCount() > 0) {
                collection.setSaveCount(collection.getSaveCount() - 1);
                collectionRepository.save(collection);
            }
        }
    }

    @Override
    public boolean isMarkedByUser(Long userId, Long collectionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. id : " + userId));

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없습니다. id : " + collectionId));

        return markedCollectionRepository.existsByUserAndCollection(user, collection);
    }

    @Override
    public CollectionListResponseDto getUserMarkedCollections(Long userId, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<MarkedCollection> markedCollections = markedCollectionRepository.findByUserIdWithCollection(userId, pageable);

        // MarkedCollection에서 Collection 추출
        List<Collection> collections = markedCollections.getContent().stream()
                .map(MarkedCollection::getCollection)
                .collect(Collectors.toList());

        // 포스터 정보와 마크 정보 매핑
        Page<CollectionResponseDto> responseDtos = markedCollections.map(mc -> {
            Collection collection = mc.getCollection();
            List<ContentPosterDto> posters = getCollectionPosters(collection.getCollectionId());
            return CollectionResponseDto.from(collection, posters, true);
        });

        return CollectionListResponseDto.from(responseDtos);
    }

    @Override
    public List<CollectionResponseDto> getWeeklyPopularCollections(Long userId, Integer limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        Pageable pageable = PageRequest.of(0, limit);

        List<Collection> popularCollections = markedCollectionRepository.findPopularCollectionsLastWeek(weekAgo, pageable);

        // 사용자의 마크 정보 가져오기
        List<Long> markedCollectionIds = userId != null ?
                markedCollectionRepository.findMarkedCollectionIdsByUserId(userId) : new ArrayList<>();

        return popularCollections.stream()
                .map(collection -> {
                    List<ContentPosterDto> posters = getCollectionPosters(collection.getCollectionId());
                    boolean isMarked = markedCollectionIds.contains(collection.getCollectionId());
                    return CollectionResponseDto.from(collection, posters, isMarked);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CollectionListResponseDto getCollectionsByContent(Long contentId, String contentType,
                                                             String sortType, Long userId,
                                                             Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // 정렬 방식에 따라 다른 쿼리 실행
        Page<Collection> collections;
        if ("latest".equalsIgnoreCase(sortType)) {
            collections = collectionRepository.findByContentOrderByCreatedAt(contentId, contentType, pageable);
        } else {  // default: popular
            collections = collectionRepository.findByContentOrderByPopularity(contentId, contentType, pageable);
        }

        // 사용자의 마크 정보와 포스터 정보 매핑
        Page<CollectionResponseDto> responseDtos = mapCollectionsWithPostersAndMarks(collections, userId);

        return CollectionListResponseDto.from(responseDtos);
    }

    // Helper Methods
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

    private Page<CollectionResponseDto> mapCollectionsWithPostersAndMarks(Page<Collection> collections, Long userId) {
        if (collections.isEmpty()) {
            return collections.map(CollectionResponseDto::of);
        }

        // 컬렉션 ID 목록
        List<Long> collectionIds = collections.getContent().stream()
                .map(Collection::getCollectionId)
                .collect(Collectors.toList());

        // 포스터 정보 가져오기
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

        // 사용자의 마크 정보 가져오기
        List<Long> markedCollectionIds = userId != null ?
                markedCollectionRepository.findMarkedCollectionIdsByUserIdAndCollectionIds(userId, collectionIds) :
                new ArrayList<>();

        // 결과 매핑
        return collections.map(collection -> {
            List<ContentPosterDto> posters = postersMap.getOrDefault(collection.getCollectionId(), new ArrayList<>());
            boolean isMarked = markedCollectionIds.contains(collection.getCollectionId());
            return CollectionResponseDto.from(collection, posters, isMarked);
        });
    }
}
