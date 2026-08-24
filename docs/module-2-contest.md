# 模块 2：赛事聚合与爬虫 — 实施文档

> 多平台赛事统一展示、定时/手动爬虫、入库去重、爬取日志  
> 文档版本：v2.0 | 状态：**7 平台抓取 + Redis 缓存/限流 + 详情/日志 API 已完成**

---

## 1. 模块目标

| 目标 | 说明 |
|------|------|
| 赛事聚合 | 多 OJ 赛事写入统一 `contest` 表，按 `source + external_id` 去重 |
| 分页查询 | 公开 API 按平台、状态筛选赛事列表 |
| 分布式爬虫 | 各平台独立 Fetch 实现，公共 Pipeline 调度 |
| 增量更新 | `raw_hash` 对比，无变化跳过、有变化更新 |
| 爬取审计 | 每次执行写入 `contest_crawl_log`，Admin 返回 `logId` |
| 平台开关 | `contest_source.crawl_enabled` 控制是否抓取 |

**本模块暂不做（后续迭代）：**

- 赛事日历视图 API
- 热门赛事独立预热任务

**v2.0 已完成：**

- 洛谷 / CCPC / ICPC / 蓝桥杯抓取实现
- Redis 列表缓存（`ContestCacheService`，爬虫后失效）
- 爬虫 Redis 限流（`CrawlRateLimiter`，读 `rate_limit_sec`）
- `GET /api/contests/{id}` 赛事详情
- `GET /api/admin/crawl/logs` 爬取日志分页
- 前端：`AdminCrawlLogsPage`、`ContestDetailPage`

**依赖：** 模块 0（Result、异常、MyBatis）、模块 1（Admin 手动爬虫需 `ROLE_ADMIN`）

---

## 2. 架构总览

模块按职责拆为 **三块**，共享 contest 领域模型，互不耦合业务细节：

```text
module/contest/
├── controller/          # HTTP 入口（查询 + Admin 爬虫）
├── dto/                 # 跨层业务模型（爬虫归一化）
├── vo/                  # 查询 API 返回
├── entity/              # 数据库实体
├── query/               # 读：分页列表
├── crawl/               # 写：抓取 + 入库
└── log/                 # 写：爬取日志
```

### 2.1 三层职责

| 子域 | 职责 | 核心类 |
|------|------|--------|
| **query** | 只读 `contest` 表，Entity → VO | `ContestQueryService` |
| **crawl** | 抓取、归一化、入库 | `CrawlPipeline`、`ContestPersistService` |
| **log** | 爬取结果落库、更新源状态 | `CrawlLogService`、`CrawlOutcome` |

### 2.2 数据模型分层

```text
各平台原始响应
    → 平台 Mapper 转成 ContestDTO（统一业务模型）
        → ContestEntity（入库）
            → contest 表

查询时：
contest 表 → ContestEntity → ContestVO（API 返回，裁剪字段）

爬取日志：
ContestPersistCountsDTO + 爬取元信息 → CrawlOutcome → contest_crawl_log
```

| 类型 | 包路径 | 用途 |
|------|--------|------|
| `ContestDTO` | `dto/` | 爬虫流水线内部统一模型，各平台 Fetch 最终都产出它 |
| `ContestVO` | `vo/` | 对外查询 API 返回，不含 `rawHash` 等内部字段 |
| `ContestEntity` | `entity/` | 与 `contest` 表一一对应 |
| `ContestPersistCountsDTO` | `log/dto/` | 入库统计五元组，被 `CrawlOutcome` 组合 |
| `CrawlOutcome` | `log/dto/` | 单次爬取完整结果（成功/失败） |

---

## 3. 核心调用链

### 3.1 手动爬虫（Admin）

