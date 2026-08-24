package org.fjnu305.acm01.module.inbox.constant;

/**
 * 消息分类（由发件人/ref 推导，供前端 Tab 筛选）。
 */
public final class InboxCategory {

    public static final String OFFICIAL = "OFFICIAL";
    public static final String FRIEND = "FRIEND";
    public static final String PERSONAL = "PERSONAL";

    private InboxCategory() {
    }

    public static String normalize(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        String upper = category.trim().toUpperCase();
        return "SYSTEM".equals(upper) ? OFFICIAL : upper;
    }
}
