package org.fjnu305.acm01.module.notify.delivery.websockethandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;
import org.fjnu305.acm01.module.contest.query.mapper.ContestQueryMapper;
import org.fjnu305.acm01.module.notify.entity.NotifyLogEntity;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyLogMapper;
import org.fjnu305.acm01.module.subscription.entity.ContestSubscriptionEntity;
import org.fjnu305.acm01.module.subscription.mapper.ContestSubscriptionMapper;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.fjnu305.acm01.module.websocket.api.RealtimePushService;
import org.fjnu305.acm01.module.websocket.dto.PushMessage;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotifyHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final RealtimePushService realtimePushService;
    private final UserMapper userMapper;
    private final ContestQueryMapper contestQueryMapper;
    private final ContestSubscriptionMapper subscriptionMapper;
    private final NotifyLogMapper notifyLogMapper;

    public Long resolveUserId(NotifyTaskEntity task) {
        ContestSubscriptionEntity subscription = subscriptionMapper.selectById(task.getSubscriptionId());
        return subscription != null ? subscription.getUserId() : null;
    }

    public boolean send(NotifyTaskEntity task) {
        return sendMerged(List.of(task));
    }

    public boolean sendMerged(List<NotifyTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return false;
        }

        List<PushItem> items = buildPushItems(tasks);
        if (items.isEmpty()) {
            return false;
        }

        Long userId = items.get(0).user().getId();
        if (!realtimePushService.isUserOnline(userId)) {
            log.debug("User {} not online, WebSocket notify skipped for {} task(s)", userId, tasks.size());
            for (NotifyTaskEntity task : tasks) {
                writeLog(task, userId, "OFFLINE", "SKIPPED", "User not online");
            }
            return true;
        }

        PushMessage message = buildPushMessage(items);
        try {
            realtimePushService.pushToUser(userId, message);
            for (PushItem item : items) {
                writeLog(item.task(), userId, "WEBSOCKET", "SUCCESS", null);
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to push WebSocket notify for user {}", userId, e);
            for (PushItem item : items) {
                writeLog(item.task(), userId, "WEBSOCKET", "FAILED", e.getMessage());
            }
            return false;
        }
    }

    private List<PushItem> buildPushItems(List<NotifyTaskEntity> tasks) {
        List<PushItem> items = new ArrayList<>();
        for (NotifyTaskEntity task : tasks) {
            ContestSubscriptionEntity subscription = subscriptionMapper.selectById(task.getSubscriptionId());
            if (subscription == null) {
                writeLog(task, null, null, "FAILED", "Subscription not found");
                continue;
            }
            UserEntity user = userMapper.selectById(subscription.getUserId());
            if (user == null) {
                writeLog(task, subscription.getUserId(), null, "FAILED", "User not found");
                continue;
            }
            ContestEntity contest = contestQueryMapper.selectById(subscription.getContestId());
            if (contest == null) {
                writeLog(task, subscription.getUserId(), "WEBSOCKET", "FAILED", "Contest not found");
                continue;
            }
            if (!items.isEmpty() && !items.get(0).user().getId().equals(user.getId())) {
                writeLog(task, subscription.getUserId(), "WEBSOCKET", "FAILED", "Batch user mismatch");
                continue;
            }
            items.add(new PushItem(task, user, contest, subscription.getRemindBeforeMinutes()));
        }
        return items;
    }

    private PushMessage buildPushMessage(List<PushItem> items) {
        PushItem first = items.get(0);
        String title;
        String body;
        if (items.size() == 1) {
            title = "\u8d5b\u524d\u63d0\u9192 - " + first.contest().getTitle();
            body = buildSection(first.contest(), toRemindLabel(first.remindMinutes()));
        } else {
            title = "\u8d5b\u524d\u63d0\u9192 - " + items.size() + " \u573a\u6bd4\u8d5b";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                PushItem item = items.get(i);
                if (i > 0) {
                    sb.append("\n\n");
                }
                sb.append("--- \u7b2c ").append(i + 1).append(" \u573a ---\n");
                sb.append(buildSection(item.contest(), toRemindLabel(item.remindMinutes())));
            }
            body = sb.toString();
        }

        return PushMessage.builder()
                .type("NOTIFY_REMINDER")
                .title(title)
                .body(body)
                .contestId(first.contest().getId())
                .contestTitle(first.contest().getTitle())
                .contestUrl(first.contest().getUrl())
                .build();
    }

    private static String buildSection(ContestEntity contest, String remindLabel) {
        String url = contest.getUrl() != null ? contest.getUrl() : "\u2014";
        return String.format(
                "\u5e73\u53f0\uff1a%s%n\u8d5b\u4e8b\uff1a%s%n\u5f00\u59cb\u65f6\u95f4\uff1a%s%n\u63d0\u9192\uff1a\u8d5b\u524d\u7ea6 %s%n\u94fe\u63a5\uff1a%s",
                contest.getSource(),
                contest.getTitle(),
                contest.getStartTime().format(FORMATTER),
                remindLabel,
                url);
    }

    private static String toRemindLabel(int remindMinutes) {
        if (remindMinutes >= 1440) {
            return "24 \u5c0f\u65f6";
        }
        if (remindMinutes >= 60) {
            return "1 \u5c0f\u65f6";
        }
        return remindMinutes + " \u5206\u949f";
    }

    private void writeLog(NotifyTaskEntity task, Long userId, String target, String status, String error) {
        NotifyLogEntity logEntity = new NotifyLogEntity();
        logEntity.setNotifyTaskId(task.getId());
        logEntity.setUserId(userId);
        logEntity.setChannel("WEBSOCKET");
        logEntity.setTarget(target);
        logEntity.setStatus(status);
        logEntity.setErrorMessage(error);
        notifyLogMapper.insert(logEntity);
    }

    private record PushItem(NotifyTaskEntity task, UserEntity user, ContestEntity contest, int remindMinutes) {
    }
}