package org.fjnu305.acm01.Common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 爬虫执行日志结果状态，对应 {@code contest_crawl_log.status}?
 * 爬虫执行日志结果状态，对应 {@code contest_crawl_log.status}?
 */
@Getter
@RequiredArgsConstructor
public enum CrawlLogStatus {

    SUCCESS("SUCCESS"),
    FAILED("FAILED");

    private final String value;
}
