package org.fjnu305.acm01.Common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    TOKEN_INVALID(1002, "Token无效"),
    USER_ALREADY_EXISTS(1003, "用户名已存在"),
    PASSWORD_WRONG(1004, "密码错误"),
    USER_DISABLED(1005, "账号已被禁用"),
    ROLE_NOT_FOUND(1006, "角色配置异常");

    private final int code;
    private final String message;
}
