# 模块 8：题解分享 — 实施文档

> 文档版本：v1.0 | 状态：**MVP 已完成**

---

## 1. 模块定位

题解分享专区：用户发布 Markdown 题解、收藏、浏览算法模板；管理员可下架违规内容；发布时同步至 ES 索引（模块 9）。

| 包路径 | `org.fjnu305.acm01.module.solution` |
|--------|--------------------------------------|
| 建表脚本 | `src/main/resources/db/init-solution.sql` |

---

## 2. 数据表

| 表名 | 说明 |
|------|------|
| `solution` | 题解主表 |
| `solution_favorite` | 用户收藏 |
| `solution_template` | 算法模板库（含系统预置） |

**`solution.status`：** `1` 已发布 · `0` 草稿 · `2` 下架（管理员 takedown）

---

## 3. API 一览

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/solutions` | 公开 | 分页列表，支持 `keyword` / `tag` |
| GET | `/api/solutions/{id}` | 公开 | 详情（下架返回 3003） |
| GET | `/api/solutions/templates` | 公开 | 模板列表 |
| POST | `/api/solutions` | 登录 | 发布题解 |
| PUT | `/api/solutions/{id}` | 登录（作者） | 更新 |
| DELETE | `/api/solutions/{id}` | 登录（作者） | 软删除 |
| POST | `/api/solutions/{id}/favorite` | 登录 | 收藏 |
| DELETE | `/api/solutions/{id}/favorite` | 登录 | 取消收藏 |
| PUT | `/api/admin/solutions/{id}/takedown` | ADMIN | 下架 |

---

## 4. Markdown XSS 消毒

**类：** `module/solution/util/MarkdownSanitizer.java`

- 剥离 `<script>` 标签与 `on*` 事件属性
- 使用 Jsoup `Safelist.none()` 移除全部 HTML 标签
- 在 `SolutionService.create/update` 入库前调用

---

## 5. 与模块 9 联动

发布/更新且 `status=1` 时，`SearchDocumentFactory.indexSolution()` 写入 ES；下架/删除时 `delete("solution", id)`。

---

## 6. 前端页面

| 路由 | 组件 |
|------|------|
| `/solutions` | `SolutionListPage` |
| `/solutions/new` | `SolutionEditPage` |
| `/solutions/:id` | `SolutionDetailPage` |

API 客户端：`acm01-web/src/api/solution.ts`

---

## 7. 初始化

```text
mysql acm < src/main/resources/db/init-solution.sql
```

建议顺序：在 `init-user.sql` 之后执行。
