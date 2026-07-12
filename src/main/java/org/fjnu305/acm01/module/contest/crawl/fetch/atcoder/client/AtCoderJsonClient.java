package org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.AtCoderCrawlProperties;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.dto.AtCoderContestItem;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.module.contest.crawl.config.CrawlHttpProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 爬虫失败时的 JSON 接口降级客户端（kenkoooo contests.json）。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AtCoderJsonClient {

    private final CrawlHttpClient crawlHttpClient;
    private final CrawlHttpProperties crawlHttpProperties;
    private final AtCoderCrawlProperties atCoderCrawlProperties;

    public List<AtCoderContestItem> fetch() {
        String url = atCoderCrawlProperties.getJsonFallbackUrl();
        AtCoderContestItem[] items;
        try {
            items = crawlHttpClient.getJson(url, AtCoderContestItem[].class);
        } catch (CrawlFetchException e) {
            throw e;
        } catch (Exception e) {
            throw CrawlFetchException.parseError(
                    url,
                    crawlHttpProperties.getMaxRetries(),
                    crawlHttpClient.getLastAttemptCount(),
                    "AtCoder JSON parse failed: " + e.getMessage(),
                    e
            );
        }

        if (items == null || items.length == 0) {
            log.warn("[atcoder] JSON fallback returned empty list");
            return Collections.emptyList();
        }

        log.info("[atcoder] JSON fallback fetched {} items", items.length);
        return Arrays.asList(items);
    }
}
