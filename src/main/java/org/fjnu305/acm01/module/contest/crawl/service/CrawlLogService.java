package org.fjnu305.acm01.module.contest.crawl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.CrawlLogStatus;
import org.fjnu305.acm01.module.contest.crawl.pipeline.CrawlOutcome;
import org.fjnu305.acm01.module.contest.crawler.http.CrawlHttpProperties;
import org.fjnu305.acm01.module.contest.entity.ContestCrawlLogEntity;
import org.fjnu305.acm01.module.contest.mapper.ContestCrawlLogMapper;
import org.fjnu305.acm01.module.contest.mapper.ContestSourceMapper;
import org.springframework.stereotype.Service;

/**
 * 爬虫日志：根据 {@link CrawlOutcome} 写入 contest_crawl_log，并更新 contest_source。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlLogService {

    private static final int MAX_ERROR_MESSAGE_LEN = 1000;
    private static final int MAX_ERROR_DETAIL_LEN = 4000;

    private final ContestCrawlLogMapper crawlLogMapper;
    private final ContestSourceMapper contestSourceMapper;
    private final CrawlHttpProperties crawlHttpProperties;

    /**
     * 根据流水线执行结果写一条日志（成功或失败各一条）。
     */
    public void record(CrawlOutcome outcome) {
        ContestCrawlLogEntity entity = new ContestCrawlLogEntity();
        entity.setSource(outcome.getSource().getValue());
        entity.setTriggerType(outcome.getTriggerType().getValue());
        entity.setRequestUrl(outcome.getRequestUrl());
        entity.setElapsedMs(safeElapsed(outcome.getElapsedMs()));
        entity.setMaxRetries(outcome.getMaxRetries() > 0 ? outcome.getMaxRetries() : crawlHttpProperties.getMaxRetries());
        entity.setTotalAttempts(outcome.getTotalAttempts() > 0 ? outcome.getTotalAttempts() : 1);

        if (outcome.isSuccess()) {
            entity.setStatus(CrawlLogStatus.SUCCESS.getValue());
            entity.setFetchedCount(outcome.getFetchedCount());
            insertSafely(entity);
            contestSourceMapper.updateOnSuccess(outcome.getSource().getValue());
            return;
        }

        entity.setStatus(CrawlLogStatus.FAILED.getValue());
        entity.setFetchedCount(0);
        entity.setErrorType(outcome.getErrorType() != null ? outcome.getErrorType().getValue() : null);
        entity.setHttpStatus(outcome.getHttpStatus());
        entity.setErrorMessage(truncate(outcome.getErrorMessage(), MAX_ERROR_MESSAGE_LEN));
        entity.setErrorDetail(truncate(outcome.getErrorDetail(), MAX_ERROR_DETAIL_LEN));
        insertSafely(entity);
        contestSourceMapper.updateOnFailure(outcome.getSource().getValue());
    }

    private void insertSafely(ContestCrawlLogEntity entity) {
        try {
            crawlLogMapper.insert(entity);
        } catch (Exception e) {
            log.error("[{}] 写入 contest_crawl_log 失败: {}", entity.getSource(), e.getMessage(), e);
        }
    }

    private static int safeElapsed(long elapsedMs) {
        return (int) Math.min(elapsedMs, Integer.MAX_VALUE);
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) {
            return null;
        }
        return text.length() <= maxLen ? text : text.substring(0, maxLen);
    }
}
