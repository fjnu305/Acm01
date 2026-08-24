package org.fjnu305.acm01.module.inbox.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.module.inbox.dto.BroadcastMessageDTO;
import org.fjnu305.acm01.module.inbox.dto.SendPersonalMessageDTO;
import org.fjnu305.acm01.module.inbox.vo.ChatMessageVO;
import org.fjnu305.acm01.module.inbox.vo.ConversationSummaryVO;
import org.fjnu305.acm01.module.inbox.vo.InboxMessageVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收件箱门面：委托 query / chat / broadcast，Controller 仅依赖此类。
 */
@Service
@RequiredArgsConstructor
public class InboxService {

    private final InboxQueryService queryService;
    private final InboxChatService chatService;
    private final InboxBroadcastService broadcastService;

    public PageResult<InboxMessageVO> list(Long recipientId, String category, int pageNum, int pageSize) {
        return queryService.list(recipientId, category, pageNum, pageSize);
    }

    public long countUnread(Long recipientId) {
        return queryService.countUnread(recipientId);
    }

    public InboxMessageVO getDetail(Long recipientId, Long messageId) {
        return queryService.getDetail(recipientId, messageId);
    }

    public InboxMessageVO markRead(Long recipientId, Long messageId) {
        return queryService.markRead(recipientId, messageId);
    }

    public void markAllRead(Long recipientId) {
        queryService.markAllRead(recipientId);
    }

    public ChatMessageVO sendPersonal(Long senderId, SendPersonalMessageDTO request) {
        return chatService.sendPersonal(senderId, request);
    }

    public List<ChatMessageVO> listThread(Long viewerId, Long peerId, int limit) {
        return queryService.listThread(viewerId, peerId, limit);
    }

    public List<ConversationSummaryVO> listConversations(Long userId) {
        return queryService.listConversations(userId);
    }

    public void markThreadRead(Long viewerId, Long peerId) {
        queryService.markThreadRead(viewerId, peerId);
    }

    public int broadcast(BroadcastMessageDTO request) {
        return broadcastService.broadcast(request);
    }
}
