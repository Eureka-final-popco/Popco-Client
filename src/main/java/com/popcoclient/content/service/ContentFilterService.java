package com.popcoclient.content.service;

import com.popcoclient.content.document.ContentFilterDocument;
import com.popcoclient.content.dto.response.*;
import com.popcoclient.content.entity.enums.ReactionType;
import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.content.repository.ContentReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentFilterService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final WebClient webClient;
    private final ContentReactionRepository contentReactionRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PaginatedContentFilterResponseDto filterContents(
            String contentType, List<String> genres, BigDecimal minRating, BigDecimal maxRating,
            List<String> platforms, Integer minReleaseYear, Integer maxReleaseYear,
            Map<String, Object> ageGroupFilter,
            Map<String, Object> personaFilter,
            Map<String, Object> popcorithmFilter,
            Long userId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        List<Criteria> staticCriteriaList = new ArrayList<>();

        if (contentType != null && !contentType.isEmpty()) {
            staticCriteriaList.add(new Criteria("contentType").is(contentType));
        }

        if (genres != null && !genres.isEmpty()) {
            staticCriteriaList.add(new Criteria("genres").in(genres));
        }

        if (minRating != null || maxRating != null) {
            Criteria ratingCriteria = new Criteria("ratingAverage");

            if (minRating != null) {
                ratingCriteria = ratingCriteria.greaterThanEqual(minRating);
                log.info("평점 필터 적용: ratingAverage >= {}", minRating);
            }
            if (maxRating != null) {
                ratingCriteria = ratingCriteria.lessThanEqual(maxRating);
                log.info("평점 필터 적용: ratingAverage <= {}", maxRating);
            }
            staticCriteriaList.add(ratingCriteria);
        }

        if (platforms != null && !platforms.isEmpty()) {
            staticCriteriaList.add(new Criteria("platforms.keyword").in(platforms));
        }

        if (minReleaseYear != null || maxReleaseYear != null) {
            Criteria dateCriteria = new Criteria("releaseDate");
            if (minReleaseYear != null) {
                String minDate = LocalDate.of(minReleaseYear, 1, 1).format(DATE_FORMATTER);
                dateCriteria = dateCriteria.greaterThanEqual(minDate);
                log.info("개봉연도 필터 적용: releaseDate >= {}", minDate);
            }
            if (maxReleaseYear != null) {
                String maxDate = LocalDate.of(maxReleaseYear, 12, 31).format(DATE_FORMATTER);
                dateCriteria = dateCriteria.lessThanEqual(maxDate);
                log.info("개봉연도 필터 적용: releaseDate <= {}", maxDate);
            }
            staticCriteriaList.add(dateCriteria);
        }

        boolean hasAnyStaticFilter = !staticCriteriaList.isEmpty();
        Criteria currentStaticCriteria = null;
        if (hasAnyStaticFilter) {
            currentStaticCriteria = staticCriteriaList.get(0);
            for (int i = 1; i < staticCriteriaList.size(); i++) {
                currentStaticCriteria = currentStaticCriteria.and(staticCriteriaList.get(i));
            }
        }

        Map<ContentId, Float> combinedDynamicContentScores = new LinkedHashMap<>();
        AtomicBoolean anyDynamicFilterRequested = new AtomicBoolean(false);

        if (ageGroupFilter != null && !ageGroupFilter.isEmpty()) {
            anyDynamicFilterRequested.set(true);
            Object minAgeObj = ageGroupFilter.get("minAge");
            int minAgeParam = (minAgeObj instanceof Number) ? ((Number) minAgeObj).intValue() : 0;
            Object maxAgeObj = ageGroupFilter.get("maxAge");
            int maxAgeParam = (maxAgeObj instanceof Number) ? ((Number) maxAgeObj).intValue() : 100;
            Object requestedLimitObj = ageGroupFilter.get("limit");
            int requestedLimit = (requestedLimitObj instanceof Integer) ? (Integer) requestedLimitObj : 50;
            int fastapiLimit = Math.min(requestedLimit, 50);

            try {
                List<PopularContentResponseDto> fetchedItems = callFastApiForAgeGroupPopular(minAgeParam, maxAgeParam, fastapiLimit).block();
                if (fetchedItems == null || fetchedItems.isEmpty()) {
                    log.info("연령대 필터 요청 결과가 비어있습니다. 다른 동적 필터가 없으므로 빈 응답을 반환합니다.");
                    return new PaginatedContentFilterResponseDto(Collections.emptyList(), 0, page, size);
                }
                combinedDynamicContentScores = fetchedItems.stream()
                        .collect(Collectors.toMap(
                                item -> new ContentId(safeParseLong(item.getContentId()), item.getType()),
                                item -> item.getPopularityScore() != null ? item.getPopularityScore() : 0.0f,
                                (oldValue, newValue) -> oldValue,
                                LinkedHashMap::new
                        ));
                if (combinedDynamicContentScores.isEmpty()) {
                    return new PaginatedContentFilterResponseDto(Collections.emptyList(), 0, page, size);
                }
            } catch (Exception e) {
                log.error("연령대 인기 콘텐츠를 가져오는 중 오류 발생: {}", e.getMessage());
                return new PaginatedContentFilterResponseDto(Collections.emptyList(), 0, page, size);
            }
        }

        if (personaFilter != null && !personaFilter.isEmpty()) {
            anyDynamicFilterRequested.set(true);
            Object personaIdObj = personaFilter.get("personaId");
            int personaId = personaIdObj != null ? ((Number) personaIdObj).intValue() : 0;
            int limit = personaFilter.getOrDefault("limit", 50) instanceof Integer ? (Integer) personaFilter.get("limit") : 50;

            try {
                List<PopularContentResponseDto> fetchedItems = callFastApiForPersonaPopular(personaId, limit).block();

                log.info("=== FastAPI 페르소나 필터 결과 ===");
                log.info("받은 콘텐츠 개수: {}", fetchedItems != null ? fetchedItems.size() : 0);

                if (fetchedItems != null) {
                    fetchedItems.forEach(item ->
                            log.info("FastAPI - ContentId: {}, Type: '{}', Title: {}",
                                    item.getContentId(), item.getType(), item.getTitle()));
                }

                Map<ContentId, Float> newScores = fetchedItems.stream()
                        .collect(Collectors.toMap(
                                item -> new ContentId(safeParseLong(item.getContentId()), item.getType()),
                                item -> item.getPopularityScore() != null ? item.getPopularityScore() : 0.0f,
                                (oldValue, newValue) -> oldValue,
                                LinkedHashMap::new
                        ));

                log.info("=== ContentId 변환 결과 ===");
                log.info("변환된 ContentId 개수: {}", newScores.size());
                newScores.keySet().forEach(contentId ->
                        log.info("변환된 ContentId - ID: {}, Type: '{}'", contentId.getId(), contentId.getType()));

                if (combinedDynamicContentScores.isEmpty()) {
                    combinedDynamicContentScores = newScores;
                    log.info("첫 번째 동적 필터로 설정됨: {} 개", combinedDynamicContentScores.size());
                } else {
                    Set<ContentId> beforeIntersection = new HashSet<>(combinedDynamicContentScores.keySet());
                    Set<ContentId> newKeys = newScores.keySet();

                    log.info("=== 교집합 계산 ===");
                    log.info("기존 동적 필터: {} 개", beforeIntersection.size());
                    log.info("새로운 필터: {} 개", newKeys.size());

                    Set<ContentId> intersection = new HashSet<>(beforeIntersection);
                    intersection.retainAll(newKeys);
                    log.info("교집합 결과: {} 개", intersection.size());

                    intersection.forEach(contentId ->
                            log.info("교집합 ContentId - ID: {}, Type: '{}'", contentId.getId(), contentId.getType()));

                    combinedDynamicContentScores.keySet().retainAll(newScores.keySet());
                }

                log.info("최종 동적 필터 콘텐츠 개수: {}", combinedDynamicContentScores.size());

                if (combinedDynamicContentScores.isEmpty()) {
                    log.info("페르소나 필터 적용 후 교집합이 비어있습니다.");
                    return new PaginatedContentFilterResponseDto(Collections.emptyList(), 0, page, size);
                }
            } catch (Exception e) {
                log.error("페르소나 인기 콘텐츠를 가져오는 중 오류 발생: {}", e.getMessage());
                return new PaginatedContentFilterResponseDto(Collections.emptyList(), 0, page, size);
            }
        }

        if (popcorithmFilter != null && !popcorithmFilter.isEmpty()) {
            anyDynamicFilterRequested.set(true);
            Integer userIdFromFilter = popcorithmFilter.get("userId") != null ? ((Number) popcorithmFilter.get("userId")).intValue() : null;
            Integer limitFromFilter = popcorithmFilter.get("limit") != null ? ((Number) popcorithmFilter.get("limit")).intValue() : 50;

            try {
                List<PopcorithmMovieRecommendationDto> fetchedRecommendations = callFastApiForPopcorithmRecommendations(userIdFromFilter, limitFromFilter).block();
                if (fetchedRecommendations == null || fetchedRecommendations.isEmpty()) {
                    log.info("팝코리즘 필터가 요청되었으나 가져온 항목이 없습니다.");
                    return new PaginatedContentFilterResponseDto(Collections.emptyList(), 0, page, size);
                }

                Map<ContentId, Float> newScores = fetchedRecommendations.stream()
                        .collect(Collectors.toMap(
                                rec -> new ContentId(Long.valueOf(rec.getContentId()), rec.getType()),
                                rec -> rec.getScore() != null ? rec.getScore() : 0.0f,
                                (oldValue, newValue) -> oldValue,
                                LinkedHashMap::new
                        ));

                if (combinedDynamicContentScores.isEmpty()) {
                    combinedDynamicContentScores = newScores;
                } else {
                    combinedDynamicContentScores.keySet().retainAll(newScores.keySet());
                }

                if (combinedDynamicContentScores.isEmpty()) {
                    log.info("팝코리즘 필터 적용 후 교집합이 비어있습니다.");
                    return new PaginatedContentFilterResponseDto(Collections.emptyList(), 0, page, size);
                }
            } catch (Exception e) {
                log.error("팝코리즘 추천을 가져오는 중 오류 발생: {}", e.getMessage());
                return new PaginatedContentFilterResponseDto(Collections.emptyList(), 0, page, size);
            }
        }

        Criteria finalQueryCriteria = null;
        Criteria dynamicFilterCriteria = null;
        Set<ContentId> finalDynamicKeys = combinedDynamicContentScores.keySet();

        if (anyDynamicFilterRequested.get() && !finalDynamicKeys.isEmpty()) {
            log.info("=== Elasticsearch 쿼리 조건 ===");
            log.info("동적 필터로 검색할 ContentId 개수: {}", finalDynamicKeys.size());

//            List<String> compositeKeys = finalDynamicKeys.stream()
//                    .map(contentId -> contentId.getId() + "_" + contentId.getType())
//                    .collect(Collectors.toList());

            List<Long> contentIds = finalDynamicKeys.stream()
                    .map(ContentId::getId)
                    .collect(Collectors.toList());

            dynamicFilterCriteria = new Criteria("contentId").in(contentIds);

            log.info("동적 필터 조건: contentId IN {}", contentIds);

//            Set<ContentId> finalDynamicKeysSet = new HashSet<>(finalDynamicKeys);
        }

        if (dynamicFilterCriteria != null && hasAnyStaticFilter) {
            finalQueryCriteria = dynamicFilterCriteria.and(currentStaticCriteria);
            log.info("동적 필터와 정적 필터를 결합했습니다");
        } else if (dynamicFilterCriteria != null) {
            finalQueryCriteria = dynamicFilterCriteria;
            log.info("동적 필터만 사용합니다");
        } else if (hasAnyStaticFilter) {
            finalQueryCriteria = currentStaticCriteria;
            log.info("정적 필터만 사용합니다");
        } else {
            finalQueryCriteria = new Criteria("contentId").exists();
            log.info("적용된 특정 필터가 없습니다. 'contentId'가 존재하는 모든 콘텐츠를 쿼리합니다.");
        }

        log.info("최종 Elasticsearch 쿼리: {}", finalQueryCriteria.toString());

        CriteriaQuery query = new CriteriaQuery(finalQueryCriteria);
        query.setPageable(pageable);

        if(!anyDynamicFilterRequested.get()) {
            query.addSort(Sort.by(Sort.Direction.DESC, "releaseDate"));
            log.info("동적 필터가 없어, 기본 정렬을 최신순(releaseDate DESC)으로 설정했습니다.");
        }

        SearchHits<ContentFilterDocument> searchHits = elasticsearchOperations.search(query, ContentFilterDocument.class);
        List<ContentFilterDocument> contentsFromEs = searchHits.get().map(SearchHit::getContent).collect(Collectors.toList());

        log.info("=== Elasticsearch 검색 결과 ===");
        log.info("ES에서 반환된 콘텐츠 개수: {}", contentsFromEs.size());
        log.info("ES 총 히트 수: {}", searchHits.getTotalHits());

        contentsFromEs.forEach(doc ->
                log.info("ES 콘텐츠 - ID: {}, Type: '{}', Title: {}",
                        doc.getContentId(), doc.getContentType(), doc.getTitle()));

        if (anyDynamicFilterRequested.get()) {
            final Map<ContentId, Float> finalScoresMap = combinedDynamicContentScores;
            final Set<ContentId> finalDynamicKeysSet = new HashSet<>(finalDynamicKeys);

            List<ContentFilterDocument> filteredContents = contentsFromEs.stream()
                    .filter(doc -> {
                        ContentId docContentId = new ContentId(doc.getContentId(), doc.getContentType());
                        if (!finalDynamicKeysSet.contains(docContentId)) {
                            log.debug("동적 필터 조건에 맞지 않아 제외: {}", docContentId);
                            return false;
                        }

                        if (minRating != null) {
                            if (doc.getRatingAverage() == null || doc.getRatingAverage().compareTo(minRating) < 0) {
                                log.debug("평점 조건으로 제외: contentId={}, rating={}, 요구평점={}",
                                        doc.getContentId(), doc.getRatingAverage(), minRating);
                                return false;
                            }
                        }
                        if (maxRating != null) {
                            if (doc.getRatingAverage() != null && doc.getRatingAverage().compareTo(maxRating) > 0) {
                                return false;
                            }
                        }

                        if (minReleaseYear != null || maxReleaseYear != null) {
                            if (doc.getReleaseDate() != null) {
                                try {
                                    LocalDate releaseDate = doc.getReleaseDate();
                                    if (minReleaseYear != null && releaseDate.getYear() < minReleaseYear) {
                                        return false;
                                    }
                                    if (maxReleaseYear != null && releaseDate.getYear() > maxReleaseYear) {
                                        log.debug("개봉연도 조건으로 제외: contentId={}, year={}, 최대년도={}",
                                                doc.getContentId(), releaseDate.getYear(), maxReleaseYear);
                                        return false;
                                    }
                                } catch (Exception e) {
                                    log.warn("날짜 파싱 실패: contentId={}, releaseDate={}", doc.getContentId(), doc.getReleaseDate());
                                    return false;
                                }
                            }
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

            log.info("필터링 결과: ES에서 {}개 → 최종 {}개", contentsFromEs.size(), filteredContents.size());

            List<ContentFilterDocument> sortedContents = filteredContents.stream()
                    .sorted(Comparator.comparing(
                            doc -> finalScoresMap.getOrDefault(
                                    new ContentId(doc.getContentId(), doc.getContentType()), -1000000f),
                            Comparator.reverseOrder()))
                    .collect(Collectors.toList());

            List<FilteredContentItemResponseDto> finalContents = checkUserEngagement(sortedContents, userId);
            return new PaginatedContentFilterResponseDto(finalContents, sortedContents.size(), page, size);
        } else {
            List<FilteredContentItemResponseDto> finalContents = checkUserEngagement(contentsFromEs, userId);
            return new PaginatedContentFilterResponseDto(finalContents, searchHits.getTotalHits(), page, size);
        }
    }

    private List<FilteredContentItemResponseDto> checkUserEngagement(List<ContentFilterDocument> contents, Long userId) {
        if (userId == null) {
            return contents.stream()
                    .map(doc -> new FilteredContentItemResponseDto(doc, false, false))
                    .collect(Collectors.toList());
        }

        Set<ContentId> likedContentIds = contentReactionRepository.findContentIdsByUserAndReaction(userId, ReactionType.LIKE);
        Set<ContentId> dislikedContentIds = contentReactionRepository.findContentIdsByUserAndReaction(userId, ReactionType.DISLIKE);

        return contents.stream().map(doc -> {
            ContentId currentContentId = new ContentId(doc.getContentId(), doc.getContentType());
            boolean isLiked = likedContentIds.contains(currentContentId);
            boolean isDisliked = dislikedContentIds.contains(currentContentId);
            return new FilteredContentItemResponseDto(doc, isLiked, isDisliked);
        }).collect(Collectors.toList());
    }

    private Mono<List<PopularContentResponseDto>> callFastApiForAgeGroupPopular(int minAge, int maxAge, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/recommends/filters/popular-by-age-group")
                        .queryParam("age_group_min", (Object) minAge)
                        .queryParam("age_group_max", (Object) maxAge)
                        .queryParam("limit", (Object) limit)
                        .build())
                .retrieve()
                .bodyToMono(PopularContentListApiResponseDto.class)
                .map(response -> {
                    if (response == null || response.getRecommendations() == null) {
                        return Collections.<PopularContentResponseDto>emptyList();
                    }
                    return response.getRecommendations();
                })
                .onErrorResume(e -> {
                    log.error("연령대별 인기 콘텐츠 FastAPI 호출 오류: {}", e.getMessage());
                    return Mono.just(Collections.<PopularContentResponseDto>emptyList());
                });
    }

    private Mono<List<PopularContentResponseDto>> callFastApiForPersonaPopular(int personaId, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/recommends/filters/personas/popular-by-persona/{persona_id}")
                        .queryParam("limit", (Object) limit)
                        .build(personaId))
                .retrieve()
                .bodyToMono(PopularContentListApiResponseDto.class)
                .map(response -> {
                    if (response == null || response.getRecommendations() == null) {
                        return Collections.<PopularContentResponseDto>emptyList();
                    }
                    log.info("FastAPI에서 받은 페르소나 콘텐츠 개수: {}", response.getRecommendations().size());
                    response.getRecommendations().forEach(item ->
                            log.info("FastAPI 콘텐츠: ID={}, Type={}, Title={}",
                                    item.getContentId(), item.getType(), item.getTitle()));
                    return response.getRecommendations();
                })
                .onErrorResume(e -> {
                    log.error("페르소나별 인기 콘텐츠 FastAPI 호출 오류: {}", e.getMessage());
                    return Mono.just(Collections.<PopularContentResponseDto>emptyList());
                });
    }

    private Mono<List<PopcorithmMovieRecommendationDto>> callFastApiForPopcorithmRecommendations(Integer userId, Integer limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/recommends/popcorithms/users/{userId}/limits/{limit}")
                        .queryParam("user_id", (Object) userId)
                        .build(userId, limit))
                .retrieve()
                .bodyToMono(PopcorithmRecommendationApiResponseDto.class)
                .map(response -> {
                    if (response == null || response.getRecommendations() == null) {
                        return Collections.<PopcorithmMovieRecommendationDto>emptyList();
                    }
                    return response.getRecommendations();
                })
                .onErrorResume(e -> {
                    log.error("팝코리즘 추천 FastAPI 호출 오류: {}", e.getMessage());
                    return Mono.just(Collections.<PopcorithmMovieRecommendationDto>emptyList());
                });
    }

    private Long safeParseLong(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("문자열 {}에서 Long 타입 파싱 실패: {}", value, e.getMessage());
            return null;
        }
    }
}