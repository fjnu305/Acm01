# 数据库表结构文档

> 数据库：`acm`（MySQL 8.x，InnoDB，utf8mb4）  
> 依据：`src/main/resources/db/*.sql`、Entity、Mapper XML 整理  
> 更新日期：2026-08-23

---

## 1. 表总览

| 表名 | 模块 | 说明 | 建表脚本 |
|------|------|------|----------|
| `user` | 模块 1 用户 | 用户账号与资料、OJ 竞技字段 | `init-user.sql` |
| `role` | 模块 1 用户 | 角色定义（USER / ADMIN） | `init-rbac.sql` |
| `user_role` | 模块 1 用户 | 用户 ↔ 角色多对多 | `init-rbac.sql` |
| `permission` | 模块 1 用户 | 细粒度权限（表已建，代码未用） | `init-rbac.sql` |
| `role_permission` | 模块 1 用户 | 角色 ↔ 权限（表已建，代码未用） | `init-rbac.sql` |
| `contest` | 模块 2 赛事 | 统一赛事表 | `init-contest.sql` |
| `contest_source` | 模块 2 爬虫 | 爬虫源配置 | `init-contest-crawl.sql` |
| `contest_crawl_log` | 模块 2 爬虫 | 爬虫执行日志 | `init-contest-crawl.sql` |
| `contest_subscription` | 模块 3 订阅 | 用户赛事订阅意图 | `init-subscription.sql` |
| `notify_task` | 模块 3 订阅 | 待执行的邮件提醒任务 | `init-subscription.sql` |
| `notify_log` | 模块 4 通知 | 邮件发送审计 | `init-subscription.sql` |
| `solution` | 模块 8 题解 | 题解主表 | `init-solution.sql` |
| `solution_favorite` | 模块 8 题解 | 题解收藏 | `init-solution.sql` |
| `solution_template` | 模块 8 题解 | 算法模板库 | `init-solution.sql` |
| `team_post` | 模块 7 组队 | 组队帖 | `init-team.sql` |
| `team_member` | 模块 7 组队 | 成员与邀请 | `init-team.sql` |
| `match_record` | 模块 7 组队 | 匹配推荐记录 | `init-team.sql` |
| `oj_account` | 模块 10 OJ 同步 | OJ 账号绑定 | `init-oj-sync.sql` |
| `user_rating_snapshot` | 模块 10 OJ 同步 | Rating 每日快照 | `init-oj-sync.sql` |
| `topic` | 模块 6 社交 | 话题 | `init-social.sql` |
| `post` | 模块 6 社交 | 动态 | `init-social.sql` |
| `comment` | 模块 6 社交 | 评论 | `init-social.sql` |
| `post_like` | 模块 6 社交 | 点赞 | `init-social.sql` |
| `follow` | 模块 6 社交 | 关注 | `init-social.sql` |

---

## 2. 模块 1：用户与权限

### 2.1 表关系

```text
user ──< user_role >── role
                         │
                    role_permission（表已建，代码未用）
                         │
                    permission（表已建，代码未用）
```

### 2.2 `user` — 用户表

> 业务代码：`UserEntity`、`UserMapper.xml`  
> 建表脚本：`init-user.sql`  
> 说明：账号信息与 ACM 竞技字段合并在单表，未单独拆 `user_profile`。  
> 字符集：`utf8mb4` / `utf8mb4_0900_ai_ci`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT | NO | AUTO_INCREMENT | 用户 ID，主键 |
| `username` | VARCHAR(50) | NO | — | 登录名，唯一 |
| `password` | VARCHAR(255) | NO | — | BCrypt 密文 |
| `nickname` | VARCHAR(50) | YES | NULL | 昵称 |
| `email` | VARCHAR(100) | YES | NULL | 邮箱（邮件提醒收件地址） |
| `avatar` | VARCHAR(500) | YES | NULL | 头像 URL（本地上传存 `/uploads/avatars/...`） |
| `gender` | TINYINT | YES | 0 | 性别：0 未知，1 男，2 女 |
| `school` | VARCHAR(100) | YES | NULL | 学校 |
| `bio` | VARCHAR(500) | YES | NULL | 个人简介 |
| `cf_handle` | VARCHAR(50) | YES | NULL | Codeforces 账号 |
| `atcoder_handle` | VARCHAR(50) | YES | NULL | AtCoder 账号 |
| `nowcoder_handle` | VARCHAR(50) | YES | NULL | 牛客账号 |
| `luogu_handle` | VARCHAR(50) | YES | NULL | 洛谷账号 |
| `cf_rating` | INT | YES | 0 | Codeforces Rating |
| `solved_count` | INT | YES | 0 | 总刷题数 |
| `ac_count` | INT | YES | 0 | AC 题目数 |
| `contest_count` | INT | YES | 0 | 参赛场次 |
| `status` | TINYINT | YES | 1 | 状态：1 正常，0 禁用 |
| `last_login_time` | DATETIME | YES | NULL | 最后登录时间 |
| `created_time` | DATETIME | YES | CURRENT_TIMESTAMP | 创建时间 |
| `updated_time` | DATETIME | YES | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| `deleted` | TINYINT | YES | 0 | 逻辑删除：0 未删除，1 已删除 |

