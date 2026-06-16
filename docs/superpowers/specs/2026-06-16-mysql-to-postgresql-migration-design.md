# MySQL → PostgreSQL 迁移设计

**日期：** 2026-06-16  
**状态：** 已确认，待实施

## 背景与目标

博客（blog.hazenix.top）当前使用 MySQL，监控系统（Umami）使用 PostgreSQL 17。
为减少运维复杂度，将博客数据库从 MySQL 迁移到同一 PostgreSQL 17 实例，
MySQL 容器退场，PostgreSQL 同时服务博客和 Umami 两个 database。

## 整体架构变化

**迁移前：**
```
blog(Spring Boot) → mysql(:3306)         → db_hazeblog
umami             → umami-db(PG, 注释掉) → umami
```

**迁移后：**
```
blog(Spring Boot) → postgres(:5432) → db_hazeblog
umami             → postgres(:5432) → umami
MySQL 容器移除
```

## PostgreSQL 服务规划

| 项目 | 值 |
|------|-----|
| 镜像 | `postgres:17-alpine` |
| 容器名 | `postgres` |
| 内存限制 | 1.2g（接管原 MySQL 配额） |
| 数据卷 | `./postgres/data:/var/lib/postgresql/data` |
| 初始化脚本 | `./postgres/init/01-init.sh` |

**Database 和用户：**

| Database | 用户 | 说明 |
|----------|------|------|
| `db_hazeblog` | `root` | 博客主库，容器启动自动创建 |
| `umami` | `umami` | 监控库，初始化脚本创建 |

## 数据迁移方案

选用 **pgloader 直连 MySQL 自动迁移**（方案 A）。

### 步骤一：博客数据迁移（pgloader）

在宿主机执行，pgloader 作为一次性容器，连接仍在运行的 MySQL，写入 PostgreSQL：

```bash
docker run --rm --network blog \
  -v $(pwd)/migrate.load:/migrate.load \
  ghcr.io/dimitri/pgloader:latest \
  pgloader /migrate.load
```

`migrate.load` 配置文件（放在 `../migrate.load`，即 docker-compose.yml 同级）：

```
LOAD DATABASE
  FROM    mysql://root:Hazenixbzh66MySQLbulai@mysql:3306/db_hazeblog
  INTO    postgresql://root:Hazenixbzh66MySQLbulai@postgres:5432/db_hazeblog
WITH include drop, create tables, create indexes,
     reset sequences, foreign keys;
```

pgloader 自动处理：
- `AUTO_INCREMENT` → `SERIAL` / sequence
- MySQL 字符集声明 → 移除
- `ENGINE=InnoDB` → 移除
- 类型映射（`tinyint(1)` → `boolean` 等）

### 步骤二：Umami 数据恢复

```bash
docker exec -i postgres psql -U umami -d umami < ../umami_backup.sql
```

### 步骤三：验证后移除 MySQL

迁移验证通过后，从 docker-compose.yml 删除 mysql 服务及相关 depends_on。

## Docker Compose 改动

### 新增 postgres 服务

```yaml
postgres:
  image: postgres:17-alpine
  container_name: postgres
  mem_limit: 1.2g
  mem_reservation: 1g
  memswap_limit: 1.2g
  shm_size: 128m
  restart: always
  ports:
    - "5432:5432"
  environment:
    TZ: Asia/Shanghai
    POSTGRES_USER: root
    POSTGRES_PASSWORD: Hazenixbzh66MySQLbulai
    POSTGRES_DB: db_hazeblog
  volumes:
    - ./postgres/data:/var/lib/postgresql/data
    - ./postgres/init:/docker-entrypoint-initdb.d
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U root -d db_hazeblog"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 30s
  networks:
    - blog-net
```

### 初始化脚本（`./postgres/init/01-init.sh`）

```bash
#!/bin/bash
set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER umami WITH PASSWORD 'Hazenixbzh66MySQLbulai';
    CREATE DATABASE umami OWNER umami;
EOSQL
```

### 恢复 umami 服务

取消 umami 容器注释，`DATABASE_URL` 改为指向新的 postgres 服务：

```yaml
DATABASE_URL: postgresql://umami:Hazenixbzh66MySQLbulai@postgres:5432/umami
```

### blog 服务

`depends_on` 从 `mysql` 改为 `postgres`：

```yaml
depends_on:
  postgres:
    condition: service_healthy
  redis:
    condition: service_started
```

### 移除 mysql 服务

整块删除 mysql 服务定义（待迁移验证通过后执行）。

## Spring Boot 后端改动

### pom.xml（blog-server）

```xml
<!-- 移除 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- 添加 -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### application.yml — datasource URL

```yaml
spring:
  datasource:
    druid:
      driver-class-name: ${blog.datasource.driver-class-name}
      url: jdbc:postgresql://${blog.datasource.host}:${blog.datasource.port}/${blog.datasource.database}
      username: ${blog.datasource.username}
      password: ${blog.datasource.password}
```

MySQL 专属参数全部移除：`serverTimezone`、`useUnicode`、`characterEncoding`、
`zeroDateTimeBehavior`、`useSSL`、`allowPublicKeyRetrieval`、`autoReconnect`、
`rewriteBatchedStatements`。

### application-prod.yml

```yaml
datasource:
  driver-class-name: org.postgresql.Driver
  host: postgres
  port: 5432
  database: db_hazeblog
  username: root
  password: Hazenixbzh66MySQLbulai
```

### application-test.yml

```yaml
datasource:
  driver-class-name: org.postgresql.Driver
  host: localhost
  port: 5432
  database: db_hazeblog
  username: root
  password: Hazenixbzh66MySQLbulai
```

## SQL 兼容性

所有 Mapper XML 无需修改：

| MySQL 用法 | PostgreSQL 支持 | 结论 |
|-----------|----------------|------|
| `concat('%', #{x}, '%')` | ✅ 相同 | 不改 |
| `NOW()` | ✅ 相同 | 不改 |
| `LIMIT #{n}` | ✅ 相同 | 不改 |
| `COALESCE()` | ✅ 相同 | 不改 |
| `useGeneratedKeys="true"` | ✅ MyBatis 自动用 `RETURNING id` | 不改 |
| PageHelper 分页 | ✅ 内置 PostgreSQL 方言 | 不改 |

## 实施顺序

1. 准备 PostgreSQL 目录结构和初始化脚本
2. 修改 docker-compose.yml（添加 postgres，恢复 umami，暂不删 mysql）
3. 启动 postgres，验证两个 database 创建成功
4. 运行 pgloader 迁移博客数据
5. 恢复 Umami 数据
6. 启动 umami，验证监控系统正常
7. 修改 Spring Boot 后端（pom.xml + 三个 yml）
8. 重新打包部署 blog 容器
9. 端对端验证博客功能正常
10. 从 docker-compose.yml 删除 mysql 服务，停止 MySQL 容器

## 风险与注意事项

- pgloader 对 MySQL `ENUM` 类型会转为 PostgreSQL `TEXT` + CHECK 约束，需验证业务逻辑无影响
- pgloader 迁移时 MySQL 必须保持在线（blog 容器可以先停掉，MySQL 继续跑）
- `umami_backup.sql` 是 PG17 的 dump，必须用 PG17 容器恢复，不能降级到 15
- 迁移完成前保留 MySQL 数据卷（`./mysql/data`），确认无误后再清理
