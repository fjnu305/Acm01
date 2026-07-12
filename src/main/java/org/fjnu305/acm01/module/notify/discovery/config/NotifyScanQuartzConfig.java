package org.fjnu305.acm01.module.notify.discovery.config;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.notify.config.NotifyProperties;
import org.fjnu305.acm01.module.notify.discovery.job.NotifyScanJob;
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
@ConditionalOnProperty(prefix = "notify", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotifyScanQuartzConfig {

    public static final String JOB_NAME = "notifyScanJob";
    public static final String JOB_GROUP = "notify";
    public static final String TRIGGER_NAME = "notifyScanTrigger";
    public static final String TRIGGER_GROUP = "notify";

    private final NotifyProperties notifyProperties;

    @Bean
    public JobDetail notifyScanJobDetail() {
        return JobBuilder.newJob(NotifyScanJob.class)
                .withIdentity(JOB_NAME, JOB_GROUP)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger notifyScanTrigger(JobDetail notifyScanJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(notifyScanJobDetail)
                .withIdentity(TRIGGER_NAME, TRIGGER_GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule(notifyProperties.getScanCron()))
                .build();
    }
}
