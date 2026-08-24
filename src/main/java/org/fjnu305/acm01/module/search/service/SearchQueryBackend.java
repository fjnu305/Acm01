package org.fjnu305.acm01.module.search.service;

import org.fjnu305.acm01.module.search.vo.SearchResultVO;

/**
 * 搜索查询后端：ES / MySQL 实现分离，由 {@link SearchQueryService} 选择与回退。
 */
public interface SearchQueryBackend {

    SearchResultVO search(String query, String type, int page, int size, SearchResultVO shell);
}
