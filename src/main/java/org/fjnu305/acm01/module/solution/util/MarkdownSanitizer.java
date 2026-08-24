package org.fjnu305.acm01.module.solution.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.regex.Pattern;

/** Basic markdown sanitization for solution content. */
public final class MarkdownSanitizer {

    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?is)<script[^>]*>.*?</script>");
    private static final Pattern EVENT_ATTR_PATTERN = Pattern.compile("(?i)\\s+on\\w+\\s*=\\s*(['\"]).*?\\1");

    private MarkdownSanitizer() {
    }

    public static String sanitize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String stripped = SCRIPT_PATTERN.matcher(raw).replaceAll("");
        stripped = EVENT_ATTR_PATTERN.matcher(stripped).replaceAll("");
        stripped = Jsoup.clean(stripped, Safelist.none());
        return stripped.trim();
    }
}
