package org.fjnu305.acm01.module.notify.delivery.emailhandler;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.notify.config.NotifyProperties;
import org.fjnu305.acm01.module.notify.delivery.support.NotifyReminderCopy;
import org.fjnu305.acm01.module.notify.delivery.support.NotifyReminderItem;
import org.fjnu305.acm01.module.notify.delivery.support.NotifyReminderLoader;
import org.fjnu305.acm01.module.notify.entity.NotifyLogEntity;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyLogMapper;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotifyHandler {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final NotifyReminderLoader reminderLoader;
    private final NotifyLogMapper notifyLogMapper;
    private final NotifyProperties properties;

    public boolean sendMerged(List<NotifyTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return false;
        }
        if (!properties.getEmail().isEnabled()) {
            log.warn("Email notify disabled, skip {} task(s)", tasks.size());
            for (NotifyTaskEntity task : tasks) {
                writeLog(task, null, null, "FAILED", "Email notify disabled in config");
            }
            return false;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            for (NotifyTaskEntity task : tasks) {
                writeLog(task, null, null, "FAILED", "JavaMailSender not configured");
            }
            return false;
        }

        List<NotifyReminderItem> items = reminderLoader.load(tasks, true, (task, reason) ->
                writeLog(task, null, null, "FAILED", reason));
        if (items.isEmpty()) {
            return false;
        }

        NotifyReminderItem first = items.get(0);
        UserEntity user = first.user();
        String subject = items.size() == 1
                ? "[ACMer] 赛前提醒 - " + first.contest().getTitle()
                : "[ACMer] 赛前提醒 - " + items.size() + " 场比赛";
        String body = buildMergedBody(items);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.getEmail().getFrom());
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(mimeMessage);
            for (NotifyReminderItem item : items) {
                writeLog(item.task(), user.getId(), user.getEmail(), "SUCCESS", null);
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to send merged email for user {}", user.getId(), e);
            for (NotifyReminderItem item : items) {
                writeLog(item.task(), user.getId(), user.getEmail(), "FAILED", e.getMessage());
            }
            return false;
        }
    }

    private static String buildMergedBody(List<NotifyReminderItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("你订阅的比赛即将开始");
        if (items.size() > 1) {
            sb.append("（共 ").append(items.size()).append(" 条提醒）");
        }
        sb.append("\n\n");
        for (int i = 0; i < items.size(); i++) {
            NotifyReminderItem item = items.get(i);
            if (items.size() > 1) {
                sb.append("--- 第 ").append(i + 1).append(" 条 ---\n");
            }
            sb.append(NotifyReminderCopy.section(item.contest(), NotifyReminderCopy.remindLabel(item.remindMinutes())));
            if (i < items.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private void writeLog(NotifyTaskEntity task, Long userId, String target, String status, String error) {
        NotifyLogEntity logEntity = new NotifyLogEntity();
        logEntity.setNotifyTaskId(task.getId());
        logEntity.setUserId(userId);
        logEntity.setChannel("EMAIL");
        logEntity.setTarget(target);
        logEntity.setStatus(status);
        logEntity.setErrorMessage(error);
        notifyLogMapper.insert(logEntity);
    }
}
