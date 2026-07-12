package org.fjnu305.acm01.module.contest.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.module.contest.crawl.service.ContestCrawlService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/contests")
@RequiredArgsConstructor
public class ContestAdminController {

    private final ContestCrawlService contestCrawlService;

    /**
     * ????????????? contest_crawl_log ? logId ????
     */
    @PostMapping("/crawl/{sourceCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> crawl(@PathVariable String sourceCode) {
        ContestSource source = resolveSource(sourceCode);
        return Result.success(contestCrawlService.crawlManual(source));
    }

    private ContestSource resolveSource(String sourceCode) {
        for (ContestSource source : ContestSource.values()) {
            if (source.getValue().equalsIgnoreCase(sourceCode)) {
                return source;
            }
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "Unknown platform: " + sourceCode);
    }
}
