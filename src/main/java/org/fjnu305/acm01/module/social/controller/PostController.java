package org.fjnu305.acm01.module.social.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.social.dto.CreatePostRequest;
import org.fjnu305.acm01.module.social.service.PostService;
import org.fjnu305.acm01.module.social.vo.PostVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public Result<PostVO> create(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody CreatePostRequest request) {
        return Result.success(postService.createPost(loginUser.getUserId(), request));
    }

    @GetMapping("/feed/hot")
    public Result<List<PostVO>> hotFeed(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(postService.getHotFeed(loginUser.getUserId()));
    }

    @GetMapping
    public Result<PageResult<PostVO>> list(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(postService.listPosts(loginUser.getUserId(), pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<PostVO> detail(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        return Result.success(postService.getPost(id, loginUser.getUserId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteOwn(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        postService.deleteOwnPost(loginUser.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<PostVO> like(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        return Result.success(postService.likePost(loginUser.getUserId(), id));
    }

    @DeleteMapping("/{id}/like")
    public Result<PostVO> unlike(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        return Result.success(postService.unlikePost(loginUser.getUserId(), id));
    }
}
