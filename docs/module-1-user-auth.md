# 模块 1：用户与权限体系 — 实施文档

> 注册 / 登录、RBAC 角色权限、分角色主页、管理后台基础  
> 文档版本：v2.0 | 状态：**管理后台与用户 CRUD 已完成**

---

## 1. 模块目标

| 目标 | 说明 |
|------|------|
| 用户注册 / 登录 | BCrypt 密码、JWT 签发 |
| RBAC 权限 | 基于 `user` + `role` + `user_role` 的角色体系 |
| 个人资料 | 用户信息查询（`/api/user/me`） |
| 管理员能力 | 管理面板 API，仅 ADMIN 可访问 |
| 前后端分离 | React 登录 / 注册 / 分角色主页 |

**v2.0 已完成：**

- `GET /api/admin/users` 用户分页搜索
- `PUT /api/admin/users/{id}/status` 禁用/启用
- `PUT /api/admin/users/{id}/roles` 角色分配
- 仪表盘今日注册数真实统计
- JWT Filter 每次请求从 DB 刷新角色，防 Token 提权
- 前端 `AdminUsersPage`

**依赖：** 模块 0（Result、Security、MyBatis、BCrypt）

---

## 2. 包结构（已实现）

```text
org.fjnu305.acm01.module.user/
├── controller/
│   ├── AuthController.java            # POST /api/auth/register、/login
│   ├── UserController.java            # GET  /api/user/me
│   └── AdminController.java           # GET  /api/admin/dashboard
├── service/
│   ├── AuthService.java               # 注册、登录核心逻辑
│   ├── UserService.java               # 用户信息组装
│   └── AdminService.java              # 管理面板数据
├── mapper/
│   ├── UserMapper.java
│   └── RoleMapper.java
├── entity/
│   ├── UserEntity.java
│   └── RoleEntity.java
└── dto/
    ├── RegisterRequest.java
    ├── LoginRequest.java
    ├── AuthResponse.java              # { token, user }
    └── UserInfoVO.java                # 返回给前端的用户信息
```

**MyBatis XML：**

```text
resources/mapper/
├── UserMapper.xml
└── RoleMapper.xml
```

**SQL 脚本：**

```text
resources/db/init-rbac.sql             # 建表 role/permission/user_role/role_permission + 角色数据
resources/db/init-roles.sql            # 仅补 USER / ADMIN 角色（已有表时用）
```

---

## 3. 数据库设计

### 3.1 表关系

```text
user ──< user_role >── role
                         │
                    role_permission（表已建，代码未用）
                         │
                    permission（表已建，代码未用）
```

### 3.2 `user` — 用户表（已实现）

用户基础信息 + ACM 竞技字段合并在一张表（未单独拆 `user_profile`）。  
建表脚本：`src/main/resources/db/init-user.sql`

| 字段分组 | 主要字段 |
|----------|----------|
| 账号 | `username`(唯一)、`password`(BCrypt)、`status`、`deleted` |
| 资料 | `nickname`、`email`、`avatar`、`gender`(默认0)、`school`、`bio` |
| OJ 账号 | `cf_handle`、`atcoder_handle`、`nowcoder_handle`、`luogu_handle`（VARCHAR(50)） |
| 竞技数据 | `cf_rating`、`solved_count`、`ac_count`、`contest_count` |
| 时间 | `last_login_time`、`created_time`、`updated_time` |

**索引：** `username`(UNIQUE)、`idx_cf_rating`、`idx_solved_count`、`idx_status`

### 3.3 `role` — 角色表

> 建表脚本：`src/main/resources/db/init-rbac.sql`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键，自增 |
| `role_code` | VARCHAR(50) | 唯一，如 `USER`、`ADMIN` |
| `role_name` | VARCHAR(50) | 显示名 |
| `status` | TINYINT | 默认 1；1 启用，0 禁用 |
| `created_time` | DATETIME | 创建时间 |

**索引：** `role_code`(UNIQUE)

### 3.4 `user_role` — 用户角色关联

| 字段 | 说明 |
|------|------|
| `user_id` + `role_id` | 联合主键 |

### 3.5 `permission` / `role_permission`（表已建，代码未用）

| 表 | 主要字段 |
|----|----------|
| `permission` | `perm_code`(VARCHAR(100) UNIQUE)、`perm_name`、`created_time` |
| `role_permission` | `role_id` + `permission_id` 联合主键 |

线库 `permission` 表已有权限种子数据（`AUTO_INCREMENT` 可从 63 起）；应用层鉴权当前仍基于 `role` + `user_role`，未读取 `role_permission`。

### 3.6 初始化 SQL

