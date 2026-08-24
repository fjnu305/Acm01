package org.fjnu305.acm01.module.inbox.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.module.inbox.dto.BroadcastMessageDTO;
import org.fjnu305.acm01.module.inbox.service.InboxService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/inbox")
@RequiredArgsConstructor
public class AdminInboxController {

    private final InboxService inboxService;

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Integer>> broadcast(@Valid @RequestBody BroadcastMessageDTO request) {
        int delivered = inboxService.broadcast(request);
        return Result.success(Map.of("delivered", delivered));
    }
}
