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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentFilterService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final WebClient webClient;

    public ContentFilterService(ElasticsearchOperations elasticsearchOperations, WebClient webClient) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.webClient = webClient;
    }

    public PaginatedContentFilterResponseDto filterContents(
            String contentType, List<String> genres, BigDecimal minRating, BigDecimal maxRating,
            String platform, Integer minReleaseYear, Integer maxReleaseYear,
            String filterType,
            Map<String, Object> filterParams,
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

        if (minRating != null && maxRating != null) {
            criteria = criteria.and("ratingAverage").between(minRating, maxRating);
        } else if (minRating != null) {
            criteria = criteria.and("ratingAverage").greaterThanEqual(minRating);
        } else if (maxRating != null) {
            criteria = criteria.and("ratingAverage").lessThanEqual(maxRating);
        }

        if (platform != null && !platform.isEmpty()) {
            criteria = criteria.and("platforms").is(platform);
        }

        if (minReleaseYear != null || maxReleaseYear != null) {
            LocalDate startDate = (minReleaseYear != null) ? LocalDate.of(minReleaseYear, 1, 1) : null;
            LocalDate endDate = (maxReleaseYear != null) ? LocalDate.of(maxReleaseYear, 12, 31) : null;

            if (startDate != null && endDate != null) {
                criteria = criteria.and("releaseDate").between(startDate, endDate);
            } else if (startDate != null) {
                criteria = criteria.and("releaseDate").greaterThanEqual(startDate);
            } else if (endDate != null) {
                criteria = criteria.and("releaseDate").lessThanEqual(endDate);
            }
        }

        CriteriaQuery query = new CriteriaQuery(criteria, pageable);

        List<Long> dynamicContentIds = null;
        if (filterType != null && !filterType.isEmpty()) {
            switch (filterType) {
                case "age_group_popular":
                    Integer minAgeParam = (Integer) filterParams.get("minAge");
                    Integer maxAgeParam = (Integer) filterParams.get("maxAge");

                    if (minAgeParam != null && maxAgeParam != null) {
                        try {
                            int fastapiLimit = (filterParams.containsKey("limit") && filterParams.get("limit") instanceof Integer) ?
                                    (Integer) filterParams.get("limit") : 50;
                            dynamicContentIds = callFastApiForAgeGroupPopular(minAgeParam, maxAgeParam, fastapiLimit).block();
                        } catch (Exception e) {
                            System.err.println("Error calling FastAPI for ageGroup popular with min/max age: " + e.getMessage());
                            dynamicContentIds = Collections.emptyList();
                        }
                    } else {
                        System.err.println("Missing 'minAge' or 'maxAge' for age_group_popular filterType.");
                        dynamicContentIds = Collections.emptyList();
                    }
                    break;
                case "persona_popular":
                    Object personaIdObj = filterParams.get("personaId");
                    if (personaIdObj != null) {
                        try {
                            int personaId;
                            if (personaIdObj instanceof Integer) {
                                personaId = (Integer) personaIdObj;
                            } else if (personaIdObj instanceof String) {
                                personaId = Integer.parseInt((String) personaIdObj);
                            } else {
                                throw new IllegalArgumentException("Unsupported type for personaId: " + personaIdObj.getClass());
                            }

                            int fastapiLimit = (filterParams.containsKey("limit") && filterParams.get("limit") instanceof Integer) ?
                                    (Integer) filterParams.get("limit") : 50;

                            dynamicContentIds = callFastApiForPersonaPopular(personaId, fastapiLimit).block();
                        } catch (Exception e) {
                            System.err.println("Error calling FastAPI for persona popular: " + e.getMessage());
                            dynamicContentIds = Collections.emptyList();
                        }
                    }
                    break;

                case "popcorithm_recommendation":
                    Integer popcorithmUserId = (Integer) filterParams.get("userId");
                    Integer popcorithmLimit = (Integer) filterParams.get("limit");

                    if (popcorithmUserId != null && popcorithmLimit != null) {
                        try {
                            dynamicContentIds = callFastApiForPopcorithmRecommendations(popcorithmUserId, popcorithmLimit).block();
                        } catch (Exception e) {
                            System.err.println("Error calling FastAPI for popcorithm_recommendation: " + e.getMessage());
                            dynamicContentIds = Collections.emptyList();
                        }
                    } else {
                        System.err.println("Missing userId or limit for popcorithm_recommendation.");
                        dynamicContentIds = Collections.emptyList();
                    }
                    break;
                default:
                    System.err.println("Unsupported filterType: " + filterType);
                    dynamicContentIds = Collections.emptyList();
                    break;
            }
        }

        if (dynamicContentIds != null && !dynamicContentIds.isEmpty()) {
            query.addCriteria(Criteria.where("contentId").in(dynamicContentIds));
        } else if (filterType != null && !filterType.isEmpty() && !dynamicContentIds.isEmpty()) {
            return new PaginatedContentFilterResponseDto(Collections.emptyList(), 0, page, size);
        } else if (filterType != null && !filterType.isEmpty() && dynamicContentIds.isEmpty()) {
            return new PaginatedContentFilterResponseDto(Collections.emptyList(), 0, page, size);
        }


        SearchHits<ContentFilterDocument> searchHits = elasticsearchOperations.search(query, ContentFilterDocument.class);
        List<ContentFilterDocument> contents = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        long totalElements = searchHits.getTotalHits();

        return new PaginatedContentFilterResponseDto(contents, totalElements, page, size);
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
                .map(response -> response.getRecommendations().stream()
                        .map(rec -> Long.parseLong(rec.getContentId()))
                        .collect(Collectors.toList()));
    }

    private Mono<List<Long>> callFastApiForPersonaPopular(int personaId, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/recommends/filters/personas/popular-by-persona/{persona_id}")
                        .queryParam("limit", limit)
                        .build(personaId))
                .retrieve()
                .bodyToMono(PopularContentListApiResponseDto.class)
                .map(response -> response.getRecommendations().stream()
                        .map(rec -> Long.parseLong(rec.getContentId()))
                        .collect(Collectors.toList()));
    }

    private Mono<List<Long>> callFastApiForPopcorithmRecommendations(int userId, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/recommends/popcorithms/users/{userId}/limits/{limit}")
                        .build(userId, limit))
                .retrieve()
                .bodyToMono(PopcorithmRecommendationApiResponseDto.class)
                .map(response -> response.getRecommendations().stream()
                        .map(rec -> Long.parseLong(rec.getContentId()))
                        .collect(Collectors.toList()));
    }
}