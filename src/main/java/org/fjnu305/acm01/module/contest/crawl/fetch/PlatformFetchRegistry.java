package org.fjnu305.acm01.module.contest.crawl.fetch;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按 {@link ContestSource} 索引 {@link PlatformContestFetchService} 实现。
 */
@Component
public class PlatformFetchRegistry {

    private final Map<ContestSource, PlatformContestFetchService> index;

    public PlatformFetchRegistry(List<PlatformContestFetchService> services) {
        this.index = services.stream()
                .collect(Collectors.toUnmodifiableMap(
                        PlatformContestFetchService::getSource,
                        Function.identity()
                ));
    }

    public Collection<PlatformContestFetchService> all() {
        return index.values();
    }

    public PlatformContestFetchService getRequired(ContestSource source) {
        PlatformContestFetchService service = index.get(source);
        if (service == null) {
            throw new IllegalArgumentException("No PlatformContestFetchService for source: " + source);
        }
        return service;
    }
}
