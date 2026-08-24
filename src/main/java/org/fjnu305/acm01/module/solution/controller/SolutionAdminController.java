package org.fjnu305.acm01.module.solution.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.module.solution.service.SolutionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/solutions")
@RequiredArgsConstructor
public class SolutionAdminController {

    private final SolutionService solutionService;

    @PutMapping("/{id}/takedown")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> takedown(@PathVariable Long id) {
        solutionService.takedown(id);
        return Result.success();
    }
}
