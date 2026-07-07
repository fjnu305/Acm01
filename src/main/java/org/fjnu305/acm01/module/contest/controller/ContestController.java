package org.fjnu305.acm01.module.contest.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.module.contest.dto.ContestQueryRequest;
import org.fjnu305.acm01.module.contest.dto.ContestVO;
import org.fjnu305.acm01.module.contest.query.service.ContestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 赛事公开查询 API，路径 {@code /api/contests}。
 */
@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    /**
     * 分页赛事列表。
     *
     * @param source   平台来源，如 codeforces
     * @param status   1即将开始 2进行中 3已结束
     * @param pageNum  页码，从 1 开始
     * @param pageSize 每页条数，最大 100
     */
    @GetMapping
    public Result<PageResult<ContestVO>> list(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        ContestQueryRequest request = new ContestQueryRequest();
        request.setSource(source);
        request.setStatus(status);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        return Result.success(contestService.listContests(request));
    }
}
