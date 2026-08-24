# 模块 9：全文检索 — 实施文档

> 文档版本：v1.0 | 状态：**MVP 已完成（应用内同步 + MySQL 降级）**

---

## 1. 模块定位

对题解、组队帖提供统一全文检索接口，支持关键词高亮；默认 MySQL `LIKE` 降级，启用 ES 后走 Elasticsearch。

| 包路径 | `org.fjnu305.acm01.module.search` |

---

## 2. 核心组件

| 类 | 职责 |
|----|------|
| `SearchProperties` | `search.enabled`、索引名、ES URI |
| `SearchIndexConfig` | 启动时创建 `acm_search` 索引与 mapping |
| `SearchSyncService` | 索引写入/删除（`ElasticsearchSearchSyncService` / `NoOpSearchSyncService`） |
| `SearchDocumentFactory` | 题解/组队发布时构建 `SearchDocument` |
| `SearchQueryService` | ES 检索 + 高亮，失败或未启用时 MySQL 降级 |
| `SearchController` | `GET /api/search?q=&type=` |

---

## 3. API

```http
GET /api/search?q=二分&type=all&pageNum=1&pageSize=20
```

| 参数 | 说明 |
|------|------|
| `q` | 关键词（必填） |
| `type` | `all` / `solution` / `team` |
| `pageNum` / `pageSize` | 分页 |

**响应字段 `hits[].highlights`：** ES 或 MySQL 降级时生成的 `<em>` 高亮片段。

---

## 4. 同步时机

| 事件 | 动作 |
|------|------|
| 题解发布/更新且 status=1 | `index solution:{id}` |
| 题解下架/删除 | `delete solution:{id}` |
| 组队帖发布 | `index team:{id}` |

---

## 5. 配置

```yaml
search:
  enabled: false          # 设为 true 并配置 ES URI 后启用
  index-name: acm_search
  elasticsearch-uri: http://localhost:9200
  default-page-size: 20

spring:
  elasticsearch:
    uris: ${SEARCH_ELASTICSEARCH_URIS:http://localhost:9200}
```

启用步骤：

1. 启动 Elasticsearch 8.x
2. `search.enabled=true`
3. 设置 `SEARCH_ELASTICSEARCH_URIS` 或 `spring.elasticsearch.uris`
4. 重启应用（`SearchIndexConfig` 自动建索引）
5. 重新发布题解/组队帖以写入索引

---

## 6. 前端

| 路由 | 组件 |
|------|------|
| `/search` | `SearchPage` |

API：`acm01-web/src/api/search.ts`

---

## 7. 未来：Canal Binlog 同步（未实现）

> MVP 使用应用层 `SearchSyncService` 在发布时同步；生产环境可替换为 Canal 无侵入方案。

```text
MySQL Binlog
    ↓ Canal Server
Canal Client（独立进程或 Spring 模块）
    ↓ 解析 INSERT/UPDATE/DELETE
SearchSyncService.index / delete
    ↓
Elasticsearch acm_search
```

**优势：**

- 业务代码零侵入，历史数据可全量 + 增量同步
- 解耦发布路径与检索延迟

**待办：**

- 部署 Canal Server，配置 `solution` / `team_post` 表过滤
- 实现 Canal Consumer 调用 `SearchSyncService`
- 全量 Reindex 任务（首次上线 ES）

---

## 8. 依赖

- `spring-boot-starter-elasticsearch`（已在 `pom.xml`）
- 索引字段：`type`, `refId`, `title`, `content`, `tags`, `authorId`, `authorName`, `createdTime`
