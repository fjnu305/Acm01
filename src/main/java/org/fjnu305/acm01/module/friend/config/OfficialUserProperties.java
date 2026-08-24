package org.fjnu305.acm01.module.friend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "acm.official")
public class OfficialUserProperties {

    /** 官方系统好友用户名，注册时自动加为好友 */
    private String username = "acmer_official";
}
