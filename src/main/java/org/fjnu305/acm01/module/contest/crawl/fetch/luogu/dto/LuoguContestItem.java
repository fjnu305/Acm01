package org.fjnu305.acm01.module.contest.crawl.fetch.luogu.dto;

import lombok.Data;

@Data
public class LuoguContestItem {

    private String id;
    private String name;
    private Long startEpochSecond;
    private Long endEpochSecond;
    private String type;
}
