package org.fjnu305.acm01.module.inbox.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.util.TextUtils;
import org.fjnu305.acm01.module.friend.service.OfficialUserService;
import org.fjnu305.acm01.module.inbox.entity.InboxMessageEntity;
import org.fjnu305.acm01.module.inbox.mapper.InboxMessageMapper;
import org.fjnu305.acm01.module.inbox.vo.InboxMessageVO;
import org.fjnu305.acm01.module.websocket.api.RealtimePushService;
import org.fjnu305.acm01.module.websocket.dto.PushMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InboxDeliveryService {

    private final InboxMessageMapper inboxMessageMapper;
    private final RealtimePushService realtimePushService;
    private final OfficialUserService officialUserService;

    public InboxMessageVO deliverPersonal(Long senderId, Long recipientId, String title, String body) {
        return deliverAndPreview(senderId, recipientId, title, body, null, null);
    }

    public InboxMessageVO deliverWithRef(Long senderId,
                                         Long recipientId,
                                         String title,
                                         String body,
                                         String refType,
                                         Long refId) {
        return deliverAndPreview(senderId, recipientId, title, body, refType, refId);
    }

    public boolean deliverBroadcast(Long recipientId, String title, String body) {
        Long officialUserId = officialUserService.requireOfficialUserId();
        if (recipientId == null || officialUserId.equals(recipientId)) {
            return false;
        }
        deliver(build(officialUserId, recipientId, title, body, null, null));
        return true;
    }

    private InboxMessageVO deliverAndPreview(Long senderId,
                                           Long recipientId,
                                           String title,
                                           String body,
                                           String refType,
                                           Long refId) {
        InboxMessageEntity entity = deliver(build(senderId, recipientId, title, body, refType, refId));
        return inboxMessageMapper.selectDetail(
                entity.getId(), recipientId, officialUserService.requireOfficialUserId());
    }

    private InboxMessageEntity deliver(InboxMessageEntity entity) {
        inboxMessageMapper.insert(entity);
        InboxMessageVO preview = inboxMessageMapper.selectDetail(
                entity.getId(),
                entity.getRecipientId(),
                officialUserService.requireOfficialUserId());
        realtimePushService.pushToUser(entity.getRecipientId(), PushMessage.builder()
                .type("INBOX")
                .title(entity.getTitle())
                .body(TextUtils.truncate(entity.getBody(), 120))
                .inboxId(entity.getId())
                .inboxCategory(preview != null ? preview.getCategory() : null)
                .inboxSenderId(entity.getSenderId())
                .inboxRefType(entity.getRefType())
                .build());
        return entity;
    }

    private static InboxMessageEntity build(Long senderId,
                                            Long recipientId,
                                            String title,
                                            String body,
                                            String refType,
                                            Long refId) {
        InboxMessageEntity entity = new InboxMessageEntity();
        entity.setSenderId(senderId);
        entity.setRecipientId(recipientId);
        entity.setTitle(title);
        entity.setBody(body);
        entity.setRefType(refType);
        entity.setRefId(refId);
        return entity;
    }
}
