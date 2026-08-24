# 测试矩阵与验收清单

> 与 `src/test/java` 目录结构同步维护。

## 1. 全链路 Security 过滤器（`security/AnonymousEndpointSecurityTest`）

| 用例 | 期望 |
|------|------|
| 匿名 `POST /api/subscriptions` | 401 |
| 匿名 `POST /api/posts` | 401 |
| 匿名 `POST /api/teams` | 401 |
| 匿名 `POST /api/teams/1/invite/2` | 401 |
| 匿名 `GET /api/user/me` | 401 |
| 匿名 `GET /api/oj/accounts` | 401 |
| 匿名 `GET /api/contests` | 200 |
| 匿名 `GET /api/search?q=test` | 200 |
| 匿名 `GET /api/teams` | 200 |

## 2. JWT 过滤器单元（`security/JwtAuthenticationFilterTest`）

| 用例 | 期望 |
|------|------|
| 有效 Token + 活跃用户 | 写入 SecurityContext，ROLE_USER |
| 有效 Token + 禁用用户 | 不写入认证 |
| DB 角色覆盖 Token 内角色 | 使用 DB 的 ADMIN |
| 无效 Token | 不写入认证 |

## 3. `@PreAuthorize` 管理端（`security/AdminApiSecurityTest`）

| 用例 | 期望 |
|------|------|
| 匿名 / USER 访问 admin | 403 |
| ADMIN 访问 admin | 200 |

## 4. WebSocket（`websocket/StompAuthInterceptorTest`）

| 用例 | 期望 |
|------|------|
| CONNECT 无 Token | 抛异常 |
| CONNECT 无效 Token | 抛异常 |
| CONNECT 有效 Token | 设置 STOMP User |

## 5. 水平越权（`module/*/`*AccessTest）

| 模块 | 用例 | 期望 |
|------|------|------|
| 订阅 | A 取消 B 的订阅 | `SUBSCRIPTION_NOT_FOUND` |
| 题解 | A 改/删 B 的题解 | `SOLUTION_FORBIDDEN` |
| 动态 | A 删 B 的动态 | `FORBIDDEN` |
| 组队 | 非队长邀请 | `TEAM_FORBIDDEN` |
| 组队 | 非被邀请人接受 | `TEAM_INVITE_NOT_FOUND` |

## 6. 边界（`boundary/`）

| 用例 | 期望 |
|------|------|
| `pageNum=0` | 按第 1 页 |
| `pageSize=999` | 钳制为 100 |
| 赛事不存在 | `CONTEST_NOT_FOUND` |

## 7. 上线前仍建议手工 / 集成环境验收

- 真实 MySQL + Redis + RabbitMQ + ES 端到端
- WebSocket STOMP 真连接推送
- SMTP 实发
- 带真实 JWT 的登录后全流程（WebMvc 切片与 `@MockitoBean` 对 JWT 有冲突，由过滤器单测 + 匿名全链路测覆盖）

## 8. 生产安全加固（`JwtAuthenticationFilter`）

- 每次请求从 DB 重载角色（撤销权限后立即生效）
- 禁用账号（`status=0`）即使 Token 有效也不建立认证
