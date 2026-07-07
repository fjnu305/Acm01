# 爬虫日志字段 — 全平台统一映射

> 表：`contest_crawl_log`  
> 写入：`CrawlLogService` ← `ContestCrawlService` ← 各 `*Crawler` + `CrawlHttpClient`

---

## 1. 七平台 `request_url` 约定

| source | 策略类 | request_url（`getPrimaryRequestUrl()`） |
|--------|--------|----------------------------------------|
| codeforces | CodeforcesCrawler | `https://codeforces.com/api/contest.list` |
| atcoder | AtCoderCrawler | `https://atcoder.jp/contests/` |
| nowcoder | NowcoderCrawler | `https://ac.nowcoder.com/acm/contest/vip-index?topCategoryFilter=13` |
| luogu | LuoguCrawler | `https://www.luogu.com.cn/contest/list?_contentOnly=1` |
| ccpc | CcpcCrawler | `https://ccpc.io/placard` |
| icpc | IcpcCrawler | `https://icpc.global/regionals/upcoming` |
| lanqiao | LanqiaoCrawler | `https://dasai.lanqiao.cn/` |

---

## 2. 日志字段统一来源

| 字段 | 成功 | 失败 | 赋值位置 |
|------|------|------|----------|
| source | ContestSource.value | 同左 | `CrawlLogService` ← `crawler.getSource()` |
| status | SUCCESS | FAILED | `CrawlLogStatus` |
| trigger_type | AUTO / MANUAL | 同左 | `ContestCrawlService.crawl(source, trigger)` |
| error_type | NULL | 见下表 | `CrawlFetchException.errorType` |
| http_status | NULL | 503/429 等 | `CrawlFetchException.httpStatus` |
| request_url | 上表 | 同左 | `crawler.getPrimaryRequestUrl()` |
| max_retries | yml 配置（3） | 同左 | `CrawlHttpProperties` / 异常 |
| total_attempts | 1 | max_retries+1 或实际 | HTTP 耗尽=4；API 业务错=1 |
| fetched_count | list.size() | 0 | 爬取结果 |
| elapsed_ms | 计时 | 同左 | `ContestCrawlService` |
| error_message | NULL | 异常 message | 截断 1000 字 |
| error_detail | NULL | 堆栈 | 截断 4000 字 |

---

## 3. error_type 与策略对应

| error_type | 触发场景 | 典型平台 |
|------------|----------|----------|
| HTTP_ERROR | 网络错误 / 4xx(非429) / 5xx 重试耗尽 | 全部（经 CrawlHttpClient） |
| API_ERROR | HTTP 200 但平台 API status!=OK | Codeforces |
| PARSE_ERROR | JSON/HTML 解析失败 | 全部 |
| TIMEOUT | 连接/读取超时 | 全部 |
| UNKNOWN | 未预期 RuntimeException | 全部 |

---

## 4. 与 contest_source 联动

| 结果 | contest_source 更新 |
|------|---------------------|
| SUCCESS | last_crawl_status=SUCCESS, fail_count=0 |
| FAILED | last_crawl_status=FAILED, fail_count+1 |

---

*建表脚本：`src/main/resources/db/init-contest-crawl.sql`*