```sql
-- 新库：init-rbac.sql（建表 + 角色数据）
-- 已有表：init-roles.sql（仅补角色数据）
INSERT IGNORE INTO role (role_code, role_name, status) VALUES
('USER', '普通用户', 1),
('ADMIN', '管理员', 1);
```

**设置管理员（手动执行）：**

```sql
INSERT INTO user_role (user_id, role_id)
SELECT <用户ID>, id FROM role WHERE role_code = 'ADMIN';
```

---

## 4. 核心业务流程

### 4.1 注册

```text
POST /api/auth/register  { username, password, nickname?, email? }
  ↓
AuthController（@Valid 校验）
  ↓
AuthService.register()
  ① countByUsername → 已存在则 1003
  ② 查 role 表 USER → 不存在则 1006
  ③ BCrypt 加密 password
  ④ INSERT user（status=1, deleted=0）
  ⑤ INSERT user_role（绑定 USER）
  ⑥ 查用户角色列表
  ⑦ JwtTokenProvider.createToken(userId, username, roles)
  ⑧ 组装 UserInfoVO
  ↓
返回 { token, user }
```

### 4.2 登录

```text
POST /api/auth/login  { username, password }
  ↓
AuthService.login()
  ① selectByUsername → 不存在则 1001
  ② status=0 → 1005 账号禁用
  ③ BCrypt.matches → 失败则 1004
  ④ updateLastLoginTime
  ⑤ 查 user_role → 空则 1006
  ⑥ 签发 JWT
  ↓
返回 { token, user }
```

### 4.3 获取当前用户

```text
GET /api/user/me
Header: Authorization: Bearer <token>
  ↓
JwtAuthenticationFilter → LoginUser
  ↓
UserController.me(@AuthenticationPrincipal LoginUser)
  ↓
UserService.getUserInfo(userId) → UserInfoVO
```

### 4.4 管理面板

```text
GET /api/admin/dashboard
  ↓
SecurityConfig：/api/admin/** 需 ROLE_ADMIN
AdminController：@PreAuthorize("hasRole('ADMIN')")
  ↓
AdminService.getDashboardOverview()
  → totalUsers（真实统计）
  → pendingReviews / todayRegistrations（占位 0）
```

---

## 5. API 设计

### 5.1 认证接口（公开）

| 方法 | 路径 | 请求体 | 响应 |
|------|------|--------|------|
| POST | `/api/auth/register` | `RegisterRequest` | `Result<AuthResponse>` |
| POST | `/api/auth/login` | `LoginRequest` | `Result<AuthResponse>` |

**RegisterRequest 校验：**

| 字段 | 规则 |
|------|------|
| username | 非空，3~50 字符 |
| password | 非空，6~32 字符 |
| nickname | 可选，最多 50 |
| email | 可选，最多 100 |

**AuthResponse 结构：**

```json
{
  "token": "eyJ...",
  "user": {
    "userId": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "roles": ["USER"],
    "cfRating": 0,
    "solvedCount": 0
  }
}
```

### 5.2 用户接口（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/me` | 当前用户完整信息 |

### 5.3 管理接口（需 ADMIN）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard` | 管理面板概览 |

**Postman 测试权限：**

| 场景 | 期望 |
|------|------|
| 无 Token 访问 `/api/admin/dashboard` | 401 |
| 普通 USER Token | 403 |
| ADMIN Token | 200 |

---

## 6. 权限模型（RBAC）

```text
                    ┌─────────────┐
                    │   用户请求   │
                    └──────┬──────┘
                           ↓
              ┌────────────────────────┐
              │  JwtAuthenticationFilter │
              │  Token → LoginUser       │
              │  roles → ROLE_USER/ADMIN │
              └────────────┬───────────┘
                           ↓
              ┌────────────────────────┐
              │     SecurityConfig        │
              │  URL 级：/api/admin/**    │
              └────────────┬───────────┘
                           ↓
              ┌────────────────────────┐
              │  @PreAuthorize（方法级）  │
              └────────────────────────┘
```

| 角色 | role_code | Spring Authority | 能力 |
|------|-----------|------------------|------|
| 普通用户 | USER | ROLE_USER | 登录、查看个人资料 |
| 管理员 | ADMIN | ROLE_ADMIN | 上述 + `/api/admin/**` |

**为什么注册默认 USER：** 防止用户自选 ADMIN；管理员由 DB 手动分配。

---

## 7. 各层职责

