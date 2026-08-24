package org.fjnu305.acm01.module.social.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.social.service.FollowService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    public Result<Void> follow(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long userId) {
        followService.follow(loginUser.getUserId(), userId);
        return Result.success();
    }

    @DeleteMapping("/{userId}")
    public Result<Void> unfollow(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long userId) {
        followService.unfollow(loginUser.getUserId(), userId);
        return Result.success();
    }

    @GetMapping("/following")
    public Result<List<Long>> following(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(followService.listFollowing(loginUser.getUserId()));
    }
}
