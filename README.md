# ACMer / Acm01

面向个人备赛的多源 OJ 赛事数据中台：聚合多平台赛程与 Rating，提供赛前提醒、竞赛社区（讨论 / 题解 / 组队），并将赛后笔记沉淀为可检索的 Wiki 知识库。

仓库：[github.com/fjnu305/Acm01](https://github.com/fjnu305/Acm01)

## 功能亮点

- **延迟通知队列**：开赛前 N 分钟提醒建模为可持久化延迟任务；多线程领取用行锁互斥，领取打时间戳，崩溃后 120s 自动回收，保证提醒不丢不重。推送优先 WebSocket，邮件兜底；同用户多赛事合并并限流。
- **赛事聚合**：策略模式收口各 OJ 抓取逻辑，新增平台只需实现适配器；每平台独立限流与失败隔离；入库前内容哈希判重，仅变更触发提醒重排；每日同步 Codeforces Rating，账号 Cookie 加密存储。
- **检索与缓存**：题解可接入 Elasticsearch 全文检索（不可用时降级 MySQL）；赛程列表走 Redis，版本号控制失效，目录变更即整体失效。
- **AI 知识库**：FastAPI 独立进程提供笔记召回 + LLM 问答（BYOK）；三档预算控制召回量与延迟，回答强制引用原文；前端用力导向图可视化笔记链接。

支持的赛事来源包括 Codeforces、AtCoder、牛客、洛谷、CCPC、ICPC、蓝桥杯等。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 前端 | React 19 · TypeScript · Vite · STOMP/WebSocket |
| 后端 | Spring Boot 4 · MyBatis · Spring Security (JWT) · Quartz |
| 存储 / 中间件 | MySQL · Redis ·（可选）Elasticsearch ·（可选）RabbitMQ |
| Wiki AI | FastAPI · OpenAI 兼容 API · 本地 Markdown vault |

## 仓库结构

```text
acm01/
├── src/                 # Spring Boot 后端（端口 8080）
├── acm01-web/           # React 前端（开发端口 5173）
├── wiki-ai/             # Wiki AI sidecar（端口 8787）
├── docs/                # 测试与说明文档
└── scripts/             # 辅助脚本
```

前端开发代理：`/api`、`/uploads`、`/ws` → `8080`；`/wiki-api` → `8787`。

## 本地运行

### 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 20+
- MySQL 8+
- Redis
-（可选）Python 3.11+（Wiki AI）
-（可选）Elasticsearch 8+（全文检索）

### 1. 数据库

创建库（默认名 `acm`），按需执行 `src/main/resources/db/` 下的初始化 SQL（用户 / 赛事 / 订阅 / 社区 / 题解 / 组队 / 好友收件箱 / OJ 同步等）。Quartz 表可由应用按 `application.yml` 自动初始化。

### 2. 后端

```bash
# 可选：复制本地私密配置（邮件、密钥等），勿提交
cp src/main/resources/application-local.yml.example \
   src/main/resources/application-local.yml

# 默认连接 localhost MySQL / Redis，见 application.yml
mvn spring-boot:run
```

常用环境变量：

| 变量 | 说明 |
| --- | --- |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | 数据库连接 |
| `JASYPT_ENCRYPTOR_PASSWORD` | 解密邮件等密文配置 |
| `NOTIFY_EMAIL_ENABLED` | 是否启用邮件提醒 |
| `NOTIFY_MQ_ENABLED` | 是否用 RabbitMQ 投递通知（默认进程内） |
| `SYNC_CREDENTIAL_KEY_BASE64` | OJ Cookie 加密密钥（32 字节 Base64） |

健康检查：`GET http://localhost:8080/actuator/health`

### 3. 前端

```bash
cd acm01-web
npm install
npm run dev
```

浏览器打开 [http://localhost:5173](http://localhost:5173)。

### 4. Wiki AI（可选）

```bash
cd wiki-ai
python -m venv .venv
# Windows: .venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env   # 配置 VAULT_ROOT、CREDENTIAL_KEY_BASE64 等
uvicorn app.main:app --host 127.0.0.1 --port 8787 --reload
```

API 前缀：`http://127.0.0.1:8787/wiki-api`。更多说明见 [`wiki-ai/README.md`](wiki-ai/README.md)。

## 设计要点（简）

- **通知**：扫描任务写入可持久化表 → 领取（行锁）→ WebSocket / 邮件投递；处理超时自动回收，避免卡死丢单。
- **爬虫**：统一 HTTP 客户端与调度；平台适配器隔离失败；内容哈希减噪，变更才重排订阅提醒。
- **实时推送**：WebSocket Session 管理；多实例时用 Redis 做 presence / 推送桥接。
- **内容可见性**：题解 / 帖子 / 组队等统一访问策略，列表与搜索按可见性过滤。

## 测试

```bash
mvn test
```

更多说明见 [`docs/testing/README.md`](docs/testing/README.md)。

## 安全说明

以下内容**不要**提交到仓库：

- `application-local.yml`、`.env`、JWT 私钥、数据库密码、邮箱授权码
- `node_modules/`、`target/`、`.venv/`、本地 `data/` 上传目录

模板文件：`application-local.yml.example`、`wiki-ai/.env.example`。

## License

个人作品 / 学习项目，未另声明许可证时保留所有权利。
