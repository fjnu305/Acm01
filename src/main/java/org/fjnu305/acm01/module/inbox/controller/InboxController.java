package org.fjnu305.acm01.module.inbox.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.inbox.dto.SendPersonalMessageDTO;
import org.fjnu305.acm01.module.inbox.service.InboxService;
import org.fjnu305.acm01.module.inbox.vo.ChatMessageVO;
import org.fjnu305.acm01.module.inbox.vo.ConversationSummaryVO;
import org.fjnu305.acm01.module.inbox.vo.InboxMessageVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inbox")
@RequiredArgsConstructor
public class InboxController {

    private final InboxService inboxService;

    @GetMapping
    public Result<PageResult<InboxMessageVO>> list(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(inboxService.list(loginUser.getUserId(), category, pageNum, pageSize));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(inboxService.countUnread(loginUser.getUserId()));
    }

    @GetMapping("/conversations")
    public Result<List<ConversationSummaryVO>> conversations(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(inboxService.listConversations(loginUser.getUserId()));
    }

    @GetMapping("/thread/{peerId}")
    public Result<List<ChatMessageVO>> thread(@AuthenticationPrincipal LoginUser loginUser,
                                              @PathVariable Long peerId,
                                              @RequestParam(defaultValue = "100") int limit) {
        return Result.success(inboxService.listThread(loginUser.getUserId(), peerId, limit));
    }

    @PostMapping("/thread/{peerId}/read")
    public Result<Void> markThreadRead(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long peerId) {
        inboxService.markThreadRead(loginUser.getUserId(), peerId);
        return Result.success();
    }

    @PostMapping("/read-all")
    public Result<Void> markAllRead(@AuthenticationPrincipal LoginUser loginUser) {
        inboxService.markAllRead(loginUser.getUserId());
        return Result.success();
    }

    @PostMapping("/messages")
    public Result<ChatMessageVO> sendPersonal(@AuthenticationPrincipal LoginUser loginUser,
                                              @Valid @RequestBody SendPersonalMessageDTO request) {
        return Result.success(inboxService.sendPersonal(loginUser.getUserId(), request));
    }

    @GetMapping("/{id:\\d+}")
    public Result<InboxMessageVO> detail(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        return Result.success(inboxService.getDetail(loginUser.getUserId(), id));
    }

    @PostMapping("/{id:\\d+}/read")
    public Result<InboxMessageVO> markRead(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        return Result.success(inboxService.markRead(loginUser.getUserId(), id));
    }
}
