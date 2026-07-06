# ACM 竞赛聚合平台 — 模块架构规划

> 基于现有代码（JWT 骨架 + MySQL 配置）的模块化单体架构拆分方案

---

## 1. 现有代码资产

| 已有组件 | 路径 | 状态 |
|----------|------|------|
| JWT 签发/解析 | `Security/JwtTokenProvider.java` | ✅ 可用 |
| JWT 配置 | `Security/JwtProperties.java` | ✅ 可用 |
| 密钥加载 | `Common/JwtSecretFileUtil.java` | ✅ 可用 |
| 认证 Filter | `Security/JwtAuthenticationFilter.java` | ⚠️ 需补全 |
| MySQL 数据源 | `resources/application.yml` | ✅ 已配置 |
| Redis | `pom.xml` 依赖 | ❌ 未使用 |
| Elasticsearch | `pom.xml` 依赖 | ❌ 未使用 |
| MyBatis | `pom.xml` 依赖 | ❌ 未使用 |

---

## 2. 推荐包结构

| 包名 | 职责 |
|------|------|
| `common` | 统一响应、异常、工具类 |
| `config` | Security / Redis / RabbitMQ / Quartz / ES / WebSocket 配置 |
| `security` | JWT、SecurityConfig、UserDetails |
| `module-user` | 用户与权限 |
| `module-contest` | 赛事聚合 + 爬虫 |
| `module-subscription` | 订阅提醒 |
| `module-notify` | 消息分发（MQ 消费、邮件、WebSocket） |
| `module-social` | 社交社区 |
| `module-team` | 组队匹配 |
| `module-solution` | 题解分享 |
| `module-search` | 全文检索（ES + Canal） |
| `module-sync` | OJ 竞技数据同步 |

**每个 module 内部统一三层：** `controller → service → mapper/entity`

---

## 3. 模块总览（8+1）

| 编号 | 模块名 | 对应简历亮点 | 优先级 |
|------|--------|--------------|--------|
| 0 | 公共基础设施 | 项目地基 | P0 最先 |
| 1 | 用户与权限体系 | 权限 + 用户体系完整 | P1 |
| 2 | 赛事聚合与爬虫 | 定时任务分布式爬虫 | P2 核心 |
| 3 | 订阅提醒 | 高并发异步订阅提醒 | P4 |
| 4 | 消息分发 | RabbitMQ 异步解耦 | P4 |
| 5 | 实时通信 WebSocket | 实时在线消息推送 | P5 |
| 6 | 社交社区 | ACmer 社交交流 | P6 |
| 7 | 组队匹配 | 实时社交智能组队 | P7 |
| 8 | 题解分享 | 题解分享专区 | P8 |
| 9 | 全文检索 ES | 分布式全文检索引擎 | P9 |
| 10 | OJ 数据同步 | 跨平台竞技数据同步 | P10 最后 |

---

## 4. 各模块详细设计

### 模块 0：公共基础设施

| 子项 | 内容 |
|------|------|
| 统一响应 | `Result<T>`、`PageResult` |
| 全局异常 | `GlobalExceptionHandler`、业务异常码 |
| 配置类 | `SecurityConfig`、`RedisConfig`、`MyBatisConfig` |
| 公共枚举 | 赛事来源、消息类型、用户角色 |
| JWT 补全 | 角色 → `GrantedAuthority`，写入 `SecurityContext` |
| 依赖 | 无 |
| 预估周期 | 1 周 |

---

### 模块 1：用户与权限体系

| 子项 | 内容 |
|------|------|
| 核心表 | `user`、`user_profile`、`user_role` |
| 核心接口 | 注册/登录、个人主页、管理员审核 |
| 技术点 | BCrypt 密码、JWT 鉴权、RBAC（USER / ADMIN） |
| 对外提供 | 用户 ID、标签（算法擅长、地区、Rating）→ 模块 6、7 |
| 对外提供 | 管理员审核接口 → 模块 5、8 内容审核 |
| 依赖 | 模块 0 |
| 预估周期 | 1 ~ 1.5 周 |

---

### 模块 2：赛事聚合与爬虫

| 子项 | 内容 |
|------|------|
| 核心表 | `contest`、`contest_source` |
| 设计模式 | 策略模式 `crawler/strategy/` |
| 爬虫实现 | Codeforces、AtCoder、牛客、洛谷、CCPC、ICPC、蓝桥杯 |
| 去重键 | `source + external_id` 唯一 |
| 增量更新 | 对比 `updated_at`，只更新变化字段 |
| Redis | 爬虫请求频率限流，防 IP 封禁 |
| Quartz | 每 6h / 每日凌晨全量+增量抓取 |
| 对外提供 | 赛事列表/详情/日历 API → 模块 3、6 |
| 对外提供 | 热门赛事 Redis 缓存 |
| 依赖 | 模块 0、1（爬虫可读可匿名） |
| pom 需补 | `jsoup`、`okhttp`、`spring-boot-starter-quartz` |
| 预估周期 | 2 ~ 3 周 |

