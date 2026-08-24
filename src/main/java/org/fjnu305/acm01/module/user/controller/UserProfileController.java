package org.fjnu305.acm01.module.user.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.user.dto.PublicProfileVO;
import org.fjnu305.acm01.module.user.dto.UserSearchVO;
import org.fjnu305.acm01.module.user.service.UserProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/{id}/profile")
    public Result<PublicProfileVO> profile(@PathVariable Long id,
                                           @AuthenticationPrincipal LoginUser loginUser) {
        Long viewerId = loginUser != null ? loginUser.getUserId() : null;
        return Result.success(userProfileService.getPublicProfile(id, viewerId));
    }

    @GetMapping("/search")
    public Result<List<UserSearchVO>> search(@AuthenticationPrincipal LoginUser loginUser,
                                             @RequestParam("q") String keyword,
                                             @RequestParam(defaultValue = "20") int limit) {
        return Result.success(userProfileService.searchUsers(loginUser.getUserId(), keyword, limit));
    }
}
