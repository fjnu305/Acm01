package org.fjnu305.acm01.module.notify.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(DataSource.class)
public class NotifyTaskSchemaPatch implements InitializingBean {

    private final DataSource dataSource;

    @Override
    public void afterPropertiesSet() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE notify_task ADD COLUMN claimed_at DATETIME NULL");
            log.info("Added notify_task.claimed_at");
        } catch (Exception e) {
            log.debug("notify_task.claimed_at already present or patch skipped: {}", e.getMessage());
        }
    }
}