**爬虫策略类：**

| 类名 | 平台 |
|------|------|
| `CodeforcesCrawler` | Codeforces |
| `AtCoderCrawler` | AtCoder |
| `NowcoderCrawler` | 牛客 |
| `LuoguCrawler` | 洛谷 |
| `CcpcCrawler` | CCPC |
| `IcpcCrawler` | ICPC |
| `LanqiaoCrawler` | 蓝桥杯 |

---

### 模块 3：订阅提醒

| 子项 | 内容 |
|------|------|
| 核心表 | `contest_subscription`、`notify_task` |
| 核心接口 | 订阅/取消、自定义提醒时间（赛前 1h / 24h） |
| 技术点 | Quartz 扫描即将开始的赛事 → 生成提醒任务 |
| 流程 | Quartz Job → 查 subscription → 命中则投递 RabbitMQ |
| 依赖 | 模块 1、2 |
| pom 需补 | `spring-boot-starter-amqp` |
| 预估周期 | 1.5 ~ 2 周 |

---

### 模块 4：消息分发

| 组件 | 职责 |
|------|------|
| `ContestNotifyProducer` | 模块 3 调用，发 MQ |
| `ContestNotifyConsumer` | 批量消费，线程池并行 |
| `EmailNotifyHandler` | 邮件推送 |
| `InAppNotifyHandler` | 写 `notification` 表（站内消息） |
| `WebSocketNotifyHandler` | 调用模块 5 推实时弹窗 |

| 子项 | 内容 |
|------|------|
| 核心表 | `notification`、`notify_log`（幂等、重试） |
| 依赖 | 模块 3、5 |
| 预估周期 | 1 周 |

---

### 模块 5：实时通信 WebSocket

| 组件 | 说明 |
|------|------|
| `WebSocketConfig` | STOMP 或原生 WebSocket |
| `NotifyWebSocketHandler` | 按 userId 推送 |
| `SessionManager` | 在线用户 Session 管理（可放 Redis） |

| 推送场景 | 触发模块 |
|----------|----------|
| 赛前提醒弹窗 | 模块 4 |
| 好友发动态、评论、点赞 | 模块 6 |
| 组队邀请 | 模块 7 |

| 子项 | 内容 |
|------|------|
| 依赖 | 模块 0、1 |
| pom 需补 | `spring-boot-starter-websocket` |
| 预估周期 | 0.5 ~ 1 周 |

---

### 模块 6：社交社区

| 子项 | 内容 |
|------|------|
| 核心表 | `post`、`comment`、`like`、`follow`、`topic` |
| 核心接口 | 发动态、评论、点赞、关注、话题讨论 |
| Redis | 首页动态 Feed 缓存、点赞计数 |
| 审核 | ADMIN 删帖/封禁（模块 1 权限） |
| 对外提供 | 用户竞技画像展示（接模块 10） |
| 对外提供 | 动态变更 → 模块 5 WebSocket 推送 |
| 依赖 | 模块 1、5 |
| 预估周期 | 2 周 |

---

### 模块 7：组队匹配

| 子项 | 内容 |
|------|------|
| 核心表 | `team_post`、`team_member`、`match_record` |
| 匹配维度 | Rating 区间、地区、擅长算法（DP/图论/数据结构） |
| 匹配公式 | `matchScore = w1×rating差 + w2×地区相同 + w3×算法标签重合度` |
| 依赖 | 模块 1、5、6 |
| 预估周期 | 1 ~ 1.5 周 |

---

### 模块 8：题解分享

| 子项 | 内容 |
|------|------|
| 核心表 | `solution`、`solution_favorite`、`solution_template` |
| 核心能力 | 发布题解、收藏、评论、算法模板库 |
| 依赖 | 模块 1、6 |
| 预估周期 | 1 周 |

---

### 模块 9：全文检索 ES

| 组件 | 说明 |
|------|------|
| Canal | 监听 MySQL Binlog，`solution` / `post` 增量同步 ES |
| IK 分词 | 题解标题、正文、标签检索 |
| 高亮 | 搜索结果关键词高亮 |

| 子项 | 内容 |
|------|------|
| 依赖 | 模块 8、6 |
| 说明 | Canal 需单独部署；MVP 可用手动同步接口代替 |
| 预估周期 | 1 ~ 2 周 |

---

### 模块 10：OJ 数据同步

| 子项 | 内容 |
|------|------|
| 核心表 | `oj_account`（AES 加密 cookie/token）、`user_rating_snapshot` |
| 支持平台 | Codeforces API、洛谷、牛客（优先有 API 的） |
| 展示 | 个人主页多平台 Rating 曲线、参赛记录 |
| 依赖 | 模块 1 |
| 说明 | 模拟登录风险高，面试讲设计即可，实现可简化 |
| 预估周期 | 2 周 |

---

## 5. 开发阶段与里程碑

