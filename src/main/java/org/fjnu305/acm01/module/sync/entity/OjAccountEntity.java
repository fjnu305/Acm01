package org.fjnu305.acm01.module.sync.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OjAccountEntity {

    private Long id;
    private Long userId;
    private String platform;
    private String handle;
    private String encryptedCredential;
    private String credentialIv;
    private Integer status;
    private LocalDateTime lastSyncAt;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
