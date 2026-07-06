# ai-notes

一个基于 Spring Boot 3 + Java 21 的 AI 驱动笔记管理后端系统。用户可以通过 AI 对话管理笔记、分类，支持全文检索、向量检索和 AI 图片生成。

## 功能特性

### 用户管理
- 用户注册 / 登录（JWT 认证）
- 个人信息修改、头像更换、密码修改

### 笔记管理
- 笔记的增删改查（标题、内容、封面图、分类、状态）
- 支持草稿 / 已发布两种状态
- 双模式分页：偏移量分页（传统列表）+ 游标分页（无限滚动）

### 分类管理
- 分类的增删改查
- 分类统计（每个分类下的笔记数量）

### AI 对话（SSE 流式）
- 基于 DeepSeek 大模型的流式对话
- AI 可调用工具函数直接操作笔记和分类（自动 CRUD）
- JDBC 持久化的对话记忆（每用户保留最近 10 条消息）
- 支持 AI 图片生成（SiliconFlow Z-Image-Turbo）

### 搜索能力
- ParadeDB BM25 全文检索
- pgvector 向量余弦相似度检索（1024 维）

### 文件上传
- MinIO 对象存储，支持图片等文件上传

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.3.0 |
| ORM | MyBatis | 3.0.3 |
| 数据库 | ParadeDB（PostgreSQL） | pg17 |
| 缓存 / 会话 | Redis | 6.2 |
| 对象存储 | MinIO | latest |
| AI 框架 | Spring AI | 1.0.4 |
| 大模型 | DeepSeek（对话）、SiliconFlow（向量 + 图片） | - |
| 认证 | JWT（jjwt） | 0.12.6 |
| 分页 | PageHelper | 1.4.6 |
| 线程上下文 | TransmittableThreadLocal（Alibaba TTL） | 2.14.4 |
| 构建工具 | Maven | - |

## 项目结构

```
ai-notes/
├── dockerFlie/
│   └── docker-compose.yml    # 基础设施编排
├── sql/
│   ├── init.sql              # 数据库表结构
│   └── rag.sql               # ParadeDB 扩展与索引
├── src/main/
│   ├── java/com/chj/
│   │   ├── config/           # 配置类（Chat、Web、拦截器）
│   │   ├── controller/       # REST 控制器
│   │   ├── service/impl/     # 业务逻辑实现
│   │   ├── mapper/           # MyBatis 数据访问
│   │   ├── tool/             # Spring AI @Tool 函数
│   │   ├── interceptors/     # JWT 认证拦截器
│   │   ├── pojo/             # 实体类与 DTO
│   │   └── utils/            # 工具类（JWT、MD5、MinIO 等）
│   └── resources/
│       ├── application.yml   # 主配置
│       └── prompt/           # AI 提示词
└── pom.xml
```

## 部署要求

### 环境依赖

- JDK 21+
- Maven 3.8+
- Docker & Docker Compose

### 基础设施（Docker Compose）

| 服务 | 镜像 | 端口 | 凭据 |
|------|------|------|------|
| ParadeDB | `paradedb/paradedb:latest-pg17` | 5432 | root / 1234 |
| Redis | `redis:6.2` | 6379 | 密码: 1234 |
| MinIO | `minio/minio:latest` | 9000（API）, 9001（控制台） | minioadmin / minioadmin |

### 快速启动

#### 1. 启动基础设施

```bash
docker compose -f dockerFlie/docker-compose.yml up -d
```

#### 2. 初始化数据库

连接 PostgreSQL，依次执行：

```bash
# 创建表结构
psql -h localhost -U root -d ai_notes -f sql/init.sql

# 安装 ParadeDB 扩展与索引
psql -h localhost -U root -d ai_notes -f sql/rag.sql
```

#### 3. 配置 MinIO 桶匿名读取

```bash
docker exec -it minio-server sh
mc alias set local http://localhost:9000 minioadmin minioadmin
mc anonymous set download local/ai-notes
```

#### 4. 配置 API Key

在 `application.yml` 或环境变量中配置：

```yaml
apikey:
  deepseek: your-deepseek-api-key      # DeepSeek 对话 API Key
  SiliconFlow: your-siliconflow-api-key # SiliconFlow 向量 / 图片 API Key
```

#### 5. 构建并运行

```bash
# 构建
mvn clean package -DskipTests

# 运行
mvn spring-boot:run
```

## API 概览

### 用户模块 `/user`

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/user/register` | 注册 | 否 |
| POST | `/user/login` | 登录（返回 JWT） | 否 |
| GET | `/user/userInfo` | 获取当前用户信息 | 是 |
| PUT | `/user/update` | 更新用户信息 | 是 |
| PATCH | `/user/updateAvatar` | 更新头像 | 是 |
| PATCH | `/user/updatePwd` | 修改密码 | 是 |

### 笔记模块 `/article`

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/article` | 创建笔记 | 是 |
| GET | `/article` | 列表（支持偏移/游标分页 + 搜索） | 是 |
| GET | `/article/public` | 公开笔记列表 | 否 |
| GET | `/article/detail` | 笔记详情 | 是 |
| PUT | `/article` | 更新笔记 | 是 |
| DELETE | `/article` | 删除笔记 | 是 |

### 分类模块 `/category`

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/category` | 创建分类 | 是 |
| GET | `/category` | 分类列表 | 是 |
| GET | `/category/detail` | 分类详情 | 是 |
| PUT | `/category` | 更新分类 | 是 |
| DELETE | `/category` | 删除分类 | 是 |

### AI 对话模块 `/chat`

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/chat` | SSE 流式对话 | 是 |
| DELETE | `/chat` | 清除对话记忆 | 是 |
| POST | `/chat/image` | AI 图片生成 | 是 |

### 文件上传

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/upload` | 文件上传至 MinIO | 是 |

## AI 工具函数

AI 对话过程中，大模型可自动调用以下工具：

**笔记工具（ArticleTool）**
- 获取笔记总数
- 全文检索笔记
- 新增 / 修改 / 删除笔记
- 按分类查询笔记
- 获取分类统计

**分类工具（CategoryTool）**
- 获取分类列表
- 新增 / 修改 / 删除分类
- 生成分类英文别名建议

**用户工具（UserTool）**
- 获取当前登录用户信息

## 数据库设计

| 表 | 说明 |
|----|------|
| `user` | 用户表（用户名、密码、昵称、头像等） |
| `category` | 分类表（名称、别名、创建人） |
| `article` | 笔记表（标题、内容、封面、状态、分类、向量嵌入） |

**索引：**
- BM25 全文索引（`idx_article_search`）— ParadeDB pg_search
- HNSW 向量索引 — pgvector 余弦相似度

## 架构要点

- **认证流程**：JWT Token 存储在客户端 Authorization Header 中，Redis 用于会话校验与登出黑名单
- **线程安全**：`TtlToolCallbackWrapper` 使用 TransmittableThreadLocal 解决 Spring AI 从 Tomcat 切换到 WebReactive 调度线程后 ThreadLocal 上下文丢失的问题
- **对话记忆**：基于 JDBC 的滑动窗口记忆，每用户保留最近 10 条消息
- **系统提示词**：配置为始终以中文回答，引用笔记标题作为来源，避免描述思考过程
