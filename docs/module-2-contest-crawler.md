# 模块 2：赛事聚合与爬虫 — 实施方案

> 基于现有架构（模块 0 基础设施 + 模块 1 用户权限）的模块 2 设计与实施指南  
> 文档版本：v2.0 | 状态：**迭代 1 已落地，迭代 2 部分待做**

---

## 0. 当前实施状态（2026-07）

| 能力 | 状态 | 说明 |
|------|------|------|
| `contest` 表 + Mapper | ✅ 已完成 | `resources/db/init-contest.sql` |
| `contest_source` + `contest_crawl_log` | ✅ 已完成 | `resources/db/init-contest-crawl.sql` |
| `CodeforcesCrawler` 官方 API 爬取 | ✅ 已完成 | 过滤、映射、入库全链路 |
| 其余 6 平台 Crawler | ⏳ 占位 | 类已注册，`doFetch()` 返回空列表 |
| `CrawlPipeline`（fetch → persist → log） | ✅ 已完成 | 统一流水线 |
| `ContestPersistService` 批量入库 | ✅ 已完成 | 1 次 SELECT + batch INSERT + JDBC batch UPDATE |
| `ContestCrawlService` + `CrawlerRegistry` | ✅ 已完成 | 开关、策略查找、手动/批量触发入口 |
| `CrawlHttpClient` OkHttp + 重试 | ✅ 已完成 | `contest.crawl.http.*` 配置 |
| `GET /api/contests` 分页列表 | ✅ 已完成 | 支持 `source` / `status` 筛选 |
| `POST /api/admin/contests/crawl/{source}` | ✅ 已完成 | ADMIN 手动触发，返回入库统计 |
| Security 放行 `/api/contests/**` | ✅ 已完成 | `SecurityConfig` |
| 前端 Codeforces 列表页 | ✅ 已完成 | `CodeforcesContestListPage.tsx` |
| 管理端一键爬取 | ✅ 已完成 | `AdminHomePage` → `triggerContestCrawl()` |
| Quartz 定时 Job | ⏳ 占位 | `ContestCrawlJob` / `ContestStatusRefreshJob` 空类 |
| Redis 赛事缓存 / 爬虫限流 | ⏳ 占位 | `ContestCacheService` 空类；限流未接入 |
| 详情 / 日历 / 热门 API | ❌ 未做 | 仅列表接口 |
| 管理端源配置 API | ❌ 未做 | 无 `GET/PUT contest-sources` |

**迭代 1 验收：** Codeforces 手动爬取 → 写入 `contest` → 前端列表可展示。✅ 已达成。

---

## 1. 模块目标

| 目标 | 说明 |
|------|------|
| 数据采集 | 定时 / 手动从各 OJ / 官方站点抓取赛事信息 |
| 数据归一 | 多平台异构数据统一写入 `contest` 表 |
| 对外查询 | 提供列表 / 详情 / 日历 API，供模块 3（订阅）、模块 6（讨论）使用 |
| 性能优化 | 热门赛事 Redis 缓存；爬虫 Redis 限流防 IP 封禁（迭代 2） |

**本模块不做：**

- 用户赛事订阅（模块 3）
- 社交 / 动态（模块 6）

---

## 2. 与现有架构的衔接

### 2.1 已有可复用能力

| 已有组件 | 路径 / 说明 | 模块 2 用途 |
|----------|-------------|-------------|
| 赛事来源枚举 | `Common/enums/ContestSource.java` | 与 `contest.source` 字段对齐 |
| 爬虫触发 / 日志枚举 | `CrawlTriggerType` / `CrawlLogStatus` / `CrawlErrorType` | `contest_crawl_log` |
| 统一响应 | `Common/result/Result.java`、`PageResult.java` | 赛事 API 返回格式 |
| 异常体系 | `Common/exception/*`、`CrawlFetchException` | 爬虫失败结构化 + 写日志 |
| Redis | `Config/RedisConfig.java` | 缓存 + 限流（待接入） |
| Security | `Config/SecurityConfig.java` | 赛事查询公开；管理爬取需 ADMIN |
| 用户权限 | `module/user` + ADMIN 角色 | 手动触发爬虫 |

