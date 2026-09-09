package org.fjnu305.acm01.module.notify.delivery.websockethandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.notify.delivery.support.NotifyReminderCopy;
import org.fjnu305.acm01.module.notify.delivery.support.NotifyReminderItem;
import org.fjnu305.acm01.module.notify.delivery.support.NotifyReminderLoader;
import org.fjnu305.acm01.module.notify.entity.NotifyLogEntity;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyLogMapper;
import org.fjnu305.acm01.module.websocket.api.RealtimePushService;
import org.fjnu305.acm01.module.websocket.dto.PushMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotifyHandler {

    private final RealtimePushService realtimePushService;
    private final NotifyReminderLoader reminderLoader;
    private final NotifyLogMapper notifyLogMapper;

    public boolean sendMerged(List<NotifyTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return false;
        }

        List<NotifyReminderItem> items = reminderLoader.load(tasks, false, (task, reason) ->
                writeLog(task, null, null, "FAILED", reason));
        if (items.isEmpty()) {
            return false;
        }

        Long userId = items.get(0).user().getId();
        if (!realtimePushService.isUserOnline(userId)) {
            log.debug("User {} not online, WebSocket notify skipped for {} task(s)", userId, tasks.size());
            for (NotifyTaskEntity task : tasks) {
                writeLog(task, userId, "OFFLINE", "SKIPPED", "User not online");
            }
            return false;
        }

        PushMessage message = buildPushMessage(items);
        try {
            realtimePushService.pushToUser(userId, message);
            for (NotifyReminderItem item : items) {
                writeLog(item.task(), userId, "WEBSOCKET", "SUCCESS", null);
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to push WebSocket notify for user {}", userId, e);
            for (NotifyReminderItem item : items) {
                writeLog(item.task(), userId, "WEBSOCKET", "FAILED", e.getMessage());
            }
            return false;
        }
    }

    private PushMessage buildPushMessage(List<NotifyReminderItem> items) {
        NotifyReminderItem first = items.get(0);
        String title;
        String body;
        if (items.size() == 1) {
            title = "赛前提醒 - " + first.contest().getTitle();
            body = NotifyReminderCopy.section(first.contest(), NotifyReminderCopy.remindLabel(first.remindMinutes()));
        } else {
            title = "赛前提醒 - " + items.size() + " 场比赛";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                NotifyReminderItem item = items.get(i);
                if (i > 0) {
                    sb.append("\n\n");
                }
                sb.append("--- 第 ").append(i + 1).append(" 场 ---\n");
                sb.append(NotifyReminderCopy.section(item.contest(), NotifyReminderCopy.remindLabel(item.remindMinutes())));
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
}
