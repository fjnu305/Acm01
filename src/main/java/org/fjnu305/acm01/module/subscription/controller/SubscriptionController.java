package org.fjnu305.acm01.module.subscription.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.subscription.dto.SubscribeRequest;
import org.fjnu305.acm01.module.subscription.service.SubscriptionService;
import org.fjnu305.acm01.module.subscription.vo.SubscriptionVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 赛事订阅 API：订阅、取消、查询我的订阅列表。
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public Result<List<SubscriptionVO>> subscribe(@AuthenticationPrincipal LoginUser loginUser,
                                                    @Valid @RequestBody SubscribeRequest request) {
        return Result.success(subscriptionService.subscribe(loginUser.getUserId(), request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> cancel(@AuthenticationPrincipal LoginUser loginUser,
                               @PathVariable Long id) {
        subscriptionService.cancel(loginUser.getUserId(), id);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<SubscriptionVO>> mySubscriptions(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(subscriptionService.listMySubscriptions(loginUser.getUserId()));
    }
}
