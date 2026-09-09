package org.fjnu305.acm01.module.notify.delivery.support;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;
import org.fjnu305.acm01.module.contest.query.mapper.ContestQueryMapper;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.subscription.entity.ContestSubscriptionEntity;
import org.fjnu305.acm01.module.subscription.mapper.ContestSubscriptionMapper;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
@RequiredArgsConstructor
public class NotifyReminderLoader {

    private final ContestSubscriptionMapper subscriptionMapper;
    private final UserMapper userMapper;
    private final ContestQueryMapper contestQueryMapper;

    public Long resolveUserId(NotifyTaskEntity task) {
        if (task == null || task.getSubscriptionId() == null) {
            return null;
        }
        ContestSubscriptionEntity subscription = subscriptionMapper.selectById(task.getSubscriptionId());
        return subscription == null ? null : subscription.getUserId();
    }

    public List<NotifyReminderItem> load(List<NotifyTaskEntity> tasks,
                                         boolean requireEmail,
                                         BiConsumer<NotifyTaskEntity, String> onSkip) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }
        List<Long> subscriptionIds = tasks.stream().map(NotifyTaskEntity::getSubscriptionId).distinct().toList();
        Map<Long, ContestSubscriptionEntity> subscriptions = indexSubscriptions(subscriptionIds);

        List<Long> userIds = subscriptions.values().stream().map(ContestSubscriptionEntity::getUserId).distinct().toList();
        Map<Long, UserEntity> users = indexUsers(userIds);

        List<Long> contestIds = subscriptions.values().stream()
                .map(ContestSubscriptionEntity::getContestId)
                .distinct()
                .toList();
        Map<Long, ContestEntity> contests = indexContests(contestIds);

        List<NotifyReminderItem> items = new ArrayList<>();
        for (NotifyTaskEntity task : tasks) {
            ContestSubscriptionEntity subscription = subscriptions.get(task.getSubscriptionId());
            if (subscription == null) {
                onSkip.accept(task, "Subscription not found");
                continue;
            }
            UserEntity user = users.get(subscription.getUserId());
            if (user == null) {
                onSkip.accept(task, requireEmail ? "User email not found" : "User not found");
                continue;
            }
            if (requireEmail && !StringUtils.hasText(user.getEmail())) {
                onSkip.accept(task, "User email not found");
                continue;
            }
            ContestEntity contest = contests.get(subscription.getContestId());
            if (contest == null) {
                onSkip.accept(task, "Contest not found");
                continue;
            }
            if (!items.isEmpty() && !items.get(0).user().getId().equals(user.getId())) {
                onSkip.accept(task, "Batch user mismatch");
                continue;
            }
            items.add(new NotifyReminderItem(task, user, contest, subscription.getRemindBeforeMinutes()));
        }
        return items;
    }

    private Map<Long, ContestSubscriptionEntity> indexSubscriptions(List<Long> ids) {
        Map<Long, ContestSubscriptionEntity> map = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        for (ContestSubscriptionEntity row : subscriptionMapper.selectByIds(ids)) {
            map.put(row.getId(), row);
        }
        return map;
    }

    private Map<Long, UserEntity> indexUsers(List<Long> ids) {
        Map<Long, UserEntity> map = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        for (UserEntity row : userMapper.selectByIds(ids)) {
            map.put(row.getId(), row);
        }
        return map;
    }

    private Map<Long, ContestEntity> indexContests(List<Long> ids) {
        Map<Long, ContestEntity> map = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        for (ContestEntity row : contestQueryMapper.selectByIds(ids)) {
            map.put(row.getId(), row);
        }
        return map;
    }
}
