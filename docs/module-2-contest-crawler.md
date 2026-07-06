# 模块 2：赛事聚合与爬虫 — 实施方案

> 基于现有架构（模块 0 基础设施 + 模块 1 用户权限）的模块 2 设计与实施指南  
> 文档版本：v1.0 | 状态：规划阶段，待开发

---

## 1. 模块目标

| 目标 | 说明 |
|------|------|
| 数据采集 | 定时从各 OJ / 官方站点抓取赛事信息 |
| 数据归一 | 多平台异构数据统一写入 `contest` 表 |
| 对外查询 | 提供列表 / 详情 / 日历 API，供模块 3（订阅）、模块 6（讨论）使用 |
| 性能优化 | 热门赛事 Redis 缓存；爬虫 Redis 限流防 IP 封禁 |

**本模块不做：**

- 用户赛事订阅（模块 3）
- 社交 / 动态（模块 6）
- 前端完整赛事页（可迭代 3 再做，API 优先）

---

## 2. 与现有架构的衔接

### 2.1 已有可复用能力

| 已有组件 | 路径 / 说明 | 模块 2 用途 |
|----------|-------------|-------------|
| 赛事来源枚举 | `Common/enums/ContestSource.java` | 与 `contest.source` 字段对齐 |
| 统一响应 | `Common/result/Result.java`、`PageResult.java` | 赛事 API 返回格式 |
| 异常体系 | `Common/exception/*` | 扩展错误码 2001~2003 |
| Redis | `Config/RedisConfig.java` | 缓存 + 爬虫限流 |
| Security | `Config/SecurityConfig.java` | 赛事查询接口公开访问 |
| 用户权限 | `module/user` + ADMIN 角色 | 手动触发爬虫、管理爬虫源 |

### 2.2 模块 1 分层模板（照搬）

```text
module/user/                    module/contest/（新增）
  controller/        →            controller/
  service/           →            service/
  mapper/            →            mapper/
  entity/ + dto/     →            entity/ + dto/
                                  crawler/          ← 模块 2 特有
                                  job/              ← Quartz 定时任务
```

### 2.3 新增包结构

```text
org.fjnu305.acm01.module.contest/
├── controller/
│   ├── ContestController.java           # 公开查询 API
│   └── ContestAdminController.java      # 管理端（可选，迭代 3）
├── service/
│   ├── ContestService.java              # 查询、分页、日历
│   ├── ContestCrawlService.java         # 爬虫编排、去重、增量
│   └── ContestCacheService.java         # Redis 缓存读写
├── crawler/
│   ├── ContestCrawler.java              # 策略接口
│   ├── AbstractContestCrawler.java      # 公共 HTTP / 解析 / 限流钩子
│   └── strategy/
│       ├── CodeforcesCrawler.java
│       ├── AtCoderCrawler.java
│       ├── NowcoderCrawler.java
│       ├── LuoguCrawler.java
│       ├── CcpcCrawler.java
│       ├── IcpcCrawler.java
│       └── LanqiaoCrawler.java
├── job/
│   ├── ContestCrawlJob.java             # 定时爬取
│   └── ContestStatusRefreshJob.java     # 定时刷新赛事状态
├── mapper/
│   ├── ContestMapper.java
│   └── ContestSourceMapper.java
├── entity/
│   ├── ContestEntity.java
│   └── ContestSourceEntity.java
└── dto/
    ├── ContestDTO.java                  # 爬虫内部归一化对象
    ├── ContestVO.java                   # API 返回
    ├── ContestQueryRequest.java         # 列表筛选
    └── ContestCalendarVO.java           # 日历视图
```

**设计原则：** 爬虫是旁路系统，不侵入 `AuthService`；写库走 `ContestCrawlService`，读库走 `ContestController`。

---

## 3. 数据库设计

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
    contest_type    VARCHAR(32) COMMENT 'ICPC/OI/个人赛等',
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
| 自增 `id` 作为对外 ID | 模块 3 订阅表外键简单，不用平台的 `external_id` |

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

| 设计点 | 原因 |
|--------|------|
| 独立配置表 | 平台开关、cron、限流间隔可改，不用重启改代码 |
| ADMIN 可管理 | 对接模块 1 管理后台，后续可视化运维 |

