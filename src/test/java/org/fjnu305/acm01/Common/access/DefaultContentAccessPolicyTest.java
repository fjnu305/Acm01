package org.fjnu305.acm01.Common.access;

import org.fjnu305.acm01.Security.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultContentAccessPolicyTest {

    private DefaultContentAccessPolicy policy;

    private Viewer owner;
    private Viewer stranger;
    private Viewer admin;

    @BeforeEach
    void viewers() {
        owner = Viewer.of(new LoginUser(10L, "owner", "USER"));
        stranger = Viewer.of(new LoginUser(99L, "other", "USER"));
        admin = Viewer.of(new LoginUser(1L, "admin", "ADMIN"));
        policy = new DefaultContentAccessPolicy(List.of(
                mapSource(ContentTypes.SOLUTION, java.util.Map.of(
                        5L, new ContentVisibilitySnapshot(10L, 0, 0),
                        6L, new ContentVisibilitySnapshot(10L, 1, 0),
                        7L, new ContentVisibilitySnapshot(10L, 2, 0)
                )),
                mapSource(ContentTypes.TEAM, java.util.Map.of(
                        8L, new ContentVisibilitySnapshot(10L, 0, 0)
                )),
                mapSource(ContentTypes.POST, java.util.Map.of(
                        3L, new ContentVisibilitySnapshot(10L, 0, 0)
                ))
        ));
    }

    @Test
    void solutionDraft_hiddenFromStranger_visibleToOwnerAndAdmin() {
        assertThat(policy.canRead(ContentTypes.SOLUTION, 5L, Viewer.anonymous())).isFalse();
        assertThat(policy.canRead(ContentTypes.SOLUTION, 5L, stranger)).isFalse();
        assertThat(policy.canRead(ContentTypes.SOLUTION, 5L, owner)).isTrue();
        assertThat(policy.canRead(ContentTypes.SOLUTION, 5L, admin)).isTrue();
    }

    @Test
    void publishedSolution_visibleToAnonymous() {
        assertThat(policy.canRead(ContentTypes.SOLUTION, 6L, Viewer.anonymous())).isTrue();
    }

    @Test
    void takenDownSolution_onlyAdmin() {
        assertThat(policy.canRead(ContentTypes.SOLUTION, 7L, owner)).isFalse();
        assertThat(policy.canRead(ContentTypes.SOLUTION, 7L, admin)).isTrue();
    }

    @Test
    void closedTeam_hiddenFromPublic_visibleToLeader() {
        assertThat(policy.canRead(ContentTypes.TEAM, 8L, Viewer.anonymous())).isFalse();
        assertThat(policy.canRead(ContentTypes.TEAM, 8L, owner)).isTrue();
    }

    @Test
    void unpublishedPost_hiddenFromStranger() {
        assertThat(policy.canRead(ContentTypes.POST, 3L, stranger)).isFalse();
        assertThat(policy.canRead(ContentTypes.POST, 3L, owner)).isTrue();
    }

    private static ContentVisibilitySource mapSource(String type,
                                                    java.util.Map<Long, ContentVisibilitySnapshot> byId) {
        return new ContentVisibilitySource() {
            @Override
            public String type() {
                return type;
            }

            @Override
            public ContentVisibilitySnapshot load(Long refId) {
                return byId.get(refId);
            }
        };
    }
}
