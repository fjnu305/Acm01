package org.fjnu305.acm01.Common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    public String toAuthority() {
        return "ROLE_" + value;
    }
}