### 3.3 初始化数据

```sql
INSERT IGNORE INTO contest_source (source_code, source_name, crawl_enabled, rate_limit_sec) VALUES
('codeforces', 'Codeforces', 1, 2),
('atcoder',    'AtCoder',    1, 3),
('nowcoder',   '牛客网',      1, 3),
('luogu',      '洛谷',        1, 3),
('ccpc',       'CCPC',       1, 5),
('icpc',       'ICPC',       1, 5),
('lanqiao',    '蓝桥杯',      1, 5);
```

> `source_code` 需与 `Common/enums/ContestSource.java` 中 `value` 字段一致。

---

## 4. 核心架构

### 4.1 三层拆分

```text
┌─────────────────────────────────────────────────────────┐
│  调度层    Quartz ContestCrawlJob / StatusRefreshJob    │
└──────────────────────────┬──────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│  采集层    ContestCrawlService                          │
│            → CodeforcesCrawler / AtCoderCrawler / ...   │
│            → Redis 限流 → 归一化 ContestDTO               │
└──────────────────────────┬──────────────────────────────┘
                           ↓ 去重 + 增量
┌─────────────────────────────────────────────────────────┐
│  存储层    contest 表                                    │
│  缓存层    Redis（列表 / 热门）                          │
│  查询层    ContestController → ContestService             │
└─────────────────────────────────────────────────────────┘
```

### 4.2 策略模式 — `ContestCrawler` 接口

每个平台实现类需提供：

| 方法 | 作用 |
|------|------|
| `getSource()` | 返回 `ContestSource` 枚举 |
| `fetchContests()` | 拉取并解析，返回 `List<ContestDTO>` |
| `enabled()` | 读取 `contest_source.crawl_enabled` |

**为什么用策略模式：** 7 大平台协议各异（JSON / HTML / 不同字段），一个类维护成本极高；新增平台 = 新增一个 Crawler 类 + Spring 自动注入，主流程不变。

### 4.3 爬虫策略类一览

| 类名 | 平台 | 数据来源 | 优先级 |
|------|------|----------|--------|
| `CodeforcesCrawler` | Codeforces | 官方 JSON API | P0 迭代 1 |
| `AtCoderCrawler` | AtCoder | HTML / 非官方 API | P1 迭代 2 |
| `NowcoderCrawler` | 牛客 | HTML + Jsoup | P1 迭代 2 |
| `LuoguCrawler` | 洛谷 | HTML / API | P2 迭代 3 |
| `CcpcCrawler` | CCPC | 官网 HTML | P2 迭代 3 |
| `IcpcCrawler` | ICPC | 官网 HTML | P2 迭代 3 |
| `LanqiaoCrawler` | 蓝桥杯 | 官网 HTML | P2 迭代 3 |

### 4.4 `ContestCrawlService` 编排流程

```text
for each enabled Crawler:
  ① Redis 限流检查（key: crawl:rate:{source}，间隔见 contest_source.rate_limit_sec）
  ② List<ContestDTO> list = crawler.fetchContests()
  ③ for each dto:
       existing = SELECT BY (source, external_id)
       if existing == null        → INSERT
       else if dto.rawHash != existing.rawHash → UPDATE
       else                       → SKIP
  ④ UPDATE contest_source SET last_crawl_time, last_crawl_status
  ⑤ DELETE Redis 赛事缓存（contest:hot、contest:list:*）
```

### 4.5 赛事状态刷新（独立 Job）

| Job | Cron | 作用 |
|-----|------|------|
| `ContestCrawlJob` | `0 0 */6 * * ?` | 每 6 小时全量爬取 |
| `ContestStatusRefreshJob` | `0 */10 * * * ?` | 每 10 分钟批量更新 status |

**为什么 status 单独 Job：** 爬虫不会每分钟执行，但「即将开始 → 进行中 → 已结束」随时间变化，轻量 SQL 即可。

```sql
-- status 规则示例
-- start_time > NOW()           → 1 即将开始
-- start_time <= NOW() < end    → 2 进行中
-- end_time <= NOW()            → 3 已结束
```

---

## 5. API 设计