| 层 | 类 | 职责 |
|----|-----|------|
| Controller | `AuthController` | 接收入参，调 Service，返回 Result |
| Controller | `UserController` | 从 SecurityContext 取 userId |
| Controller | `AdminController` | 管理 API，双重 ADMIN 校验 |
| Service | `AuthService` | 注册 / 登录事务逻辑 |
| Service | `UserService` | Entity → UserInfoVO |
| Service | `AdminService` | 管理统计数据 |
| Mapper | `UserMapper` | user 表 CRUD |
| Mapper | `RoleMapper` | role / user_role 查询与插入 |

---

## 8. 前端（acm01-web）

### 8.1 结构

```text
acm01-web/src/
├── App.tsx                 # 登录态 + 角色路由
├── api/auth.ts             # login / register / me / admin API
└── pages/
    ├── LoginPage.tsx       # 登录
    ├── RegisterPage.tsx    # 注册
    ├── UserHomePage.tsx    # 普通用户主页
    └── AdminHomePage.tsx   # 管理员主页
```

### 8.2 前端逻辑

```text
启动 → localStorage 有 Token？
  ├─ 有 → GET /api/user/me 恢复登录
  └─ 无 → 显示登录 / 注册

已登录 → roles 含 ADMIN？
  ├─ 是 → AdminHomePage（调 /api/admin/dashboard）
  └─ 否 → UserHomePage（选手主页 + Token 展示）
```

### 8.3 开发联调

| 服务 | 地址 | 启动命令 |
|------|------|----------|
| 后端 | http://localhost:8080 | `cd acm01 && mvn spring-boot:run` |
| 前端 | http://localhost:5173 | `cd acm01-web && npm run dev` |

> 注意：`spring-boot:run` 中间是**冒号**，不是空格。

---

## 9. 与模块 0 的衔接

| 模块 0 能力 | 模块 1 使用方式 |
|-------------|----------------|
| `Result` | 所有 Controller 返回 |
| `BusinessException` | AuthService 抛业务错误 |
| `ErrorCode` 1001~1006 | 用户相关错误 |
| `SecurityConfig` | 追加 `/api/admin/**` 规则 |
| `PasswordEncoder` | 注册加密、登录比对 |
| `JwtTokenProvider` | 登录成功后签发 Token |
| `LoginUser` | UserController 注入当前用户 |
| `MybatisConfig` | 扫描 UserMapper / RoleMapper |

---

## 10. 对外提供（供后续模块）

| 提供项 | 说明 | 使用模块 |
|--------|------|----------|
| 用户 ID | JWT / LoginUser | 全部业务模块 |
| 角色鉴权 | ADMIN 管理接口 | 模块 2 爬虫管理、模块 5/8 审核 |
| UserInfoVO | 用户资料结构 | 模块 6 社交、模块 10 OJ 同步 |
| user 表竞技字段 | cf_rating 等 | 模块 7 组队匹配 |

---

## 11. 完成标准（自检清单）

- [x] 注册：用户名唯一、密码 BCrypt 存储
- [x] 登录：密码校验、禁用账号拦截
- [x] 注册自动绑定 USER 角色
- [x] JWT 返回 + `/api/user/me` 可用
- [x] ADMIN 才能访问 `/api/admin/dashboard`
- [x] 前端登录 / 注册 / 分角色主页
- [x] Postman 无 Token → 401；USER → 403；ADMIN → 200
- [ ] permission 细粒度权限（未做）
- [ ] 管理员用户管理界面（未做）
- [ ] 忘记密码（未做）

---

## 12. 已知待改进项

| 项 | 说明 |
|----|------|
| JWT roles 信任 Token | Filter 从 JWT 读角色，撤销角色后旧 Token 仍有效至过期 |
| permission 表未用 | 线库已建 `permission`/`role_permission` 并有种子数据；应用层仍仅角色级 RBAC |
| user 与 profile 未拆表 | 单表存储，后期数据量大可考虑拆分 |
| AdminService 部分占位 | `pendingReviews` 等硬编码 0 |

---

## 13. 后续迭代建议

| 迭代 | 内容 |
|------|------|
| 1.1 | Filter 改为 DB 查角色，防 Token 提权 |
| 1.2 | 管理员：用户列表、禁用、分配角色 API |
| 1.3 | 个人资料修改 API（头像、学校、bio） |
| 1.4 | 接入 `permission` 表，细粒度鉴权 |

---

## 14. 面试话术

> 实现基于 RBAC 的用户体系：`user` + `role` + `user_role` 多对多关联，注册默认 USER，管理员 DB 分配；  
> 密码 BCrypt 加密，登录签发 JWT，Spring Security 配置 URL 级 + 方法级双重 ADMIN 校验；  
> 前后端分离，React 按 roles 渲染选手主页 / 管理后台，API 层确保非 ADMIN 无法访问管理接口。

---

*文档维护：模块 1 变更时同步更新。*
