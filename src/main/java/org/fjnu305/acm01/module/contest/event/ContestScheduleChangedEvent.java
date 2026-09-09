package org.fjnu305.acm01.module.contest.event;

import java.time.LocalDateTime;

/**
 * Fired after crawl persist when a contest {@code start_time} actually changes.
 */
public record ContestScheduleChangedEvent(Long contestId, LocalDateTime newStartTime) {
}
