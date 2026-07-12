package org.fjnu305.acm01.module.notify.writeTask.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.NotifyTaskStatus;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyTaskMapper;
import org.fjnu305.acm01.module.notify.writeTask.api.NotifyTaskScheduler;
import org.fjnu305.acm01.module.notify.writeTask.dto.NotifyScheduleCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotifyTaskScheduleService implements NotifyTaskScheduler {

    private final NotifyTaskMapper notifyTaskMapper;

    @Override
    @Transactional
    public void schedule(NotifyScheduleCommand command) {
        LocalDateTime scheduledAt = command.getContestStartTime()
                .minusMinutes(command.getRemindBeforeMinutes());
        if (!scheduledAt.isAfter(LocalDateTime.now())) {
            return;
        }

        String idempotentKey = buildIdempotentKey(
                command.getUserId(),
                command.getContestId(),
                command.getChannel(),
                command.getRemindBeforeMinutes());

        NotifyTaskEntity existing = notifyTaskMapper.selectByIdempotentKey(idempotentKey);
        if (existing != null) {
            if (NotifyTaskStatus.CANCELLED.getValue().equals(existing.getStatus())) {
                notifyTaskMapper.reactivate(existing.getId(), command.getSubscriptionId(), scheduledAt);
            }
            return;
        }

        notifyTaskMapper.insert(buildTask(command, scheduledAt, idempotentKey));
    }

    @Override
    @Transactional
    public void cancelBySubscriptionId(Long subscriptionId) {
        notifyTaskMapper.cancelBySubscriptionId(subscriptionId);
    }

    public static String buildIdempotentKey(Long userId, Long contestId, String channel, int remindMinutes) {
        return userId + ":" + contestId + ":" + channel + ":" + remindMinutes;
    }

    private NotifyTaskEntity buildTask(NotifyScheduleCommand command,
                                       LocalDateTime scheduledAt,
                                       String idempotentKey) {
        NotifyTaskEntity task = new NotifyTaskEntity();
        task.setSubscriptionId(command.getSubscriptionId());
        task.setChannel(command.getChannel());
        task.setScheduledAt(scheduledAt);
        task.setStatus(NotifyTaskStatus.PENDING.getValue());
        task.setRetryCount(0);
        task.setIdempotentKey(idempotentKey);
        return task;
    }
}
