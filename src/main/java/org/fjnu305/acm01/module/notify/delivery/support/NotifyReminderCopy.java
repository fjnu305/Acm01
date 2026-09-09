package org.fjnu305.acm01.module.notify.delivery.support;

import org.fjnu305.acm01.module.contest.entity.ContestEntity;

import java.time.format.DateTimeFormatter;

public final class NotifyReminderCopy {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private NotifyReminderCopy() {
    }

    public static String remindLabel(int remindMinutes) {
        if (remindMinutes >= 1440) {
            return "24 小时";
        }
        if (remindMinutes >= 60) {
            return "1 小时";
        }
        return remindMinutes + " 分钟";
    }

    public static String section(ContestEntity contest, String remindLabel) {
        String url = contest.getUrl() != null ? contest.getUrl() : "—";
        return String.format(
                "平台：%s%n赛事：%s%n开始时间：%s%n提醒：赛前约 %s%n链接：%s",
                contest.getSource(),
                contest.getTitle(),
                contest.getStartTime().format(FORMATTER),
                remindLabel,
                url);
    }
}
