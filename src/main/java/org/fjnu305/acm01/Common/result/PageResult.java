package org.fjnu305.acm01.Common.result;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> list;
    private long total;
    private long pageNum;
    private long pageSize;

    public static <T> PageResult<T> of(List<T> list, long total, long pageNum, long pageSize) {
        PageResult<T> page = new PageResult<>();
        page.list = list;
        page.total = total;
        page.pageNum = pageNum;
        page.pageSize = pageSize;
        return page;
    }
}
