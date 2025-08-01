package com.popcoclient.content.repository.search;

import com.popcoclient.content.document.ContentDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ContentSearchRepository extends ElasticsearchRepository<ContentDocument, String> {

    Page<ContentDocument> findByTitleContaining(String title, Pageable pageable);

    Page<ContentDocument> findByContentType(String contentType, Pageable pageable);

    Page<ContentDocument> findByTitleContainingAndContentType(String title, String contentType, Pageable pageable);
}