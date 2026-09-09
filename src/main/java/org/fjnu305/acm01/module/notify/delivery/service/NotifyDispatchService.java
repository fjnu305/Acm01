package org.fjnu305.acm01.module.notify.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.NotifyTaskStatus;
import org.fjnu305.acm01.module.notify.config.NotifyProperties;
import org.fjnu305.acm01.module.notify.delivery.emailhandler.EmailNotifyHandler;
import org.fjnu305.acm01.module.notify.delivery.support.NotifyReminderLoader;
import org.fjnu305.acm01.module.notify.delivery.websockethandler.WebSocketNotifyHandler;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyTaskMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyDispatchService {

    private static final String RATE_KEY_PREFIX = "notify:rate:";

    private final NotifyTaskMapper notifyTaskMapper;
    private final EmailNotifyHandler emailNotifyHandler;
    private final WebSocketNotifyHandler webSocketNotifyHandler;
    private final NotifyReminderLoader reminderLoader;
    private final NotifyProperties properties;
    private final StringRedisTemplate stringRedisTemplate;

    public void deliver(Long userId, List<NotifyTaskEntity> tasks) {
        try {
            acquireRateLimit(userId);
            processUserBatch(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Notify delivery interrupted for user {}", userId, e);
        }
    }

    /**
     * Channel I/O stays outside a DB transaction. Status writes are per-row.
     */
    public void processUserBatch(List<NotifyTaskEntity> tasks) {
        List<NotifyTaskEntity> actionable = new ArrayList<>();
        for (NotifyTaskEntity task : tasks) {
            NotifyTaskEntity fresh = notifyTaskMapper.selectById(task.getId());
            if (fresh == null) {
                continue;
            }
            String status = fresh.getStatus();
            if (NotifyTaskStatus.PENDING.getValue().equals(status)
                    || NotifyTaskStatus.FAILED.getValue().equals(status)
                    || NotifyTaskStatus.PROCESSING.getValue().equals(status)) {
                actionable.add(fresh);
            }
        }
        if (actionable.isEmpty()) {
            return;
        }

        Long userId = reminderLoader.resolveUserId(actionable.get(0));
        if (userId == null) {
            for (NotifyTaskEntity task : actionable) {
                handleFailure(task, "Subscription or user not found");
            }
            return;
        }

        boolean success;
        String error = null;
        try {
            success = webSocketNotifyHandler.sendMerged(actionable);
            if (!success) {
                success = emailNotifyHandler.sendMerged(actionable);
            }
            if (!success) {
                error = "No delivery channel succeeded";
            }
        } catch (Exception e) {
            log.error("Notify batch failed for user {}", userId, e);
            success = false;
            error = e.getMessage();
        }

        if (success) {
            for (NotifyTaskEntity task : actionable) {
                int updated = notifyTaskMapper.markSent(task.getId());
                if (updated == 0) {
                    log.warn("Task {} already processed, skip duplicate send", task.getId());
                }
            }
        } else {
            for (NotifyTaskEntity task : actionable) {
                handleFailure(task, error);
            }
        }
    }

    private void acquireRateLimit(Long userId) throws InterruptedException {
        while (!tryAcquireRateLimit(userId)) {
            log.debug("Rate limit wait for user {}", userId);
            Thread.sleep(1000);
        }
    }

    private boolean tryAcquireRateLimit(Long userId) {
        String key = RATE_KEY_PREFIX + userId;
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(properties.getRateLimit().getUserMinIntervalSeconds()));
        return Boolean.TRUE.equals(acquired);
    }

    private void handleFailure(NotifyTaskEntity task, String error) {
        int maxRetries = properties.getMaxRetries();
        if (task.getRetryCount() < maxRetries) {
            int backoff = backoffSeconds(task.getRetryCount());
            notifyTaskMapper.incrementRetryWithBackoff(task.getId(), truncate(error), backoff);
        } else {
            notifyTaskMapper.markDead(task.getId(), truncate(error));
        }
    }

    private int backoffSeconds(int retryCount) {
        int base = Math.max(1, properties.getRetry().getBaseDelaySeconds());
        long delay = (long) base * (1L << Math.min(retryCount, 10));
        return (int) Math.min(delay, 3600);
    }

    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
