# 数据库表结构文档

> 数据库：`acm`（MySQL 8.x，InnoDB，utf8mb4）  
> 依据：`src/main/resources/db/*.sql`、Entity、Mapper XML 整理  
> 更新日期：2026-07-07

---

## 1. 表总览

| 表名 | 模块 | 说明 | 建表脚本 |
|------|------|------|----------|
| `user` | 模块 1 用户 | 用户账号与资料、OJ 竞技字段 | 无独立脚本，见 §2.1（由代码反推） |
| `role` | 模块 1 用户 | 角色定义（USER / ADMIN） | 无独立脚本，见 §2.2 |
| `user_role` | 模块 1 用户 | 用户 ↔ 角色多对多 | 无独立脚本，见 §2.3 |
| `permission` | 模块 1 用户 | 细粒度权限（规划） | 文档提及，代码未使用 |
| `role_permission` | 模块 1 用户 | 角色 ↔ 权限（规划） | 文档提及，代码未使用 |
| `contest` | 模块 2 赛事 | 统一赛事表 | `init-contest.sql` |
| `contest_source` | 模块 2 爬虫 | 爬虫源配置 | `init-contest-crawl.sql` |
| `contest_crawl_log` | 模块 2 爬虫 | 爬虫执行日志 | `init-contest-crawl.sql` |

---

## 2. 模块 1：用户与权限

### 2.1 表关系

```text
user ──< user_role >── role
                         │
                    role_permission（未接入）
                         │
                    permission（未接入）
```

### 2.2 `user` — 用户表

> 业务代码：`UserEntity`、`UserMapper.xml`  
> 说明：账号信息与 ACM 竞技字段合并在单表，未单独拆 `user_profile`。

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT | NO | AUTO_INCREMENT | 主键 |
| `username` | VARCHAR(50) | NO | — | 登录名，唯一 |
| `password` | VARCHAR(255) | NO | — | BCrypt 密文 |
| `nickname` | VARCHAR(50) | YES | NULL | 昵称 |
| `email` | VARCHAR(100) | YES | NULL | 邮箱 |
| `avatar` | VARCHAR(500) | YES | NULL | 头像 URL |
| `gender` | TINYINT | YES | NULL | 性别 |
| `school` | VARCHAR(100) | YES | NULL | 学校 |
| `bio` | VARCHAR(500) | YES | NULL | 个人简介 |
| `cf_handle` | VARCHAR(64) | YES | NULL | Codeforces _handle |
| `atcoder_handle` | VARCHAR(64) | YES | NULL | AtCoder handle |
| `nowcoder_handle` | VARCHAR(64) | YES | NULL | 牛客 handle |
| `luogu_handle` | VARCHAR(64) | YES | NULL | 洛谷 handle |
| `cf_rating` | INT | YES | 0 | CF Rating |
| `solved_count` | INT | YES | 0 | 解题数 |
| `ac_count` | INT | YES | 0 | AC 数 |
| `contest_count` | INT | YES | 0 | 参赛数 |
| `status` | TINYINT | NO | 1 | 1 正常 0 禁用 |
| `last_login_time` | DATETIME | YES | NULL | 最后登录时间 |
| `created_time` | DATETIME | YES | CURRENT_TIMESTAMP | 创建时间 |
| `updated_time` | DATETIME | YES | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `deleted` | TINYINT | NO | 0 | 逻辑删除 0 否 1 是 |

**索引（推断）：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `id` |
| uk_username | UNIQUE | `username` |

---

### 2.3 `role` — 角色表

> 业务代码：`RoleEntity`、`RoleMapper.xml`  
> 初始化：`init-roles.sql`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `id` | BIGINT | NO | AUTO_INCREMENT | 主键 |
| `role_code` | VARCHAR(32) | NO | — | 角色编码，如 `USER`、`ADMIN`，唯一 |
| `role_name` | VARCHAR(64) | NO | — | 显示名 |
| `status` | TINYINT | NO | 1 | 1 启用 0 禁用 |
| `created_time` | DATETIME | YES | CURRENT_TIMESTAMP | 创建时间 |

**索引（推断）：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `id` |
| uk_role_code | UNIQUE | `role_code` |

**初始数据：**

```sql
INSERT IGNORE INTO role (role_code, role_name, status) VALUES
('USER',  '普通用户', 1),
('ADMIN', '管理员',   1);
```

---

### 2.4 `user_role` — 用户角色关联表

> 业务代码：`RoleMapper.insertUserRole`、`selectRoleCodesByUserId`

| 字段 | 类型 | 空 | 默认 | 说明 |
|------|------|----|------|------|
| `user_id` | BIGINT | NO | — | 用户 ID，FK → `user.id` |
| `role_id` | BIGINT | NO | — | 角色 ID，FK → `role.id` |

**索引（推断）：**

| 索引名 | 类型 | 字段 |
|--------|------|------|
| PRIMARY | 主键 | `user_id`, `role_id` |

**设置管理员示例：**

```sql
INSERT INTO user_role (user_id, role_id)
SELECT <用户ID>, id FROM role WHERE role_code = 'ADMIN';
```

---

### 2.5 `permission` / `role_permission`（规划）

模块 1 文档中提及「表已建、代码未用」，当前仓库内**无建表脚本、无 Entity/Mapper**。后续细粒度 RBAC 迭代时补充。

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

## 4. ER 关系简图

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
└───────────────┘
```

---

## 5. 初始化脚本清单

| 文件 | 内容 |
|------|------|
| `src/main/resources/db/init-contest.sql` | 建表 `contest` |
| `src/main/resources/db/init-contest-crawl.sql` | 建表 `contest_source`、`contest_crawl_log` + 平台初始数据 |
| `src/main/resources/db/init-roles.sql` | 初始化 `role` 数据（USER / ADMIN） |

**建议执行顺序：**

```text
1. user / role / user_role（手动或历史脚本，仓库内暂无 CREATE）
2. init-roles.sql
3. init-contest.sql
4. init-contest-crawl.sql
```

---

## 6. 备注

- 连接配置见 `application.yml`：`jdbc:mysql://localhost:3306/acm`
- 模块 1 用户表结构根据 Entity/Mapper **反推**，若线库字段有差异以实际 DDL 为准
- 后续模块（订阅、社交等）表尚未创建，不在本文档范围内
