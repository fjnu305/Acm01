package org.fjnu305.acm01.module.notify.discovery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.notify.config.NotifyProperties;
import org.fjnu305.acm01.module.notify.discovery.api.NotifyDeliveryPublisher;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyTaskMapper;
import org.fjnu305.acm01.module.subscription.entity.ContestSubscriptionEntity;
import org.fjnu305.acm01.module.subscription.mapper.ContestSubscriptionMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyScanService {

    private final NotifyTaskMapper notifyTaskMapper;
    private final ContestSubscriptionMapper subscriptionMapper;
    private final NotifyDeliveryPublisher deliveryPublisher;
    private final NotifyProperties notifyProperties;
    private final NotifyTaskClaimService notifyTaskClaimService;

    public void scanAndDispatch() {
        int reclaimed = notifyTaskClaimService.reclaimStale(notifyProperties.getStaleProcessingSeconds());
        if (reclaimed > 0) {
            log.warn("Reclaimed {} stale PROCESSING notify task(s)", reclaimed);
        }
        int batchSize = notifyProperties.getScanBatchSize();
        List<NotifyTaskEntity> claimed = notifyTaskClaimService.claimDueTasks(batchSize);
        if (claimed.isEmpty()) {
            return;
        }

        Map<Long, List<NotifyTaskEntity>> grouped = groupByUser(claimed);
        log.info("Notify scan dispatching {} user batch(es) from {} claimed task(s)",
                grouped.size(), claimed.size());

        long interval = notifyProperties.getSendIntervalMillis();
        for (Map.Entry<Long, List<NotifyTaskEntity>> entry : grouped.entrySet()) {
            List<Long> ids = entry.getValue().stream().map(NotifyTaskEntity::getId).toList();
            try {
                deliveryPublisher.publish(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("Failed to dispatch notify batch for user {}, releasing claim", entry.getKey(), e);
                notifyTaskMapper.releaseToPending(ids);
            }
            sleepInterval(interval);
        }
    }

    private Map<Long, List<NotifyTaskEntity>> groupByUser(List<NotifyTaskEntity> tasks) {
        Map<Long, List<NotifyTaskEntity>> grouped = new LinkedHashMap<>();
        List<Long> orphanIds = new ArrayList<>();
        List<Long> subscriptionIds = tasks.stream()
                .map(NotifyTaskEntity::getSubscriptionId)
                .distinct()
                .toList();
        Map<Long, ContestSubscriptionEntity> subscriptions = new LinkedHashMap<>();
        if (!subscriptionIds.isEmpty()) {
            for (ContestSubscriptionEntity subscription : subscriptionMapper.selectByIds(subscriptionIds)) {
                subscriptions.put(subscription.getId(), subscription);
            }
        }
        for (NotifyTaskEntity task : tasks) {
            ContestSubscriptionEntity subscription = subscriptions.get(task.getSubscriptionId());
            if (subscription == null) {
                orphanIds.add(task.getId());
                continue;
            }
            grouped.computeIfAbsent(subscription.getUserId(), ignored -> new ArrayList<>()).add(task);
        }
        if (!orphanIds.isEmpty()) {
            notifyTaskMapper.markDeadBatch(orphanIds, "Subscription not found");
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
