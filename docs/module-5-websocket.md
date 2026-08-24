# 模块 5：实时通信 WebSocket — 实施文档

> STOMP over SockJS；赛前提醒弹窗推送  
> 文档版本：v1.0 | 状态：**MVP 已完成**

---

## 1. 模块定位

**模块 5 负责「在线用户的实时推送」**：用户登录后建立 WebSocket 连接，模块 4 投递提醒时并行调用 `WebSocketNotifyHandler` 推送弹窗消息。

| 组件 | 职责 |
|------|------|
| `WebSocketConfig` | 注册 STOMP 端点 `/ws`，配置 broker |
| `StompAuthInterceptor` | CONNECT 帧校验 JWT |
| `SessionManager` | 跟踪 userId ↔ sessionId，判断在线 |
| `RealtimePushService` | 按 userId 推送 `PushMessage` |
| `WebSocketNotifyHandler` | 模块 4 delivery 渠道处理器（与 Email 并行） |

---

## 2. 包结构

```text
org.fjnu305.acm01.module.websocket/
├── WebSocketConfig.java
├── StompAuthInterceptor.java
├── SessionManager.java
├── api/RealtimePushService.java
├── dto/PushMessage.java
└── service/RealtimePushServiceImpl.java

org.fjnu305.acm01.module.notify.delivery.websockethandler/
└── WebSocketNotifyHandler.java
```

---

## 3. 连接与鉴权

### 3.1 端点

| 项 | 值 |
|----|-----|
| SockJS 端点 | `http://localhost:8080/ws` |
| 应用前缀 | `/app` |
| Broker | `/topic`, `/queue` |
| 用户前缀 | `/user` |

### 3.2 JWT

客户端在 STOMP **CONNECT** 帧携带 Header：

```text
Authorization: Bearer <token>
```

`StompAuthInterceptor` 校验后将 `userId` 写入 Principal（`getName()` = userId 字符串）。

### 3.3 订阅

登录用户订阅：

```text
/user/queue/notifications
```

服务端 `RealtimePushServiceImpl.pushToUser(userId, message)` 投递到同一路径。

---

## 4. 与模块 4 的集成

```text
NotifyDispatchService.processUserBatch
    ├─ channel=EMAIL    → EmailNotifyHandler.sendMerged
    └─ channel=WEBSOCKET → WebSocketNotifyHandler.sendMerged
                              └─ RealtimePushService.pushToUser
```

订阅时（模块 3）会为每个提醒档位登记 **两条** `notify_task`：

- `EMAIL` — 发邮件
- `WEBSOCKET` — 在线弹窗（用户离线时记 SKIPPED，任务仍标记 SENT）

---

## 5. PushMessage 格式

```json
{
  "type": "NOTIFY_REMINDER",
  "title": "赛前提醒 - Codeforces Round #999",
  "body": "平台：CODEFORCES\n赛事：...",
  "contestId": 42,
  "contestTitle": "Codeforces Round #999",
  "contestUrl": "https://..."
}
```

---

## 6. 前端集成

| 文件 | 职责 |
|------|------|
| `context/NotificationContext.tsx` | STOMP 客户端、连接生命周期 |
| `App.tsx` | 包裹 `NotificationProvider` |
| `vite.config.ts` | 代理 `/ws`（`ws: true`） |

依赖：`@stomp/stompjs`、`sockjs-client`

收到推送后展示右上角 toast（约 8 秒自动消失）。

---

## 7. 配置与安全

- `SecurityConfig` 放行 `/ws/**`（握手）；业务鉴权在 STOMP CONNECT。
- CORS 允许 `localhost:5173` / `127.0.0.1:5173`。

---

## 8. 测试步骤

1. 启动后端 + 前端，登录用户。
2. 浏览器 Network → WS，确认 `/ws` 连接成功。
3. 订阅一场即将开始的赛事。
4. 将 `notify_task`（channel=WEBSOCKET）的 `scheduled_at` 改为过去时间：

```sql
UPDATE notify_task SET scheduled_at = DATE_SUB(NOW(), INTERVAL 1 MINUTE)
WHERE channel = 'WEBSOCKET' AND status = 'PENDING' LIMIT 1;
```

5. 约 1 分钟后应出现弹窗 toast；`notify_log` 有 `WEBSOCKET` / `SUCCESS` 记录。

---

## 9. 相关文档

| 文档 | 内容 |
|------|------|
| [`module-4-notify.md`](module-4-notify.md) | 提醒任务、delivery、重试 |
| [`architecture-modules.md`](architecture-modules.md) | 全站模块规划 |