### 5.1 公开接口（无需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/contests` | 分页列表，支持 source / status / 时间范围筛选 |
| GET | `/api/contests/{id}` | 赛事详情 |
| GET | `/api/contests/calendar` | 日历视图（按日期分组） |
| GET | `/api/contests/hot` | 热门赛事 Top N（Redis 缓存） |

**SecurityConfig 需增加（实施时）：**

```java
.requestMatchers("/api/contests/**").permitAll()
```

### 5.2 管理接口（迭代 3，需 ADMIN）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/contests/crawl/{source}` | 手动触发指定平台爬虫 |
| POST | `/api/admin/contests/crawl/all` | 手动触发全平台爬虫 |
| GET | `/api/admin/contest-sources` | 查看各平台爬虫状态 |
| PUT | `/api/admin/contest-sources/{code}` | 修改开关 / cron / 限流间隔 |

### 5.3 响应示例

**列表 `GET /api/contests?pageNum=1&pageSize=10&source=codeforces`**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "source": "codeforces",
        "title": "Codeforces Round #999",
        "startTime": "2026-07-10T14:35:00",
        "endTime": "2026-07-10T16:35:00",
        "status": 1,
        "url": "https://codeforces.com/contest/999",
        "difficulty": "Div.2"
      }
    ],
    "total": 42,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 6. Redis 设计

| Key 模式 | 用途 | TTL |
|----------|------|-----|
| `contest:list:{queryHash}` | 列表查询结果缓存 | 5 ~ 10 分钟 |
| `contest:hot` | 热门赛事 Top N | 10 分钟 |
| `contest:detail:{id}` | 单场详情缓存 | 10 分钟 |
| `crawl:rate:{source}` | 爬虫限流（两次请求间隔） | 按 `rate_limit_sec` |

**缓存失效时机：** 每次爬虫 Job 执行完毕后，删除 `contest:hot` 和 `contest:list:*`（或依赖短 TTL）。

---

## 7. 依赖与配置

### 7.1 pom.xml 新增

```xml
<!-- HTTP 客户端 -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
<!-- HTML 解析 -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
<!-- 定时任务 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

### 7.2 application.yml 新增

```yaml
spring:
  quartz:
    job-store-type: memory   # 单机够用；集群再换 jdbc

contest:
  crawl:
    enabled: true            # 总开关
  cache:
    hot-size: 10             # 热门赛事数量
    ttl-minutes: 10          # 缓存过期分钟
