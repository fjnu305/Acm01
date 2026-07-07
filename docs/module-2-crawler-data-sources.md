# 模块 2：七平台爬虫策略 — 数据来源调研说明

> 对应 `module-2-contest-crawler.md` §4.3 七个策略类  
> 目的：实现前明确「可直接调 API」与「必须爬虫/解析页面」的边界  
> **本文档仅调研，不涉及代码改动**

---

## 1. 总览

| 分类 | 平台 | 说明 |
|------|------|------|
| **官方公开 JSON API** | Codeforces | 唯一有正式 REST 文档、匿名可访问 |
| **页面内嵌 JSON（半 API）** | 洛谷、牛客 | 无公开文档，但响应为结构化 JSON，不必整页 Jsoup |
| **非官方第三方 JSON** | AtCoder（可选） | kenkoooo 等社区镜像，需限速、注意稳定性 |
| **纯 HTML / SPA 爬虫** | AtCoder 官网、CCPC、ICPC、蓝桥杯 | 无聚合 API，需 Jsoup 或逆向前端接口 |

**结论（按实现成本从低到高）：**

1. **迭代 1**：只做 `CodeforcesCrawler`（官方 API）
2. **迭代 2**：`LuoguCrawler`（内嵌 JSON）+ `NowcoderCrawler`（HTML 内嵌 JSON）+ `AtCoderCrawler`（优先非官方 JSON 或 HTML）
3. **迭代 3**：`CcpcCrawler` / `IcpcCrawler` / `LanqiaoCrawler`（纯爬虫，信息分散、维护成本高）

---

## 2. 各策略类详细说明

### 2.1 `CodeforcesCrawler` — P0 迭代 1

