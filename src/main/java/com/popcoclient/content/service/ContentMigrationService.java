package com.popcoclient.content.service;

import com.popcoclient.content.document.ContentFilterDocument;
import com.popcoclient.content.repository.filter.ContentFilterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentMigrationService {

    private final ContentFilterRepository contentFilterRepository;
    private final JdbcTemplate jdbcTemplate;

    private Map<Integer, String> allGenresMap;
    private Map<Integer, String> allProvidersMap;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @PostConstruct
    public void init() {
        log.info("DB에서 모든 장르 로드 중...");
        allGenresMap = jdbcTemplate.query(
                "SELECT id, name FROM genres",
                (rs, rowNum) -> Map.entry(rs.getInt("id"), rs.getString("name"))
        ).stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
        ));
        log.info("{}개의 장르 로드 완료.", allGenresMap.size());

        log.info("DB에서 모든 제공자 로드 중...");
        allProvidersMap = jdbcTemplate.query(
                "SELECT id, name FROM providers",
                (rs, rowNum) -> Map.entry(rs.getInt("id"), rs.getString("name"))
        ).stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
        ));
        log.info("{}개의 제공자 로드 완료.", allProvidersMap.size());
    }

    @PostConstruct
    @Transactional(readOnly = true)
    public void migrateContentDataToFilterIndex() {
        log.info("RDB에서 contents_filter 인덱스로 콘텐츠 데이터 마이그레이션 시작...");

        Map<String, List<Integer>> contentGenresMap = new java.util.HashMap<>();
        jdbcTemplate.query(
                "SELECT content_id, content_type, genre_id FROM content_genres",
                (rs) -> {
                    String key = rs.getLong("content_id") + "_" + rs.getString("content_type");
                    contentGenresMap.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(rs.getInt("genre_id"));
                }
        );
        log.info("콘텐츠-장르 매핑 로드 완료. 총 {}개의 매핑 항목.", contentGenresMap.size());

        Map<String, List<Integer>> contentPlatformsMap = new java.util.HashMap<>();
        jdbcTemplate.query(
                "SELECT content_id, type AS content_type, provider_id FROM watch_providers",
                (rs) -> {
                    String key = rs.getLong("content_id") + "_" + rs.getString("content_type");
                    contentPlatformsMap.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(rs.getInt("provider_id"));
                }
        );
        log.info("콘텐츠-플랫폼 매핑 로드 완료. 총 {}개의 매핑 항목.", contentPlatformsMap.size());

        String contentSql = "SELECT id, type, title, rating_average, release_date, poster_path FROM contents";
        int batchSize = 1000;
        List<ContentFilterDocument> filterDocuments = new ArrayList<>(batchSize);

        jdbcTemplate.query(contentSql, rs -> {
            Long contentId = ((Number) rs.getObject("id")).longValue();
            String contentType = (String) rs.getObject("type");
            String compositeKeyString = contentId + "_" + contentType;

            List<String> genres = contentGenresMap.getOrDefault(compositeKeyString, Collections.emptyList())
                    .stream()
                    .map(genreId -> allGenresMap.getOrDefault(genreId, null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            List<String> platforms = contentPlatformsMap.getOrDefault(compositeKeyString, Collections.emptyList())
                    .stream()
                    .map(providerId -> allProvidersMap.getOrDefault(providerId, null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            BigDecimal ratingAverage = rs.getObject("rating_average") instanceof BigDecimal ?
                    (BigDecimal) rs.getObject("rating_average") :
                    null;

            ContentFilterDocument doc = ContentFilterDocument.builder()
                    .id(compositeKeyString)
                    .contentId(contentId)
                    .title((String) rs.getObject("title"))
                    .genres(genres)
                    .platforms(platforms)
                    .contentType(contentType)
                    .releaseDate(rs.getObject("release_date") != null ? LocalDate.parse(rs.getObject("release_date").toString(), DATE_FORMATTER) : null)
                    .ratingAverage(ratingAverage)
                    .posterPath((String) rs.getObject("poster_path"))
                    .popularityScore(null)
                    .build();

            filterDocuments.add(doc);

            if (filterDocuments.size() >= batchSize) {
                try {
                    contentFilterRepository.saveAll(filterDocuments);
                    log.info("contents_filter 인덱스로 {}개의 문서 마이그레이션 완료.", filterDocuments.size());
                } catch (Exception e) {
                    log.error("배치 저장 중 오류 발생: {}", e.getMessage());
                }
                filterDocuments.clear();
            }
        });

        if (!filterDocuments.isEmpty()) {
            try {
                contentFilterRepository.saveAll(filterDocuments);
                log.info("contents_filter 인덱스로 {}개의 문서 마이그레이션 완료.", filterDocuments.size());
            } catch (Exception e) {
                log.error("배치 저장 중 오류 발생: {}", e.getMessage());
            }
        }

        log.info("RDB에서 contents_filter 인덱스로 콘텐츠 데이터 마이그레이션이 성공적으로 완료되었습니다.");
    }
}