### 2.2 读写分离的包结构（当前实现）

爬虫是**旁路写入系统**，与查询侧分离：

```text
module/contest/
├── controller/              # HTTP 入口
│   ├── ContestController.java          # 公开查询
│   └── ContestAdminController.java     # 管理端爬取
├── query/service/           # 读侧
│   ├── ContestService.java             # 分页列表
│   └── ContestCacheService.java        # Redis 缓存（占位）
├── crawl/                   # 写侧编排
│   ├── pipeline/
│   │   ├── CrawlPipeline.java          # fetch → persist → log
│   │   └── CrawlOutcome.java           # 成功/失败结果载体
│   ├── registry/
│   │   └── CrawlerRegistry.java        # 按 ContestSource 找策略
│   ├── config/
│   │   └── CrawlSourceConfig.java      # 读 contest_source.crawl_enabled
│   ├── service/
│   │   ├── ContestCrawlService.java    # 触发入口
│   │   ├── ContestPersistService.java  # 批量入库
│   │   └── CrawlLogService.java        # 写 contest_crawl_log
│   └── job/
│       └── ContestCrawlJob.java        # Quartz 占位
├── crawler/                 # 各平台拉取策略
│   ├── ContestCrawler.java
│   ├── AbstractContestCrawler.java     # fetchContests 模板 + rawHash
│   ├── exception/CrawlFetchException.java
│   ├── http/
│   │   ├── CrawlHttpClient.java        # OkHttp + 重试
│   │   └── CrawlHttpProperties.java
│   ├── dto/CodeforcesContestListResponse.java
│   └── strategy/
│       ├── CodeforcesCrawler.java      # ✅ 已实现
│       └── AtCoder / Nowcoder / ...    # ⏳ 占位
├── job/
│   └── ContestStatusRefreshJob.java    # Quartz 占位
├── mapper/
│   ├── ContestMapper.java
│   ├── ContestMapperSupport.java       # JDBC batch UPDATE
│   ├── ContestSourceMapper.java
│   └── ContestCrawlLogMapper.java
├── entity/
│   ├── ContestEntity.java
│   ├── ContestSourceEntity.java
│   └── ContestCrawlLogEntity.java
└── dto/
    ├── ContestDTO.java                 # 爬虫归一化对象
    ├── ContestVO.java                  # API 返回
    ├── ContestQueryRequest.java
    ├── ContestCrawlResult.java         # 单次爬取完整结果
    ├── ContestPersistResult.java       # insert/update/skip 统计
    └── ContestCalendarVO.java          # 日历（未用）
```

**设计原则：** 爬虫不侵入 `AuthService`；**写**走 `crawl` 包，**读**走 `query/service` + `ContestController`。

---

## 3. 数据库设计

> 建表脚本：`resources/db/init-contest.sql`、`resources/db/init-contest-crawl.sql`  
> 字段说明详见：`docs/database-schema.md`

### 3.1 `contest` — 统一赛事表（核心）

```sql
USE acm;

CREATE TABLE contest (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '赛事ID',
    source          VARCHAR(32)  NOT NULL COMMENT '来源，对应 ContestSource 枚举',
    external_id     VARCHAR(128) NOT NULL COMMENT '平台原始ID',
    title           VARCHAR(255) NOT NULL COMMENT '赛事名称',
    description     TEXT COMMENT '赛事描述',
    url             VARCHAR(500) COMMENT '报名/详情链接',
    start_time      DATETIME     NOT NULL COMMENT '开始时间',
    end_time        DATETIME COMMENT '结束时间',
    register_start  DATETIME COMMENT '报名开始',
    register_end    DATETIME COMMENT '报名截止',
    status          TINYINT DEFAULT 1 COMMENT '1即将开始 2进行中 3已结束',
    difficulty      VARCHAR(32) COMMENT '难度标签',
    contest_type    VARCHAR(32) COMMENT '平台赛制/类型字段',
    location        VARCHAR(100) COMMENT '线上/线下地点',
    raw_hash        VARCHAR(64) COMMENT '原始数据哈希，用于增量判断',
    last_crawled_at DATETIME COMMENT '最后爬取时间',
    created_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_source_external (source, external_id),
    INDEX idx_start_time (start_time),
    INDEX idx_source (source),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一赛事表';
```

