package org.fjnu305.acm01.Common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    INTERNAL_ERROR(500, "Internal server error"),

    USER_NOT_FOUND(1001, "User not found"),
    TOKEN_INVALID(1002, "Invalid token"),
    USER_ALREADY_EXISTS(1003, "Username already exists"),
    PASSWORD_WRONG(1004, "Wrong password"),
    USER_DISABLED(1005, "Account disabled"),
    ROLE_NOT_FOUND(1006, "Role configuration error"),

    CONTEST_NOT_FOUND(2001, "Contest not found"),
    SUBSCRIPTION_NOT_FOUND(2002, "Subscription not found"),
    INVALID_REMIND_TIME(2003, "Invalid remind time"),
    CONTEST_ALREADY_STARTED(2004, "Contest already started"),
    EMAIL_REQUIRED(2005, "Email is required for email notification");

    private final int code;
    private final String message;
}
