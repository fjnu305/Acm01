package org.fjnu305.acm01.module.contest.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.module.contest.dto.ContestCrawlResult;
import org.fjnu305.acm01.module.contest.crawl.service.ContestCrawlService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 赛事管理端 API，路径 {@code /api/admin/contests}，需 ADMIN 角色。
 * <p>爬取结果写入 contest 表，并更新 contest_source / contest_crawl_log。</p>
 */
@RestController
@RequestMapping("/api/admin/contests")
@RequiredArgsConstructor
public class ContestAdminController {

    private final ContestCrawlService contestCrawlService;

    /**
     * 手动触发指定平台爬虫：Crawler → ContestDTO → contest 表。
     * <p>示例：{@code POST /api/admin/contests/crawl/codeforces}</p>
     *
     * @param sourceCode 路径参数，对应 contest_source.source_code / ContestSource.value
     * @return 入库统计与耗时
     */
    @PostMapping("/crawl/{sourceCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ContestCrawlResult> crawl(@PathVariable String sourceCode) {
        ContestSource source = resolveSource(sourceCode);
        return Result.success(contestCrawlService.crawlManual(source));
    }

    /**
     * 将 URL 路径中的平台编码解析为 ContestSource 枚举。
     *
     * @throws BusinessException 未知平台时 400
     */
    private ContestSource resolveSource(String sourceCode) {
        for (ContestSource source : ContestSource.values()) {
            if (source.getValue().equalsIgnoreCase(sourceCode)) {
                return source;
            }
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "未知平台: " + sourceCode);
    }
}
