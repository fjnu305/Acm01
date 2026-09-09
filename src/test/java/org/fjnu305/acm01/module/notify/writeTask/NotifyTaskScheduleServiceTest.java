package org.fjnu305.acm01.module.notify.writeTask;

import org.fjnu305.acm01.Common.enums.NotifyTaskStatus;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyTaskMapper;
import org.fjnu305.acm01.module.notify.writeTask.dto.NotifyScheduleCommand;
import org.fjnu305.acm01.module.notify.writeTask.service.NotifyTaskScheduleService;
import org.fjnu305.acm01.module.subscription.entity.ContestSubscriptionEntity;
import org.fjnu305.acm01.module.subscription.mapper.ContestSubscriptionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyTaskScheduleServiceTest {

    @Mock
    private NotifyTaskMapper notifyTaskMapper;

    @Mock
    private ContestSubscriptionMapper subscriptionMapper;

    @InjectMocks
    private NotifyTaskScheduleService scheduleService;

    @Test
    void schedule_existingPending_updatesScheduledAt() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime oldAt = start.minusMinutes(1440).minusHours(3);
        NotifyTaskEntity existing = new NotifyTaskEntity();
        existing.setId(9L);
        existing.setStatus(NotifyTaskStatus.PENDING.getValue());
        existing.setScheduledAt(oldAt);
        when(notifyTaskMapper.selectByIdempotentKey(any())).thenReturn(existing);

        scheduleService.schedule(NotifyScheduleCommand.builder()
                .subscriptionId(1L)
                .userId(2L)
                .contestId(3L)
                .channel("EMAIL")
                .remindBeforeMinutes(1440)
                .contestStartTime(start)
                .build());

        verify(notifyTaskMapper).updateScheduledAt(eq(9L), eq(start.minusMinutes(1440)));
        verify(notifyTaskMapper, never()).insert(any());
    }

    @Test
    void rescheduleByContestId_cancelsWhenNewTimeAlreadyPassed() {
        ContestSubscriptionEntity sub = new ContestSubscriptionEntity();
        sub.setId(11L);
        sub.setRemindBeforeMinutes(60);
        when(subscriptionMapper.selectActiveByContestId(5L)).thenReturn(List.of(sub));

        NotifyTaskEntity pending = new NotifyTaskEntity();
        pending.setId(21L);
        pending.setStatus(NotifyTaskStatus.PENDING.getValue());
        when(notifyTaskMapper.selectPendingBySubscriptionId(11L)).thenReturn(List.of(pending));

        scheduleService.rescheduleByContestId(5L, LocalDateTime.now().minusMinutes(5));

        verify(notifyTaskMapper).cancelById(21L);
        verify(notifyTaskMapper, never()).updateScheduledAt(any(), any());
    }
}
