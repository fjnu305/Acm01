package org.fjnu305.acm01.module.contest.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.contest.service.ContestCrawlService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 赛事管理端 API（迭代 3）。
 * <p>路径前缀 {@code /api/admin/contests}，需 ADMIN 角色。</p>
 * <ul>
 *   <li>手动触发爬虫</li>
 *   <li>查看 / 修改 contest_source 配置</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/admin/contests")
@RequiredArgsConstructor
public class ContestAdminController {

    private final ContestCrawlService contestCrawlService;
}
