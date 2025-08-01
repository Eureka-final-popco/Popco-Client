package com.popcoclient.content.service;

import com.popcoclient.content.document.ContentDocument;
import com.popcoclient.content.entity.Content;
import com.popcoclient.content.repository.ContentRepository;
import com.popcoclient.content.repository.search.ContentSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentIndexingService {

    private final ContentRepository contentRepository;
    private final ContentSearchRepository searchRepository;

    // 애플리케이션 시작 시 자동 인덱싱
    @PostConstruct
    public void initialIndex() {
        log.info("Checking if initial indexing is needed...");
        long count = searchRepository.count();
        if (count == 0) {
            log.info("No documents found in Elasticsearch. Starting initial indexing...");
            reindexAllContents();
        } else {
            log.info("Found {} documents in Elasticsearch", count);
        }
    }

    @Transactional(readOnly = true)
    public void reindexAllContents() {
        log.info("Starting full reindex of contents");

        try {
            List<Content> contents = contentRepository.findAll();
            log.info("Found {} contents in database", contents.size());

            if (contents.isEmpty()) {
                log.warn("No contents found in database to index");
                return;
            }

            // 기존 인덱스 삭제
            searchRepository.deleteAll();
            log.info("Deleted all existing documents from index");

            // 배치로 저장
            List<ContentDocument> documents = contents.stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());

            searchRepository.saveAll(documents);
            log.info("Successfully indexed {} documents", documents.size());

            // 검증
            long indexedCount = searchRepository.count();
            log.info("Verification: {} documents now in Elasticsearch", indexedCount);

        } catch (Exception e) {
            log.error("Error during reindexing", e);
        }
    }

    private ContentDocument convertToDocument(Content content) {
        try {
            String documentId = content.getContentId().getId() + "_" + content.getContentId().getType();

            List<ContentDocument.CastInfo> castInfos = content.getCasts().stream()
                    .limit(10) // 상위 10명만
                    .map(cast -> ContentDocument.CastInfo.builder()
                            .actorName(cast.getActor().getName())
                            .characterName(cast.getCharacterName())
                            .build())
                    .collect(Collectors.toList());

            List<ContentDocument.CrewInfo> crewInfos = content.getCrews().stream()
                    .filter(crew -> "Director".equals(crew.getJob())) // 감독만
                    .map(crew -> ContentDocument.CrewInfo.builder()
                            .name(crew.getCrewMember().getName())
                            .job(crew.getJob())
                            .build())
                    .collect(Collectors.toList());

            ContentDocument doc = ContentDocument.builder()
                    .id(documentId)
                    .contentId(content.getContentId().getId())
                    .contentType(content.getContentId().getType())
                    .title(content.getTitle())
                    .overview(content.getOverview())
                    .ratingAverage(content.getRatingAverage())
                    .releaseDate(content.getReleaseDate())
                    .posterPath(content.getPosterPath())  // 추가
                    .cast(castInfos)
                    .crew(crewInfos)
                    .build();

            log.debug("Converted content: {} - {}", documentId, content.getTitle());
            return doc;

        } catch (Exception e) {
            log.error("Error converting content: {}", content.getContentId(), e);
            throw e;
        }
    }
}