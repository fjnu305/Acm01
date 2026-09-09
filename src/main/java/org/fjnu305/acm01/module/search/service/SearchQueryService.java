package org.fjnu305.acm01.module.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.access.ContentAccessPolicy;
import org.fjnu305.acm01.Common.access.Viewer;
import org.fjnu305.acm01.module.search.config.SearchProperties;
import org.fjnu305.acm01.module.search.support.SearchHitSupport;
import org.fjnu305.acm01.module.search.vo.SearchHitVO;
import org.fjnu305.acm01.module.search.vo.SearchResultVO;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchQueryService {

    private final SearchProperties searchProperties;
    private final ObjectProvider<ElasticsearchSearchBackend> elasticsearchBackendProvider;
    private final MysqlSearchBackend mysqlSearchBackend;
    private final ContentAccessPolicy contentAccessPolicy;

    public SearchResultVO search(String query, String type, int pageNum, int pageSize) {
        return search(query, type, pageNum, pageSize, Viewer.current());
    }

    public SearchResultVO search(String query, String type, int pageNum, int pageSize, Viewer viewer) {
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
                    return applyVisibility(es.search(q, normalizedType, page, size, result), viewer);
                } catch (Exception e) {
                    log.warn("Elasticsearch search failed, fallback to MySQL: {}", e.getMessage());
                }
            }
        }
        return applyVisibility(mysqlSearchBackend.search(q, normalizedType, page, size, result), viewer);
    }

    private SearchResultVO applyVisibility(SearchResultVO result, Viewer viewer) {
        List<SearchHitVO> source = result.getHits() == null ? List.of() : result.getHits();
        List<SearchHitVO> visible = new ArrayList<>();
        int hidden = 0;
        for (SearchHitVO hit : source) {
            if (contentAccessPolicy.canRead(hit.getType(), hit.getRefId(), viewer)) {
                visible.add(hit);
            } else {
                hidden++;
            }
        }
        result.setHits(visible);
        long total = result.getTotal();
        if (hidden > 0 && total >= hidden) {
            result.setTotal(total - hidden);
        }
        return result;
    }
}
