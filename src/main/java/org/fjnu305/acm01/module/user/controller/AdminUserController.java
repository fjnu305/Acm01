package org.fjnu305.acm01.module.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.module.user.dto.AdminUserVO;
import org.fjnu305.acm01.module.user.dto.UpdateUserRolesRequest;
import org.fjnu305.acm01.module.user.dto.UpdateUserStatusRequest;
import org.fjnu305.acm01.module.user.service.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<AdminUserVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(adminUserService.listUsers(keyword, pageNum, pageSize));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody UpdateUserStatusRequest request) {
        adminUserService.updateStatus(id, request.getStatus());
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
        adminUserService.updateRoles(id, request);
        return Result.success();
    }
}