```text
POST /api/admin/contests/crawl/{sourceCode}
    │
    ▼
ContestAdminController
    │  resolveSource(sourceCode) → ContestSource
    ▼
ContestCrawlService.crawlManual(source)
    │  CrawlSourceConfig.isCrawlEnabled?
    │  PlatformFetchRegistry.getRequired(source)
    ▼
CrawlPipeline.execute(fetchService, MANUAL)
    │
    ├─① fetchSupport.attachRawHash(fetchService.fetchContests())
    │      各平台 *ContestFetchService → List<ContestDTO>
    │
    ├─② persistService.persistAll(fetched)
    │      → ContestPersistCountsDTO
    │         (fetched / inserted / updated / skipped / ignored)
    │
    ├─③ CrawlOutcome.success(..., persistCounts, ...)
    │
    └─④ logService.record(outcome)
           → INSERT contest_crawl_log
           → UPDATE contest_source.last_crawl_*
    │
    ▼
返回 Result<Long> logId
```

### 3.2 失败路径

```text
fetch 阶段抛 CrawlFetchException
    → CrawlPipeline.catch
    → CrawlOutcome.fromException(...)   # persistCounts 全 0
    → logService.record()
    → 继续向上抛异常（API 返回错误）
```

### 3.3 赛事查询（公开）

```text
GET /api/contests?source=&status=&pageNum=&pageSize=
    │
    ▼
ContestController
    ▼
ContestQueryService.listContests(...)
    ▼
ContestQueryMapper.selectPage / countPage
    ▼
Entity → ContestVO → PageResult
```

### 3.4 入库决策（persistAll）

```text
List<ContestDTO>
    │
    ├─ 校验失败（缺 source/externalId/title/startTime）→ ignoredCount++
    │
    ├─ selectRawHashByKeys（批量查已有记录的 hash）
    │
    ├─ 库里不存在           → batchInsert    → insertedCount
    ├─ 存在但 rawHash 变了  → batchUpdate    → updatedCount
    └─ 存在且 rawHash 相同  → 跳过           → skippedCount
```

---

## 4. 包结构（已实现）

```text
org.fjnu305.acm01.module.contest/
│
├── controller/
│   ├── ContestController.java           # GET /api/contests
│   └── ContestAdminController.java      # POST /api/admin/contests/crawl/{sourceCode}
│
├── dto/
│   └── ContestDTO.java                  # 爬虫统一业务模型
│
├── vo/
│   └── ContestVO.java                   # 查询 API 返回
│
├── entity/
│   ├── ContestEntity.java               # contest 表
│   └── ContestSourceEntity.java         # contest_source 表
│
├── query/
│   ├── service/
│   │   └── ContestQueryService.java
│   └── mapper/
│       └── ContestQueryMapper.java
│
├── crawl/
│   ├── config/
│   │   └── CrawlSourceConfig.java       # 读 crawl_enabled
│   ├── pipeline/
│   │   └── CrawlPipeline.java           # 抓取→入库→记日志 总调度
│   ├── service/
│   │   ├── ContestCrawlService.java     # 爬虫入口（手动/批量）
│   │   └── ContestPersistService.java   # 入库 + 统计
│   ├── mapper/
│   │   ├── ContestPersistMapper.java    # contest 写操作
│   │   └── ContestSourceMapper.java     # contest_source 读写
│   └── fetch/
│       ├── common/                      # 平台无关公共层
│       │   ├── PlatformContestFetchService.java
│       │   ├── PlatformFetchRegistry.java
│       │   ├── ContestFetchSupport.java
│       │   ├── exception/CrawlFetchException.java
│       │   └── http/
│       │       ├── CrawlHttpClient.java
│       │       └── CrawlHttpProperties.java
│       ├── codeforces/                  # ✅ 已实现
│       ├── atcoder/                     # ✅ 已实现（HTML 爬虫主源 + 接口降级）
│       ├── nowcoder/                    # ✅ 已实现（HTML 列表页）
│       ├── luogu/                       # ⏳ stub
│       ├── ccpc/                        # ⏳ stub
│       ├── icpc/                        # ⏳ stub
│       └── lanqiao/                     # ⏳ stub
│
└── log/
    ├── dto/
    │   ├── ContestPersistCountsDTO.java # 入库五计数
    │   └── CrawlOutcome.java            # 爬取完整结果（组合上者）
    ├── entity/
    │   └── ContestCrawlLogEntity.java
    ├── service/
    │   └── CrawlLogService.java
    └── mapper/
        └── ContestCrawlLogMapper.java
```

**MyBatis XML：**

