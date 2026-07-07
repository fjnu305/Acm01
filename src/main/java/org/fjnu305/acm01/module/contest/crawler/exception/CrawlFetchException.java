package org.fjnu305.acm01.module.contest.crawler.exception;

import lombok.Getter;
import org.fjnu305.acm01.Common.enums.CrawlErrorType;

/**
 * 爬虫拉取失败统一异常。
 * <p>
 * 字段与 {@code contest_crawl_log} 表对应；由 {@link org.fjnu305.acm01.module.contest.crawl.pipeline.CrawlOutcome} 转为日志实体。
 * 全平台策略（Codeforces / AtCoder / …）均通过本异常上报失败，保证日志结构一致。
 * </p>
 */
@Getter
public class CrawlFetchException extends RuntimeException {

    private final CrawlErrorType errorType;
    private final Integer httpStatus;
    private final String requestUrl;
    private final int maxRetries;
    private final int totalAttempts;

    private CrawlFetchException(CrawlErrorType errorType,
                                Integer httpStatus,
                                String requestUrl,
                                int maxRetries,
                                int totalAttempts,
                                String message,
                                Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.httpStatus = httpStatus;
        this.requestUrl = requestUrl;
        this.maxRetries = maxRetries;
        this.totalAttempts = totalAttempts;
    }

    /** HTTP 层失败（含重试耗尽） */
    public static CrawlFetchException httpError(String requestUrl,
                                                Integer httpStatus,
                                                int maxRetries,
                                                int totalAttempts,
                                                String message,
                                                Throwable cause) {
        CrawlErrorType type = isTimeout(cause, message) ? CrawlErrorType.TIMEOUT : CrawlErrorType.HTTP_ERROR;
        return new CrawlFetchException(type, httpStatus, requestUrl, maxRetries, totalAttempts, message, cause);
    }

    /** 平台 API 业务失败（HTTP 成功但 status!=OK 等） */
    public static CrawlFetchException apiError(String requestUrl,
                                             int maxRetries,
                                             String message) {
        return new CrawlFetchException(
                CrawlErrorType.API_ERROR, null, requestUrl, maxRetries, 1, message, null);
    }

    /** JSON / HTML 解析失败 */
    public static CrawlFetchException parseError(String requestUrl,
                                                 int maxRetries,
                                                 int totalAttempts,
                                                 String message,
                                                 Throwable cause) {
        return new CrawlFetchException(
                CrawlErrorType.PARSE_ERROR, null, requestUrl, maxRetries, totalAttempts, message, cause);
    }

    /** 未分类失败 */
    public static CrawlFetchException unknown(String requestUrl,
                                              int maxRetries,
                                              String message,
                                              Throwable cause) {
        return new CrawlFetchException(
                CrawlErrorType.UNKNOWN, null, requestUrl, maxRetries, 1, message, cause);
    }

    private static boolean isTimeout(Throwable cause, String message) {
        String text = message != null ? message.toLowerCase() : "";
        if (text.contains("timeout") || text.contains("timed out")) {
            return true;
        }
        return cause != null && cause.getMessage() != null
                && cause.getMessage().toLowerCase().contains("timeout");
    }
}
