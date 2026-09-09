package org.fjnu305.acm01.module.notify.delivery.support;

import org.fjnu305.acm01.module.contest.entity.ContestEntity;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.user.entity.UserEntity;

public record NotifyReminderItem(
        NotifyTaskEntity task,
        UserEntity user,
        ContestEntity contest,
        int remindMinutes
) {
}
