package com.popcoclient.content.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.io.StringReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeIndex() {
        try {
            String indexName = "contents";

            // 인덱스 존재 여부 확인
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();

            if (!exists) {
                createIndexWithSettings(indexName);
                log.info("Created index '{}' with Korean analyzer", indexName);
            } else {
                log.info("Index '{}' already exists", indexName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize Elasticsearch index", e);
        }
    }

    private void createIndexWithSettings(String indexName) throws Exception {
        // 인덱스 설정 JSON
        String settingsJson = """
        {
          "number_of_shards": 1,
          "number_of_replicas": 0,
          "analysis": {
            "analyzer": {
              "korean": {
                "type": "nori",
                "tokenizer": "nori_tokenizer"
              },
              "ngram_analyzer": {
                "type": "custom",
                "tokenizer": "ngram_tokenizer",
                "filter": ["lowercase"]
              },
              "edge_ngram_analyzer": {
                "type": "custom",
                "tokenizer": "edge_ngram_tokenizer",
                "filter": ["lowercase"]
              }
            },
            "tokenizer": {
              "nori_tokenizer": {
                "type": "nori_tokenizer",
                "decompound_mode": "mixed"
              },
              "ngram_tokenizer": {
                "type": "ngram",
                "min_gram": 2,
                "max_gram": 10,
                "token_chars": ["letter", "digit"]
              },
              "edge_ngram_tokenizer": {
                "type": "edge_ngram",
                "min_gram": 1,
                "max_gram": 20,
                "token_chars": ["letter", "digit"]
              }
            }
          }
        }
        """;

        // 매핑 JSON
        String mappingsJson = """
        {
          "properties": {
            "title": {
              "type": "text",
              "analyzer": "korean",
              "fields": {
                "keyword": {
                  "type": "keyword"
                },
                "ngram": {
                  "type": "text",
                  "analyzer": "ngram_analyzer"
                },
                "edge_ngram": {
                  "type": "text",
                  "analyzer": "edge_ngram_analyzer",
                  "search_analyzer": "standard"
                }
              }
            },
            "overview": {
              "type": "text",
              "analyzer": "korean"
            },
            "cast": {
              "type": "nested",
              "properties": {
                "actorName": {
                  "type": "text",
                  "analyzer": "korean"
                },
                "characterName": {
                  "type": "text",
                  "analyzer": "korean"
                }
              }
            },
            "crew": {
              "type": "nested",
              "properties": {
                "name": {
                  "type": "text",
                  "analyzer": "korean"
                },
                "job": {
                  "type": "keyword"
                }
              }
            }
          }
        }
        """;

        // 인덱스 생성
        CreateIndexRequest request = CreateIndexRequest.of(c -> c
                .index(indexName)
                .settings(s -> s.withJson(new StringReader(settingsJson)))
                .mappings(m -> m.withJson(new StringReader(mappingsJson)))
        );

        elasticsearchClient.indices().create(request);
    }
}