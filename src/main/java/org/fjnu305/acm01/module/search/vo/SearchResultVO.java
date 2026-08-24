package org.fjnu305.acm01.module.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchResultVO {

    private String query;
    private String type;
    private List<SearchHitVO> hits;
    private long total;
    private long pageNum;
    private long pageSize;
}
