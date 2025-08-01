package com.popcoclient.content.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.popcoclient.content.document.ContentDocument;
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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentSearchService {

    private final ContentSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

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

    // 자동완성
    public List<String> autocomplete(String prefix) {
        log.info("Autocomplete for prefix: {}", prefix);

        if (prefix == null || prefix.trim().isEmpty()) {
            return List.of();
        }

        // match_phrase_prefix 쿼리 사용
        Query query = Query.of(q -> q
                .matchPhrasePrefix(m -> m
                        .field("title")
                        .query(prefix)
                )
        );

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(query)
                .withMaxResults(10)
                .build();

        SearchHits<ContentDocument> searchHits =
                elasticsearchOperations.search(searchQuery, ContentDocument.class);

        List<String> suggestions = searchHits.stream()
                .map(hit -> hit.getContent().getTitle())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

        log.info("Found {} suggestions for prefix: {}", suggestions.size(), prefix);
        return suggestions;
    }

    // 고급 검색
    public List<ContentDocument> advancedSearch(String keyword, String contentType) {
        log.info("Advanced search - keyword: {}, type: {}", keyword, contentType);

        Query query = Query.of(q -> q
                .bool(b -> {
                    // 키워드 검색
                    b.must(m -> m
                            .multiMatch(mm -> mm
                                    .query(keyword)
                                    .fields("title^3", "title.ngram^2", "overview",
                                            "cast.actorName", "cast.characterName",
                                            "crew.name")
                                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                            )
                    );

                    // 콘텐츠 타입 필터
                    if (contentType != null && !contentType.isEmpty()) {
                        b.filter(f -> f
                                .term(t -> t
                                        .field("contentType")
                                        .value(contentType)
                                )
                        );
                    }

                    return b;
                })
        );

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(query)
                .withMaxResults(100)
                .build();

        SearchHits<ContentDocument> searchHits =
                elasticsearchOperations.search(searchQuery, ContentDocument.class);

        List<ContentDocument> results = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        log.info("Advanced search found {} results", results.size());
        return results;
    }
}