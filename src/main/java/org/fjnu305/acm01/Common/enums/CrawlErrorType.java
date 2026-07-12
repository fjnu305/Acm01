package org.fjnu305.acm01.Common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 爬虫失败类型，对?{@code contest_crawl_log.error_type}?
 * 爬虫失败类型，对?{@code contest_crawl_log.error_type}?
 */
@Getter
@RequiredArgsConstructor
public enum CrawlErrorType {

    /** {@link org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient} 网络/HTTP 重试耗尽 */
    HTTP_ERROR("HTTP_ERROR"),

    API_ERROR("API_ERROR"),

    /** JSON / HTML 解析失败 */
    PARSE_ERROR("PARSE_ERROR"),

    /** 连接或读取超?*/
    TIMEOUT("TIMEOUT"),

    /** 未分类异?*/
    UNKNOWN("UNKNOWN");

    private final String value;
}
