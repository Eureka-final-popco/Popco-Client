package com.popcoclient.collection.service.impl;

import com.popcoclient.collection.dto.request.CollectionContentBatchRequestDto;
import com.popcoclient.collection.dto.request.CollectionContentRequestDto;
import com.popcoclient.collection.dto.response.CollectionContentBatchResponseDto;
import com.popcoclient.collection.dto.response.CollectionContentResponseDto;
import com.popcoclient.collection.dto.response.FailedContentDto;
import com.popcoclient.collection.entity.Collection;
import com.popcoclient.collection.entity.CollectionContent;
import com.popcoclient.collection.repository.CollectionContentRepository;
import com.popcoclient.collection.repository.CollectionRepository;
import com.popcoclient.collection.service.CollectionContentService;
import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.content.repository.ContentRepository;
import com.popcoclient.exception.business.CollectionNotFoundException;
import com.popcoclient.exception.business.ContentAlreadyExistsInCollectionException;
import com.popcoclient.exception.business.ContentNotFoundException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionContentServiceImpl implements CollectionContentService {

    private final CollectionContentRepository collectionContentRepository;
    private final CollectionRepository collectionRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CollectionContentResponseDto addContentToCollection(Long userId, Long collectionId, CollectionContentRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. id : " + userId));

        Collection collection = collectionRepository.findByUserAndCollectionId(user, collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없거나 권한이 없습니다. userid : " + userId + " collectionid : " + collectionId));

        ContentId contentId = new ContentId(request.getContentId(), request.getContentType());
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException("컨텐츠를 찾을 수 없습니다. id : " + request.getContentId() + " type : " + request.getContentType()));

        if (collectionContentRepository.existsByCollectionAndContent(collection, content)) {
            throw new ContentAlreadyExistsInCollectionException("이미 컬렉션에 추가된 컨텐츠입니다.");
        }

        CollectionContent collectionContent = CollectionContent.builder()
                .collection(collection)
                .content(content)
                .build();

        // 컬렉션의 컨텐츠 수 증가
        collection.setContentCount(collection.getContentCount() + 1);
        collectionRepository.save(collection);

        // CollectionContent 저장
        CollectionContent savedContent = collectionContentRepository.save(collectionContent);
        return CollectionContentResponseDto.from(savedContent);
    }

    @Override
    @Transactional
    public CollectionContentBatchResponseDto addMultipleContentsToCollection(Long userId, Long collectionId, CollectionContentBatchRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. id : " + userId));

        Collection collection = collectionRepository.findByUserAndCollectionId(user, collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없거나 권한이 없습니다. userid : " + userId + " collectionid : " + collectionId));

        List<CollectionContentResponseDto> successContents = new ArrayList<>();
        List<FailedContentDto> failedContents = new ArrayList<>();
        int addedCount = 0;

        for (CollectionContentRequestDto contentRequest : request.getContents()) {
            try {
                ContentId contentId = new ContentId(contentRequest.getContentId(), contentRequest.getContentType());

                // 컨텐츠 존재 확인
                Content content = contentRepository.findById(contentId)
                        .orElseThrow(() -> new ContentNotFoundException("컨텐츠를 찾을 수 없습니다"));

                // 중복 확인
                if (collectionContentRepository.existsByCollectionAndContent(collection, content)) {
                    failedContents.add(FailedContentDto.of(
                            contentRequest.getContentId(),
                            contentRequest.getContentType(),
                            "이미 컬렉션에 존재하는 컨텐츠입니다"
                    ));
                    continue;
                }

                // CollectionContent 생성 및 저장
                CollectionContent collectionContent = CollectionContent.builder()
                        .collection(collection)
                        .content(content)
                        .build();

                CollectionContent savedContent = collectionContentRepository.save(collectionContent);
                successContents.add(CollectionContentResponseDto.from(savedContent));
                addedCount++;

            } catch (ContentNotFoundException e) {
                failedContents.add(FailedContentDto.of(
                        contentRequest.getContentId(),
                        contentRequest.getContentType(),
                        "컨텐츠를 찾을 수 없습니다"
                ));
            } catch (Exception e) {
                log.error("컨텐츠 추가 중 오류 발생: contentId={}, type={}",
                        contentRequest.getContentId(), contentRequest.getContentType(), e);
                failedContents.add(FailedContentDto.of(
                        contentRequest.getContentId(),
                        contentRequest.getContentType(),
                        "컨텐츠 추가 중 오류가 발생했습니다"
                ));
            }
        }

        // 컬렉션의 컨텐츠 수 업데이트
        if (addedCount > 0) {
            collection.setContentCount(collection.getContentCount() + addedCount);
            collectionRepository.save(collection);
        }

        return CollectionContentBatchResponseDto.of(
                successContents,
                failedContents,
                request.getContents().size()
        );
    }

    @Override
    public Page<CollectionContentResponseDto> getCollectionContents(Long collectionId, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<CollectionContent> contents = collectionContentRepository.findByCollectionCollectionId(collectionId, pageable);
        return contents.map(CollectionContentResponseDto::from);
    }

    @Override
    public List<CollectionContentResponseDto> getAllCollectionContents(Long collectionId) {
        collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없거나 권한이 없습니다. id : " + collectionId));

        List<CollectionContent> contents = collectionContentRepository.findByCollectionIdWithContent(collectionId);
        return contents.stream()
                .map(CollectionContentResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeContentFromCollection(Long userId, Long collectionId, Long contentId, String contentType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. id : " + userId));

        Collection collection = collectionRepository.findByUserAndCollectionId(user, collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없거나 권한이 없습니다. id : " + collectionId));

        ContentId contentIdObj = new ContentId(contentId, contentType);
        Content content = contentRepository.findById(contentIdObj)
                .orElseThrow(() -> new ContentNotFoundException("컨텐츠를 찾을 수 없습니다. id : " + contentId + " contnetType : " + contentType));

        // 컬렉션의 컨텐츠 수 감소
        if (collection.getContentCount() > 0) {
            collection.setContentCount(collection.getContentCount() - 1);
            collectionRepository.save(collection);
        }

        collectionContentRepository.deleteByCollectionAndContent(collection, content);
    }

    @Override
    public long getCollectionContentCount(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없거나 권한이 없습니다. id : " + collectionId));
        return collectionContentRepository.countByCollection(collection);
    }

    @Override
    public boolean isContentInCollection(Long collectionId, Long contentId, String contentType) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException("컬렉션을 찾을 수 없거나 권한이 없습니다. id : " + collectionId));

        ContentId contentIdObj = new ContentId(contentId, contentType);
        Content content = contentRepository.findById(contentIdObj)
                .orElseThrow(() -> new ContentNotFoundException("컨텐츠를 찾을 수 없습니다. id : " + contentId + " contnetType : " + contentType));

        return collectionContentRepository.existsByCollectionAndContent(collection, content);
    }
}
