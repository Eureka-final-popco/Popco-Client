package com.popcoclient.content.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.popcoclient.content.document.ContentDocument;
import com.popcoclient.content.dto.response.AutocompleteResponse;
import com.popcoclient.content.dto.response.ContentSearchResponse;
import com.popcoclient.content.entity.ContentReaction;
import com.popcoclient.content.entity.enums.ReactionType;
import com.popcoclient.content.repository.ContentReactionRepository;
import com.popcoclient.content.repository.search.ContentSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentSearchService {

    private final ContentSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ContentReactionRepository contentReactionRepository;

    // 기본 검색
    public Page<ContentDocument> searchContents(String keyword, Pageable pageable) {
        log.info("Searching contents with keyword: {}", keyword);

        Query query = Query.of(q -> q
                .multiMatch(m -> m
                        .query(keyword)
                        .fields("title^3", "title.ngram^2", "overview")
                        .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                )
        );

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(pageable)
                .build();

        SearchHits<ContentDocument> hits = elasticsearchOperations.search(searchQuery, ContentDocument.class);

        List<ContentDocument> content = hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // PageImpl 사용하여 Page 객체 생성
        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }

    // 자동완성 - 제목과 배우 이름 모두 검색
    public List<AutocompleteResponse> autocomplete(String prefix) {
        log.info("Autocomplete for prefix: {}", prefix);

        if (prefix == null || prefix.trim().isEmpty()) {
            return List.of();
        }

        List<AutocompleteResponse> results = new ArrayList<>();

        // 1. 제목 검색
        Query titleQuery = Query.of(q -> q
                .matchPhrasePrefix(m -> m
                        .field("title")
                        .query(prefix)
                )
        );

        NativeQuery titleSearchQuery = NativeQuery.builder()
                .withQuery(titleQuery)
                .withMaxResults(10)
                .build();

        SearchHits<ContentDocument> titleHits =
                elasticsearchOperations.search(titleSearchQuery, ContentDocument.class);

        // 제목 결과 추가
        titleHits.stream()
                .limit(5) // 제목은 최대 5개
                .forEach(hit -> {
                    ContentDocument content = hit.getContent();
                    results.add(AutocompleteResponse.builder()
                            .value(content.getTitle())
                            .type("content")
                            .contentId(content.getContentId())
                            .contentType(content.getContentType())
                            .build());
                });

        // 2. 배우 이름 검색 - 쿼리 개선
        Query actorQuery = Query.of(q -> q
                .nested(n -> n
                        .path("cast")
                        .query(nq -> nq
                                .bool(b -> b
                                        .should(s -> s
                                                .matchPhrasePrefix(m -> m
                                                        .field("cast.actorName")
                                                        .query(prefix)
                                                )
                                        )
                                        .should(s -> s
                                                .match(m -> m
                                                        .field("cast.actorName")
                                                        .query(prefix)
                                                )
                                        )
                                )
                        )
                )
        );

        NativeQuery actorSearchQuery = NativeQuery.builder()
                .withQuery(actorQuery)
                .withMaxResults(100) // 더 많이 가져오기
                .build();

        SearchHits<ContentDocument> actorHits =
                elasticsearchOperations.search(actorSearchQuery, ContentDocument.class);

        // 배우 이름 중복 제거 및 결과 추가
        Set<String> addedActors = new HashSet<>();
        Map<String, Integer> actorCount = new HashMap<>(); // 배우가 나타난 횟수 카운트

        actorHits.stream()
                .flatMap(hit -> hit.getContent().getCast().stream())
                .forEach(cast -> {
                    String actorName = cast.getActorName();
                    // 대소문자 구분 없이 prefix로 시작하는 배우 이름만 필터링
                    if (actorName.toLowerCase().contains(prefix.toLowerCase())) {
                        actorCount.merge(actorName, 1, Integer::sum);
                    }
                });

        // 등장 횟수가 많은 순으로 정렬하여 상위 5개만 추가
        actorCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    results.add(AutocompleteResponse.builder()
                            .value(entry.getKey())
                            .type("actors")
                            .build());
                });

        log.info("Found {} suggestions for prefix: {} (titles: {}, actors: {})",
                results.size(), prefix,
                results.stream().filter(r -> "content".equals(r.getType())).count(),
                results.stream().filter(r -> "actor".equals(r.getType())).count());

        return results;
    }

    // 고급 검색
    public Page<ContentSearchResponse> advancedSearch(String keyword, String contentType,
                                                      List<String> actors, Long userId,
                                                      Pageable pageable) {
        log.info("Advanced search - keyword: {}, type: {}, actors: {}, userId: {}, page: {}",
                keyword, contentType, actors, userId, pageable.getPageNumber());

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // 키워드 검색 개선
        if (keyword != null && !keyword.trim().isEmpty()) {
            String keywordNoSpace = keyword.replaceAll("\\s+", "");

            boolBuilder.must(m -> m
                    .bool(b -> {
                        // 원본 키워드 검색
                        b.should(s -> s
                                .multiMatch(mm -> mm
                                        .query(keyword)
                                        .fields("title^5", "title.keyword^4", "overview",
                                                "cast.actorName", "cast.characterName",
                                                "crew.name")
                                        .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                                )
                        );

                        // 공백 제거 버전
                        if (!keyword.equals(keywordNoSpace)) {
                            b.should(s -> s
                                    .wildcard(w -> w
                                            .field("title.keyword")
                                            .value("*" + keywordNoSpace + "*")
                                            .caseInsensitive(true)
                                    )
                            );

                            b.should(s -> s
                                    .match(match -> match
                                            .field("title.ngram")
                                            .query(keywordNoSpace)
                                    )
                            );
                        }

                        // Fuzzy 검색
                        b.should(s -> s
                                .fuzzy(f -> f
                                        .field("title")
                                        .value(keyword)
                                        .fuzziness("AUTO")
                                )
                        );

                        // 각 단어로 분리해서 검색
                        String[] words = keyword.split("\\s+");
                        if (words.length > 1) {
                            BoolQuery.Builder mustBuilder = new BoolQuery.Builder();
                            for (String word : words) {
                                mustBuilder.must(m2 -> m2
                                        .match(match -> match
                                                .field("title")
                                                .query(word)
                                        )
                                );
                            }
                            b.should(s -> s.bool(mustBuilder.build()));
                        }

                        b.minimumShouldMatch("1");
                        return b;
                    })
            );
        }

        // 콘텐츠 타입 필터
        if (contentType != null && !contentType.isEmpty()) {
            boolBuilder.filter(f -> f
                    .term(t -> t
                            .field("contentType")
                            .value(contentType)
                    )
            );
        }

        // 배우 검색 - 모든 배우가 포함된 콘텐츠만 검색
        if (actors != null && !actors.isEmpty()) {
            for (String actor : actors) {
                // 배우 이름도 공백 처리
                String actorNoSpace = actor.replaceAll("\\s+", "");

                boolBuilder.must(m -> m
                        .nested(n -> n
                                .path("cast")
                                .query(q -> q
                                        .bool(b -> {
                                            // 원본 배우 이름
                                            b.should(s -> s
                                                    .match(match -> match
                                                            .field("cast.actorName")
                                                            .query(actor)
                                                    )
                                            );

                                            // 공백 제거한 배우 이름
                                            if (!actor.equals(actorNoSpace)) {
                                                b.should(s -> s
                                                        .wildcard(w -> w
                                                                .field("cast.actorName.keyword")
                                                                .value("*" + actorNoSpace + "*")
                                                                .caseInsensitive(true)
                                                        )
                                                );
                                            }

                                            // Fuzzy 매칭
                                            b.should(s -> s
                                                    .fuzzy(f -> f
                                                            .field("cast.actorName")
                                                            .value(actor)
                                                            .fuzziness("AUTO")
                                                    )
                                            );

                                            b.minimumShouldMatch("1");
                                            return b;
                                        })
                                )
                        )
                );
            }
        }

        Query query = Query.of(q -> q.bool(boolBuilder.build()));

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(pageable)
                .build();

        SearchHits<ContentDocument> searchHits =
                elasticsearchOperations.search(searchQuery, ContentDocument.class);

        List<ContentDocument> documents = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // ContentDocument를 ContentSearchResponse로 변환
        List<ContentSearchResponse> responses = documents.stream()
                .map(ContentSearchResponse::from)
                .collect(Collectors.toList());

        // 로그인한 사용자인 경우 좋아요 정보 추가
        if (userId != null) {
            addLikeInfo(responses, userId);
        }

        log.info("Advanced search found {} results", searchHits.getTotalHits());

        return new PageImpl<>(responses, pageable, searchHits.getTotalHits());
    }

    private void addLikeInfo(List<ContentSearchResponse> responses, Long userId) {
        if (responses.isEmpty()) {
            return;
        }

        // 각 컨텐츠에 대해 개별적으로 좋아요 정보 조회
        responses.forEach(response -> {
            // contentType과 contentId 분리
            String[] idParts = response.getId().split("_");
            Long contentId = Long.parseLong(idParts[0]);
            String contentType = idParts[1];

            // 해당 컨텐츠에 대한 사용자의 좋아요 여부 확인
            Optional<ContentReaction> reaction = contentReactionRepository
                    .findByUserAndContentAndReaction(userId, contentId, contentType, ReactionType.LIKE);

            // 좋아요 여부 설정
            response.setIsLiked(reaction.isPresent());
        });
    }
}