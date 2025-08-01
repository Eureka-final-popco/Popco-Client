package com.popcoclient.content.repository.filter;

import com.popcoclient.content.document.ContentFilterDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentFilterRepository extends ElasticsearchRepository<ContentFilterDocument, String> {
}