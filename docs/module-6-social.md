# 模块 6：社交社区 — 实施文档

> 动态、评论、点赞、关注与热门 Feed  
> 文档版本：v1.0 | 状态：**MVP 已完成**

---

## 1. 模块目标

| 目标 | 说明 |
|------|------|
| 发动态 | 用户发布文字动态，可选话题 |
| 互动 | 评论、点赞 |
| 关注 | 关注 / 取消关注其他用户 |
| 热门 Feed | 按点赞数排序，Redis 缓存 5 分钟 |
| 管理 | ADMIN 删帖 API |

**依赖：** 模块 1（用户鉴权）

---

## 2. 包结构

```text
module/social/
├── controller/PostController.java
├── controller/CommentController.java
├── controller/FollowController.java
├── controller/PostAdminController.java
├── service/PostService.java
├── service/CommentService.java
├── service/FollowService.java
├── service/FeedService.java          # Redis 热门缓存
├── config/SocialFeedProperties.java
├── entity/mapper/dto/vo
```

---

## 3. 数据库

脚本：`src/main/resources/db/init-social.sql`

| 表 | 说明 |
|----|------|
| `topic` | 话题 |
| `post` | 动态 |
| `comment` | 评论 |
| `post_like` | 点赞 |
| `follow` | 关注关系 |

---

## 4. API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/posts` | 发动态 |
| GET | `/api/posts/feed/hot` | 热门 Feed |
| GET | `/api/posts` | 分页列表 |
| GET | `/api/posts/{id}` | 详情 |
| DELETE | `/api/posts/{id}` | 删自己的帖 |
| POST/DELETE | `/api/posts/{id}/like` | 点赞/取消 |
| POST | `/api/posts/{postId}/comments` | 评论 |
| GET | `/api/posts/{postId}/comments` | 评论列表 |
| POST/DELETE | `/api/follow/{userId}` | 关注/取消 |
| GET | `/api/follow/following` | 我关注的人 |
| DELETE | `/api/admin/posts/{id}` | 管理员删帖 |

---

## 5. Redis 缓存

- Key：`social:feed:hot`
- TTL：5 分钟（`social.feed.cache-ttl-minutes`）
- 发帖/点赞/删帖时 `evictHotFeed()`

---

## 6. 前端

- `SocialFeedPage` — 动态广场
- 路由：`/social`

---

## 7. 后续迭代

- WebSocket 推送新动态
- 话题页、@用户
- ES 全文检索（模块 9）