```text
resources/mapper/
├── ContestQueryMapper.xml       # 分页查询 contest
├── ContestPersistMapper.xml     # insert / batchInsert / batchUpdate
├── ContestSourceMapper.xml      # contest_source 状态更新
└── ContestCrawlLogMapper.xml    # 爬取日志写入
```

**SQL 脚本：**

```text
resources/db/
├── init-contest.sql             # contest 表
└── init-contest-crawl.sql       # contest_source + contest_crawl_log + 初始平台数据
```

**配置（application.yml）：**

```text
contest.crawl.http.*             # OkHttp 超时、重试
contest.crawl.atcoder.*          # AtCoder 双源模式、URL、保留天数
```

---

## 5. API 接口

### 5.1 公开查询

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/contests` | 匿名 | 分页列表 |

**Query 参数：**

| 参数 | 类型 | 默认 | 说明 |
|------|------|------|------|
| `source` | String | — | 平台 code，如 `codeforces`、`atcoder` |
| `status` | Integer | — | 1 即将开始 / 2 进行中 / 3 已结束 |
| `pageNum` | int | 1 | 页码 |
| `pageSize` | int | 20 | 每页条数，最大 100 |

### 5.2 管理端爬虫

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/admin/contests/crawl/{sourceCode}` | ADMIN | 手动触发单平台爬虫 |

**响应：** `Result<Long>`，`data` 为 `contest_crawl_log.id`（失败时抛业务异常，仍可能已写入失败日志）。

**sourceCode 取值：** `codeforces` | `atcoder` | `nowcoder` | `luogu` | `ccpc` | `icpc` | `lanqiao`（与 `ContestSource` 枚举对齐）

---

## 6. 数据库设计

> 完整字段说明见 `docs/database-schema.md` §3

| 表 | 说明 |
|----|------|
| `contest` | 统一赛事，唯一键 `(source, external_id)` |
| `contest_source` | 爬虫源配置，`crawl_enabled` 开关 |
| `contest_crawl_log` | 每次爬取一条日志，含五计数 + 错误信息 |

**contest_crawl_log 统计字段（来自 ContestPersistCountsDTO）：**

| 字段 | 含义 |
|------|------|
| `fetched_count` | 爬虫传入总条数 |
| `inserted_count` | 新插入 |
| `updated_count` | rawHash 变化后更新 |
| `skipped_count` | 已存在且 hash 未变 |
| `ignored_count` | 校验不合法丢弃 |

---

## 7. 平台实现状态

| 平台 | FetchService | 数据源 | 状态 |
|------|--------------|--------|------|
| Codeforces | `CodeforcesContestFetchService` | 官方 API | ✅ 可用 |
| AtCoder | `AtCoderContestFetchService` | 官网 HTML 爬虫主源，失败降级 kenkoooo 接口 | ✅ 可用 |
| 牛客 | `NowcoderContestFetchService` | 牛客系列赛 HTML 列表页 | ✅ 可用 |
| 洛谷 | `LuoguContestFetchService` | — | ⏳ 返回空列表 |
| CCPC | `CcpcContestFetchService` | — | ⏳ 返回空列表 |
| ICPC | `IcpcContestFetchService` | — | ⏳ 返回空列表 |
| 蓝桥杯 | `LanqiaoContestFetchService` | — | ⏳ 返回空列表 |

**AtCoder 抓取策略（固定）：** 先爬 `atcoder.jp/contests/`，仅爬虫抛异常时降级 kenkoooo JSON 接口。

---

## 8. 各文件说明

### 8.1 controller — HTTP 入口

| 文件 | 说明 |
|------|------|
| `ContestController.java` | 公开赛事列表，参数直接传给 `ContestQueryService`，无 Request DTO 包装 |
| `ContestAdminController.java` | Admin 手动爬虫，`sourceCode` 解析为 `ContestSource`，返回 `logId` |

### 8.2 dto / vo / entity — 数据模型

| 文件 | 说明 |
|------|------|
| `ContestDTO.java` | 爬虫流水线统一模型；`source` 用枚举，入库前转 String；含 `rawHash` |
| `ContestVO.java` | 查询 API 对外字段，由 `ContestQueryService` 从 Entity 组装 |
| `ContestEntity.java` | 映射 `contest` 表全字段，含软删 `deleted` |
| `ContestSourceEntity.java` | 映射 `contest_source` 表 |

