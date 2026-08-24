package org.fjnu305.acm01.module.contest.crawl.fetch.lanqiao.dto;

import lombok.Data;

@Data
public class LanqiaoContestItem {

    private String id;
    private String title;
    private Long startEpochSecond;
    private Long endEpochSecond;
}
