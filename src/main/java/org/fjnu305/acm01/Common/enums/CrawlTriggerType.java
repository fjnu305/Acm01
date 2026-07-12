package org.fjnu305.acm01.Common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 爬虫触发方式，对应 {@code contest_crawl_log.trigger_type}。
 */
@Getter
@RequiredArgsConstructor
public enum CrawlTriggerType {

    /** Quartz 定时任务触发 {@link org.fjnu305.acm01.module.contest.crawl.schedule.ContestCrawlJob} */
    AUTO("AUTO"),

    /** 管理端手动触发 {@link org.fjnu305.acm01.module.contest.controller.ContestAdminController} */
    MANUAL("MANUAL");

    private final String value;
}
