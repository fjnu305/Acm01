package org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * AtCoder 外部数据统一项：HTML 爬虫与 JSON 接口均产出此结构。
 * 结束时间由 {@code startEpochSecond + durationSecond} 推算。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtCoderContestItem {

    private String id;
    private String title;

    @JsonProperty("start_epoch_second")
    private Long startEpochSecond;

    @JsonProperty("duration_second")
    private Long durationSecond;

    @JsonProperty("rate_change")
    private String rateChange;
}