| 设计点 | 原因 |
|--------|------|
| `source + external_id` 唯一键 | 去重，同一平台同一场比赛不重复插入 |
| `raw_hash` | 增量更新：hash 未变则 SKIP，减少无意义 UPDATE |
| 自增 `id` 作为对外 ID | 模块 3 订阅表外键简单 |

### 3.2 `contest_source` — 爬虫源配置表

```sql
CREATE TABLE contest_source (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_code       VARCHAR(32) NOT NULL UNIQUE COMMENT '对应 ContestSource 枚举值',
    source_name       VARCHAR(64) NOT NULL COMMENT '平台显示名',
    crawl_enabled     TINYINT DEFAULT 1 COMMENT '是否启用爬虫 1是 0否',
    crawl_cron        VARCHAR(64) DEFAULT '0 0 */6 * * ?' COMMENT 'Quartz cron 表达式',
    rate_limit_sec    INT DEFAULT 3 COMMENT '两次请求最小间隔（秒）',
    last_crawl_time   DATETIME COMMENT '上次爬取时间',
    last_crawl_status VARCHAR(32) COMMENT 'SUCCESS / FAILED',
    fail_count        INT DEFAULT 0 COMMENT '连续失败次数',
    created_time      DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爬虫源配置表';
```

开关读取：**唯一入口** `CrawlSourceConfig.isCrawlEnabled()`；库中无行时默认启用。

### 3.3 `contest_crawl_log` — 爬虫执行日志（已实现）

每次爬取（成功或失败）写 **一条** 日志；HTTP 重试次数记录在 `total_attempts`。

| 字段 | 说明 |
|------|------|
| `trigger_type` | `AUTO` / `MANUAL` |
| `status` | `SUCCESS` / `FAILED` |
| `error_type` | `HTTP_ERROR` / `API_ERROR` / `PARSE_ERROR` / `TIMEOUT` / `UNKNOWN` |
| `fetched_count` | 成功时解析条数；失败为 0 |
| `elapsed_ms` | 本次总耗时 |

### 3.4 初始化数据

见 `init-contest-crawl.sql` 末尾 `INSERT IGNORE INTO contest_source ...`（7 个平台）。

---

## 4. 核心架构

### 4.1 爬虫流水线（当前主流程）

```text
ContestAdminController / ContestCrawlJob（待接）
        ↓
ContestCrawlService.crawl(source, triggerType)
  ① CrawlSourceConfig 检查 crawl_enabled
  ② CrawlerRegistry 获取对应 Crawler
        ↓
CrawlPipeline.execute(crawler, triggerType)
  ③ crawler.fetchContests()           → List<ContestDTO>
  ④ ContestPersistService.persistAll() → batch SELECT / INSERT / UPDATE
  ⑤ CrawlLogService.record()          → contest_crawl_log + 更新 contest_source
        ↓
返回 ContestCrawlResult（含 persist 统计 + elapsedMs）
```

### 4.2 策略接口 — `ContestCrawler`

各平台**只负责拉取与映射**，不含开关、入库、日志：

| 方法 | 作用 |
|------|------|
| `getSource()` | 返回 `ContestSource` |
| `getPrimaryRequestUrl()` | 主请求 URL，写入 `contest_crawl_log.request_url` |
| `fetchContests()` | 拉取并解析；失败抛 `CrawlFetchException` |

> 原设计中的 `enabled()` 已移除，改由 `CrawlSourceConfig` 统一读取 `contest_source.crawl_enabled`。

### 4.3 批量入库 — `ContestPersistService.persistAll`

```text
for each ContestDTO:
  校验 source / externalId / title / startTime → 不合格计入 ignored
  按 (source, externalId) 去重（同批内后者覆盖）

batch SELECT raw_hash WHERE (source, external_id) IN (...)

split:
  不存在           → toInsert
  raw_hash 变化    → toUpdate
  raw_hash 相同    → skipped

contestMapper.batchInsert(toInsert)
contestMapperSupport.batchUpdateBySourceAndExternalId(toUpdate)  // SqlSession BATCH
```

