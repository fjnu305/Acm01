package org.fjnu305.acm01.module.team.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.inbox.constant.InboxRefType;
import org.fjnu305.acm01.module.inbox.service.InboxDeliveryService;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.user.support.UserDisplayNameResolver;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamNotificationService {

    private final InboxDeliveryService inboxDeliveryService;
    private final UserDisplayNameResolver displayNames;

    public void notifyInvite(Long leaderId, Long targetUserId, TeamPostEntity post, Long memberId, String body) {
        inboxDeliveryService.deliverWithRef(
                leaderId,
                targetUserId,
                displayNames.resolve(leaderId) + " 邀请你加入组队「" + post.getTitle() + "」",
                body,
                InboxRefType.TEAM_INVITE,
                memberId);
    }

    public void notifyApplicationSubmitted(Long applicantId, Long leaderId, TeamPostEntity post, Long memberId) {
        inboxDeliveryService.deliverWithRef(
                applicantId,
                leaderId,
                displayNames.resolve(applicantId) + " 申请加入你的组队「" + post.getTitle() + "」",
                "前往队伍详情页审核申请。",
                InboxRefType.TEAM_APPLY,
                memberId);
    }

    public void notifyApplicationApproved(Long leaderId, Long applicantId, TeamPostEntity post) {
        inboxDeliveryService.deliverWithRef(
                leaderId,
                applicantId,
                "你已成功加入组队「" + post.getTitle() + "」",
                displayNames.resolve(leaderId) + " 同意了你的申请。",
                InboxRefType.TEAM_APPLY_ACCEPTED,
                post.getId());
    }

    public void notifyApplicationRejected(Long leaderId, Long applicantId, TeamPostEntity post) {
        inboxDeliveryService.deliverWithRef(
                leaderId,
                applicantId,
                displayNames.resolve(leaderId) + " 拒绝了你的组队申请「" + post.getTitle() + "」",
                "你可以看看其他招募帖，或稍后再试。",
                InboxRefType.TEAM_APPLY_REJECTED,
                post.getId());
    }

    public void notifyApplicationAutoRejectedFull(Long leaderId, Long applicantId, TeamPostEntity post) {
        inboxDeliveryService.deliverWithRef(
                leaderId,
                applicantId,
                "组队「" + post.getTitle() + "」已满员",
                displayNames.resolve(leaderId) + " 的队伍已达到人数上限，你的申请已自动关闭。",
                InboxRefType.TEAM_APPLY_REJECTED,
                post.getId());
    }
}
