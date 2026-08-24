package org.fjnu305.acm01.module.contest.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.module.contest.log.query.CrawlLogQueryService;
import org.fjnu305.acm01.module.contest.log.vo.CrawlLogVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/crawl")
@RequiredArgsConstructor
public class CrawlLogAdminController {

    private final CrawlLogQueryService crawlLogQueryService;

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<CrawlLogVO>> listLogs(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(crawlLogQueryService.listLogs(source, status, pageNum, pageSize));
    }
}