### 4.4 `raw_hash` 计算 — `AbstractContestCrawler.computeRawHash`

参与 hash 的字段（`|` 拼接后 SHA-256）：

`source | externalId | title | startTime | endTime | url | difficulty`

> `contestType`、`status` 等**不在** hash 中；若仅 status 随时间变化，需靠 `ContestStatusRefreshJob`（待实现）刷新。

### 4.5 Codeforces 字段映射（迭代 1 参考实现）

**API：** `GET https://codeforces.com/api/contest.list`

| contest 列 | 来源 | 说明 |
|-----------|------|------|
| `external_id` | `id` | 字符串化 |
| `title` | `name` | 去首尾空格 |
| `url` | 模板 | `https://codeforces.com/contest/{id}` |
| `start_time` | `startTimeSeconds` | Unix 秒 → `Asia/Shanghai` |
| `end_time` | 计算 | `startTime + durationSeconds` |
| `register_start` / `register_end` | — | **API 不提供**，保持 NULL |
| `status` | `phase` 映射 | BEFORE→1, CODING/系统测试→2, FINISHED→3；未知则按时间推断 |
| `contest_type` | `type` **原样** | CF 官方枚举：`CF` / `ICPC` / `IOI`（见下） |
| `difficulty` | **标题解析** | 与 `contest_type` 无关 |
| `location` | 固定 | `"Online"` |

**入库过滤：**

- `type` 白名单：仅 `CF`、`ICPC`；`IOI` 等丢弃
- `startTimeSeconds` 无效则跳过
- `FINISHED` 且开始时间早于 90 天前则跳过（控制历史数据量）

**`contest_type` vs `difficulty`（易混淆）：**

| 字段 | 含义 | 示例 |
|------|------|------|
| `contest_type` | Codeforces API 的**计分/赛制**字段 `type`，直接映射 | `CF`、`ICPC` |
| `difficulty` | 从**赛事名称**提取的 Div / Educational 标签 | `Div.2`、`Div.1+2`、`Educational` |

`type=ICPC` 在 CF 里常指 Educational Round 等 ICPC 赛制，**不等于** CCPC 现场赛。

**难度解析规则（`extractDifficulty`）：**

- 扫描标题中所有 `Div. 1~4`
- 单个 → `Div.2`
- 多个 → `Div.1+2`（如 *Div. 1 + Div. 2*）
- 无 Div 且标题含 `educational` → `Educational`

### 4.6 HTTP 重试 — `CrawlHttpClient`

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `contest.crawl.http.max-retries` | 3 | 失败后重试次数（不含首次） |
| `retry-base-delay-millis` | 500 | 指数退避基数 |
| `read-timeout-seconds` | 30 | 读超时 |

重试耗尽抛 `CrawlFetchException`（含 `http_status`、`total_attempts`），`CrawlPipeline` 写失败日志后再抛出。

### 4.7 赛事状态刷新（待实现）

| Job | Cron（计划） | 状态 |
|-----|-------------|------|
| `ContestCrawlJob` | `0 0 */6 * * ?` | 空类，未接 Quartz |
| `ContestStatusRefreshJob` | `0 */10 * * * ?` | 空类，未接 Quartz |

---

## 5. API 设计

### 5.1 公开接口（无需登录）

| 方法 | 路径 | 状态 | 说明 |
|------|------|------|------|
| GET | `/api/contests` | ✅ | 分页列表；`source`、`status`、`pageNum`、`pageSize` |
| GET | `/api/contests/{id}` | ❌ | 赛事详情 |
| GET | `/api/contests/calendar` | ❌ | 日历视图 |
| GET | `/api/contests/hot` | ❌ | 热门 Top N |

**SecurityConfig（已配置）：**

```java
.requestMatchers("/api/auth/**", "/api/contests/**", "/error").permitAll()
```

### 5.2 管理接口（需 ADMIN）

| 方法 | 路径 | 状态 | 说明 |
|------|------|------|------|
| POST | `/api/admin/contests/crawl/{sourceCode}` | ✅ | 手动触发，如 `codeforces` |
| POST | `/api/admin/contests/crawl/all` | ❌ | 全平台爬取（`crawlAll` 已有，未暴露 HTTP） |
| GET | `/api/admin/contest-sources` | ❌ | 查看各平台状态 |
| PUT | `/api/admin/contest-sources/{code}` | ❌ | 修改开关 / cron / 限流 |

