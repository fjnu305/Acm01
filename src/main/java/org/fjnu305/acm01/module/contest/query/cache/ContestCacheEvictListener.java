package org.fjnu305.acm01.module.contest.query.cache;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.contest.event.ContestCatalogChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ContestCacheEvictListener {

    private final ContestCacheService contestCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onCatalogChanged(ContestCatalogChangedEvent event) {
        contestCacheService.evictAllLists();
    }
}
