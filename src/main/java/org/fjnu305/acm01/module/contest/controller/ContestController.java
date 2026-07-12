package org.fjnu305.acm01.module.contest.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.module.contest.vo.ContestVO;
import org.fjnu305.acm01.module.contest.query.service.ContestQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestQueryService contestQueryService;

    @GetMapping
    public Result<PageResult<ContestVO>> list(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(contestQueryService.listContests(source, status, pageNum, pageSize));
    }
}
