package org.fjnu305.acm01.module.social.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.social.dto.CreateCommentRequest;
import org.fjnu305.acm01.module.social.service.CommentService;
import org.fjnu305.acm01.module.social.vo.CommentVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public Result<CommentVO> create(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request) {
        return Result.success(commentService.createComment(loginUser.getUserId(), postId, request));
    }

    @GetMapping
    public Result<List<CommentVO>> list(@PathVariable Long postId) {
        return Result.success(commentService.listComments(postId));
    }
}
