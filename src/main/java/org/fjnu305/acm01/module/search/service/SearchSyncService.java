package org.fjnu305.acm01.module.search.service;

import org.fjnu305.acm01.module.search.document.SearchDocument;

public interface SearchSyncService {

    void index(SearchDocument document);

    void delete(String type, Long refId);
}
