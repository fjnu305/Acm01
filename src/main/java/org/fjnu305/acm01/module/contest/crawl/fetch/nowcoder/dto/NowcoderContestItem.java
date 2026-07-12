package org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.dto;

import lombok.Data;

/**
 * 牛客外部数据统一项。结束时间由 {@code startEpochSecond + durationSecond} 推算。
 */
@Data
public class NowcoderContestItem {

    private String id;
    private String title;
    private Long startEpochSecond;
    private Long durationSecond;
    /** Rating 限制等信息 */
    private String rateChange;
    private Long registerStartEpochSecond;
    private Long registerEndEpochSecond;
    private String organizer;
}
