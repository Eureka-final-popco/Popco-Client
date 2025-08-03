package com.popcoclient.content.service;

import com.popcoclient.content.document.ContentDocument;
import com.popcoclient.content.document.ContentFilterDocument;
import com.popcoclient.content.repository.filter.ContentFilterRepository;
import com.popcoclient.content.repository.search.ContentSearchRepository; // ElasticContentRepository로 이름 변경됨

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentMigrationService {

    private final ContentSearchRepository contentSearchRepository;
    private final ContentFilterRepository contentFilterRepository;
    private final JdbcTemplate jdbcTemplate;

    private Map<Integer, String> allGenresMap;
    private Map<Integer, String> allProvidersMap;

    @PostConstruct
    public void init() {
        log.info("Loading all genres from DB...");
        allGenresMap = jdbcTemplate.query(
                "SELECT id, name FROM genres",
                (rs, rowNum) -> Map.entry(rs.getInt("id"), rs.getString("name"))
        ).stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
        ));
        log.info("Loaded {} genres.", allGenresMap.size());

        log.info("Loading all providers from DB...");
        allProvidersMap = jdbcTemplate.query(
                "SELECT id, name FROM providers",
                (rs, rowNum) -> Map.entry(rs.getInt("id"), rs.getString("name"))
        ).stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
        ));
        log.info("Loaded {} providers.", allProvidersMap.size());
    }

    @PostConstruct
    @Transactional(readOnly = true)
    public void migrateContentDataToFilterIndex() {
        log.info("Starting content data migration to contents_filter index...");

        if (contentSearchRepository.count() == 0) {
            log.warn("No documents found in 'contents' index. Skipping migration.");
            return;
        }

        int pageNumber = 0;
        int pageSize = 1000;
        Page<ContentDocument> contentPage;

        do {
            contentPage = contentSearchRepository.findAll(PageRequest.of(pageNumber, pageSize));

            List<Map.Entry<Long, String>> compositeKeys = contentPage.getContent().stream()
                    .map(esContent -> Map.entry(esContent.getContentId(), esContent.getContentType()))
                    .collect(Collectors.toList());

            if (compositeKeys.isEmpty() && !contentPage.isEmpty()) {
                break;
            } else if (compositeKeys.isEmpty()) {
                break;
            }

            String contentIdPlaceholders = compositeKeys.stream()
                    .map(key -> "(?, ?)")
                    .collect(Collectors.joining(","));

            Object[] params = new Object[compositeKeys.size() * 2];
            for (int i = 0; i < compositeKeys.size(); i++) {
                params[i * 2] = compositeKeys.get(i).getKey();
                params[i * 2 + 1] = compositeKeys.get(i).getValue();
            }

            Map<String, List<Integer>> contentGenresMap = jdbcTemplate.query(
                    String.format("SELECT cg.content_id, cg.content_type, cg.genre_id FROM content_genres cg " +
                            "WHERE (cg.content_id, cg.content_type) IN (%s)", contentIdPlaceholders),
                    params,
                    (rs) -> {
                        Map<String, List<Integer>> map = new java.util.HashMap<>();
                        while (rs.next()) {
                            String key = rs.getLong("content_id") + "_" + rs.getString("content_type");
                            map.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(rs.getInt("genre_id"));
                        }
                        return map;
                    }
            );

            Map<String, List<Integer>> contentPlatformsMap = jdbcTemplate.query(
                    String.format("SELECT wp.content_id, wp.type AS content_type, wp.provider_id FROM watch_providers wp " +
                            "WHERE (wp.content_id, wp.type) IN (%s)", contentIdPlaceholders),
                    params,
                    (rs) -> {
                        Map<String, List<Integer>> map = new java.util.HashMap<>();
                        while (rs.next()) {
                            String key = rs.getLong("content_id") + "_" + rs.getString("content_type");
                            map.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(rs.getInt("provider_id"));
                        }
                        return map;
                    }
            );

            List<ContentFilterDocument> filterDocuments = contentPage.getContent().stream()
                    .map(esContent -> {
                        String compositeKeyString = esContent.getContentId() + "_" + esContent.getContentType();

                        List<String> genres = contentGenresMap.getOrDefault(compositeKeyString, Collections.emptyList())
                                .stream()
                                .map(genreId -> allGenresMap.getOrDefault(genreId, null))
                                .filter(name -> name != null)
                                .collect(Collectors.toList());

                        List<String> platforms = contentPlatformsMap.getOrDefault(compositeKeyString, Collections.emptyList())
                                .stream()
                                .map(providerId -> allProvidersMap.getOrDefault(providerId, null))
                                .filter(name -> name != null)
                                .collect(Collectors.toList());

                        return ContentFilterDocument.builder()
                                .id(esContent.getId())
                                .contentId(esContent.getContentId())
                                .title(esContent.getTitle())
                                .genres(genres)
                                .platforms(platforms)
                                .contentType(esContent.getContentType())
                                .ratingAverage(esContent.getRatingAverage())
                                .releaseDate(esContent.getReleaseDate())
                                .build();
                    })
                    .collect(Collectors.toList());

            if (!filterDocuments.isEmpty()) {
                contentFilterRepository.saveAll(filterDocuments);
                log.info("Migrated {} documents to contents_filter index. Total migrated so far: {}",
                        filterDocuments.size(), (pageNumber + 1) * pageSize);
            }

            pageNumber++;
        } while (contentPage.hasNext());

        log.info("Content data migration to contents_filter index finished.");
    }
}