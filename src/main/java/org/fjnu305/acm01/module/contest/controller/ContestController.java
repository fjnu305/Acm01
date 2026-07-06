package org.fjnu305.acm01.module.contest.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.contest.service.ContestService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 赛事公开查询 API。
 * <p>路径前缀 {@code /api/contests}，无需登录（Security 需放行）。</p>
 * <ul>
 *   <li>{@code GET /} — 分页列表</li>
 *   <li>{@code GET /{id}} — 详情</li>
 *   <li>{@code GET /calendar} — 日历</li>
 *   <li>{@code GET /hot} — 热门</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;
}
