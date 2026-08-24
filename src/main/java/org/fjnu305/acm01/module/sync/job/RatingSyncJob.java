package org.fjnu305.acm01.module.sync.job;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.sync.service.RatingSyncService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
public class RatingSyncJob extends QuartzJobBean {

    @Autowired
    private RatingSyncService ratingSyncService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("Rating sync auto job started");
        ratingSyncService.syncAllAccounts();
        log.info("Rating sync auto job finished");
    }
}
