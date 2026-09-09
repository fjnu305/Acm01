package org.fjnu305.acm01.module.contest.event;

/**
 * Fired after crawl persist when the contest catalog actually changed.
 * Listeners should bump read-model caches after commit.
 */
public record ContestCatalogChangedEvent(int insertedCount, int updatedCount) {
}
