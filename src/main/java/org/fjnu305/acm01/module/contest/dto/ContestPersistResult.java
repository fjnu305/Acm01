package org.fjnu305.acm01.module.contest.dto;

import lombok.Builder;
import lombok.Data;

/**
 * contest 表批量入库统计结果。
 */
@Data
@Builder
public class ContestPersistResult {

    /** 本次 Crawler 解析出的有效条数 */
    private int fetched;

    /** 新 INSERT 到 contest 表的条数（uk_source_external 不存在） */
    private int inserted;

    /** raw_hash 变化触发的 UPDATE 条数 */
    private int updated;

    /** raw_hash 未变 SKIP 的条数 */
    private int skipped;

    /** 缺少 source/external_id/title/start_time 被忽略的条数 */
    private int ignored;
}
