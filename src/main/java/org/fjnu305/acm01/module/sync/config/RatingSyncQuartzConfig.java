package org.fjnu305.acm01.module.sync.config;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.sync.job.RatingSyncJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sync.schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RatingSyncQuartzConfig {

    public static final String JOB_NAME = "ratingSyncJob";
    public static final String JOB_GROUP = "ojSync";
    public static final String TRIGGER_NAME = "ratingSyncTrigger";
    public static final String TRIGGER_GROUP = "ojSync";

    private final SyncProperties syncProperties;

    @Bean
    public JobDetail ratingSyncJobDetail() {
        return JobBuilder.newJob(RatingSyncJob.class)
                .withIdentity(JOB_NAME, JOB_GROUP)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger ratingSyncTrigger(JobDetail ratingSyncJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(ratingSyncJobDetail)
                .withIdentity(TRIGGER_NAME, TRIGGER_GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule(syncProperties.getSchedule().getCron()))
                .build();
    }
}
