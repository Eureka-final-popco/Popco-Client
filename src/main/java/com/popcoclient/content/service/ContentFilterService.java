package com.popcoclient.content.service;

import com.popcoclient.content.document.ContentFilterDocument;
import com.popcoclient.content.dto.response.PaginatedContentFilterResponseDto;
import com.popcoclient.content.dto.response.PopularContentListApiResponseDto;
import com.popcoclient.content.dto.response.PopcorithmRecommendationApiResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public class ContentFilterService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final WebClient webClient;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ContentFilterService(ElasticsearchOperations elasticsearchOperations, WebClient webClient) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.webClient = webClient;
    }

    public PaginatedContentFilterResponseDto filterContents(
            String contentType, List<String> genres, BigDecimal minRating, BigDecimal maxRating,
            List<String> platforms, Integer minReleaseYear, Integer maxReleaseYear,
            Map<String, Object> ageGroupFilter,
            Map<String, Object> personaFilter,
            Map<String, Object> popcorithmFilter,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Criteria criteria = new Criteria();

        if (contentType != null && !contentType.isEmpty()) {
            criteria = criteria.and("contentType").is(contentType);
        }

        if (genres != null && !genres.isEmpty()) {
            criteria = criteria.and("genres").in(genres);
        }

        if (minRating != null || maxRating != null) {
            Criteria ratingCriteria = new Criteria("ratingAverage");
            if (minRating != null) ratingCriteria = ratingCriteria.greaterThanEqual(minRating);
            if (maxRating != null) ratingCriteria = ratingCriteria.lessThanEqual(maxRating);
            criteria = criteria.and(ratingCriteria);
        }

        if (platforms != null && !platforms.isEmpty()) {
            criteria = criteria.and("platforms").in(platforms);
        }

        if (minReleaseYear != null || maxReleaseYear != null) {
            Criteria dateCriteria = new Criteria("releaseDate");
            if (minReleaseYear != null) {
                dateCriteria = dateCriteria.greaterThanEqual(LocalDate.of(minReleaseYear, 1, 1).format(DATE_FORMATTER));
            }
            if (maxReleaseYear != null) {
                dateCriteria = dateCriteria.lessThanEqual(LocalDate.of(maxReleaseYear, 12, 31).format(DATE_FORMATTER));
            }
            criteria = criteria.and(dateCriteria);
        }

        final Set<Long> combinedDynamicContentIds = new HashSet<>();
        AtomicBoolean anyDynamicFilterRequested = new AtomicBoolean(false);

        if (ageGroupFilter != null && !ageGroupFilter.isEmpty()) {
            anyDynamicFilterRequested.set(true);
            int minAgeParam = ageGroupFilter.get("minAge") != null ? ((Number) ageGroupFilter.get("minAge")).intValue() : 0;
            int maxAgeParam = ageGroupFilter.get("maxAge") != null ? ((Number) ageGroupFilter.get("maxAge")).intValue() : 100;
            int requestedLimit = ageGroupFilter.getOrDefault("limit", 50) instanceof Integer ? (Integer) ageGroupFilter.get("limit") : 50;
            int fastapiLimit = Math.min(requestedLimit, 50);

            try {
                List<Long> fetchedIds = callFastApiForAgeGroupPopular(minAgeParam, maxAgeParam, fastapiLimit).block();
                if (fetchedIds != null) {
                    combinedDynamicContentIds.addAll(fetchedIds);
                }
            } catch (Exception e) {
                combinedDynamicContentIds.clear();
            }
        }

        if (personaFilter != null && !personaFilter.isEmpty()) {
            anyDynamicFilterRequested.set(true);
            Object personaIdObj = personaFilter.get("personaId");
            int personaId = personaIdObj != null ? ((Number) personaIdObj).intValue() : 0;
            int limit = personaFilter.getOrDefault("limit", 50) instanceof Integer ? (Integer) personaFilter.get("limit") : 50;
            try {
                List<Long> fetchedIds = callFastApiForPersonaPopular(personaId, limit).block();
                if (fetchedIds != null) {
                    if (combinedDynamicContentIds.isEmpty()) combinedDynamicContentIds.addAll(fetchedIds);
                    else combinedDynamicContentIds.retainAll(fetchedIds);
                }
            } catch (Exception e) {
                combinedDynamicContentIds.clear();
            }
        }

        if (popcorithmFilter != null && !popcorithmFilter.isEmpty()) {
            anyDynamicFilterRequested.set(true);
            Integer userId = (Integer) popcorithmFilter.get("userId");
            Integer limit = (Integer) popcorithmFilter.get("limit");
            try {
                List<Long> fetchedIds = callFastApiForPopcorithmRecommendations(userId, limit).block();
                if (fetchedIds != null) {
                    if (combinedDynamicContentIds.isEmpty()) combinedDynamicContentIds.addAll(fetchedIds);
                    else combinedDynamicContentIds.retainAll(fetchedIds);
                }
            } catch (Exception e) {
                combinedDynamicContentIds.clear();
            }
        }

        if (anyDynamicFilterRequested.get() && combinedDynamicContentIds.isEmpty()) {
            return new PaginatedContentFilterResponseDto(Collections.emptyList(), 0, page, size);
        }

        if (!combinedDynamicContentIds.isEmpty()) {
            criteria = criteria.and("contentId").in(combinedDynamicContentIds);
        }

        CriteriaQuery query = new CriteriaQuery(criteria, pageable);
        SearchHits<ContentFilterDocument> searchHits = elasticsearchOperations.search(query, ContentFilterDocument.class);
        List<ContentFilterDocument> contents = searchHits.get().map(SearchHit::getContent).collect(Collectors.toList());

        return new PaginatedContentFilterResponseDto(contents, searchHits.getTotalHits(), page, size);
    }

    private Mono<List<Long>> callFastApiForAgeGroupPopular(int minAge, int maxAge, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/recommends/filters/popular-by-age-group")
                        .queryParam("age_group_min", minAge)
                        .queryParam("age_group_max", maxAge)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .bodyToMono(PopularContentListApiResponseDto.class)
                .map(response -> {
                    if (response == null || response.getRecommendations() == null) {
                        return Collections.<Long>emptyList();
                    }
                    return response.getRecommendations().stream()
                            .map(rec -> safeParseLong(rec.getContentId()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                })
                .onErrorResume(e -> Mono.just(Collections.<Long>emptyList()));
    }

    private Mono<List<Long>> callFastApiForPersonaPopular(int personaId, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/recommends/filters/personas/popular-by-persona/{persona_id}")
                        .queryParam("limit", limit)
                        .build(personaId))
                .retrieve()
                .bodyToMono(PopularContentListApiResponseDto.class)
                .map(response -> {
                    if (response == null || response.getRecommendations() == null) {
                        return Collections.<Long>emptyList();
                    }
                    return response.getRecommendations().stream()
                            .map(rec -> safeParseLong(rec.getContentId()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                })
                .onErrorResume(e -> Mono.just(Collections.<Long>emptyList()));
    }

    private Mono<List<Long>> callFastApiForPopcorithmRecommendations(int userId, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/recommends/popcorithms/users/{userId}/limits/{limit}")
                        .queryParam("user_id", userId)
                        .build(userId, limit))
                .retrieve()
                .bodyToMono(PopcorithmRecommendationApiResponseDto.class)
                .map(response -> {
                    if (response == null || response.getRecommendations() == null) {
                        return Collections.<Long>emptyList();
                    }
                    return response.getRecommendations().stream()
                            .map(rec -> safeParseLong(rec.getContentId()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                })
                .onErrorResume(e -> Mono.just(Collections.<Long>emptyList()));
    }

    private Long safeParseLong(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}