package org.fjnu305.acm01.module.contest.crawler.strategy;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 蓝桥杯赛事爬虫策略实现。
 * <p>
 * <b>数据来源：</b>蓝桥杯官方站点 HTML<br>
 * <b>特点：</b>省赛 / 国赛周期性强，需过滤历史届次。
 * </p>
 */
@Component
public class LanqiaoCrawler extends AbstractContestCrawler {

    @Override
    public ContestSource getSource() {
        return ContestSource.LANQIAO;
    }

    @Override
    protected List<ContestDTO> doFetch() {
        // TODO 迭代 3：解析蓝桥杯官网
        return Collections.emptyList();
    }
}