### 8.3 query — 读路径

| 文件 | 说明 |
|------|------|
| `ContestQueryService.java` | 分页参数校验（pageSize ≤ 100），Entity → VO |
| `ContestQueryMapper.java` | `selectPage`、`countPage` |
| `ContestQueryMapper.xml` | 按 `source`、`status` 条件分页 SQL |

### 8.4 crawl — 爬虫与入库

| 文件 | 说明 |
|------|------|
| `ContestCrawlService.java` | 爬虫对外服务：`crawlManual`、`crawlAll`；检查 `crawl_enabled` 后调 Pipeline |
| `CrawlPipeline.java` | **核心调度**：fetch → persist → 拼 `CrawlOutcome` → 记日志；捕获异常写失败日志 |
| `ContestPersistService.java` | 校验、去重、batchInsert/batchUpdate；返回 `ContestPersistCountsDTO` |
| `CrawlSourceConfig.java` | 读 `contest_source.crawl_enabled`，未配置默认允许 |
| `ContestPersistMapper.java` | contest 写：`insert`、`batchInsert`、`batchUpdateBySourceAndExternalId`、`selectRawHashByKeys` |
| `ContestPersistMapper.xml` | 批量 INSERT（foreach VALUES）；批量 UPDATE（foreach 多语句，需 `allowMultiQueries=true`） |
| `ContestSourceMapper.java` | `selectCrawlEnabled`、`updateOnSuccess`、`updateOnFailure` |
| `ContestSourceMapper.xml` | 更新 `last_crawl_time`、`last_crawl_status`、`fail_count` |

### 8.5 crawl/fetch/common — 抓取公共层

| 文件 | 说明 |
|------|------|
| `PlatformContestFetchService.java` | 平台抓取接口：`getSource`、`getPrimaryRequestUrl`、`fetchContests` |
| `PlatformFetchRegistry.java` | Spring 注入所有 FetchService，按 `ContestSource` 索引 |
| `ContestFetchSupport.java` | 为 `ContestDTO` 计算 SHA-256 `rawHash`（增量更新依据） |
| `CrawlFetchException.java` | 抓取异常，携带 `errorType`、`httpStatus`、重试次数 |
| `CrawlHttpClient.java` | OkHttp 封装，连接池、超时、指数退避重试 |
| `CrawlHttpProperties.java` | 绑定 `contest.crawl.http.*` 配置 |

### 8.6 crawl/fetch/codeforces — Codeforces

| 文件 | 说明 |
|------|------|
| `CodeforcesContestFetchService.java` | 实现 `PlatformContestFetchService`，调 API + Mapper |
| `CodeforcesApiClient.java` | 请求 `https://codeforces.com/api/contest.list` |
| `CodeforcesContestMapper.java` | API 响应 → `List<ContestDTO>` |
| `CodeforcesContestListResponse.java` | CF API JSON 结构 DTO |

### 8.7 crawl/fetch/atcoder — AtCoder

| 文件 | 说明 |
|------|------|
| `AtCoderContestFetchService.java` | 平台入口，委托 `AtCoderFetchCoordinator` |
| `AtCoderFetchCoordinator.java` | 固定策略：HTML 爬虫主源，失败降级 kenkoooo 接口 |
| `AtCoderCrawlProperties.java` | 绑定 `contest.crawl.atcoder.*`（URL、保留天数） |
| `AtCoderContestMapper.java` | kenkoooo / HTML 行 → `ContestDTO` |
| `AtCoderJsonClient.java` | 爬虫失败时拉取 JSON 降级数据 |
| `AtCoderHtmlClient.java` | 解析 atcoder.jp/contests/ HTML |
| `AtCoderContestItem.java` | 外部数据统一项（HTML / JSON 共用） |

### 8.8 crawl/fetch/* — 未实现平台 stub

