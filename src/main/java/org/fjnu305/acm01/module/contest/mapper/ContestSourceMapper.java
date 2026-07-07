package org.fjnu305.acm01.module.contest.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 爬虫源配置表 {@code contest_source} 数据访问。
 * <p>爬取结束后更新 {@code last_crawl_time}、{@code last_crawl_status}、{@code fail_count}，见文档 §3.2。</p>
 */
@Mapper
public interface ContestSourceMapper {

    /**
     * 爬取成功：更新 last_crawl_status=SUCCESS，fail_count 清零。
     *
     * @param sourceCode 列 source_code，如 codeforces
     */
    int updateOnSuccess(@Param("sourceCode") String sourceCode);

    /**
     * 爬取失败：更新 last_crawl_status=FAILED，fail_count 自增 1。
     *
     * @param sourceCode 列 source_code
     */
    int updateOnFailure(@Param("sourceCode") String sourceCode);

    /**
     * 查询平台是否启用爬虫。
     *
     * @param sourceCode 列 source_code
     * @return 1 启用 0 禁用；无记录时返回 null
     */
    Integer selectCrawlEnabled(@Param("sourceCode") String sourceCode);
}
