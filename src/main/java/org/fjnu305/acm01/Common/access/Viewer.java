package org.fjnu305.acm01.Common.access;

import org.fjnu305.acm01.Security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

/**
 * Who is asking. Anonymous callers have a null userId.
 */
public final class Viewer {

    private final Long userId;
    private final boolean admin;

    private Viewer(Long userId, boolean admin) {
        this.userId = userId;
        this.admin = admin;
    }

    public static Viewer anonymous() {
        return new Viewer(null, false);
    }

    public static Viewer user(Long userId) {
        if (userId == null) {
            return anonymous();
        }
        return new Viewer(userId, false);
    }

    public static Viewer of(LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            return anonymous();
        }
        return new Viewer(loginUser.getUserId(), hasAdminRole(loginUser.getRoles()));
    }

    public static Viewer current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return of(loginUser);
        }
        return anonymous();
    }

    public Long userId() {
        return userId;
    }

    public boolean authenticated() {
        return userId != null;
    }

    public boolean admin() {
        return admin;
    }

    public boolean is(Long otherUserId) {
        return userId != null && userId.equals(otherUserId);
    }

    private static boolean hasAdminRole(String roles) {
        if (!StringUtils.hasText(roles)) {
            return false;
        }
        for (String role : roles.split(",")) {
            if ("ADMIN".equalsIgnoreCase(role.trim())) {
                return true;
            }
        }
        return false;
    }
}
