package org.fjnu305.acm01.module.contest.log.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.CrawlLogStatus;
import org.fjnu305.acm01.Common.http.AppHttpProperties;
import org.fjnu305.acm01.module.contest.crawl.mapper.ContestSourceMapper;
import org.fjnu305.acm01.module.contest.log.dto.ContestPersistCountsDTO;
import org.fjnu305.acm01.module.contest.log.dto.CrawlOutcome;
import org.fjnu305.acm01.module.contest.log.entity.ContestCrawlLogEntity;
import org.fjnu305.acm01.module.contest.log.mapper.ContestCrawlLogMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlLogService {

    private static final int MAX_ERROR_MESSAGE_LEN = 1000;
    private static final int MAX_ERROR_DETAIL_LEN = 4000;

    private final ContestCrawlLogMapper crawlLogMapper;
    private final ContestSourceMapper contestSourceMapper;
    private final AppHttpProperties appHttpProperties;

    public Long record(CrawlOutcome outcome) {
        ContestCrawlLogEntity entity = new ContestCrawlLogEntity();
        entity.setSource(outcome.getSource().getValue());
        entity.setTriggerType(outcome.getTriggerType().getValue());
        entity.setRequestUrl(outcome.getRequestUrl());
        entity.setElapsedMs(safeElapsed(outcome.getElapsedMs()));
        entity.setMaxRetries(outcome.getMaxRetries() > 0 ? outcome.getMaxRetries() : appHttpProperties.getMaxRetries());
        entity.setTotalAttempts(outcome.getTotalAttempts() > 0 ? outcome.getTotalAttempts() : 1);

        if (outcome.isSuccess()) {
            ContestPersistCountsDTO counts = outcome.getPersistCounts();
            entity.setStatus(CrawlLogStatus.SUCCESS.getValue());
            entity.setFetchedCount(counts.getFetchedCount());
            entity.setInsertedCount(counts.getInsertedCount());
            entity.setUpdatedCount(counts.getUpdatedCount());
            entity.setSkippedCount(counts.getSkippedCount());
            entity.setIgnoredCount(counts.getIgnoredCount());
            Long logId = insertSafely(entity);
            contestSourceMapper.updateOnSuccess(outcome.getSource().getValue());
            return logId;
        }

        entity.setStatus(CrawlLogStatus.FAILED.getValue());
        entity.setFetchedCount(0);
        entity.setInsertedCount(0);
        entity.setUpdatedCount(0);
        entity.setSkippedCount(0);
        entity.setIgnoredCount(0);
        entity.setErrorType(outcome.getErrorType() != null ? outcome.getErrorType().getValue() : null);
        entity.setHttpStatus(outcome.getHttpStatus());
        entity.setErrorMessage(truncate(outcome.getErrorMessage(), MAX_ERROR_MESSAGE_LEN));
        entity.setErrorDetail(truncate(outcome.getErrorDetail(), MAX_ERROR_DETAIL_LEN));
        Long logId = insertSafely(entity);
        contestSourceMapper.updateOnFailure(outcome.getSource().getValue());
        return logId;
    }

    private Long insertSafely(ContestCrawlLogEntity entity) {
        try {
            crawlLogMapper.insert(entity);
            return entity.getId();
        } catch (Exception e) {
            log.error("[{}] failed to write contest_crawl_log: {}", entity.getSource(), e.getMessage(), e);
            return null;
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
