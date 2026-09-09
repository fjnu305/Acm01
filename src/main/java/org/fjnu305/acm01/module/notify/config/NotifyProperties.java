package org.fjnu305.acm01.module.notify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "notify")
public class NotifyProperties {

    private boolean enabled = true;
    private String scanCron = "0 */1 * * * ?";
    private int scanBatchSize = 200;
    private int maxRetries = 3;
    /** 相邻两次投递（按用户批次）之间的间隔毫秒 */
    private long sendIntervalMillis = 1000;
    /** Cross-instance lock TTL for the scan job. */
    private int scanLockTtlSeconds = 50;
    /** PROCESSING 超过该秒数视为 worker 崩溃，扫任务时回收为 PENDING */
    private int staleProcessingSeconds = 120;
    private Retry retry = new Retry();
    private Email email = new Email();
    private Mq mq = new Mq();
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class Email {
        private boolean enabled = false;
        private String from = "noreply@acmer.local";
    }

    @Data
    public static class Mq {
        /** 单机默认 false：进程内直调 delivery */
        private boolean enabled = false;
        private String exchange = "notify.exchange";
        private String queue = "notify.delivery.queue";
        private String routingKey = "notify.delivery";
    }

    @Data
    public static class RateLimit {
        /** 同一用户两次邮件之间的最短间隔（秒），Redis 限流 */
        private int userMinIntervalSeconds = 60;
    }

    @Data
    public static class Retry {
        private int baseDelaySeconds = 30;
    }
}
