# 模块 0：公共基础设施 — 实施文档

> ACM 竞赛聚合平台后端地基：统一规范、安全认证、中间件配置  
> 文档版本：v1.0 | 状态：**已完成**

---

## 1. 模块目标

| 目标 | 说明 |
|------|------|
| 统一 API 规范 | 所有接口返回 `{ code, message, data }` |
| 统一异常治理 | 业务错误 / 参数错误 / 401 / 403 结构化返回 |
| JWT 无状态认证 | 前后端分离，Token 鉴权，无 Session |
| 中间件就绪 | MyBatis、Redis 配置完成，供后续模块直接使用 |
| 公共枚举 | 跨模块共享常量（角色、赛事来源、提醒类型） |

**本模块不做：**

- 具体业务（用户、赛事、社交等）
- RabbitMQ / Quartz / WebSocket / ES（后续模块按需引入）

**依赖：** 无（项目第一个完成的模块）

---

## 2. 包结构（已实现）

```text
org.fjnu305.acm01/
├── Acm01Application.java              # 启动类
│
├── Common/                            # 公共层
│   ├── JwtSecretFileUtil.java         # JWT 密钥文件读取（原有）
│   ├── result/
│   │   ├── Result.java                # 统一响应
│   │   └── PageResult.java            # 分页响应
│   ├── exception/
│   │   ├── ErrorCode.java             # 错误码枚举
│   │   ├── BusinessException.java     # 业务异常
│   │   └── GlobalExceptionHandler.java
│   └── enums/
│       ├── UserRole.java              # USER / ADMIN
│       ├── ContestSource.java         # 赛事来源（模块 2 用）
│
├── Security/                          # 安全 / JWT
│   ├── JwtTokenProvider.java          # Token 签发与解析（原有）
│   ├── JwtProperties.java             # JWT 配置（原有）
│   ├── JwtAuthenticationFilter.java   # 请求过滤器（已补全）
│   ├── LoginUser.java                 # 当前登录用户对象
│   ├── JwtAuthenticationEntryPoint.java  # 401 JSON
│   └── JwtAccessDeniedHandler.java       # 403 JSON
│
└── Config/                            # 配置类
    ├── SecurityConfig.java            # Spring Security 主配置
    ├── CorsConfig.java                # 跨域（前端 5173）
    ├── MybatisConfig.java             # @MapperScan
    └── RedisConfig.java               # RedisTemplate Bean
```

> **命名说明：** 包名使用 `Common`、`Security`、`Config`（大写），与项目最初 JWT 代码风格一致，避免 Windows 大小写冲突。

---

## 3. 核心组件说明

### 3.1 统一响应 `Result<T>`

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

| 类 | 路径 | 作用 |
|----|------|------|
| `Result` | `Common/result/Result.java` | `success()` / `fail()` 静态工厂 |
| `PageResult` | `Common/result/PageResult.java` | 列表分页：`list / total / pageNum / pageSize` |

**为什么：** 前端只对接一种 JSON 结构；全局异常处理器也返回 `Result`。

---

### 3.2 异常体系

| 类 | 作用 |
|----|------|
| `ErrorCode` | HTTP 级（400/401/403/500）+ 业务码（1001 起） |
| `BusinessException` | Service 层抛出，携带 `code + message` |
| `GlobalExceptionHandler` | `@RestControllerAdvice`，捕获并转 `Result` |

**处理范围：**

| 异常类型 | HTTP | 返回 |
|----------|------|------|
| `BusinessException` | 200* | `Result.fail(业务码, msg)` |
| 参数校验失败 | 400 | `Result.fail(400, msg)` |
| 未登录 | 401 | `Result.fail(401, ...)` |
| 无权限 | 403 | `Result.fail(403, ...)` |
| 其他 | 500 | `Result.fail(500, ...)` |

---

### 3.3 JWT 认证链路

```text
请求进入
  ↓
JwtAuthenticationFilter
  ├─ 无 Token / Token 无效 → 不设置 SecurityContext，继续过滤链
  └─ Token 有效 → 解析 userId / username / roles
                 → 转为 ROLE_xxx 权限
                 → LoginUser 写入 SecurityContext
  ↓
SecurityConfig 规则判断
  ├─ 公开路径 → 直接访问
  ├─ 需登录   → 无认证则 JwtAuthenticationEntryPoint → 401 JSON
  └─ 需 ADMIN → 无权限则 JwtAccessDeniedHandler → 403 JSON
  ↓
Controller（@AuthenticationPrincipal LoginUser）
```

| 组件 | 路径 | 职责 |
|------|------|------|
| `JwtTokenProvider` | `Security/JwtTokenProvider.java` | 创建 / 验证 / 解析 JWT |
| `JwtProperties` | `Security/JwtProperties.java` | 过期时间；密钥从文件加载 |
| `JwtSecretFileUtil` | `Common/JwtSecretFileUtil.java` | 读 `classpath:jwt-secret.key` |
| `JwtAuthenticationFilter` | `Security/JwtAuthenticationFilter.java` | 每请求解析 Token |
| `LoginUser` | `Security/LoginUser.java` | `userId / username / roles` |

**JWT Payload 字段：**

| Claim | 说明 |
|-------|------|
| `sub` | 用户名 |
| `userId` | 用户 ID |
| `roles` | 逗号分隔，如 `USER` 或 `USER,ADMIN` |

---

### 3.4 Spring Security 配置

**文件：** `Config/SecurityConfig.java`

