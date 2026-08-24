package org.fjnu305.acm01.module.sync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BindCfHandleRequest {

    @NotBlank(message = "Codeforces handle is required")
    @Size(max = 50, message = "Handle too long")
    private String handle;

    /** 可选：平台 cookie/token，AES-GCM 加密存储 */
    @Size(max = 2048, message = "Credential too long")
    private String credential;
}
