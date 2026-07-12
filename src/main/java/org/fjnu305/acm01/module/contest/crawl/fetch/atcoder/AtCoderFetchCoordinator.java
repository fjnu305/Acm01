package org.fjnu305.acm01.module.contest.crawl.fetch.atcoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.client.AtCoderHtmlClient;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.client.AtCoderJsonClient;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AtCoder 固定策略：官网 HTML 爬虫主源，仅爬虫失败时降级 JSON 接口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AtCoderFetchCoordinator {

    private final AtCoderJsonClient jsonClient;
    private final AtCoderHtmlClient htmlClient;
    private final AtCoderContestMapper contestMapper;

    public List<ContestDTO> fetch() {
        try {
            List<ContestDTO> crawled = contestMapper.mapItems(htmlClient.fetch());
            log.info("[atcoder] 爬虫成功，使用 HTML 结果 fetched={}", crawled.size());
            return crawled;
        } catch (CrawlFetchException e) {
            log.warn("[atcoder] 爬虫失败，降级 JSON 接口: {}", e.getMessage());
            List<ContestDTO> fallback = contestMapper.mapItems(jsonClient.fetch());
            log.info("[atcoder] fallback=JSON fetched={}", fallback.size());
            return fallback;
        }
    }
}
