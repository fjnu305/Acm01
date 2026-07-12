package org.fjnu305.acm01.module.notify.discovery.job;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.notify.config.NotifyProperties;
import org.fjnu305.acm01.module.notify.discovery.api.NotifyDeliveryPublisher;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyTaskMapper;
import org.fjnu305.acm01.module.subscription.entity.ContestSubscriptionEntity;
import org.fjnu305.acm01.module.subscription.mapper.ContestSubscriptionMapper;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@DisallowConcurrentExecution
public class NotifyScanJob extends QuartzJobBean {

    @Autowired
    private NotifyTaskMapper notifyTaskMapper;

    @Autowired
    private ContestSubscriptionMapper subscriptionMapper;

    @Autowired
    private NotifyDeliveryPublisher deliveryPublisher;

    @Autowired
    private NotifyProperties notifyProperties;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        if (!notifyProperties.isEnabled()) {
            return;
        }

        int batchSize = notifyProperties.getScanBatchSize();
        List<NotifyTaskEntity> dueTasks = notifyTaskMapper.selectDueTasks(batchSize);
        if (dueTasks.isEmpty()) {
            return;
        }

        Map<Long, List<NotifyTaskEntity>> grouped = groupByUser(dueTasks);
        log.info("Notify scan job dispatching {} user batch(es) from {} due task(s)",
                grouped.size(), dueTasks.size());

        long interval = notifyProperties.getSendIntervalMillis();

        for (Map.Entry<Long, List<NotifyTaskEntity>> entry : grouped.entrySet()) {
            try {
                deliveryPublisher.publish(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("Failed to dispatch notify batch for user {}", entry.getKey(), e);
            }
            sleepInterval(interval);
        }
    }

    private Map<Long, List<NotifyTaskEntity>> groupByUser(List<NotifyTaskEntity> tasks) {
        Map<Long, List<NotifyTaskEntity>> grouped = new LinkedHashMap<>();
        for (NotifyTaskEntity task : tasks) {
            ContestSubscriptionEntity subscription = subscriptionMapper.selectById(task.getSubscriptionId());
            if (subscription == null) {
                continue;
            }
            grouped.computeIfAbsent(subscription.getUserId(), ignored -> new ArrayList<>()).add(task);
        }
        return grouped;
    }

    private void sleepInterval(long interval) {
        if (interval <= 0) {
            return;
        }
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
