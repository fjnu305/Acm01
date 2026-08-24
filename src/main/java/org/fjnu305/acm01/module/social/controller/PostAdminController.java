package org.fjnu305.acm01.module.social.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.module.social.service.PostService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class PostAdminController {

    private final PostService postService;

    @DeleteMapping("/{id}")
    public Result<Void> adminDelete(@PathVariable Long id) {
        postService.adminDeletePost(id);
        return Result.success();
    }
}