| 文件 | 说明 |
|------|------|
| `NowcoderContestFetchService.java` | 平台入口，委托 `NowcoderFetchCoordinator` |
| `NowcoderFetchCoordinator.java` | HTML 爬虫协调 |
| `NowcoderHtmlClient.java` | 解析 vip-index 列表页 `platform-item` |
| `NowcoderContestMapper.java` | `NowcoderContestItem` → `ContestDTO` |
| `NowcoderContestItem.java` | 外部数据统一项 |
| `LuoguContestFetchService.java` | 同上 |
| `CcpcContestFetchService.java` | 同上 |
| `IcpcContestFetchService.java` | 同上 |
| `LanqiaoContestFetchService.java` | 同上 |

> stub 保留是为保证 `PlatformFetchRegistry` 完整、定时任务遍历不报错，后续按平台补 Client + Mapper 即可。

### 8.9 log — 爬取日志

| 文件 | 说明 |
|------|------|
| `ContestPersistCountsDTO.java` | 入库统计：`fetched/inserted/updated/skipped/ignored` |
| `CrawlOutcome.java` | 单次爬取完整快照；**组合** `ContestPersistCountsDTO`；提供 `success` / `fromException` 工厂方法 |
| `ContestCrawlLogEntity.java` | 映射 `contest_crawl_log` 表 |
| `CrawlLogService.java` | `record(CrawlOutcome)`：写日志 + 更新 `contest_source` 成功/失败状态 |
| `ContestCrawlLogMapper.java` | 日志 INSERT |
| `ContestCrawlLogMapper.xml` | 插入 SQL |

---

## 9. 关键设计决策

| 决策 | 说明 |
|------|------|
| 三块分离 query / crawl / log | 读、写、审计各自独立，Mapper 也分到对应子包 |
| `ContestDTO` 作统一模型 | 各平台原始 DTO 留在 `fetch/{platform}/dto/`，只在边界转成 `ContestDTO` |
| 删除 `ContestCrawlResult` / `ContestPersistResult` | 统计写入 `contest_crawl_log`，Admin 只返回 `logId` |
| `CrawlOutcome` 组合 `ContestPersistCountsDTO` | 避免五计数与爬取元信息混在一个扁平类里重复定义 |
| 批量 UPDATE 用 XML foreach | 与 `batchInsert` 风格一致；JDBC URL 需 `allowMultiQueries=true` |
| `raw_hash` 增量 | 核心字段拼接后 SHA-256，相同则 `skipped`，不同则 `updated` |
| stub 平台保留 | Registry 完整注册，避免按枚举遍历时缺失实现 |

---

## 10. 配置参考

```yaml
# application.yml 摘录
spring:
  datasource:
    url: jdbc:mysql://...?allowMultiQueries=true   # 批量 UPDATE 需要

contest:
  crawl:
    http:
      max-idle-connections: 5
      connect-timeout-seconds: 10
      read-timeout-seconds: 30
      max-retries: 3
      retry-base-delay-millis: 500
    atcoder:
      json-fallback-url: https://kenkoooo.com/atcoder/resources/contests.json
      html-url: https://atcoder.jp/contests/
      finished-keep-days: 90
```

---

## 11. 本地验证

```bash
# 编译
mvn clean compile

# 初始化库表（按顺序）
# init-contest.sql → init-contest-crawl.sql

# 手动爬虫（需 ADMIN Token）
POST /api/admin/contests/crawl/codeforces
POST /api/admin/contests/crawl/atcoder

# 查询
GET /api/contests?source=codeforces&pageNum=1&pageSize=20
```

---

## 12. 后续迭代

| 优先级 | 事项 |
|--------|------|
| P1 | Quartz Job 调 `ContestCrawlService.crawlAll(AUTO)` |
| P1 | 牛客 / 洛谷 HTML 或 API 抓取实现 |
| P2 | Redis 热门赛事缓存 |
| P2 | `contest_source.rate_limit_sec` 限流落地 |
| P3 | 爬取日志查询 Admin API |
| P3 | 赛事详情 `GET /api/contests/{id}` |

---

## 13. 相关文档

| 文档 | 内容 |
|------|------|
| `docs/architecture-modules.md` | 全项目模块规划 |
| `docs/database-schema.md` | contest 相关表字段详解 |
| `docs/module-0-infrastructure.md` | 公共基础设施 |
| `docs/module-1-user-auth.md` | 用户权限（Admin 鉴权） |