```

---

## 8. 错误码扩展

在 `Common/exception/ErrorCode.java` 中新增：

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 2001 | CONTEST_NOT_FOUND | 赛事不存在 |
| 2002 | CRAWL_SOURCE_DISABLED | 爬虫源未启用 |
| 2003 | CRAWL_EXECUTION_FAILED | 爬虫执行失败 |

---

## 9. 分阶段实施计划

### 迭代 1（约 1 周）— 跑通一条链路

| 步骤 | 任务 | 验收标准 |
|------|------|----------|
| 1 | 执行建表 SQL（`contest`） | 表存在 |
| 2 | 创建 `module/contest` 包骨架 | 编译通过 |
| 3 | Entity / Mapper / ContestService | 基础 CRUD |
| 4 | **仅实现 `CodeforcesCrawler`** | 能抓到 CF 赛事 |
| 5 | `ContestCrawlService` 去重 + 增量 | DB 不重复、有更新 |
| 6 | `ContestController` 列表 + 详情 | Postman 无 Token 可访问 |
| 7 | SecurityConfig 放行 `/api/contests/**` | 401 不再出现 |

### 迭代 2（约 1 周）— 自动化 + 性能

| 步骤 | 任务 |
|------|------|
| 1 | Quartz `ContestCrawlJob` + `ContestStatusRefreshJob` |
| 2 | Redis 列表 / 热门 / 详情缓存 |
| 3 | Redis 爬虫限流 |
| 4 | 新增 `AtCoderCrawler` + `NowcoderCrawler` |
| 5 | 日历 API `/api/contests/calendar` |
| 6 | 建 `contest_source` 表 + 初始化数据 |

### 迭代 3（约 1 周）— 补平台 + 管理 + 前端

| 步骤 | 任务 |
|------|------|
| 1 | 洛谷、CCPC、ICPC、蓝桥杯 Crawler |
| 2 | `ContestAdminController` 手动触发 / 状态查看 |
| 3 | 前端 `ContestListPage` + `ContestCalendarPage` |
| 4 | `UserHomePage` 赛事卡片从「即将上线」改为真实跳转 |

---

## 10. 各平台数据来源参考

| 平台 | 推荐数据源 | 协议 | 难度 |
|------|-----------|------|------|
| Codeforces | `https://codeforces.com/api/contest.list` | JSON REST | ⭐ 低 |
| AtCoder | `https://atcoder.jp/contests/` | HTML | ⭐⭐ 中 |
| 牛客 | 竞赛列表页 | HTML + Jsoup | ⭐⭐ 中 |
| 洛谷 | 比赛页面 | HTML / 非官方 API | ⭐⭐ 中 |
| CCPC | 官网新闻/赛事页 | HTML | ⭐⭐⭐ 高 |
| ICPC | 官网 | HTML | ⭐⭐⭐ 高 |
| 蓝桥杯 | 官网 | HTML | ⭐⭐⭐ 高 |

> **迭代 1 只做 Codeforces**：有官方 API、结构稳定，先跑通策略模式 + 归一化 + 入库，再复制到其他平台。

---

## 11. 模块边界

| 关联模块 | 边界说明 |
|----------|----------|
| 模块 0 | 复用 Result、异常、Redis、Security；不修改 JWT 逻辑 |
| 模块 1 | 不碰 `user` 表；ADMIN 可触发手动爬虫、管理 `contest_source` |
| 模块 3 | 订阅表外键用 `contest.id` + `contest.start_time`；模块 2 提供稳定 ID |
| 模块 6 | 赛事讨论关联 `contest_id`；模块 2 提供详情 API |

---

## 12. 前端规划（迭代 3）

在 `acm01-web` 中新增：

```text
src/
├── api/contest.ts              # 赛事 API 封装
└── pages/
    ├── ContestListPage.tsx     # 赛事列表
    └── ContestCalendarPage.tsx # 赛事日历
```

`UserHomePage` 中「赛事日历」卡片改为可点击，跳转至上述页面。

**迭代 1 ~ 2 可仅用 Postman 测 API，前端不阻塞后端开发。**

---

## 13. 面试话术

> 针对 6 大 OJ 平台接口异构、数据杂乱问题，采用**策略模式**做数据归一化清洗：每个平台独立 Crawler 实现，统一输出 `ContestDTO` 后入库。  
> 以 `source + external_id` 去重、`raw_hash` 实现增量更新，避免重复写入。  
> 配合 **Quartz** 定时调度 + **Redis** 限流控频，解决多源聚合与接口限流问题。  
> 查询侧 **Redis 缓存**热门赛事，公开 API 无需登录，为订阅提醒与社交模块提供稳定数据源。

---

## 14. 实施前 Checklist

- [ ] MySQL 执行 `contest` 建表 SQL
- [ ] （迭代 2）执行 `contest_source` 建表 + 初始化数据
- [ ] 确认 `ContestSource` 枚举值与 DB `source` / `source_code` 一致
- [ ] 本机 Redis 已启动（迭代 2 起需要）
- [ ] pom 添加 okhttp、jsoup、quartz 依赖
- [ ] `SecurityConfig` 计划放行 `/api/contests/**`

---

## 15. 类方法签名清单（迭代 1 参考）

```java
// 策略接口
public interface ContestCrawler {
    ContestSource getSource();
    List<ContestDTO> fetchContests();
    boolean enabled();
}

// 爬虫编排
public class ContestCrawlService {
    void crawlAll();
    void crawlBySource(ContestSource source);
    void upsertContest(ContestDTO dto);
}

// 查询服务
public class ContestService {
    PageResult<ContestVO> list(ContestQueryRequest query);
    ContestVO getById(Long id);
}

// 公开 API
@RestController
@RequestMapping("/api/contests")
public class ContestController {
    GET  /           → list
    GET  /{id}       → detail
}
```

---

*文档维护：模块 2 开发过程中同步更新实施状态。*