| 阶段 | 模块 | 周期 | 可演示成果 |
|------|------|------|------------|
| P0 | 模块 0 基础设施 | 1 周 | 项目能编译、登录能用 |
| P1 | 模块 1 用户权限 | 1 周 | 注册登录、个人主页骨架 |
| P2 | 模块 2 赛事爬虫 | 2 ~ 3 周 | 赛事日历、CF + 牛客能抓 |
| P3 | 模块 2 + Redis | 3 天 | 热门赛事缓存，接口 < 50ms |
| P4 | 模块 3 + 4 订阅 + MQ | 2 周 | 订阅赛事、邮件/站内提醒 |
| P5 | 模块 5 WebSocket | 1 周 | 赛前弹窗实时推送 |
| P6 | 模块 6 社交 | 2 周 | 动态、评论、关注 |
| P7 | 模块 7 组队 | 1 周 | 发布组队、简单匹配 |
| P8 | 模块 8 题解 | 1 周 | 题解发布、收藏 |
| P9 | 模块 9 ES 检索 | 1 ~ 2 周 | 题解全文搜索 |
| P10 | 模块 10 OJ 同步 | 2 周 | 主页 Rating 展示 |

**MVP 范围 = P0 ~ P5**（约 6 ~ 8 周），已覆盖爬虫、Redis、RabbitMQ、WebSocket 四大亮点。

---

## 6. 中间件接入时机

| 中间件 | 引入阶段 | 用途 |
|--------|----------|------|
| MySQL + MyBatis-Plus | P0 | 全部持久化 |
| Redis | P3 | 赛事缓存、点赞计数、Session、爬虫限流 |
| Quartz | P2 | 爬虫定时任务 |
| RabbitMQ | P4 | 提醒消息异步分发 |
| WebSocket | P5 | 实时推送 |
| Elasticsearch | P9 | 题解/动态全文检索 |
| Canal | P9 | Binlog → ES（可选） |
| 阿里云 OSS | P8 | 题解图片、头像上传 |
| 邮件服务 | P4 | 赛前邮箱提醒 |

---

## 7. 模块间依赖关系

| 模块 | 依赖 | 被依赖 |
|------|------|--------|
| 模块 0 基础设施 | 无 | 全部 |
| 模块 1 用户权限 | 模块 0 | 全部业务模块 |
| 模块 2 赛事爬虫 | 模块 0 | 模块 3、6 |
| 模块 3 订阅提醒 | 模块 1、2 | 模块 4 |
| 模块 4 消息分发 | 模块 3 | — |
| 模块 5 WebSocket | 模块 0、1 | 模块 4、6、7 |
| 模块 6 社交 | 模块 1、5 | 模块 7、8、9 |
| 模块 7 组队 | 模块 1、5、6 | — |
| 模块 8 题解 | 模块 1、6 | 模块 9 |
| 模块 9 ES 检索 | 模块 6、8 | — |
| 模块 10 OJ 同步 | 模块 1 | 模块 6（展示） |

**Hub 模块：** `user`（模块 1）、`contest`（模块 2）

---

## 8. pom.xml 待补充依赖

| 依赖 | 用途 | 引入阶段 |
|------|------|----------|
| `mybatis-plus-boot-starter` | 替代裸 MyBatis，简化 CRUD | P0 |
| `spring-boot-starter-quartz` | 定时爬虫、提醒扫描 | P2 |
| `spring-boot-starter-amqp` | RabbitMQ 消息队列 | P4 |
| `spring-boot-starter-websocket` | 实时推送 | P5 |
| `jsoup` | HTML 爬虫解析 | P2 |
| `okhttp` | HTTP 请求 | P2 |
| `spring-boot-starter-mail` | 邮件推送 | P4 |
| `aliyun-sdk-oss` | 文件存储 | P8 |
| `spring-boot-starter-validation` | 参数校验 | P0 |

---

## 9. 第一套核心数据库表（MVP）

| 表名 | 所属模块 | 说明 |
|------|----------|------|
| `user` | 模块 1 | 用户账号 |
| `user_profile` | 模块 1 | 个人资料、标签 |
| `user_role` | 模块 1 | 角色权限 |
| `contest` | 模块 2 | 赛事信息 |
| `contest_source` | 模块 2 | 数据来源配置 |
| `contest_subscription` | 模块 3 | 用户订阅规则 |
| `notify_task` | 模块 3 | 待推送任务 |
| `notification` | 模块 4 | 站内消息 |
| `notify_log` | 模块 4 | 推送日志（幂等） |

---

## 10. 面试话术模板

| 维度 | 说法 |
|------|------|
| 架构选型 | 采用模块化单体，按业务域拆 8 个核心模块，包级边界解耦 |
| 模块通信 | 同步走 Service 接口，异步走 RabbitMQ 事件 |
| 核心闭环 | 爬虫抓赛事 → 统一展示 → 用户订阅 → MQ 异步提醒 → WebSocket 实时推送 |
| 性能优化 | Redis 缓存热门赛事与首页 Feed，ES 替代 MySQL 模糊查询 |
| 数据同步 | Canal 监听 Binlog 无侵入同步 ES；AES 托管 OJ 凭证增量拉 Rating |
