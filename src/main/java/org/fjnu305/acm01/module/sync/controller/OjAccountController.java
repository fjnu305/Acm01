package org.fjnu305.acm01.module.sync.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.sync.dto.BindCfHandleRequest;
import org.fjnu305.acm01.module.sync.service.OjAccountService;
import org.fjnu305.acm01.module.sync.vo.OjAccountVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/oj/accounts")
@RequiredArgsConstructor
public class OjAccountController {

    private final OjAccountService ojAccountService;

    @PostMapping("/cf")
    public Result<OjAccountVO> bindCfHandle(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody BindCfHandleRequest request) {
        return Result.success(ojAccountService.bindCfHandle(loginUser.getUserId(), request));
    }

    @GetMapping
    public Result<List<OjAccountVO>> listAccounts(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(ojAccountService.listMyAccounts(loginUser.getUserId()));
    }
}
