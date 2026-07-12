package org.fjnu305.acm01.module.contest.crawl.schedule;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.CrawlTriggerType;
import org.fjnu305.acm01.module.contest.crawl.service.ContestCrawlService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * Quartz 定时触发赛事爬虫，调用 {@link ContestCrawlService#crawlAll(CrawlTriggerType)}。
 * <p>
 * 与 {@link org.fjnu305.acm01.module.contest.controller.ContestAdminController} 手动触发共用同一套爬取链路，
 * 日志中 {@code trigger_type=AUTO} 用于区分。
 * </p>
 */
@Slf4j
@Component
@DisallowConcurrentExecution
public class ContestCrawlJob extends QuartzJobBean {

    @Autowired
    private ContestCrawlService contestCrawlService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("Contest crawl auto job started");
        contestCrawlService.crawlAll(CrawlTriggerType.AUTO);
        log.info("Contest crawl auto job finished");
    }
}
