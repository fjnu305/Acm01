# 模块 10：OJ 竞技数据同步 — 实施文档

> Codeforces Rating 绑定与每日快照同步  
> 文档版本：v1.0 | 状态：**MVP 已完成**

---

## 1. 模块目标

| 目标 | 说明 |
|------|------|
| OJ 账号绑定 | 用户绑定 Codeforces handle，可选加密存储 cookie/token |
| Rating 同步 | 调用 CF API 拉取 Rating，写入快照表 |
| 定时任务 | Quartz 每日凌晨全量同步已绑定账号 |
| 个人主页 | `UserInfoVO` 附带 `ratingSnapshots` 列表 |

**依赖：** 模块 1（用户）、模块 0（Quartz、Redis 无关）

---

## 2. 包结构

```text
module/sync/
├── controller/OjAccountController.java
├── service/OjAccountService.java
├── service/RatingSyncService.java
├── client/CfRatingClient.java
├── vault/CredentialVault.java      # AES-GCM
├── job/RatingSyncJob.java
├── config/SyncProperties.java
├── config/RatingSyncQuartzConfig.java
├── entity/mapper/dto/vo
```

---

## 3. 数据库

脚本：`src/main/resources/db/init-oj-sync.sql`

| 表 | 说明 |
|----|------|
| `oj_account` | 用户 ↔ 平台账号，凭证 AES-GCM 加密 |
| `user_rating_snapshot` | 每日 Rating 快照，`uk(user_id, platform, snapshot_date)` |

---

## 4. API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/oj/accounts/cf` | 绑定 CF handle，立即同步 |
| GET | `/api/oj/accounts` | 我的 OJ 账号列表 |

`UserInfoVO` 扩展字段：`cfHandle`、`ratingSnapshots[]`

---

## 5. 配置

```yaml
sync:
  credential-key-base64: "<32-byte AES key Base64>"
  schedule:
    enabled: true
    cron: "0 0 3 * * ?"
```

---

## 6. 后续迭代

- 洛谷 / 牛客 API 同步
- Rating 曲线图前端组件
- 参赛记录同步
