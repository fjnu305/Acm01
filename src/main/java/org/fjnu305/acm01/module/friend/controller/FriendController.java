package org.fjnu305.acm01.module.friend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.friend.dto.SendFriendRequestDTO;
import org.fjnu305.acm01.module.friend.service.FriendService;
import org.fjnu305.acm01.module.friend.vo.FriendRequestVO;
import org.fjnu305.acm01.module.friend.vo.FriendVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public Result<List<FriendVO>> listFriends(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(friendService.listFriends(loginUser.getUserId()));
    }

    @GetMapping("/requests/incoming")
    public Result<List<FriendRequestVO>> incoming(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(friendService.listIncoming(loginUser.getUserId()));
    }

    @GetMapping("/requests/outgoing")
    public Result<List<FriendRequestVO>> outgoing(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(friendService.listOutgoing(loginUser.getUserId()));
    }

    @PostMapping("/requests")
    public Result<FriendRequestVO> sendRequest(@AuthenticationPrincipal LoginUser loginUser,
                                               @Valid @RequestBody SendFriendRequestDTO request) {
        return Result.success(friendService.sendRequest(loginUser.getUserId(), request));
    }

    @PostMapping("/requests/{id}/accept")
    public Result<Void> accept(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        friendService.acceptRequest(id, loginUser.getUserId());
        return Result.success();
    }

    @PostMapping("/requests/{id}/reject")
    public Result<Void> reject(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        friendService.rejectRequest(id, loginUser.getUserId());
        return Result.success();
    }

    @DeleteMapping("/requests/{id}")
    public Result<Void> cancel(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        friendService.cancelRequest(id, loginUser.getUserId());
        return Result.success();
    }

    @DeleteMapping("/{userId}")
    public Result<Void> removeFriend(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long userId) {
        friendService.removeFriend(loginUser.getUserId(), userId);
        return Result.success();
    }
}
