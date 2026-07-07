package org.fjnu305.acm01.module.contest.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.contest.entity.ContestCrawlLogEntity;

import java.util.List;

/**
 * 爬虫执行日志表 {@code contest_crawl_log} 数据访问。
 */
@Mapper
public interface ContestCrawlLogMapper {

    /**
     * 插入一条爬虫执行日志（成功或失败）。
     *
     * @param log 含 source、status、trigger_type、error_type 等统一字段
     * @return 影响行数
     */
    int insert(ContestCrawlLogEntity log);

    /**
     * 查询指定平台最近 N 条日志，供管理端排查。
     *
     * @param source 列 source
     * @param limit  最大条数
     */
    List<ContestCrawlLogEntity> selectRecentBySource(@Param("source") String source,
                                                     @Param("limit") int limit);
}
