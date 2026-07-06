package org.fjnu305.acm01.module.contest.crawler.strategy;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Codeforces 赛事爬虫策略实现。
 * <p>
 * <b>数据来源：</b>{@code https://codeforces.com/api/contest.list}（官方 JSON API）<br>
 * <b>优先级：</b>迭代 1 首个实现的平台，API 稳定、无需 HTML 解析。
 * </p>
 * <p>
 * <b>实现要点（TODO）：</b>
 * <ol>
 *   <li>OkHttp GET 请求 API</li>
 *   <li>解析 result 数组，过滤 type=CF 正式比赛</li>
 *   <li>映射 phase → status，映射 difficulty → Div.1/Div.2</li>
 *   <li>externalId = contest.id，url = https://codeforces.com/contests/{id}</li>
 * </ol>
 * </p>
 */
@Component
public class CodeforcesCrawler extends AbstractContestCrawler {

    /** Codeforces 官方赛事列表 API */
    private static final String API_URL = "https://codeforces.com/api/contest.list";

    @Override
    public ContestSource getSource() {
        return ContestSource.CODEFORCES;
    }

    @Override
    protected List<ContestDTO> doFetch() {
        // TODO 迭代 1：请求 API_URL，解析 JSON，构建 ContestDTO 列表
        // 示例伪代码：
        //   String json = httpGet(API_URL);
        //   for (contest : parse(json)) {
        //       ContestDTO dto = ContestDTO.builder()...build();
        //       dto.setRawHash(computeRawHash(dto));
        //   }
        return Collections.emptyList();
    }
}
