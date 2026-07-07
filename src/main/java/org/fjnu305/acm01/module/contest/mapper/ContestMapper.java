package org.fjnu305.acm01.module.contest.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;

import java.util.List;

/**
 * 统一赛事表 {@code contest} 数据访问。
 * <p>去重依赖唯一键 {@code uk_source_external(source, external_id)}，见模块 2 文档 §3.1。</p>
 */
@Mapper
public interface ContestMapper {

    /**
     * 按平台 + 外部 ID 查询未删除赛事，用于入库前去重。
     *
     * @param source     列 source，对应 ContestSource.value
     * @param externalId 列 external_id
     * @return 已存在记录；不存在返回 null
     */
    ContestEntity selectBySourceAndExternalId(@Param("source") String source,
                                              @Param("externalId") String externalId);

    /**
     * 插入新赛事；{@code last_crawled_at} 由 SQL 设为 NOW()，{@code deleted=0}。
     *
     * @param entity 映射 contest 表各业务列（不含 id/created_time/updated_time）
     * @return 影响行数
     */
    int insert(ContestEntity entity);

    /**
     * 按 source + external_id 更新赛事；{@code raw_hash} 变化时由 Service 调用。
     * <p>同时刷新 {@code last_crawled_at=NOW()}，{@code updated_time} 由数据库自动维护。</p>
     *
     * @param entity 含 source、externalId 及待更新字段
     * @return 影响行数
     */
    int updateBySourceAndExternalId(ContestEntity entity);

    /**
     * 按业务键批量查已有记录的 source / external_id / raw_hash。
     *
     * @param list 仅含 source、externalId 的键列表
     * @return 库中已存在且未删除的记录（仅前三列有值）
     */
    List<ContestEntity> selectRawHashByKeys(@Param("list") List<ContestEntity> list);

    /**
     * 批量插入新赛事。
     *
     * @param list 待 INSERT 的完整 Entity 列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<ContestEntity> list);

    /**
     * 分页查询赛事列表。
     */
    List<ContestEntity> selectPage(@Param("source") String source,
                                   @Param("status") Integer status,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    /**
     * 统计赛事列表总数。
     */
    long countPage(@Param("source") String source,
                   @Param("status") Integer status);
}
