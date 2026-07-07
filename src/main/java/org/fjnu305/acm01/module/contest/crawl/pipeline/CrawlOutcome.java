package org.fjnu305.acm01.module.contest.crawl.pipeline;

import lombok.Builder;
import lombok.Getter;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.Common.enums.CrawlErrorType;
import org.fjnu305.acm01.Common.enums.CrawlTriggerType;
import org.fjnu305.acm01.module.contest.crawler.exception.CrawlFetchException;

/**
 * 单次爬取执行结果，供 {@link org.fjnu305.acm01.module.contest.crawl.service.CrawlLogService} 统一写日志。
 * <p>替代「用异常对象携带日志字段」的方式，成功/失败均通过本对象表达。</p>
 */
@Getter
@Builder
public class CrawlOutcome {

    private final ContestSource source;
    private final CrawlTriggerType triggerType;
    private final String requestUrl;
    private final long elapsedMs;
    private final boolean success;

    private final int fetchedCount;
    private final int maxRetries;
    private final int totalAttempts;

    private final CrawlErrorType errorType;
    private final Integer httpStatus;
    private final String errorMessage;
    private final String errorDetail;

    public static CrawlOutcome success(ContestSource source,
                                       CrawlTriggerType triggerType,
                                       String requestUrl,
                                       long elapsedMs,
                                       int fetchedCount,
                                       int totalAttempts,
                                       int maxRetries) {
        return CrawlOutcome.builder()
                .source(source)
                .triggerType(triggerType)
                .requestUrl(requestUrl)
                .elapsedMs(elapsedMs)
                .success(true)
                .fetchedCount(fetchedCount)
                .totalAttempts(totalAttempts)
                .maxRetries(maxRetries)
                .build();
    }

    public static CrawlOutcome fromException(ContestSource source,
                                             CrawlTriggerType triggerType,
                                             String requestUrl,
                                             long elapsedMs,
                                             CrawlFetchException exception) {
        return CrawlOutcome.builder()
                .source(source)
                .triggerType(triggerType)
                .requestUrl(requestUrl)
                .elapsedMs(elapsedMs)
                .success(false)
                .fetchedCount(0)
                .maxRetries(exception.getMaxRetries())
                .totalAttempts(exception.getTotalAttempts())
                .errorType(exception.getErrorType())
                .httpStatus(exception.getHttpStatus())
                .errorMessage(exception.getMessage())
                .errorDetail(stackTraceOf(exception))
                .build();
    }

    private static String stackTraceOf(Throwable t) {
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }
}
