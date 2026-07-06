package org.fjnu305.acm01.module.contest.crawler.strategy;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * CCPC 赛事爬虫策略实现。
 * <p>
 * <b>数据来源：</b>CCPC 官网赛事公告页 HTML<br>
 * <b>特点：</b>更新频率较低，适合较长 cron 间隔。
 * </p>
 */
@Component
public class CcpcCrawler extends AbstractContestCrawler {

    @Override
    public ContestSource getSource() {
        return ContestSource.CCPC;
    }

    @Override
    protected List<ContestDTO> doFetch() {
        // TODO 迭代 3：解析 CCPC 官网
        return Collections.emptyList();
    }
}
