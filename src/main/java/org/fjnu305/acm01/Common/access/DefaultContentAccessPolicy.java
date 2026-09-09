package org.fjnu305.acm01.Common.access;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Public catalog rules used today. Keep unpublished / deleted rows out of
 * anonymous search; owners and admins can still see their own records.
 */
@Component
public class DefaultContentAccessPolicy implements ContentAccessPolicy {

    private final Map<String, ContentVisibilitySource> sources;

    public DefaultContentAccessPolicy(List<ContentVisibilitySource> sources) {
        this.sources = sources == null ? Map.of() : sources.stream()
                .collect(Collectors.toMap(ContentVisibilitySource::type, Function.identity(), (a, b) -> a));
    }

    @Override
    public boolean canRead(String type, Long refId, Viewer viewer) {
        if (type == null || refId == null) {
            return false;
        }
        ContentVisibilitySource source = sources.get(type);
        if (source == null) {
            return false;
        }
        ContentVisibilitySnapshot snap = source.load(refId);
        if (snap == null || isDeleted(snap.deleted())) {
            return false;
        }
        Viewer who = viewer == null ? Viewer.anonymous() : viewer;
        return switch (type) {
            case ContentTypes.SOLUTION -> canReadSolution(snap, who);
            case ContentTypes.TEAM -> canReadTeam(snap, who);
            case ContentTypes.POST -> canReadPost(snap, who);
            default -> false;
        };
    }

    private boolean canReadSolution(ContentVisibilitySnapshot snap, Viewer viewer) {
        if (isPublished(snap.status())) {
            return true;
        }
        if (snap.status() != null && snap.status() == 2) {
            return viewer.admin();
        }
        return viewer.admin() || viewer.is(snap.ownerId());
    }

    private boolean canReadTeam(ContentVisibilitySnapshot snap, Viewer viewer) {
        if (snap.status() != null && snap.status() != 0) {
            return true;
        }
        return viewer.admin() || viewer.is(snap.ownerId());
    }

    private boolean canReadPost(ContentVisibilitySnapshot snap, Viewer viewer) {
        if (isPublished(snap.status())) {
            return true;
        }
        return viewer.admin() || viewer.is(snap.ownerId());
    }

    private static boolean isPublished(Integer status) {
        return status != null && status == 1;
    }

    private static boolean isDeleted(Integer deleted) {
        return deleted != null && deleted == 1;
    }
}
