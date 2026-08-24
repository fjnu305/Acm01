package org.fjnu305.acm01.module.inbox.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.util.PageParams;
import org.fjnu305.acm01.module.friend.service.FriendService;
import org.fjnu305.acm01.module.friend.service.OfficialUserService;
import org.fjnu305.acm01.module.inbox.constant.InboxCategory;
import org.fjnu305.acm01.module.inbox.mapper.InboxMessageMapper;
import org.fjnu305.acm01.module.inbox.vo.ChatMessageVO;
import org.fjnu305.acm01.module.inbox.vo.ConversationSummaryVO;
import org.fjnu305.acm01.module.inbox.vo.InboxMessageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InboxQueryService {

    private final InboxMessageMapper inboxMessageMapper;
    private final FriendService friendService;
    private final OfficialUserService officialUserService;

    public PageResult<InboxMessageVO> list(Long recipientId, String category, int pageNum, int pageSize) {
        PageParams page = PageParams.of(pageNum, pageSize);
        Long officialUserId = officialUserService.requireOfficialUserId();
        String normalized = InboxCategory.normalize(category);
        List<InboxMessageVO> list = inboxMessageMapper.selectPage(
                recipientId, normalized, officialUserId, page.offset(), page.size());
        long total = inboxMessageMapper.countPage(recipientId, normalized, officialUserId);
        return PageResult.of(list, total, page.page(), page.size());
    }

    public long countUnread(Long recipientId) {
        return inboxMessageMapper.countUnread(recipientId);
    }

    public InboxMessageVO getDetail(Long recipientId, Long messageId) {
        InboxMessageVO detail = inboxMessageMapper.selectDetail(
                messageId, recipientId, officialUserService.requireOfficialUserId());
        if (detail == null) {
            throw new BusinessException(ErrorCode.INBOX_NOT_FOUND);
        }
        return detail;
    }

    @Transactional
    public InboxMessageVO markRead(Long recipientId, Long messageId) {
        inboxMessageMapper.markRead(messageId, recipientId);
        return getDetail(recipientId, messageId);
    }

    @Transactional
    public void markAllRead(Long recipientId) {
        inboxMessageMapper.markAllRead(recipientId);
    }

    public List<ChatMessageVO> listThread(Long viewerId, Long peerId, int limit) {
        if (!friendService.areFriends(viewerId, peerId)
                && !inboxMessageMapper.hasThreadBetween(viewerId, peerId)) {
            throw new BusinessException(ErrorCode.NOT_FRIENDS);
        }
        int capped = Math.min(Math.max(limit, 1), 200);
        return inboxMessageMapper.selectThread(
                viewerId, peerId, officialUserService.requireOfficialUserId(), capped);
    }

    public List<ConversationSummaryVO> listConversations(Long userId) {
        return inboxMessageMapper.selectConversations(userId, officialUserService.requireOfficialUserId());
    }

    @Transactional
    public void markThreadRead(Long viewerId, Long peerId) {
        inboxMessageMapper.markThreadRead(viewerId, peerId);
    }
}
