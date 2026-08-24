package org.fjnu305.acm01.module.solution.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.solution.dto.SolutionCreateRequest;
import org.fjnu305.acm01.module.solution.dto.SolutionUpdateRequest;
import org.fjnu305.acm01.module.solution.service.SolutionService;
import org.fjnu305.acm01.module.solution.vo.SolutionDetailVO;
import org.fjnu305.acm01.module.solution.vo.SolutionTagVO;
import org.fjnu305.acm01.module.solution.vo.SolutionTemplateVO;
import org.fjnu305.acm01.module.solution.vo.SolutionVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/solutions")
@RequiredArgsConstructor
public class SolutionController {

    private final SolutionService solutionService;

    @GetMapping
    public Result<PageResult<SolutionVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(solutionService.list(keyword, tag, pageNum, pageSize));
    }

    @GetMapping("/templates")
    public Result<List<SolutionTemplateVO>> templates(@RequestParam(required = false) String category) {
        return Result.success(solutionService.listTemplates(category));
    }

    @GetMapping("/tags/hot")
    public Result<List<SolutionTagVO>> hotTags(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(solutionService.listHotTags(limit));
    }

    @GetMapping("/tags/categories")
    public Result<List<SolutionTagVO>> categories() {
        return Result.success(solutionService.listCategories());
    }

    @GetMapping("/favorites")
    public Result<PageResult<SolutionVO>> favorites(@AuthenticationPrincipal LoginUser loginUser,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(solutionService.listFavorites(loginUser.getUserId(), pageNum, pageSize));
    }

    @PostMapping
    public Result<SolutionDetailVO> create(@AuthenticationPrincipal LoginUser loginUser,
                                           @Valid @RequestBody SolutionCreateRequest request) {
        return Result.success(solutionService.create(loginUser.getUserId(), request));
    }

    @GetMapping("/{id:\\d+}")
    public Result<SolutionDetailVO> detail(@PathVariable Long id,
                                           @AuthenticationPrincipal LoginUser loginUser) {
        Long viewerId = loginUser != null ? loginUser.getUserId() : null;
        return Result.success(solutionService.getDetail(id, viewerId));
    }

    @PutMapping("/{id:\\d+}")
    public Result<SolutionDetailVO> update(@AuthenticationPrincipal LoginUser loginUser,
                                           @PathVariable Long id,
                                           @Valid @RequestBody SolutionUpdateRequest request) {
        return Result.success(solutionService.update(loginUser.getUserId(), id, request));
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        solutionService.delete(loginUser.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id:\\d+}/favorite")
    public Result<Void> favorite(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        solutionService.favorite(loginUser.getUserId(), id);
        return Result.success();
    }

    @DeleteMapping("/{id:\\d+}/favorite")
    public Result<Void> unfavorite(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        solutionService.unfavorite(loginUser.getUserId(), id);
        return Result.success();
    }
}
