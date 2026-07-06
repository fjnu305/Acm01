package org.fjnu305.acm01.module.contest.service;

import org.springframework.stereotype.Service;

/**
 * 爬虫编排服务。
 * <p>职责：调度各平台 Crawler、去重（source + external_id）、增量更新（raw_hash）、更新 contest_source 状态、失效缓存。</p>
 */
@Service
public class ContestCrawlService {
}
