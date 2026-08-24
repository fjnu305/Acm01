package org.fjnu305.acm01.module.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
@EnableConfigurationProperties(SearchProperties.class)
@ConditionalOnProperty(prefix = "search", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SearchIndexConfig {

    private final ElasticsearchClient elasticsearchClient;
    private final SearchProperties searchProperties;

    @PostConstruct
    public void ensureIndex() throws IOException {
        String index = searchProperties.getIndexName();
        BooleanResponse exists = elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(index)));
        if (exists.value()) {
            return;
        }
        elasticsearchClient.indices().create(CreateIndexRequest.of(c -> c
                .index(index)
                .mappings(m -> m
                        .properties("type", p -> p.keyword(k -> k))
                        .properties("refId", p -> p.long_(l -> l))
                        .properties("title", p -> p.text(t -> t.analyzer("standard")))
                        .properties("content", p -> p.text(t -> t.analyzer("standard")))
                        .properties("tags", p -> p.text(t -> t.analyzer("standard")))
                        .properties("authorId", p -> p.long_(l -> l))
                        .properties("authorName", p -> p.keyword(k -> k))
                        .properties("createdTime", p -> p.date(d -> d)))));
        log.info("Created Elasticsearch index: {}", index);
    }
}
