package org.fjnu305.acm01.module.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.search.config.SearchProperties;
import org.fjnu305.acm01.module.search.support.SearchHitSupport;
import org.fjnu305.acm01.module.search.vo.SearchResultVO;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchQueryService {

    private final SearchProperties searchProperties;
    private final ObjectProvider<ElasticsearchSearchBackend> elasticsearchBackendProvider;
    private final MysqlSearchBackend mysqlSearchBackend;

    public SearchResultVO search(String query, String type, int pageNum, int pageSize) {
        String q = query == null ? "" : query.trim();
        String normalizedType = SearchHitSupport.normalizeType(type);
        int page = Math.max(pageNum, 1);
        int size = pageSize <= 0 ? searchProperties.getDefaultPageSize() : Math.min(pageSize, 50);

        SearchResultVO result = new SearchResultVO();
        result.setQuery(q);
        result.setType(normalizedType);
        result.setPageNum(page);
        result.setPageSize(size);

        if (!StringUtils.hasText(q)) {
            result.setHits(Collections.emptyList());
            result.setTotal(0);
            return result;
        }

        if (searchProperties.isEnabled()) {
            ElasticsearchSearchBackend es = elasticsearchBackendProvider.getIfAvailable();
            if (es != null) {
                try {
                    return es.search(q, normalizedType, page, size, result);
                } catch (Exception e) {
                    log.warn("Elasticsearch search failed, fallback to MySQL: {}", e.getMessage());
                }
            }
        }
        return mysqlSearchBackend.search(q, normalizedType, page, size, result);
    }
}