| 项 | 内容 |
|----|------|
| **是否有现成 API** | ✅ **有，官方** |
| **是否需要爬虫** | ❌ 不需要 |
| **端点** | `GET https://codeforces.com/api/contest.list` |
| **文档** | [Codeforces API Help](https://codeforces.com/apiHelp/methods) |
| **可选参数** | `gym=true` 返回 Gym 赛；`groupCode` 需登录鉴权 |
| **主要字段** | `id`, `name`, `type`, `phase`, `durationSeconds`, `startTimeSeconds` |
| **映射建议** | `externalId=id`；`url=https://codeforces.com/contest/{id}`；`startTime/endTime` 由秒时间戳 + duration 计算；`phase` → status；`type`/名称 → difficulty |
| **过滤** | 通常只保留 `type=CF` 正式 Rated 赛，排除 `FINISHED` 过久历史（按产品需求） |
| **限速** | 官方未硬性限制，建议仍设 Redis 间隔（如 60s） |

---

### 2.2 `AtCoderCrawler` — P1 迭代 2

| 项 | 内容 |
|----|------|
| **是否有现成 API** | ❌ AtCoder **无官方**公开赛事列表 API |
| **是否需要爬虫** | ⚠️ 视方案而定 |

**方案 A — 非官方 JSON（推荐优先评估）**

| 数据源 | URL | 说明 |
|--------|-----|------|
| kenkoooo AtCoderProblems | `https://kenkoooo.com/atcoder/resources/contests.json` | 全量历史赛；字段 `id`, `title`, `start_epoch_second`, `duration_second` |
| 社区 upcoming API | `https://atcoderapi-production.up.railway.app/api/upcoming-contests` | 仅即将开始；字段 `title`, `link`, `unixTime`, `duration` |

- 要求：kenkoooo 文档要求请求间隔 **≥ 1 秒**，API 可能变更
- 优点：纯 JSON，实现快
- 缺点：非官方、无 SLA

**方案 B — HTML 解析（文档原方案）**

| 数据源 | URL | 说明 |
|--------|-----|------|
| 官网列表页 | `https://atcoder.jp/contests/` | Jsoup 解析「即将举行 / 最近」表格：标题、链接、开始时间 |

- 优点：不依赖第三方
- 缺点：页面结构变更需维护选择器

**建议**：迭代 2 先用 **方案 A（kenkoooo）** 跑通；保留 HTML 作降级或校验。

---

### 2.3 `NowcoderCrawler` — P1 迭代 2

| 项 | 内容 |
|----|------|
| **是否有现成 API** | ❌ **无面向公众的竞赛列表 API** |
| **是否需要爬虫** | ✅ 需要（但多为「页面内嵌 JSON」，非纯 HTML 正则） |

> 说明：`https://api.nowcoder.com` 为 B 端招聘/笔试对接文档，需 `apiKey` 鉴权，**与 ACM 竞赛日历无关**。

| 数据源 | URL | 解析方式 |
|--------|-----|----------|
| 竞赛首页 | `https://ac.nowcoder.com/acm/contest/vip-index?topCategoryFilter=13` | 找 `div.platform-item.js-item`，读 `data-json` 属性 |
| 日历页（备选） | `https://ac.nowcoder.com/acm/contest/calendar` | 可能为前端渲染，需抓包确认 XHR |

**`data-json` 常见字段**（社区实践）：

```json
{
  "contestId": 12345,
  "contestName": "牛客周赛 Round N",
  "contestStartTime": 1700000000000,
  "contestEndTime": 1700007200000,
  "contestDuration": 7200000
}
```

**映射**：时间戳为毫秒；`url=https://ac.nowcoder.com/acm/contest/{contestId}`

**请求注意**：需完整 `User-Agent`、`Referer`（牛客域名），建议随机间隔防限流。

---

### 2.4 `LuoguCrawler` — P2 迭代 3

| 项 | 内容 |
|----|------|
| **是否有现成 API** | ⚠️ **无正式公开文档**，但有稳定的「内容-only JSON」接口 |
| **是否需要爬虫** | ❌ 通常不需要 Jsoup；✅ 需要 HTTP + JSON 解析 |

| 数据源 | URL / 方式 | 说明 |
|--------|------------|------|
| 比赛列表（推荐） | `GET https://www.luogu.com.cn/contest/list?_contentOnly=1` | 返回 JSON，`currentData.contests.result[]` |
| 等价 Header 方式 | `GET /contest/list` + `x-lentille-request: content-only` | 见 [luogu-api-docs](https://0f-0b.github.io/luogu-api-docs/) |

**主要字段**：`id`, `name`, `startTime`, `endTime`（Unix 秒）

**映射**：`url=https://www.luogu.com.cn/contest/{id}`

**注意**：需浏览器型 `User-Agent`；遵守 `robots.txt`；建议分页参数 `page=` 若列表过长。

---

### 2.5 `CcpcCrawler` — P2 迭代 3

| 项 | 内容 |
|----|------|
| **是否有现成 API** | ❌ 无公开赛事聚合 API |
| **是否需要爬虫** | ✅ **必须** |

| 数据源 | URL | 说明 |
|--------|-----|------|
| 官网公告 | `https://ccpc.io/placard` | 赛事通知、站点赛安排 |
| 赛事查询 | `https://ccpc.io/search` | 可能为 SPA，需分析网络请求 |
| 报名系统 | `https://signup.ccpc.io/` | 与官网分离，不一定有统一 JSON |

**难点**：

- 页面多为前端渲染，时间/地点常在公告正文中，**字段非结构化**
- 更新频率低，适合较长 cron（如每日 1 次）
- `externalId` 可用公告 ID 或「年份+站点名」哈希

---

### 2.6 `IcpcCrawler` — P2 迭代 3

| 项 | 内容 |
|----|------|
| **是否有现成 API** | ❌ **无全球区域赛日历 API** |
| **是否需要爬虫** | ✅ **必须** |

> 说明：[ICPC Contest API (CCS Spec)](https://ccs-specs.icpc.io/) 用于**单场进行中比赛**的计分/提交数据（CDS），不能用来拉「即将举行的区域赛列表」。

| 数据源 | URL | 说明 |
|--------|-----|------|
| 全球 upcoming | `https://icpc.global/regionals/upcoming` | 可能 SPA |
| 北美等区域 | `https://na.icpc.global/regionals/` 等 | 各区域独立站点 |
| 国内信息 | 各校/OJ 转载、CCPC 交叉 | 信息分散 |

**难点**：全球碎片化，**维护成本最高**；建议 MVP 只抓 `icpc.global` 一页 + 手动补数据，或迭代 3 后期再做。

---

### 2.7 `LanqiaoCrawler` — P2 迭代 3

| 项 | 内容 |
|----|------|
| **是否有现成 API** | ❌ 无公开比赛列表 API |
| **是否需要爬虫** | ✅ **必须** |

| 数据源 | URL | 说明 |
|--------|-----|------|
| 大赛官网 | `https://dasai.lanqiao.cn/` | 省赛/国赛通知、赛程（年度周期） |
| 蓝桥云课比赛 | `https://www.lanqiao.cn/contests/` | 练习赛、模拟赛 |
| 算法 OJ 赛 | `https://www.lanqiao.cn/oj-contest/` | 与正式蓝桥杯需区分 |

**难点**：

- 正式蓝桥杯为**年度赛事**，需过滤历史届次
- 省赛时间按省份分散，多在通知 PDF/HTML 正文中
- `contestType` 可标「省赛 / 国赛 / 云课练习赛」

---

## 3. 对照表（实现时快速查阅）

| 策略类 | 现成 API | 爬虫 | 推荐数据源 | 协议 | 迭代 |
|--------|:--------:|:----:|------------|------|------|
| `CodeforcesCrawler` | ✅ 官方 | — | `codeforces.com/api/contest.list` | JSON REST | 1 |
| `AtCoderCrawler` | ⚠️ 非官方 | 可选 | kenkoooo JSON **或** 官网 HTML | JSON / HTML | 2 |
| `NowcoderCrawler` | ❌ | ✅ | vip-index 页 `data-json` | HTML+内嵌 JSON | 2 |
| `LuoguCrawler` | ⚠️ 内嵌 JSON | — | `contest/list?_contentOnly=1` | JSON | 3 |
| `CcpcCrawler` | ❌ | ✅ | `ccpc.io/placard` 等 | HTML/SPA | 3 |
| `IcpcCrawler` | ❌ | ✅ | `icpc.global/regionals/upcoming` 等 | HTML/SPA | 3 |
| `LanqiaoCrawler` | ❌ | ✅ | `dasai.lanqiao.cn` / `lanqiao.cn/contests` | HTML | 3 |

---

## 4. 与 `ContestDTO` 字段映射共性

各策略最终需归一为 `ContestDTO`（见 `ContestDTO.java`）：

| DTO 字段 | 来源说明 |
|----------|----------|
| `source` | `ContestSource` 枚举 |
| `externalId` | 平台 ID 或 slug（AtCoder 为 contest id 字符串） |
| `title` | 赛事名称 |
| `url` | 详情/报名链接 |
| `startTime` / `endTime` | 统一转 `LocalDateTime`（注意 CF/kenkoooo 为秒，牛客为毫秒） |
| `status` | 可由时间计算，或读平台 phase |
| `difficulty` | CF Div、洛谷 Rated 等（可选） |
| `rawHash` | 调用 `AbstractContestCrawler.computeRawHash()` |

---

## 5. 风险与规范

| 风险 | 建议 |
|------|------|
| 非官方 API 下线 | AtCoder 备 HTML 方案；Luogu 监控 `_contentOnly` 变更 |
| 反爬 / 403 | 统一 OkHttp User-Agent；Redis 限流（`crawl:rate:{source}`） |
| 时区 | 统一存 UTC 或 Asia/Shanghai，与模块 2 文档一致 |
| 法律与礼仪 | 遵守各站 `robots.txt`；请求间隔 ≥ 1s；不压测 |

---

## 6. 下一步（仍不写代码）

1. **迭代 1**：确认 `CodeforcesCrawler.doFetch()` 只调 `contest.list`，过滤规则写进类注释  
2. **迭代 2**：AtCoder 选定 kenkoooo vs HTML；牛客验证 `data-json` 选择器是否仍有效  
3. **迭代 3**：CCPC / ICPC / 蓝桥杯先做「单页 POC」验证能否稳定解析，再写正式策略类  

---

*文档版本：v1.0 | 调研日期：2026-07-07*
