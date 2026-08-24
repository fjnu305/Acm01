package org.fjnu305.acm01.module.contest.crawl.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.Common.enums.CrawlTriggerType;
import org.fjnu305.acm01.module.contest.crawl.fetch.ContestFetchSupport;
import org.fjnu305.acm01.module.contest.crawl.fetch.PlatformContestFetchService;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.Common.http.AppHttpProperties;
import org.fjnu305.acm01.module.contest.crawl.service.ContestPersistService;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.fjnu305.acm01.module.contest.log.dto.ContestPersistCountsDTO;
import org.fjnu305.acm01.module.contest.log.dto.CrawlOutcome;
import org.fjnu305.acm01.module.contest.log.service.CrawlLogService;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlPipeline {

    private final ContestPersistService persistService;
    private final CrawlLogService logService;
    private final CrawlHttpClient httpClient;
    private final AppHttpProperties httpProperties;
    private final ContestFetchSupport fetchSupport;

    public Long execute(PlatformContestFetchService fetchService, CrawlTriggerType triggerType) {
        ContestSource source = fetchService.getSource();
        String requestUrl = fetchService.getPrimaryRequestUrl();
        long startMs = System.currentTimeMillis();

        try {
            log.info("[{}] fetching contests...", source.getValue());
            List<ContestDTO> fetched = fetchSupport.attachRawHash(fetchService.fetchContests());
            ContestPersistCountsDTO persistCounts = persistService.persistAll(fetched);
            long elapsed = System.currentTimeMillis() - startMs;

            Long logId = logService.record(CrawlOutcome.success(
                    source,
                    triggerType,
                    requestUrl,
                    elapsed,
                    persistCounts,
                    httpClient.getLastAttemptCount(),
                    httpProperties.getMaxRetries()));

            log.info("[{}] done: fetched={}, insert={}, update={}, skip={}, elapsed={}ms, logId={}",
                    source.getValue(),
                    persistCounts.getFetchedCount(),
                    persistCounts.getInsertedCount(),
                    persistCounts.getUpdatedCount(),
                    persistCounts.getSkippedCount(),
                    elapsed,
                    logId);
            return logId;
        } catch (CrawlFetchException e) {
            recordFailure(source, triggerType, requestUrl, startMs, e);
            throw e;
        } catch (Exception e) {
            CrawlFetchException wrapped = CrawlFetchException.unknown(
                    requestUrl,
                    httpProperties.getMaxRetries(),
                    e.getMessage(),
                    e);
            recordFailure(source, triggerType, requestUrl, startMs, wrapped);
            throw wrapped;
        } finally {
            httpClient.clearLastAttemptCount();
        }
    }

    private void recordFailure(ContestSource source,
                               CrawlTriggerType triggerType,
                               String requestUrl,
                               long startMs,
                               CrawlFetchException exception) {
        long elapsed = System.currentTimeMillis() - startMs;
        logService.record(CrawlOutcome.fromException(
                source, triggerType, requestUrl, elapsed, exception));
        log.error("[{}] crawl failed [{}]: {}",
                source.getValue(), exception.getErrorType(), exception.getMessage());
    }
}
