package org.fjnu305.acm01.module.contest.crawl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.Common.enums.CrawlTriggerType;
import org.fjnu305.acm01.module.contest.crawl.fetch.PlatformContestFetchService;
import org.fjnu305.acm01.module.contest.crawl.fetch.PlatformFetchRegistry;
import org.fjnu305.acm01.module.contest.crawl.mapper.ContestSourceMapper;
import org.fjnu305.acm01.module.contest.crawl.pipeline.CrawlPipeline;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestCrawlService {

    private final PlatformFetchRegistry fetchRegistry;
    private final CrawlPipeline crawlPipeline;
    private final ContestSourceMapper contestSourceMapper;

    public void crawlAll(CrawlTriggerType triggerType) {
        for (PlatformContestFetchService fetchService : fetchRegistry.all()) {
            ContestSource source = fetchService.getSource();
            if (!isCrawlEnabled(source)) {
                continue;
            }
            try {
                crawl(source, triggerType);
            } catch (Exception e) {
                log.error("[{}] crawl failed, continue remaining sources", source.getValue(), e);
            }
        }
    }

    public Long crawl(ContestSource source, CrawlTriggerType triggerType) {
        if (!isCrawlEnabled(source)) {
            log.debug("[{}] crawl_enabled=0, skipped", source.getValue());
            return null;
        }
        PlatformContestFetchService fetchService = fetchRegistry.getRequired(source);
        return crawlPipeline.execute(fetchService, triggerType);
    }

    public Long crawlManual(ContestSource source) {
        return crawl(source, CrawlTriggerType.MANUAL);
    }

    private boolean isCrawlEnabled(ContestSource source) {
        Integer enabled = contestSourceMapper.selectCrawlEnabled(source.getValue());
        return enabled == null || enabled == 1;
    }
}