### 5.3 响应示例

**列表 `GET /api/contests?pageNum=1&pageSize=10&source=codeforces&status=1`**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "source": "codeforces",
        "externalId": "2106",
        "title": "Codeforces Round 1104 (Div. 1 + Div. 2)",
        "startTime": "2026-07-12T20:35:00",
        "endTime": "2026-07-12T22:35:00",
        "status": 1,
        "url": "https://codeforces.com/contest/2106",
        "difficulty": "Div.1+2",
        "contestType": "CF",
        "location": "Online"
      }
    ],
    "total": 42,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

**手动爬取 `POST /api/admin/contests/crawl/codeforces`**

```json
{
  "code": 200,
  "data": {
    "source": "CODEFORCES",
    "triggerType": "MANUAL",
    "elapsedMs": 1234,
    "persist": {
      "fetched": 120,
      "inserted": 5,
      "updated": 2,
      "skipped": 113,
      "ignored": 0
    }
  }
}
```

---

## 6. Redis 设计（迭代 2，未接入）

| Key 模式 | 用途 | TTL |
|----------|------|-----|
| `contest:list:{queryHash}` | 列表查询结果缓存 | 5 ~ 10 分钟 |
| `contest:hot` | 热门赛事 Top N | 10 分钟 |
| `contest:detail:{id}` | 单场详情缓存 | 10 分钟 |
| `crawl:rate:{source}` | 爬虫限流 | 按 `rate_limit_sec` |

---

## 7. 依赖与配置

### 7.1 pom.xml

OkHttp 已引入并用于 `CrawlHttpClient`。Jsoup、Quartz 按迭代 2 计划添加。

### 7.2 application.yml（当前）

```yaml
contest:
  crawl:
    http:
      max-idle-connections: 5
      keep-alive-minutes: 5
      connect-timeout-seconds: 10
      read-timeout-seconds: 30
      write-timeout-seconds: 10
      call-timeout-seconds: 60
      max-retries: 3
      retry-base-delay-millis: 500
```

---

## 8. 错误码扩展

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 2001 | CONTEST_NOT_FOUND | 赛事不存在（详情 API 用） |
| 2002 | CRAWL_SOURCE_DISABLED | 爬虫源未启用 |
| 2003 | CRAWL_EXECUTION_FAILED | 爬虫执行失败 |

爬虫 HTTP/API 失败主要通过 `CrawlFetchException` + `contest_crawl_log` 记录，不一定映射为 2003。

---

## 9. 分阶段实施计划

### 迭代 1 — 跑通一条链路 ✅

| 步骤 | 任务 | 状态 |
|------|------|------|
| 1 | 建表 `contest` | ✅ |
| 2 | `module/contest` 包骨架 + crawl 流水线 | ✅ |
| 3 | Entity / Mapper / ContestService | ✅ |
| 4 | `CodeforcesCrawler` | ✅ |
| 5 | 去重 + 增量 + 批量入库 | ✅ |
| 6 | `ContestController` 列表 | ✅ |
| 7 | Security 放行 | ✅ |
| 8 | 管理端手动爬取 + 前端列表 | ✅ |

### 迭代 2 — 自动化 + 性能 ⏳

| 步骤 | 任务 | 状态 |
|------|------|------|
| 1 | Quartz Job 接入 | ❌ |
| 2 | Redis 缓存 | ❌ |
| 3 | Redis 爬虫限流 | ❌ |
| 4 | `AtCoderCrawler` + `NowcoderCrawler` | ❌ |
| 5 | 日历 API | ❌ |
| 6 | `contest_source` + `contest_crawl_log` | ✅ |

### 迭代 3 — 补平台 + 管理完善 ⏳

| 步骤 | 任务 | 状态 |
|------|------|------|
| 1 | 洛谷、CCPC、ICPC、蓝桥杯 Crawler | 占位 |
| 2 | 管理端源配置 / crawl-all API | ❌ |
| 3 | 详情 API + 通用赛事列表 / 日历页 | 部分（仅 CF 列表） |
| 4 | `UserHomePage` 赛事卡片真实跳转 | ✅ CF |

