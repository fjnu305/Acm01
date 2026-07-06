package org.fjnu305.acm01.module.contest.crawler.strategy;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 洛谷赛事爬虫策略实现。
 * <p>
 * <b>数据来源：</b>洛谷比赛页面 / 非官方 API<br>
 * <b>实现方式：</b>HTTP + JSON 或 HTML 解析。
 * </p>
 */
@Component
public class LuoguCrawler extends AbstractContestCrawler {

    @Override
    public ContestSource getSource() {
        return ContestSource.LUOGU;
    }

    @Override
    protected List<ContestDTO> doFetch() {
        // TODO 迭代 3：解析洛谷比赛列表
        return Collections.emptyList();
    }
}