**索引：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `id` |
| `username` | UNIQUE | `username` |
| `idx_cf_rating` | 普通 | `cf_rating` |
| `idx_solved_count` | 普通 | `solved_count` |
| `idx_status` | 普通 | `status` |

**建表 DDL（与线上一致）：**

```sql
-- 见 src/main/resources/db/init-user.sql
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) NULL DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) NULL DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(500) NULL DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT NULL DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `school` VARCHAR(100) NULL DEFAULT NULL COMMENT '学校',
    `bio` VARCHAR(500) NULL DEFAULT NULL COMMENT '个人简介',
    `cf_handle` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Codeforces账号',
    `atcoder_handle` VARCHAR(50) NULL DEFAULT NULL COMMENT 'AtCoder账号',
    `nowcoder_handle` VARCHAR(50) NULL DEFAULT NULL COMMENT '牛客账号',
    `luogu_handle` VARCHAR(50) NULL DEFAULT NULL COMMENT '洛谷账号',
    `cf_rating` INT NULL DEFAULT 0 COMMENT 'Codeforces Rating',
    `solved_count` INT NULL DEFAULT 0 COMMENT '总刷题数',
    `ac_count` INT NULL DEFAULT 0 COMMENT 'AC题目数',
    `contest_count` INT NULL DEFAULT 0 COMMENT '参赛场次',
    `status` TINYINT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
    `last_login_time` DATETIME NULL DEFAULT NULL COMMENT '最后登录时间',
    `created_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `username` (`username`),
    INDEX `idx_cf_rating` (`cf_rating`),
    INDEX `idx_solved_count` (`solved_count`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
```

---

### 2.3 `role` — 角色表

> 业务代码：`RoleEntity`、`RoleMapper.xml`  
> 建表脚本：`init-rbac.sql`  
> 字符集：`utf8mb4` / `utf8mb4_0900_ai_ci`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT | NO | AUTO_INCREMENT | 主键 |
| `role_code` | VARCHAR(50) | NO | — | 角色编码，如 `USER`、`ADMIN`，唯一 |
| `role_name` | VARCHAR(50) | NO | — | 角色名称 |
| `status` | TINYINT | YES | 1 | 状态：1 启用，0 禁用 |
| `created_time` | DATETIME | YES | CURRENT_TIMESTAMP | 创建时间 |

**索引：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `id` |
| `role_code` | UNIQUE | `role_code` |

**初始数据：**

```sql
INSERT IGNORE INTO role (role_code, role_name, status) VALUES
('USER',  '普通用户', 1),
('ADMIN', '管理员',   1);
```

---

### 2.4 `user_role` — 用户角色关联表

> 业务代码：`RoleMapper.insertUserRole`、`selectRoleCodesByUserId`  
> 建表脚本：`init-rbac.sql`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `user_id` | BIGINT | NO | — | 用户 ID，FK → `user.id` |
| `role_id` | BIGINT | NO | — | 角色 ID，FK → `role.id` |

**索引：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `user_id`, `role_id` |

**设置管理员示例：**

```sql
INSERT INTO user_role (user_id, role_id)
SELECT <用户ID>, id FROM role WHERE role_code = 'ADMIN';
```

---

### 2.5 `permission` — 权限表

> 建表脚本：`init-rbac.sql`  
> 说明：线库已建表并有初始权限数据；**业务代码尚未接入**（当前鉴权仅用 `role` + `user_role`）。

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT | NO | AUTO_INCREMENT | 主键 |
| `perm_code` | VARCHAR(100) | NO | — | 权限编码，唯一 |
| `perm_name` | VARCHAR(50) | NO | — | 权限名称 |
| `created_time` | DATETIME | YES | CURRENT_TIMESTAMP | 创建时间 |

**索引：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `id` |
| `perm_code` | UNIQUE | `perm_code` |

---

### 2.6 `role_permission` — 角色权限关联表

> 建表脚本：`init-rbac.sql`  
> 说明：线库已建表；**业务代码尚未接入**。

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `role_id` | BIGINT | NO | — | 角色 ID，FK → `role.id` |
| `permission_id` | BIGINT | NO | — | 权限 ID，FK → `permission.id` |

**索引：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `role_id`, `permission_id` |

**建表 DDL（RBAC 四表，与线上一致）：**

```sql
-- 见 src/main/resources/db/init-rbac.sql
CREATE TABLE `role` ( ... );
CREATE TABLE `permission` ( ... );
CREATE TABLE `user_role` ( ... );
CREATE TABLE `role_permission` ( ... );
```

---

## 3. 模块 2：赛事与爬虫

### 3.1 `contest` — 统一赛事表

> 脚本：`src/main/resources/db/init-contest.sql`  
> 实体：`ContestEntity`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT | NO | AUTO_INCREMENT | 赛事 ID，对外 API 主键 |
| `source` | VARCHAR(32) | NO | — | 来源，对应 `ContestSource` 枚举 |
| `external_id` | VARCHAR(128) | NO | — | 平台原始 ID |
| `title` | VARCHAR(255) | NO | — | 赛事名称 |
| `description` | TEXT | YES | NULL | 赛事描述 |
| `url` | VARCHAR(500) | YES | NULL | 报名/详情链接 |
| `start_time` | DATETIME | NO | — | 开始时间 |
| `end_time` | DATETIME | YES | NULL | 结束时间 |
| `register_start` | DATETIME | YES | NULL | 报名开始 |
| `register_end` | DATETIME | YES | NULL | 报名截止 |
| `status` | TINYINT | YES | 1 | 1 即将开始 2 进行中 3 已结束 |
| `difficulty` | VARCHAR(32) | YES | NULL | 难度标签，如 Div.2 |
| `contest_type` | VARCHAR(32) | YES | NULL | 赛事类型，如 CF / ICPC |
| `location` | VARCHAR(100) | YES | NULL | 线上/线下地点 |
| `raw_hash` | VARCHAR(64) | YES | NULL | 原始数据 SHA-256，增量更新判断 |
| `last_crawled_at` | DATETIME | YES | NULL | 最后爬取时间 |
| `created_time` | DATETIME | YES | CURRENT_TIMESTAMP | 创建时间 |
| `updated_time` | DATETIME | YES | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `deleted` | TINYINT | YES | 0 | 逻辑删除 |

**索引：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `id` |
| uk_source_external | UNIQUE | `source`, `external_id` |
| idx_start_time | INDEX | `start_time` |
| idx_source | INDEX | `source` |
| idx_status | INDEX | `status` |

**设计要点：**

- `source + external_id` 唯一 → 同平台同场比赛不重复 INSERT
- `raw_hash` 未变 → SKIP UPDATE；变化 → UPDATE 并刷新 `last_crawled_at`

---

### 3.2 `contest_source` — 爬虫源配置表

> 脚本：`src/main/resources/db/init-contest-crawl.sql`  
> 实体：`ContestSourceEntity`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT | NO | AUTO_INCREMENT | 主键 |
| `source_code` | VARCHAR(32) | NO | — | 平台编码，对应 `ContestSource.value`，唯一 |
| `source_name` | VARCHAR(64) | NO | — | 平台显示名 |
| `crawl_enabled` | TINYINT | YES | 1 | 是否启用爬虫 1 是 0 否 |
| `crawl_cron` | VARCHAR(64) | YES | `0 0 */6 * * ?` | Quartz cron 表达式 |
| `rate_limit_sec` | INT | YES | 3 | 两次请求最小间隔（秒） |
| `last_crawl_time` | DATETIME | YES | NULL | 上次爬取时间 |
| `last_crawl_status` | VARCHAR(32) | YES | NULL | 上次结果：`SUCCESS` / `FAILED` |
| `fail_count` | INT | YES | 0 | 连续失败次数 |
| `created_time` | DATETIME | YES | CURRENT_TIMESTAMP | 创建时间 |

**索引：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `id` |
| uk_source_code | UNIQUE | `source_code` |

**初始数据：**

| source_code | source_name | crawl_enabled | rate_limit_sec |
|-------------|-------------|---------------|----------------|
| codeforces | Codeforces | 1 | 2 |
| atcoder | AtCoder | 1 | 3 |
| nowcoder | 牛客网 | 1 | 3 |
| luogu | 洛谷 | 1 | 3 |
| ccpc | CCPC | 1 | 5 |
| icpc | ICPC | 1 | 5 |
| lanqiao | 蓝桥杯 | 1 | 5 |

---

### 3.3 `contest_crawl_log` — 爬虫执行日志

> 脚本：`src/main/resources/db/init-contest-crawl.sql`  
> 实体：`ContestCrawlLogEntity`  
> 写入：`CrawlLogService`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT | NO | AUTO_INCREMENT | 日志 ID |
| `source` | VARCHAR(32) | NO | — | 平台 `ContestSource.value` |
| `status` | VARCHAR(16) | NO | — | `SUCCESS` / `FAILED` |
| `trigger_type` | VARCHAR(16) | NO | `AUTO` | `AUTO` 定时 / `MANUAL` 手动 |
| `error_type` | VARCHAR(32) | YES | NULL | `HTTP_ERROR` / `API_ERROR` / `PARSE_ERROR` / `TIMEOUT` / `UNKNOWN` |
| `http_status` | INT | YES | NULL | 最后一次 HTTP 状态码 |
| `request_url` | VARCHAR(500) | YES | NULL | 主请求 URL |
| `max_retries` | TINYINT | NO | 3 | 配置的最大重试次数 |
| `total_attempts` | TINYINT | NO | 1 | 实际尝试次数（含首次） |
| `fetched_count` | INT | NO | 0 | 成功解析条数，失败为 0 |
| `elapsed_ms` | INT | YES | NULL | 本次耗时（毫秒） |
| `error_message` | VARCHAR(1000) | YES | NULL | 错误摘要 |
| `error_detail` | TEXT | YES | NULL | 堆栈或响应片段 |
| `created_time` | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |

**索引：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `id` |
| idx_source_time | INDEX | `source`, `created_time` |
| idx_status_time | INDEX | `status`, `created_time` |
| idx_error_type | INDEX | `error_type` |

**与 `contest_source` 联动：**

| 爬取结果 | contest_source 更新 |
|----------|---------------------|
| SUCCESS | `last_crawl_status=SUCCESS`，`fail_count=0` |
| FAILED | `last_crawl_status=FAILED`，`fail_count+1` |

---

## 4. 模块 3：赛事订阅与提醒

### 4.1 表关系

```text
user ──< contest_subscription >── contest
              │
              ▼
         notify_task ──> notify_log（EMAIL）
```

**业务流：**

```text
订阅 API → contest_subscription
         → notify_task（scheduled_at = contest.start_time - remind_before_minutes）
Quartz NotifyScanJob 扫描到期任务 → 发邮件 → 写 notify_log
```

### 4.2 `contest_subscription` — 用户赛事订阅

> 脚本：`src/main/resources/db/init-subscription.sql`  
> 实体：`ContestSubscriptionEntity`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT | NO | AUTO_INCREMENT | 主键 |
| `user_id` | BIGINT | NO | — | 用户 ID，FK → `user.id` |
| `contest_id` | BIGINT | NO | — | 赛事 ID，FK → `contest.id` |
| `remind_before_minutes` | INT | NO | — | 赛前提醒分钟数，如 `1440`（24h）、`60`（1h） |
| `channel` | VARCHAR(16) | NO | `EMAIL` | 固定为邮件渠道 |
| `status` | TINYINT | NO | 1 | 1 生效 / 0 已取消 |
| `created_time` | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |
| `updated_time` | DATETIME | NO | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `id` |
| uk_user_contest_remind | UNIQUE | `user_id`, `contest_id`, `remind_before_minutes` |
| idx_user_status | INDEX | `user_id`, `status` |

**设计要点：**

- 同一用户对同一场比赛可同时订阅多个提醒档位（如 24h + 1h），每条档位一行
- 唯一约束不含 `status`，取消后复订走「reactivate」而非重复 INSERT

---

### 4.3 `notify_task` — 待执行提醒任务

> 脚本：`src/main/resources/db/init-subscription.sql`  
> 实体：`org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity`  
> 生成：`org.fjnu305.acm01.module.notify.writeTask.service.NotifyTaskScheduleService.schedule`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT | NO | AUTO_INCREMENT | 主键 |
| `subscription_id` | BIGINT | NO | — | 来源订阅 ID，FK → `contest_subscription.id` |
| `channel` | VARCHAR(16) | NO | `EMAIL` | 固定为邮件渠道 |
| `scheduled_at` | DATETIME | NO | — | 计划发送时刻 = `contest.start_time - remind_before_minutes` |
| `status` | VARCHAR(16) | NO | `PENDING` | `PENDING` / `SENT` / `FAILED` / `CANCELLED` |
| `sent_at` | DATETIME | YES | NULL | 实际发送时间 |
| `retry_count` | INT | NO | 0 | 失败重试次数 |
| `error_message` | VARCHAR(500) | YES | NULL | 最后一次失败原因 |
| `idempotent_key` | VARCHAR(128) | NO | — | 幂等键，防重复发送 |
| `created_time` | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |

**幂等键格式：**

```text
{userId}:{contestId}:{channel}:{remind_before_minutes}
```

**索引：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `id` |
| uk_idempotent | UNIQUE | `idempotent_key` |
| idx_scan | INDEX | `status`, `scheduled_at` |
| idx_subscription | INDEX | `subscription_id` |

**扫描 SQL（NotifyScanJob）：**

```sql
SELECT * FROM notify_task
WHERE status IN ('PENDING', 'FAILED')
  AND scheduled_at <= NOW()
ORDER BY scheduled_at
LIMIT 200
```

---

### 4.4 `notify_log` — 邮件发送审计

> 脚本：`src/main/resources/db/init-subscription.sql`  
> 实体：`org.fjnu305.acm01.module.notify.entity.NotifyLogEntity`  
> 写入：`org.fjnu305.acm01.module.notify.delivery.emailhandler.EmailNotifyHandler`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT | NO | AUTO_INCREMENT | 主键 |
| `notify_task_id` | BIGINT | NO | — | 对应任务 ID，FK → `notify_task.id` |
| `user_id` | BIGINT | NO | — | 用户 ID |
| `channel` | VARCHAR(16) | NO | `EMAIL` | 固定为邮件渠道 |
| `target` | VARCHAR(255) | YES | NULL | 投递目标，如邮箱地址 |
| `status` | VARCHAR(16) | NO | — | `SUCCESS` / `FAILED` |
| `provider_msg_id` | VARCHAR(128) | YES | NULL | 邮件服务商回执 ID（可选） |
| `error_message` | VARCHAR(500) | YES | NULL | 失败原因 |
| `created_time` | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |

**索引：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `id` |
| idx_task | INDEX | `notify_task_id` |

**与 `notify_task` 关系：**

- 邮件是否成功以 `notify_log` 为准
- `notify_task.status = SENT` 表示业务上已处理完毕（含失败记入日志后的终态）

---

## 5. 模块 7：组队匹配

> 脚本：`src/main/resources/db/init-team.sql`  
> 文档：[`module-7-team.md`](module-7-team.md)

### 5.1 `team_post`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `user_id` | BIGINT | 发布者 |
| `title` | VARCHAR(255) | 标题 |
| `description` | TEXT | 描述 |
| `rating_min` / `rating_max` | INT | 期望 Rating 区间 |
| `region` | VARCHAR(100) | 地区/学校 |
| `tags` | VARCHAR(500) | 算法标签，逗号分隔 |
| `member_limit` | INT | 目标人数 |
| `current_count` | INT | 当前人数 |
| `status` | TINYINT | 1 招募中 · 2 已满 · 0 关闭 |

### 5.2 `team_member` / `match_record`

- `team_member`：`(team_post_id, user_id)` 唯一；`status` 0 待接受 / 1 已加入
- `match_record`：每次推荐写入 `match_score` 快照

---

## 6. 模块 8：题解分享

> 脚本：`src/main/resources/db/init-solution.sql`  
> 文档：[`module-8-solution.md`](module-8-solution.md)

### 6.1 `solution`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `user_id` | BIGINT | 作者 |
| `title` | VARCHAR(255) | 标题 |
| `content` | MEDIUMTEXT | Markdown（入库前 XSS 消毒） |
| `problem_source` / `problem_id` | VARCHAR | 题目来源与编号 |
| `tags` | VARCHAR(500) | 标签 |
| `status` | TINYINT | 1 发布 · 0 草稿 · 2 下架 |
| `favorite_count` / `view_count` | INT | 计数 |

### 6.2 `solution_favorite` / `solution_template`

- 收藏：`(user_id, solution_id)` 联合主键
- 模板：系统预置二分、Dijkstra 等模板

---

## 7. ER 关系简图

```text
┌─────────┐       ┌───────────┐       ┌─────────┐
│  user   │───<──│ user_role │──>───│  role   │
└─────────┘       └───────────┘       └─────────┘

┌───────────────┐         ┌──────────────────┐
│ contest_source│         │ contest_crawl_log│
│ (按 source)   │         │ (按 source 日志)  │
└───────┬───────┘         └──────────────────┘
        │ 配置驱动爬取
        ▼
┌───────────────┐
│    contest    │  uk(source, external_id)
└───────┬───────┘
        │
        ▼
┌─────────────────────┐       ┌─────────────┐
│ contest_subscription│───<──│ notify_task │──> notify_log
└──────────┬──────────┘
           │
      user └──────────────────────────────────
```

---

## 8. 初始化脚本清单

| 文件 | 内容 |
|------|------|
| `src/main/resources/db/init-contest.sql` | 建表 `contest` |
| `src/main/resources/db/init-contest-crawl.sql` | 建表 `contest_source`、`contest_crawl_log` + 平台初始数据 |
| `src/main/resources/db/init-user.sql` | 建表 `user` |
| `src/main/resources/db/init-rbac.sql` | 建表 `role`、`permission`、`user_role`、`role_permission` + 角色初始数据 |
| `src/main/resources/db/init-roles.sql` | 仅插入角色数据（已有表时用） |
| `src/main/resources/db/init-subscription.sql` | 建表 `contest_subscription`、`notify_task`、`notify_log` |
| `src/main/resources/db/init-oj-sync.sql` | 建表 `oj_account`、`user_rating_snapshot` |
| `src/main/resources/db/init-social.sql` | 建表 `topic`、`post`、`comment`、`post_like`、`follow` + 话题初始数据 |
| `src/main/resources/db/init-solution.sql` | 建表 `solution`、`solution_favorite`、`solution_template` |
| `src/main/resources/db/init-team.sql` | 建表 `team_post`、`team_member`、`match_record` |

**建议执行顺序：**

```text
1. init-user.sql
2. init-rbac.sql
3. init-contest.sql
4. init-contest-crawl.sql
5. init-subscription.sql
6. init-oj-sync.sql
7. init-social.sql
8. init-solution.sql
9. init-team.sql
```

---

## 9. 备注

- 连接配置见 `application.yml`：`jdbc:mysql://localhost:3306/acm`
- `user` 表结构以 `init-user.sql` 及线库 DDL 为准（2026-07-12 校对）
- 旧库若缺少 `school`/`bio` 可执行 `alter-user-profile-fields.sql`
- 模块 7/8 表结构见本文档第 5、6 节；模块 6、10 见各模块文档
