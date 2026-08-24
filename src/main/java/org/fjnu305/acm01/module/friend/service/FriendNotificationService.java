package org.fjnu305.acm01.module.friend.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.friend.entity.FriendRequestEntity;
import org.fjnu305.acm01.module.inbox.constant.InboxRefType;
import org.fjnu305.acm01.module.inbox.service.InboxDeliveryService;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.support.UserDisplayNameResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FriendNotificationService {

    private final InboxDeliveryService inboxDeliveryService;
    private final UserDisplayNameResolver displayNames;

    public void notifyRequestSent(UserEntity requester, FriendRequestEntity request) {
        inboxDeliveryService.deliverWithRef(
                requester.getId(),
                request.getAddresseeId(),
                displayNames.resolve(requester) + " 请求添加你为好友",
                StringUtils.hasText(request.getMessage()) ? request.getMessage() : "一起来刷题、组队吧！",
                InboxRefType.FRIEND_REQUEST,
                request.getId());
    }

    public void notifyRequestAccepted(UserEntity addressee, Long requesterId, Long requestId) {
        inboxDeliveryService.deliverWithRef(
                addressee.getId(),
                requesterId,
                displayNames.resolve(addressee) + " 已同意你的好友申请",
                "你们已成为好友，可以互发消息了。",
                InboxRefType.FRIEND_ACCEPTED,
                requestId);
    }
}