---

## 10. 各平台数据来源参考

详见 `docs/module-2-crawler-data-sources.md`。

| 平台 | 推荐数据源 | 协议 | 实现状态 |
|------|-----------|------|----------|
| Codeforces | `https://codeforces.com/api/contest.list` | JSON REST | ✅ |
| AtCoder | kenkoooo JSON / 官网 HTML | JSON / HTML | 占位 |
| 牛客 | 页面内嵌 JSON | 半结构化 | 占位 |
| 洛谷 / CCPC / ICPC / 蓝桥杯 | 官网 HTML | HTML | 占位 |

---

## 11. 模块边界

| 关联模块 | 边界说明 |
|----------|----------|
| 模块 0 | 复用 Result、异常、Redis、Security |
| 模块 1 | 不碰 `user` 表；ADMIN 触发爬虫 |
| 模块 3 | 订阅表外键用 `contest.id` |
| 模块 6 | 讨论关联 `contest_id` |

---

## 12. 前端（acm01-web）

```text
src/
├── api/contest.ts                    # 列表 + triggerContestCrawl
└── pages/
    ├── CodeforcesContestListPage.tsx # CF 赛事列表
    ├── AdminHomePage.tsx             # 一键爬取 + 入库统计
    └── UserHomePage.tsx              # 入口卡片
```

**触发爬取：** 管理端登录 → 「一键爬取 Codeforces」→ `POST /api/admin/contests/crawl/codeforces`

---

## 13. 面试话术

> 针对多 OJ 平台接口异构问题，采用**策略模式**：各平台独立 `ContestCrawler`，统一输出 `ContestDTO`。  
> 写侧用 **CrawlPipeline** 固定「拉取 → 批量入库 → 写日志」三步，与读侧解耦。  
> 以 `source + external_id` 去重、`raw_hash` 增量 SKIP，批量 SELECT + batch INSERT + JDBC batch UPDATE 降低 DB 往返。  
> HTTP 层 OkHttp 连接池 + 指数退避重试，失败结构化写入 `contest_crawl_log`。  
> 查询侧计划 Redis 缓存；公开列表 API 已无需登录。

---

## 14. Checklist

- [x] MySQL 执行 `init-contest.sql`
- [x] MySQL 执行 `init-contest-crawl.sql`
- [x] `ContestSource` 与 DB `source_code` 一致
- [x] Codeforces 手动爬取链路验证
- [x] `SecurityConfig` 放行 `/api/contests/**`
- [ ] Quartz Job 接入
- [ ] Redis 缓存与限流
- [ ] 其余平台 Crawler 实现
- [ ] 详情 / 日历 / 热门 API

---

## 15. 类方法签名清单（当前代码）

```java
// 策略接口
public interface ContestCrawler {
    ContestSource getSource();
    String getPrimaryRequestUrl();
    List<ContestDTO> fetchContests();
}

// 触发入口
public class ContestCrawlService {
    void crawlAll(CrawlTriggerType triggerType);
    void crawlAll();
    ContestCrawlResult crawl(ContestSource source, CrawlTriggerType triggerType);
    ContestCrawlResult crawlManual(ContestSource source);
}

// 流水线
public class CrawlPipeline {
    ContestCrawlResult execute(ContestCrawler crawler, CrawlTriggerType triggerType);
}

// 持久化
public class ContestPersistService {
    ContestPersistResult persistAll(List<ContestDTO> contests);
}

// 查询
public class ContestService {
    PageResult<ContestVO> listContests(ContestQueryRequest query);
}

// 公开 API
@RestController
@RequestMapping("/api/contests")
public class ContestController {
    GET /  → list
}

// 管理 API
@RestController
@RequestMapping("/api/admin/contests")
public class ContestAdminController {
    POST /crawl/{sourceCode}  → crawlManual
}
```

---

*文档维护：模块 2 开发过程中同步更新实施状态。上次更新：2026-07-07（v2.0，对齐 crawl 流水线与 Codeforces 字段映射）。*
