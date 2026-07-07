package org.fjnu305.acm01.module.contest.dto;

import lombok.Data;

/**
 * 赛事列表查询条件。
 */
@Data
public class ContestQueryRequest {

    /** 来源平台，如 codeforces */
    private String source;

    /** 1即将开始 2进行中 3已结束 */
    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}
