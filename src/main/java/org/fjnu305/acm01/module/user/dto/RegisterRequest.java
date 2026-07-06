package org.fjnu305.acm01.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度为 3~50 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度为 6~32 个字符")
    private String password;

    @Size(max = 50, message = "昵称最多 50 个字符")
    private String nickname;

    @Size(max = 100, message = "邮箱最多 100 个字符")
    private String email;
}
