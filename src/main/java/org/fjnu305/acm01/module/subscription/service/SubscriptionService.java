package org.fjnu305.acm01.module.subscription.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;
import org.fjnu305.acm01.module.contest.query.mapper.ContestQueryMapper;
import org.fjnu305.acm01.module.notify.writeTask.api.NotifyTaskScheduler;
import org.fjnu305.acm01.module.notify.writeTask.dto.NotifyScheduleCommand;
import org.fjnu305.acm01.module.subscription.config.SubscriptionProperties;
import org.fjnu305.acm01.module.subscription.dto.SubscribeRequest;
import org.fjnu305.acm01.module.subscription.entity.ContestSubscriptionEntity;
import org.fjnu305.acm01.module.subscription.mapper.ContestSubscriptionMapper;
import org.fjnu305.acm01.module.subscription.vo.SubscriptionVO;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 赛事订阅：管理 {@code contest_subscription}，通过 {@link NotifyTaskScheduler} 登记提醒任务。
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final ContestSubscriptionMapper subscriptionMapper;
    private final ContestQueryMapper contestQueryMapper;
    private final UserMapper userMapper;
    private final NotifyTaskScheduler notifyTaskScheduler;
    private final SubscriptionProperties properties;

    @Transactional
    public List<SubscriptionVO> subscribe(Long userId, SubscribeRequest request) {
        ContestEntity contest = contestQueryMapper.selectById(request.getContestId());
        if (contest == null) {
            throw new BusinessException(ErrorCode.CONTEST_NOT_FOUND);
        }
        if (!contest.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.CONTEST_ALREADY_STARTED);
        }

        validateEmail(userId);

        List<SubscriptionVO> created = new ArrayList<>();
        for (Integer remindMinutes : request.getRemindBeforeMinutes()) {
            if (!properties.getAllowedRemindMinutes().contains(remindMinutes)) {
                throw new BusinessException(ErrorCode.INVALID_REMIND_TIME,
                        "Unsupported remind time: " + remindMinutes);
            }

            LocalDateTime scheduledAt = contest.getStartTime().minusMinutes(remindMinutes);
            if (!scheduledAt.isAfter(LocalDateTime.now())) {
                continue;
            }

            ContestSubscriptionEntity existing = subscriptionMapper.selectAny(
                    userId, request.getContestId(), remindMinutes);
            ContestSubscriptionEntity entity;
            if (existing != null) {
                if (existing.getStatus() == 1) {
                    continue;
                }
                subscriptionMapper.reactivate(existing.getId(), "EMAIL");
                existing.setStatus(1);
                existing.setChannel("EMAIL");
                entity = existing;
            } else {
                entity = new ContestSubscriptionEntity();
                entity.setUserId(userId);
                entity.setContestId(request.getContestId());
                entity.setRemindBeforeMinutes(remindMinutes);
                entity.setChannel("EMAIL");
                entity.setStatus(1);
                subscriptionMapper.insert(entity);
            }

            notifyTaskScheduler.schedule(NotifyScheduleCommand.builder()
                    .subscriptionId(entity.getId())
                    .userId(userId)
                    .contestId(contest.getId())
                    .channel("EMAIL")
                    .remindBeforeMinutes(remindMinutes)
                    .contestStartTime(contest.getStartTime())
                    .build());

            created.add(SubscriptionVO.builder()
                    .id(entity.getId())
                    .contestId(contest.getId())
                    .contestTitle(contest.getTitle())
                    .source(contest.getSource())
                    .contestStartTime(contest.getStartTime())
                    .remindBeforeMinutes(remindMinutes)
                    .channel("EMAIL")
                    .status(1)
                    .createdTime(LocalDateTime.now())
                    .build());
        }

        if (created.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REMIND_TIME,
                    "No valid remind slots (contest may start too soon)");
        }
        return created;
    }

    @Transactional
    public void cancel(Long userId, Long subscriptionId) {
        ContestSubscriptionEntity subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null || !subscription.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
        }
        if (subscription.getStatus() != 1) {
            return;
        }
        int updated = subscriptionMapper.cancel(subscriptionId, userId);
        if (updated > 0) {
            notifyTaskScheduler.cancelBySubscriptionId(subscriptionId);
        }
    }

    public List<SubscriptionVO> listMySubscriptions(Long userId) {
        return subscriptionMapper.selectMyActive(userId);
    }

    private void validateEmail(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_REQUIRED);
        }
    }
}
