package org.fjnu305.acm01.module.notify.writeTask.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.event.ContestScheduleChangedEvent;
import org.fjnu305.acm01.module.notify.writeTask.api.NotifyTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyContestRescheduleListener {

    private final NotifyTaskScheduler notifyTaskScheduler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onContestScheduleChanged(ContestScheduleChangedEvent event) {
        log.info("Rescheduling notify tasks for contest {} start={}",
                event.contestId(), event.newStartTime());
        notifyTaskScheduler.rescheduleByContestId(event.contestId(), event.newStartTime());
    }
}
