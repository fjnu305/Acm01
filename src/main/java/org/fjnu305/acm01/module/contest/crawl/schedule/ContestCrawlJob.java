package org.fjnu305.acm01.module.contest.crawl.schedule;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.CrawlTriggerType;
import org.fjnu305.acm01.Common.job.RedisJobLock;
import org.fjnu305.acm01.module.contest.crawl.config.CrawlScheduleProperties;
import org.fjnu305.acm01.module.contest.crawl.service.ContestCrawlService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Quartz 定时触发赛事爬虫，调用 {@link ContestCrawlService#crawlAll(CrawlTriggerType)}。
 */
@Slf4j
@Component
@DisallowConcurrentExecution
public class ContestCrawlJob extends QuartzJobBean {

    public static final String LOCK_KEY = "job:contest-crawl";

    @Autowired
    private ContestCrawlService contestCrawlService;

    @Autowired
    private RedisJobLock redisJobLock;

    @Autowired
    private CrawlScheduleProperties scheduleProperties;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        int ttlMinutes = Math.max(1, scheduleProperties.getLockTtlMinutes());
        redisJobLock.tryRun(LOCK_KEY, Duration.ofMinutes(ttlMinutes), () -> {
            log.info("Contest crawl auto job started");
            contestCrawlService.crawlAll(CrawlTriggerType.AUTO);
            log.info("Contest crawl auto job finished");
        });
    }
}
