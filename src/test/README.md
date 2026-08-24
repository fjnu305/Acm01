# 测试目录说明

本目录与 `src/test/java` 下的包结构对应，按 **职责** 拆分测试。

## 目录结构

```text
src/test/java/org/fjnu305/acm01/
├── Acm01ApplicationTests.java     # 冒烟
├── support/                       # 测试基础设施、JWT Fixture、Security 测试配置
├── security/                      # 鉴权：全链路过滤器 + JWT 过滤器单测 + @PreAuthorize
├── websocket/                     # STOMP 握手鉴权
├── boundary/                      # 分页与参数边界
└── module/                        # 各业务模块
    ├── contest/crawl/             # 爬虫解析
    ├── subscription/              # 订阅越权
    ├── solution/                  # 题解越权
    ├── social/                    # 动态越权
    └── team/                      # 组队邀请越权
```

## 运行方式

```bash
# 全部（默认跳过 @Tag("integration") 外网爬虫）
mvn test

# 仅鉴权
mvn test -Dtest="org.fjnu305.acm01.security.**"

# 仅 WebSocket
mvn test -Dtest="org.fjnu305.acm01.websocket.**"

# 集成测试（外网爬虫预览）
mvn test -Dgroups=integration
```

## 约定

| 目录 | 测什么 |
|------|--------|
| `security/AnonymousEndpointSecurityTest` | 真实 `SecurityFilterChain`：匿名 401 / 公开 200 |
| `security/JwtAuthenticationFilterTest` | JWT 过滤器：禁用账号、角色重载、无效 Token |
| `security/AdminApiSecurityTest` | `@PreAuthorize` 垂直越权 |
| `module/*/*AccessTest` | Service 层水平越权 |
| `websocket/` | STOMP CONNECT 鉴权 |

完整矩阵见 [docs/testing/README.md](../docs/testing/README.md)。
