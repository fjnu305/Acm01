package org.fjnu305.acm01.module.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.search.config.SearchProperties;
import org.fjnu305.acm01.module.search.document.SearchDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search", name = "enabled", havingValue = "true")
public class ElasticsearchSearchSyncService implements SearchSyncService {

    private final ElasticsearchClient elasticsearchClient;
    private final SearchProperties searchProperties;

    @Override
    public void index(SearchDocument document) {
        try {
            elasticsearchClient.index(IndexRequest.of(i -> i
                    .index(searchProperties.getIndexName())
                    .id(document.getId())
                    .document(document)));
        } catch (Exception e) {
            log.warn("Failed to index document {}: {}", document.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(String type, Long refId) {
        try {
            elasticsearchClient.delete(DeleteRequest.of(d -> d
                    .index(searchProperties.getIndexName())
                    .id(type + ":" + refId)));
        } catch (Exception e) {
            log.warn("Failed to delete document {}:{}: {}", type, refId, e.getMessage());
        }
    }
}
