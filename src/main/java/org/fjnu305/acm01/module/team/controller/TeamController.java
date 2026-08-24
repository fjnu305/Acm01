package org.fjnu305.acm01.module.team.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.friend.vo.FriendVO;
import org.fjnu305.acm01.module.team.dto.TeamPublishRequest;
import org.fjnu305.acm01.module.team.service.TeamService;
import org.fjnu305.acm01.module.team.vo.TeamPostDetailVO;
import org.fjnu305.acm01.module.team.vo.TeamPostVO;
import org.fjnu305.acm01.module.team.vo.TeamRecommendVO;
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
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public Result<TeamPostDetailVO> publish(@AuthenticationPrincipal LoginUser loginUser,
                                            @Valid @RequestBody TeamPublishRequest request) {
        return Result.success(teamService.publish(loginUser.getUserId(), request));
    }

    @GetMapping
    public Result<PageResult<TeamPostVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(teamService.list(status, region, pageNum, pageSize));
    }

    @GetMapping("/mine")
    public Result<PageResult<TeamPostVO>> mine(@AuthenticationPrincipal LoginUser loginUser,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(teamService.listMine(loginUser.getUserId(), pageNum, pageSize));
    }

    @PostMapping("/invites/{memberId}/accept")
    public Result<Void> acceptInvite(@AuthenticationPrincipal LoginUser loginUser,
                                     @PathVariable Long memberId) {
        teamService.acceptInvite(memberId, loginUser.getUserId());
        return Result.success();
    }

    @PostMapping("/invites/{memberId}/decline")
    public Result<Void> declineInvite(@AuthenticationPrincipal LoginUser loginUser,
                                      @PathVariable Long memberId) {
        teamService.declineInvite(memberId, loginUser.getUserId());
        return Result.success();
    }

    @PostMapping("/applications/{memberId}/approve")
    public Result<Void> approveApplicationByMemberId(@AuthenticationPrincipal LoginUser loginUser,
                                                     @PathVariable Long memberId) {
        teamService.approveApplicationByMemberId(loginUser.getUserId(), memberId);
        return Result.success();
    }

    @PostMapping("/applications/{memberId}/reject")
    public Result<Void> rejectApplicationByMemberId(@AuthenticationPrincipal LoginUser loginUser,
                                                    @PathVariable Long memberId) {
        teamService.rejectApplicationByMemberId(loginUser.getUserId(), memberId);
        return Result.success();
    }

    @GetMapping("/{id:\\d+}/invitable-friends")
    public Result<List<FriendVO>> invitableFriends(@AuthenticationPrincipal LoginUser loginUser,
                                                   @PathVariable Long id) {
        return Result.success(teamService.listInvitableFriends(id, loginUser.getUserId()));
    }

    @GetMapping("/{id:\\d+}")
    public Result<TeamPostDetailVO> detail(@PathVariable Long id) {
        return Result.success(teamService.getDetail(id));
    }

    @GetMapping("/{id:\\d+}/recommend")
    public Result<List<TeamRecommendVO>> recommend(@AuthenticationPrincipal LoginUser loginUser,
                                                  @PathVariable Long id) {
        return Result.success(teamService.recommend(id, loginUser.getUserId()));
    }

    @PostMapping("/{id:\\d+}/apply")
    public Result<Void> apply(@AuthenticationPrincipal LoginUser loginUser,
                              @PathVariable Long id) {
        teamService.apply(id, loginUser.getUserId());
        return Result.success();
    }

    @PostMapping("/{id:\\d+}/close")
    public Result<Void> close(@AuthenticationPrincipal LoginUser loginUser,
                              @PathVariable Long id) {
        teamService.closeRecruitment(id, loginUser.getUserId());
        return Result.success();
    }

    @PostMapping("/{id:\\d+}/dissolve")
    public Result<Void> dissolve(@AuthenticationPrincipal LoginUser loginUser,
                                 @PathVariable Long id) {
        teamService.dissolveTeam(id, loginUser.getUserId());
        return Result.success();
    }

    @PostMapping("/{id:\\d+}/applications/{memberId}/approve")
    public Result<Void> approveApplication(@AuthenticationPrincipal LoginUser loginUser,
                                           @PathVariable Long id,
                                           @PathVariable Long memberId) {
        teamService.approveApplication(id, loginUser.getUserId(), memberId);
        return Result.success();
    }

    @PostMapping("/{id:\\d+}/applications/{memberId}/reject")
    public Result<Void> rejectApplication(@AuthenticationPrincipal LoginUser loginUser,
                                          @PathVariable Long id,
                                          @PathVariable Long memberId) {
        teamService.rejectApplication(id, loginUser.getUserId(), memberId);
        return Result.success();
    }

    @PostMapping("/{id:\\d+}/invite/{userId:\\d+}")
    public Result<Void> invite(@AuthenticationPrincipal LoginUser loginUser,
                               @PathVariable Long id,
                               @PathVariable Long userId) {
        teamService.invite(id, loginUser.getUserId(), userId);
        return Result.success();
    }
}
