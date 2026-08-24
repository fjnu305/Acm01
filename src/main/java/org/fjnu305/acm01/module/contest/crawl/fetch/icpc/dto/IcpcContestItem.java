package org.fjnu305.acm01.module.contest.crawl.fetch.icpc.dto;

import lombok.Data;

@Data
public class IcpcContestItem {

    private String id;
    private String title;
    private Long startEpochSecond;
    private Long endEpochSecond;
    private String location;
}
