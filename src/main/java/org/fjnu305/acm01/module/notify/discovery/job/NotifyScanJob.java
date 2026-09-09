package org.fjnu305.acm01.module.notify.discovery.job;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.job.RedisJobLock;
import org.fjnu305.acm01.module.notify.config.NotifyProperties;
import org.fjnu305.acm01.module.notify.discovery.service.NotifyScanService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@DisallowConcurrentExecution
public class NotifyScanJob extends QuartzJobBean {

    public static final String LOCK_KEY = "job:notify-scan";

    @Autowired
    private NotifyScanService notifyScanService;

    @Autowired
    private NotifyProperties notifyProperties;

    @Autowired
    private RedisJobLock redisJobLock;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        if (!notifyProperties.isEnabled()) {
            return;
        }
        int ttlSeconds = Math.max(10, notifyProperties.getScanLockTtlSeconds());
        redisJobLock.tryRun(LOCK_KEY, Duration.ofSeconds(ttlSeconds), notifyScanService::scanAndDispatch);
    }
}