| 配置项 | 值 | 原因 |
|--------|-----|------|
| Session | `STATELESS` | JWT 无状态 |
| CSRF | 关闭 | 前后端分离 + Token |
| CORS | 启用 | React 开发端口 5173 |
| 密码编码 | `BCryptPasswordEncoder` | 模块 1 注册登录用 |

**URL 权限规则（当前）：**

| 路径 | 权限 |
|------|------|
| `/api/auth/**` | 公开 |
| `/api/admin/**` | `ROLE_ADMIN` |
| 其他 `/api/**` | 需登录 |
| `/error` | 公开 |

> 模块 2 实施时需追加：`.requestMatchers("/api/contests/**").permitAll()`

---

### 3.5 中间件配置

| 配置类 | 作用 | 业务使用情况 |
|--------|------|----------------|
| `MybatisConfig` | `@MapperScan("org.fjnu305.acm01.**.mapper")` | 模块 1 已用 |
| `RedisConfig` | `RedisTemplate<String, Object>` | 已配置，模块 2 起使用 |
| `CorsConfig` | 允许 `localhost:5173` | 前端联调 |

---

## 4. 配置文件

**`resources/application.yml`（模块 0 相关部分）：**

```yaml
server:
  port: 8080

spring:
  datasource: ...          # MySQL acm 库
  data.redis: ...          # Redis localhost:6379

jwt:
  expiration: 86400000     # 24 小时

mybatis:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

**`resources/jwt-secret.key`：** HMAC 签名密钥（已在 `.gitignore`，勿提交仓库）

---

## 5. Maven 依赖（模块 0 相关）

| 依赖 | 用途 |
|------|------|
| `spring-boot-starter-webmvc` | REST API |
| `spring-boot-starter-security` | 安全框架 |
| `spring-boot-starter-validation` | 参数校验（模块 0 新增） |
| `mybatis-spring-boot-starter` | ORM |
| `spring-boot-starter-data-redis` | Redis |
| `jjwt-api / impl / jackson` | JWT |
| `lombok` | 简化代码 |
| `mysql-connector-j` | MySQL 驱动 |

---

## 6. 公共枚举（跨模块预留）

| 枚举 | 路径 | 用途 |
|------|------|------|
| `UserRole` | `Common/enums/UserRole.java` | `USER` / `ADMIN`，对应 Spring `ROLE_` 前缀 |
| `ContestSource` | `Common/enums/ContestSource.java` | 模块 2 爬虫来源 |

---

## 7. 错误码（模块 0 定义 + 模块 1 扩展）

| 码 | 枚举 | 说明 | 定义模块 |
|----|------|------|----------|
| 200 | SUCCESS | 成功 | 0 |
| 400 | BAD_REQUEST | 参数错误 | 0 |
| 401 | UNAUTHORIZED | 未登录 | 0 |
| 403 | FORBIDDEN | 无权限 | 0 |
| 500 | INTERNAL_ERROR | 服务器错误 | 0 |
| 1001 | USER_NOT_FOUND | 用户不存在 | 1 |
| 1002 | TOKEN_INVALID | Token 无效 | 0 |
| 1003 | USER_ALREADY_EXISTS | 用户名已存在 | 1 |
| 1004 | PASSWORD_WRONG | 密码错误 | 1 |
| 1005 | USER_DISABLED | 账号禁用 | 1 |
| 1006 | ROLE_NOT_FOUND | 角色未配置 | 1 |

**扩展规范：** 模块 2 使用 2001 段，以此类推。

---

## 8. 对外提供的能力

后续所有业务模块可直接使用：

| 能力 | 使用方式 |
|------|----------|
| 统一返回 | `return Result.success(data)` |
| 业务异常 | `throw new BusinessException(ErrorCode.XXX)` |
| 当前用户 | `@AuthenticationPrincipal LoginUser user` |
| 管理员鉴权 | `@PreAuthorize("hasRole('ADMIN')")` |
| 分页 | `PageResult.of(list, total, pageNum, pageSize)` |
| MyBatis Mapper | 放在 `module.xxx.mapper` 包下 |
| Redis | 注入 `RedisTemplate<String, Object>` |

---

## 9. 完成标准（自检清单）

- [x] `mvn compile` 无错误
- [x] Spring Boot 能正常启动
- [x] 未登录访问受保护接口 → 401 JSON
- [x] 带合法 JWT → Controller 可拿到 `LoginUser`
- [x] 无权限访问 `/api/admin/**` → 403 JSON
- [x] 业务异常 / 参数错误 → 统一 `Result` 格式
- [x] CORS 放行前端 5173 端口

---

## 10. 已知待改进项

| 项 | 说明 | 建议迭代 |
|----|------|----------|
| JWT 角色来源 | Filter 当前从 Token 读 roles，非每次查库 | 安全加固时可改为 DB 查角色 |
| Redis 未使用 | 仅配置 Bean，无业务缓存 | 模块 2 起使用 |
| ES 依赖 | pom 有，未配置 | 模块 9 使用 |
| 包名大小写 | `Common` 大写非 Java 惯例 | 可逐步统一为小写 |

---

## 11. 面试话术

> 项目采用前后端分离 + JWT 无状态认证，搭建统一响应与全局异常治理；  
> Spring Security 配置公开 / 登录 / ADMIN 三级 URL 权限，401/403 均返回 JSON；  
> 预置 MyBatis、Redis 配置，后续业务模块即插即用。

---

*文档维护：模块 0 变更时同步更新。*
