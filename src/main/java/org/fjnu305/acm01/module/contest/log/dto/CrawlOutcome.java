package org.fjnu305.acm01.module.contest.log.dto;

import lombok.Builder;
import lombok.Getter;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.Common.enums.CrawlErrorType;
import org.fjnu305.acm01.Common.enums.CrawlTriggerType;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;

@Getter
@Builder
public class CrawlOutcome {

    private final ContestSource source;
    private final CrawlTriggerType triggerType;
    private final String requestUrl;
    private final long elapsedMs;
    private final boolean success;

    private final ContestPersistCountsDTO persistCounts;
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
                                       ContestPersistCountsDTO persistCounts,
                                       int totalAttempts,
                                       int maxRetries) {
        return CrawlOutcome.builder()
                .source(source)
                .triggerType(triggerType)
                .requestUrl(requestUrl)
                .elapsedMs(elapsedMs)
                .success(true)
                .persistCounts(persistCounts != null ? persistCounts : ContestPersistCountsDTO.empty())
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
                .persistCounts(ContestPersistCountsDTO.empty())
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
