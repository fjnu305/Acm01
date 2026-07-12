package org.fjnu305.acm01.module.notify.delivery.emailhandler;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;
import org.fjnu305.acm01.module.contest.query.mapper.ContestQueryMapper;
import org.fjnu305.acm01.module.notify.config.NotifyProperties;
import org.fjnu305.acm01.module.notify.entity.NotifyLogEntity;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyLogMapper;
import org.fjnu305.acm01.module.subscription.entity.ContestSubscriptionEntity;
import org.fjnu305.acm01.module.subscription.mapper.ContestSubscriptionMapper;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotifyHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final UserMapper userMapper;
    private final ContestQueryMapper contestQueryMapper;
    private final ContestSubscriptionMapper subscriptionMapper;
    private final NotifyLogMapper notifyLogMapper;
    private final NotifyProperties properties;

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

        List<MailItem> items = buildMailItems(tasks);
        if (items.isEmpty()) {
            return false;
        }

        MailItem first = items.get(0);
        UserEntity user = first.user();
        String subject = buildSubject(items);
        String body = buildMergedBody(items);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.getEmail().getFrom());
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(mimeMessage);
            for (MailItem item : items) {
                writeLog(item.task(), user.getId(), user.getEmail(), "SUCCESS", null);
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to send merged email for user {}", user.getId(), e);
            for (MailItem item : items) {
                writeLog(item.task(), user.getId(), user.getEmail(), "FAILED", e.getMessage());
            }
            return false;
        }
    }

    private List<MailItem> buildMailItems(List<NotifyTaskEntity> tasks) {
        List<MailItem> items = new ArrayList<>();
        for (NotifyTaskEntity task : tasks) {
            ContestSubscriptionEntity subscription = subscriptionMapper.selectById(task.getSubscriptionId());
            if (subscription == null) {
                writeLog(task, null, null, "FAILED", "Subscription not found");
                continue;
            }
            UserEntity user = userMapper.selectById(subscription.getUserId());
            if (user == null || !StringUtils.hasText(user.getEmail())) {
                writeLog(task, subscription.getUserId(), null, "FAILED", "User email not found");
                continue;
            }
            ContestEntity contest = contestQueryMapper.selectById(subscription.getContestId());
            if (contest == null) {
                writeLog(task, subscription.getUserId(), user.getEmail(), "FAILED", "Contest not found");
                continue;
            }
            if (!items.isEmpty() && !items.get(0).user().getId().equals(user.getId())) {
                writeLog(task, subscription.getUserId(), user.getEmail(), "FAILED", "Batch user mismatch");
                continue;
            }
            items.add(new MailItem(task, user, contest, subscription.getRemindBeforeMinutes()));
        }
        return items;
    }

    private static String buildSubject(List<MailItem> items) {
        if (items.size() == 1) {
            return "[ACMer] \u8d5b\u524d\u63d0\u9192 - " + items.get(0).contest().getTitle();
        }
        return "[ACMer] \u8d5b\u524d\u63d0\u9192 - " + items.size() + " \u573a\u6bd4\u8d5b";
    }

    private static String buildMergedBody(List<MailItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u4f60\u8ba2\u9605\u7684\u6bd4\u8d5b\u5373\u5c06\u5f00\u59cb");
        if (items.size() > 1) {
            sb.append("\uff08\u5171 ").append(items.size()).append(" \u6761\u63d0\u9192\uff09");
        }
        sb.append("\n\n");
        for (int i = 0; i < items.size(); i++) {
            MailItem item = items.get(i);
            if (items.size() > 1) {
                sb.append("--- \u7b2c ").append(i + 1).append(" \u6761 ---\n");
            }
            sb.append(buildSection(item.contest(), toRemindLabel(item.remindMinutes())));
            if (i < items.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static String buildSection(ContestEntity contest, String remindLabel) {
        String url = contest.getUrl() != null ? contest.getUrl() : "\u2014";
        return String.format(
                "\u5e73\u53f0\uff1a%s%n"
                        + "\u8d5b\u4e8b\uff1a%s%n"
                        + "\u5f00\u59cb\u65f6\u95f4\uff1a%s%n"
                        + "\u63d0\u9192\uff1a\u8d5b\u524d\u7ea6 %s%n"
                        + "\u94fe\u63a5\uff1a%s",
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
        logEntity.setChannel("EMAIL");
        logEntity.setTarget(target);
        logEntity.setStatus(status);
        logEntity.setErrorMessage(error);
        notifyLogMapper.insert(logEntity);
    }

    private record MailItem(NotifyTaskEntity task, UserEntity user, ContestEntity contest, int remindMinutes) {
    }
}
