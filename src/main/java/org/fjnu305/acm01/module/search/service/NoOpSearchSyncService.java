package org.fjnu305.acm01.module.search.service;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.search.document.SearchDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "search", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpSearchSyncService implements SearchSyncService {

    @Override
    public void index(SearchDocument document) {
        log.debug("Search disabled, skip index {}", document.getId());
    }

    @Override
    public void delete(String type, Long refId) {
        log.debug("Search disabled, skip delete {}:{}", type, refId);
    }
}
