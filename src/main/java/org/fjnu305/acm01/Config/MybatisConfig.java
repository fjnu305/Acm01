package org.fjnu305.acm01.Config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("org.fjnu305.acm01.**.mapper")
public class MybatisConfig {
}
