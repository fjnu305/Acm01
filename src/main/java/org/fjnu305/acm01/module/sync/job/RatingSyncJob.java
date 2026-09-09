package org.fjnu305.acm01.module.sync.job;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.job.RedisJobLock;
import org.fjnu305.acm01.module.sync.config.SyncProperties;
import org.fjnu305.acm01.module.sync.service.RatingSyncService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@DisallowConcurrentExecution
public class RatingSyncJob extends QuartzJobBean {

    public static final String LOCK_KEY = "job:rating-sync";

    @Autowired
    private RatingSyncService ratingSyncService;

    @Autowired
    private RedisJobLock redisJobLock;

    @Autowired
    private SyncProperties syncProperties;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        int ttlMinutes = Math.max(5, syncProperties.getSchedule().getLockTtlMinutes());
        redisJobLock.tryRun(LOCK_KEY, Duration.ofMinutes(ttlMinutes), () -> {
            log.info("Rating sync auto job started");
            ratingSyncService.syncAllAccounts();
            log.info("Rating sync auto job finished");
        });
    }
}
