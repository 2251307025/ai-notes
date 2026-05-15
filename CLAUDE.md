# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build the project (skip tests for speed during development)
mvn clean package -DskipTests

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=JwtTest

# Run the application
mvn spring-boot:run

# Start infrastructure (PostgreSQL/ParadeDB, Redis, MinIO)
docker compose -f dockerFlie/docker-compose.yml up -d
```

## Project Overview

**ai-notes** is a Spring Boot 3.3 + Java 21 backend for an AI-powered notes app. Users write and organize notes (articles), and interact with them via a DeepSeek-powered AI chat. ParadeDB (PostgreSQL with pg_search + pgvector) provides full-text search and vector search capabilities.

## Architecture

**Standard layered architecture**: Controller -> Service -> Mapper (MyBatis) -> PostgreSQL

### Key Packages

| Package | Purpose |
|---------|---------|
| `controller/` | REST endpoints for articles, categories, users, chat (SSE), file upload |
| `service/impl/` | Business logic with interface/impl pattern |
| `mapper/` | MyBatis data access interfaces + XML mappings |
| `tool/` | Spring AI `@Tool` classes exposed as LLM function calls |
| `config/` | Bean config (AI chat, interceptors) |
| `interceptors/` | JWT + Redis authentication interceptor |
| `pojo/` | Entities, DTOs (PageBean, Result, CategoryStats) |
| `utils/` | JWT, MD5, MinIO, ThreadLocal utilities |

### AI Integration

- **Chat endpoint**: `POST /chat` returns Server-Sent Events (SSE) stream from DeepSeek
- **Tool functions**: `ArticleTool`, `CategoryTool`, `UserTool` are `@Component` + `@Tool` beans that the LLM can call to perform CRUD operations
- **Chat memory**: JDBC-backed chat history persistence (auto-initialized)
- **Thread safety**: `TtlToolCallbackWrapper` solves ThreadLocal context loss when Spring AI switches from Tomcat to WebFlux scheduler threads
- **System prompt**: Configured in `ChatConfiguration.java` — instructs the model to answer in Chinese, cite note content, and avoid describing its thought process

### Database (PostgreSQL via ParadeDB)

Tables: `user`, `category`, `article`

Full-text search uses ParadeDB's `pg_search` extension with `&@` operator syntax (see `ArticleMapper.xml`).

### Authentication

- JWT stored in Redis for session management (blacklist on logout)
- `LoginInterceptor` protects all endpoints except `/user/login` and `/user/register`
- Passwords hashed with MD5

### Pagination

Two strategies in `ArticleController`:
- **Offset pagination**: PageHelper (`pageNum`/`pageSize`) — for traditional lists
- **Cursor pagination**: `lastId`/`pageSize` — for infinite scroll (uses `WHERE id > #{lastId}`)

### Infrastructure (Docker Compose)

| Service | Image | Port |
|---------|-------|------|
| postgres | paradedb/paradedb:latest | 5432 |
| redis | redis:6.2 | 6379 |
| minio | minio/minio:latest | 9000 (API), 9001 (Console) |

MinIO bucket `ai-notes` needs anonymous download access configured after startup (see `说明.md`).

## Important Configuration

- **DeepSeek API key**: Set in `application.yml` under `spring.ai.deepseek.api-key` (should be externalized to env variable for production)
- **DB credentials**: PostgreSQL `root/1234`, Redis `1234`, MinIO `minioadmin/minioadmin` — all for local dev only
- **Spring AI version**: 1.0.4 (needs `repo.spring.io/milestone` repository in pom.xml)
