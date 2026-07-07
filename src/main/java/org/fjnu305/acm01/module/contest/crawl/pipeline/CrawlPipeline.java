package org.fjnu305.acm01.module.contest.crawl.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.CrawlTriggerType;
import org.fjnu305.acm01.module.contest.crawl.service.ContestPersistService;
import org.fjnu305.acm01.module.contest.crawl.service.CrawlLogService;
import org.fjnu305.acm01.module.contest.crawler.ContestCrawler;
import org.fjnu305.acm01.module.contest.crawler.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.crawler.http.CrawlHttpClient;
import org.fjnu305.acm01.module.contest.crawler.http.CrawlHttpProperties;
import org.fjnu305.acm01.module.contest.dto.ContestCrawlResult;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.fjnu305.acm01.module.contest.dto.ContestPersistResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 爬虫流水线：fetch → persist → log，三步完成单次爬取。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlPipeline {

    private final ContestPersistService persistService;
    private final CrawlLogService logService;
    private final CrawlHttpClient httpClient;
    private final CrawlHttpProperties httpProperties;

    /**
     * 执行单平台完整爬取流程。
     *
     * @throws CrawlFetchException 爬取失败时，日志已写入后再抛出
     */
    public ContestCrawlResult execute(ContestCrawler crawler, CrawlTriggerType triggerType) {
        String requestUrl = crawler.getPrimaryRequestUrl();
        long startMs = System.currentTimeMillis();

        try {
            List<ContestDTO> fetched = crawler.fetchContests();
            ContestPersistResult persist = persistService.persistAll(fetched);
            long elapsed = System.currentTimeMillis() - startMs;

            CrawlOutcome outcome = CrawlOutcome.success(
                    crawler.getSource(),
                    triggerType,
                    requestUrl,
                    elapsed,
                    persist.getFetched(),
                    httpClient.getLastAttemptCount(),
                    httpProperties.getMaxRetries());
            logService.record(outcome);

            log.info("[{}] 爬取入库成功：fetch={}, insert={}, update={}, skip={}, 耗时 {} ms",
                    crawler.getSource().getValue(),
                    persist.getFetched(),
                    persist.getInserted(),
                    persist.getUpdated(),
                    persist.getSkipped(),
                    elapsed);

            return ContestCrawlResult.builder()
                    .source(crawler.getSource())
                    .triggerType(triggerType)
                    .elapsedMs(elapsed)
                    .persist(persist)
                    .build();
        } catch (CrawlFetchException e) {
            recordFailureAndThrow(crawler, triggerType, requestUrl, startMs, e);
            throw e;
        } catch (Exception e) {
            CrawlFetchException wrapped = CrawlFetchException.unknown(
                    requestUrl,
                    httpProperties.getMaxRetries(),
                    e.getMessage(),
                    e);
            recordFailureAndThrow(crawler, triggerType, requestUrl, startMs, wrapped);
            throw wrapped;
        } finally {
            httpClient.clearLastAttemptCount();
        }
    }

    private void recordFailureAndThrow(ContestCrawler crawler,
                                       CrawlTriggerType triggerType,
                                       String requestUrl,
                                       long startMs,
                                       CrawlFetchException exception) {
        long elapsed = System.currentTimeMillis() - startMs;
        logService.record(CrawlOutcome.fromException(
                crawler.getSource(), triggerType, requestUrl, elapsed, exception));
        log.error("[{}] 爬取失败 [{}]: {}",
                crawler.getSource().getValue(), exception.getErrorType(), exception.getMessage());
    }
}
