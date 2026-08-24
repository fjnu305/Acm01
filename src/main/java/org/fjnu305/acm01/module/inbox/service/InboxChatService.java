package org.fjnu305.acm01.module.inbox.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.friend.service.FriendService;
import org.fjnu305.acm01.module.friend.service.OfficialUserService;
import org.fjnu305.acm01.module.inbox.dto.SendPersonalMessageDTO;
import org.fjnu305.acm01.module.inbox.mapper.InboxMessageMapper;
import org.fjnu305.acm01.module.inbox.vo.ChatMessageVO;
import org.fjnu305.acm01.module.inbox.vo.InboxMessageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InboxChatService {

    private final InboxMessageMapper inboxMessageMapper;
    private final InboxDeliveryService inboxDeliveryService;
    private final FriendService friendService;
    private final OfficialUserService officialUserService;

    @Transactional
    public ChatMessageVO sendPersonal(Long senderId, SendPersonalMessageDTO request) {
        Long recipientId = request.getRecipientId();
        if (recipientId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "recipientId is required");
        }
        if (senderId.equals(recipientId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cannot message yourself");
        }
        friendService.requireFriendship(senderId, recipientId);
        String body = request.getBody() == null ? "" : request.getBody().trim();
        if (body.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息内容不能为空");
        }
        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        InboxMessageVO delivered = inboxDeliveryService.deliverPersonal(senderId, recipientId, title, body);
        ChatMessageVO sent = inboxMessageMapper.selectThreadMessage(
                delivered.getId(), senderId, officialUserService.requireOfficialUserId());
        if (sent == null) {
            throw new BusinessException(ErrorCode.INBOX_NOT_FOUND);
        }
        return sent;
    }
}
