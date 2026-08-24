package org.fjnu305.acm01.Common.util;

/**
 * 统一分页参数归一化，避免各 Service 重复计算 offset。
 */
public final class PageParams {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 50;

    private final int page;
    private final int size;
    private final int offset;

    private PageParams(int page, int size) {
        this.page = page;
        this.size = size;
        this.offset = (page - 1) * size;
    }

    public static PageParams of(int pageNum, int pageSize) {
        int page = Math.max(pageNum, 1);
        int size = pageSize <= 0 ? DEFAULT_SIZE : Math.min(pageSize, MAX_SIZE);
        return new PageParams(page, size);
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }

    public int offset() {
        return offset;
    }
}
