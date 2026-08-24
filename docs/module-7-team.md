# 模块 7：组队匹配 — 实施文档

> 文档版本：v1.0 | 状态：**MVP 已完成**

---

## 1. 模块定位

发布组队需求，按 Rating / 地区 / 算法标签智能推荐队友，支持邀请与接受加入。

| 包路径 | `org.fjnu305.acm01.module.team` |
|--------|--------------------------------|
| 建表脚本 | `src/main/resources/db/init-team.sql` |

---

## 2. 数据表

| 表名 | 说明 |
|------|------|
| `team_post` | 组队帖 |
| `team_member` | 成员与邀请（`status=0` 待接受，`1` 已加入） |
| `match_record` | 推荐结果快照 |

---

## 3. 匹配算法

**类：** `TeamMatchService`

```text
matchScore = w1 × ratingScore + w2 × regionScore + w3 × tagScore
```

| 维度 | 计算方式 |
|------|----------|
| ratingScore | `1 - min(|user.cf_rating - target| / span, 1)`，`target = (ratingMin+ratingMax)/2` |
| regionScore | 用户 `school` 与组队帖 `region` 相同为 1，否则 0（未填则 0.5） |
| tagScore | 组队帖 `tags` 与用户题解 `tags` 的 Jaccard 相似度 |

**权重配置（`application.yml`）：**

```yaml
team:
  match:
    rating-weight: 0.4
    region-weight: 0.3
    tag-weight: 0.3
    recommend-limit: 20
```

---

## 4. API 一览

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/api/teams` | 登录 | 发布组队（自动加入 LEADER） |
| GET | `/api/teams` | 公开 | 列表 |
| GET | `/api/teams/{id}` | 公开 | 详情含成员 |
| GET | `/api/teams/{id}/recommend` | 登录（队长/成员） | 智能推荐 |
| POST | `/api/teams/{id}/invite/{userId}` | 登录（队长） | 发送邀请 |
| POST | `/api/teams/invites/{memberId}/accept` | 登录（被邀请人） | 接受邀请 |

---

## 5. 与模块 9 联动

发布组队帖后 `SearchDocumentFactory.indexTeamPost()` 同步 ES。

---

## 6. 前端页面

| 路由 | 组件 |
|------|------|
| `/teams` | `TeamListPage` |
| `/teams/new` | `TeamPublishPage` |
| `/teams/:id` | `TeamDetailPage` |

API 客户端：`acm01-web/src/api/team.ts`

---

## 7. 初始化

```text
mysql acm < src/main/resources/db/init-team.sql
```
