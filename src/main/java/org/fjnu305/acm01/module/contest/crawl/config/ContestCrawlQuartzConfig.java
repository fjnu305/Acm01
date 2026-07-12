package org.fjnu305.acm01.module.contest.crawl.config;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.contest.crawl.schedule.ContestCrawlJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 赛事爬虫 Quartz 调度：注册 {@link ContestCrawlJob} 与 cron 触发器。
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "contest.crawl.schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ContestCrawlQuartzConfig {

    public static final String JOB_NAME = "contestCrawlJob";
    public static final String JOB_GROUP = "contestCrawl";
    public static final String TRIGGER_NAME = "contestCrawlTrigger";
    public static final String TRIGGER_GROUP = "contestCrawl";

    private final CrawlScheduleProperties scheduleProperties;

    @Bean
    public JobDetail contestCrawlJobDetail() {
        return JobBuilder.newJob(ContestCrawlJob.class)
                .withIdentity(JOB_NAME, JOB_GROUP)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger contestCrawlTrigger(JobDetail contestCrawlJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(contestCrawlJobDetail)
                .withIdentity(TRIGGER_NAME, TRIGGER_GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule(scheduleProperties.getCron()))
                .build();
    }
}
