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
    EMAIL_REQUIRED(2005, "Email is required for email notification"),

    AVATAR_INVALID(1007, "Invalid avatar file"),

    SOLUTION_NOT_FOUND(3001, "Solution not found"),
    SOLUTION_FORBIDDEN(3002, "No permission to modify this solution"),
    SOLUTION_TAKEDOWN(3003, "Solution has been taken down"),

    TEAM_NOT_FOUND(4001, "Team post not found"),
    TEAM_FORBIDDEN(4002, "No permission for this team post"),
    TEAM_FULL(4003, "Team is full"),
    TEAM_INVITE_NOT_FOUND(4004, "Invite not found"),
    TEAM_ALREADY_MEMBER(4005, "Already a team member"),
    TEAM_ALREADY_APPLIED(4006, "Application already submitted"),
    TEAM_NOT_RECRUITING(4007, "Team is not recruiting"),
    TEAM_APPLICATION_NOT_FOUND(4008, "Application not found"),
    TEAM_PENDING_INVITE_EXISTS(4009, "You already have a pending invite"),

    SEARCH_DISABLED(5001, "Search service is disabled"),

    OJ_HANDLE_INVALID(6001, "Invalid OJ handle"),
    OJ_ACCOUNT_NOT_FOUND(6002, "OJ account not found"),

    POST_NOT_FOUND(7001, "Post not found"),
    COMMENT_NOT_FOUND(7002, "Comment not found"),
    POST_CONTENT_EMPTY(7003, "Post content is empty"),
    FOLLOW_SELF(7004, "Cannot follow yourself"),

    FRIEND_REQUEST_NOT_FOUND(8001, "Friend request not found"),
    FRIEND_REQUEST_FORBIDDEN(8002, "No permission for this friend request"),
    FRIEND_REQUEST_EXISTS(8003, "Friend request already pending"),
    ALREADY_FRIENDS(8004, "Already friends"),
    NOT_FRIENDS(8005, "Not friends"),
    FRIEND_SELF(8006, "Cannot add yourself as friend"),

    INBOX_NOT_FOUND(8101, "Inbox message not found");

    private final int code;
    private final String message;
}
