package org.fjnu305.acm01.Common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 爬虫失败类型，对应 {@code contest_crawl_log.error_type}。
 * <p>全平台统一枚举，各策略通过 {@link org.fjnu305.acm01.module.contest.crawler.exception.CrawlFetchException} 赋值。</p>
 */
@Getter
@RequiredArgsConstructor
public enum CrawlErrorType {

    /** {@link org.fjnu305.acm01.module.contest.crawler.http.CrawlHttpClient} 网络/HTTP 重试耗尽 */
    HTTP_ERROR("HTTP_ERROR"),

    /** HTTP 200 但平台 API 返回业务失败（如 Codeforces status=FAILED） */
    API_ERROR("API_ERROR"),

    /** JSON / HTML 解析失败 */
    PARSE_ERROR("PARSE_ERROR"),

    /** 连接或读取超时 */
    TIMEOUT("TIMEOUT"),

    /** 未分类异常 */
    UNKNOWN("UNKNOWN");

    private final String value;
}
