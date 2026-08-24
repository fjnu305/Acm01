package org.fjnu305.acm01.module.inbox.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BroadcastMessageDTO {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 5000)
    private String body;

    /** 为空则广播给全部活跃用户 */
    private List<Long> userIds;
}
