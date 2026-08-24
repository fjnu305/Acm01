package org.fjnu305.acm01.module.inbox.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.inbox.entity.InboxMessageEntity;
import org.fjnu305.acm01.module.inbox.vo.ChatMessageVO;
import org.fjnu305.acm01.module.inbox.vo.ConversationSummaryVO;
import org.fjnu305.acm01.module.inbox.vo.InboxMessageVO;

import java.util.List;

@Mapper
public interface InboxMessageMapper {

    int insert(InboxMessageEntity entity);

    InboxMessageEntity selectById(@Param("id") Long id);

    InboxMessageVO selectDetail(@Param("id") Long id,
                                @Param("recipientId") Long recipientId,
                                @Param("officialUserId") Long officialUserId);

    List<InboxMessageVO> selectPage(@Param("recipientId") Long recipientId,
                                    @Param("category") String category,
                                    @Param("officialUserId") Long officialUserId,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    long countPage(@Param("recipientId") Long recipientId,
                   @Param("category") String category,
                   @Param("officialUserId") Long officialUserId);

    long countUnread(@Param("recipientId") Long recipientId);

    int markRead(@Param("id") Long id, @Param("recipientId") Long recipientId);

    int markAllRead(@Param("recipientId") Long recipientId);

    List<ChatMessageVO> selectThread(@Param("viewerId") Long viewerId,
                                     @Param("peerId") Long peerId,
                                     @Param("officialUserId") Long officialUserId,
                                     @Param("limit") int limit);

    ChatMessageVO selectThreadMessage(@Param("id") Long id,
                                      @Param("viewerId") Long viewerId,
                                      @Param("officialUserId") Long officialUserId);

    List<ConversationSummaryVO> selectConversations(@Param("userId") Long userId,
                                                    @Param("officialUserId") Long officialUserId);

    int markThreadRead(@Param("viewerId") Long viewerId, @Param("peerId") Long peerId);

    boolean hasThreadBetween(@Param("viewerId") Long viewerId, @Param("peerId") Long peerId);
}
