package org.fjnu305.acm01.module.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.NamedValue;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.search.config.SearchProperties;
import org.fjnu305.acm01.module.search.document.SearchDocument;
import org.fjnu305.acm01.module.search.support.SearchHitSupport;
import org.fjnu305.acm01.module.search.vo.SearchHitVO;
import org.fjnu305.acm01.module.search.vo.SearchResultVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search", name = "enabled", havingValue = "true")
public class ElasticsearchSearchBackend implements SearchQueryBackend {

    private final SearchProperties searchProperties;
    private final ElasticsearchClient elasticsearchClient;

    @Override
    public SearchResultVO search(String query, String type, int page, int size, SearchResultVO result) {
        try {
            return doSearch(query, type, page, size, result);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Elasticsearch search failed: " + e.getMessage(), e);
        }
    }

    private SearchResultVO doSearch(String q, String type, int page, int size, SearchResultVO result)
            throws Exception {
        int from = (page - 1) * size;

        List<Query> filters = new ArrayList<>();
        if (!"all".equals(type)) {
            filters.add(Query.of(qb -> qb.term(t -> t.field("type").value(type))));
        }

        Query multiMatch = Query.of(qb -> qb.multiMatch(m -> m
                .query(q)
                .fields("title^3", "content", "tags^2")));

        Query finalQuery = filters.isEmpty()
                ? multiMatch
                : Query.of(qb -> qb.bool(BoolQuery.of(b -> b.must(multiMatch).filter(filters))));

        SearchResponse<SearchDocument> response = elasticsearchClient.search(
                SearchRequest.of(s -> s
                        .index(searchProperties.getIndexName())
                        .from(from)
                        .size(size)
                        .query(finalQuery)
                        .highlight(h -> h
                                .fields(NamedValue.of("title",
                                        HighlightField.of(f -> f.preTags("<em>").postTags("</em>"))))
                                .fields(NamedValue.of("content",
                                        HighlightField.of(f -> f.preTags("<em>").postTags("</em>")))))),
                SearchDocument.class);

        List<SearchHitVO> hits = new ArrayList<>();
        for (Hit<SearchDocument> hit : response.hits().hits()) {
            SearchDocument doc = hit.source();
            if (doc == null) {
                continue;
            }
            SearchHitVO vo = new SearchHitVO();
            vo.setType(doc.getType());
            vo.setRefId(doc.getRefId());
            vo.setTitle(doc.getTitle());
            vo.setSnippet(SearchHitSupport.truncate(doc.getContent(), 200));
            vo.setTags(doc.getTags());
            vo.setAuthorName(doc.getAuthorName());
            vo.setCreatedTime(doc.getCreatedTime());
            vo.setHighlights(hit.highlight() != null && !hit.highlight().isEmpty()
                    ? SearchHitSupport.extractHighlights(hit.highlight())
                    : SearchHitSupport.highlightText(doc.getTitle(), q));
            hits.add(vo);
        }

        long total = response.hits().total() != null ? response.hits().total().value() : hits.size();
        result.setHits(hits);
        result.setTotal(total);
        return result;
    }
}
