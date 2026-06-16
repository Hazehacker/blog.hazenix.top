# MySQL → PostgreSQL 迁移实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将博客后端从 MySQL 迁移到 PostgreSQL 17，同时恢复 Umami 监控系统，两个服务共用同一个 PostgreSQL 实例。

**Architecture:** 新增单一 `postgres:17-alpine` 容器，通过初始化脚本在启动时自动创建 `db_hazeblog` 和 `umami` 两个 database；pgloader 作为一次性容器直连 MySQL 搬运博客数据；Spring Boot 后端只改驱动和连接 URL，Mapper XML 无需修改。

**Tech Stack:** PostgreSQL 17-alpine, pgloader, Spring Boot + MyBatis + Druid + PageHelper, Docker Compose

---

## 文件变更总览

| 文件 | 操作 | 说明 |
|------|------|------|
| `../docker-compose.yml` | 修改 | 添加 postgres，恢复 umami，删 mysql（最后一步） |
| `../postgres/init/01-init.sh` | 新建 | 创建 umami 用户和 database |
| `../migrate.load` | 新建 | pgloader 迁移配置文件（迁移完可删） |
| `backend/blog-server/pom.xml` | 修改 | 换 PostgreSQL JDBC 驱动 |
| `backend/blog-server/src/main/resources/application.yml` | 修改 | 换 JDBC URL 格式 |
| `backend/blog-server/src/main/resources/application-prod.yml` | 修改 | 换驱动、host、port |
| `backend/blog-server/src/main/resources/application-test.yml` | 修改 | 换驱动、port |

> 所有路径中 `..` 指 `/Users/Admin/Documents/InternProject/myproject/`，即 docker-compose.yml 所在目录。
> 博客项目根目录：`/Users/Admin/Documents/InternProject/myproject/blog.hazenix.top/`

---

## Task 1：创建 PostgreSQL 初始化脚本

**Files:**
- Create: `../postgres/init/01-init.sh`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p /Users/Admin/Documents/InternProject/myproject/postgres/init
```

- [ ] **Step 2: 写初始化脚本**

新建 `/Users/Admin/Documents/InternProject/myproject/postgres/init/01-init.sh`，内容：

```bash
#!/bin/bash
set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER umami WITH PASSWORD 'Hazenixbzh66MySQLbulai';
    CREATE DATABASE umami OWNER umami;
EOSQL
```

- [ ] **Step 3: 设置执行权限**

```bash
chmod +x /Users/Admin/Documents/InternProject/myproject/postgres/init/01-init.sh
```

---

## Task 2：修改 docker-compose.yml（添加 postgres + 恢复 umami，暂保留 mysql）

**Files:**
- Modify: `../docker-compose.yml`

> 目标：让 postgres 和 umami 能启动，mysql 暂时保留供 pgloader 使用。

- [ ] **Step 1: 在 docker-compose.yml 的 services 顶部添加 postgres 服务**

在 `mysql:` 服务前插入（文件第 57 行附近），内容：

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

- [ ] **Step 2: 取消 umami 服务注释，并修改 DATABASE_URL 和 depends_on**

将文件顶部注释掉的 umami 服务块取消注释，然后将其中两处修改：

`DATABASE_URL` 改为：
```yaml
DATABASE_URL: postgresql://umami:Hazenixbzh66MySQLbulai@postgres:5432/umami
```

`depends_on` 改为（原来依赖 `umami-db`，现改为 `postgres`）：
```yaml
    depends_on:
      postgres:
        condition: service_healthy
```

- [ ] **Step 3: 取消 umami-db 服务注释的部分不处理（保持注释状态）**

旧的 `umami-db` 服务块继续保持注释，不要取消注释。

- [ ] **Step 4: 修改 blog 服务的 depends_on**

将 blog 服务的 `depends_on` 从 `mysql` 改为 `postgres`：

```yaml
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_started
```

---

## Task 3：启动 PostgreSQL，验证两个 database 创建成功

> 此步骤在服务器上（或本地有 Docker 环境）执行。

- [ ] **Step 1: 启动 postgres 容器**

```bash
cd /Users/Admin/Documents/InternProject/myproject/
docker compose up -d postgres
```

- [ ] **Step 2: 等待健康检查通过**

```bash
docker compose ps postgres
```

预期输出中 `STATUS` 列显示 `healthy`。

- [ ] **Step 3: 验证两个 database 已创建**

```bash
docker exec postgres psql -U root -c "\l"
```

预期输出中同时出现 `db_hazeblog` 和 `umami` 两行。

- [ ] **Step 4: 验证 umami 用户权限**

```bash
docker exec postgres psql -U root -c "\du"
```

预期输出中出现 `umami` 用户。

---

## Task 4：运行 pgloader 迁移博客数据

- [ ] **Step 1: 创建 pgloader 配置文件**

新建 `/Users/Admin/Documents/InternProject/myproject/migrate.load`，内容：

```
LOAD DATABASE
  FROM    mysql://root:Hazenixbzh66MySQLbulai@mysql:3306/db_hazeblog
  INTO    postgresql://root:Hazenixbzh66MySQLbulai@postgres:5432/db_hazeblog
WITH include drop, create tables, create indexes,
     reset sequences, foreign keys;
```

- [ ] **Step 2: 确认 mysql 容器正在运行**

```bash
docker compose ps mysql
```

预期：STATUS 为 `healthy` 或 `running`。

- [ ] **Step 3: 运行 pgloader**

```bash
cd /Users/Admin/Documents/InternProject/myproject/
docker run --rm --network blog \
  -v $(pwd)/migrate.load:/migrate.load \
  ghcr.io/dimitri/pgloader:latest \
  pgloader /migrate.load
```

预期：输出末尾显示迁移统计表，无 `ERROR` 行。

- [ ] **Step 4: 验证数据已迁移**

```bash
docker exec postgres psql -U root -d db_hazeblog -c "SELECT COUNT(*) FROM article;"
docker exec postgres psql -U root -d db_hazeblog -c "SELECT COUNT(*) FROM \"user\";"
docker exec postgres psql -U root -d db_hazeblog -c "\dt"
```

预期：`article` 和 `user` 的 COUNT 与 MySQL 中一致；`\dt` 列出 19 张表。

> 如果 pgloader 报 `ENUM` 类型警告属正常，pgloader 会将其转为 TEXT + CHECK 约束。

---

## Task 5：恢复 Umami 数据

- [ ] **Step 1: 确认 umami_backup.sql 存在**

```bash
ls -lh /Users/Admin/Documents/InternProject/myproject/umami_backup.sql
```

- [ ] **Step 2: 导入 umami 数据**

```bash
docker exec -i postgres psql -U umami -d umami \
  < /Users/Admin/Documents/InternProject/myproject/umami_backup.sql
```

预期：无 ERROR 输出（WARNING 可忽略）。

- [ ] **Step 3: 验证 umami 数据**

```bash
docker exec postgres psql -U umami -d umami -c "\dt"
```

预期：列出 umami 的原有表结构。

---

## Task 6：启动 Umami，验证监控系统正常

- [ ] **Step 1: 启动 umami 容器**

```bash
cd /Users/Admin/Documents/InternProject/myproject/
docker compose up -d umami
```

- [ ] **Step 2: 等待 umami 就绪**

```bash
docker compose logs umami --follow
```

看到 `Listening on port 3000` 类似日志后按 Ctrl+C。

- [ ] **Step 3: 验证 umami 可访问**

浏览器打开 `http://localhost:3000`（或服务器 IP:3000），确认能看到 Umami 登录页。

---

## Task 7：修改 Spring Boot 后端——pom.xml

**Files:**
- Modify: `backend/blog-server/pom.xml`

- [ ] **Step 1: 将 mysql-connector-java 替换为 postgresql 驱动**

定位文件第 77 行附近：
```xml
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
```

替换为：
```xml
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
```

- [ ] **Step 2: 在 backend 目录执行编译验证依赖可下载**

```bash
cd /Users/Admin/Documents/InternProject/myproject/blog.hazenix.top/backend
mvn dependency:resolve -pl blog-server -q
```

预期：无 BUILD FAILURE。

---

## Task 8：修改 application.yml — 换 JDBC URL 格式

**Files:**
- Modify: `backend/blog-server/src/main/resources/application.yml`

- [ ] **Step 1: 将 datasource url 从 MySQL 格式改为 PostgreSQL 格式**

定位 `url: jdbc:mysql://...` 这一行（第 12 行），整行替换为：

```yaml
      url: jdbc:postgresql://${blog.datasource.host}:${blog.datasource.port}/${blog.datasource.database}
```

删除原 MySQL 专属参数（`serverTimezone`、`useUnicode`、`characterEncoding`、`zeroDateTimeBehavior`、`useSSL`、`allowPublicKeyRetrieval`、`autoReconnect`、`rewriteBatchedStatements`），这些参数在 PostgreSQL 驱动中无意义。

---

## Task 9：修改 application-prod.yml — 换驱动和连接信息

**Files:**
- Modify: `backend/blog-server/src/main/resources/application-prod.yml`

- [ ] **Step 1: 修改 datasource 配置块**

定位文件第 3-8 行的 datasource 配置，整块替换为：

```yaml
  datasource:
    driver-class-name: org.postgresql.Driver
    host: postgres
    port: 5432
    database: db_hazeblog
    username: root
    password: Hazenixbzh66MySQLbulai
```

---

## Task 10：修改 application-test.yml — 换驱动和端口

**Files:**
- Modify: `backend/blog-server/src/main/resources/application-test.yml`

- [ ] **Step 1: 修改 datasource 配置块**

定位 datasource 配置（文件第 5-10 行），整块替换为：

```yaml
  datasource:
    driver-class-name: org.postgresql.Driver
    host: localhost
    port: 5432
    database: db_hazeblog
    username: root
    password: Hazenixbzh66MySQLbulai
```

---

## Task 11：重新打包 blog，更新部署

- [ ] **Step 1: 打包**

```bash
cd /Users/Admin/Documents/InternProject/myproject/blog.hazenix.top/backend
mvn clean package -DskipTests
```

预期：`BUILD SUCCESS`，生成 `blog-server/target/blog-server-1.0-SNAPSHOT.jar`。

- [ ] **Step 2: 将 jar 包复制到部署目录**

```bash
cp blog-server/target/blog-server-1.0-SNAPSHOT.jar \
   /Users/Admin/Documents/InternProject/myproject/blog/
```

> 如果部署目录不在本机，通过 scp 上传到服务器对应目录。

- [ ] **Step 3: 重启 blog 容器**

```bash
cd /Users/Admin/Documents/InternProject/myproject/
docker compose up -d --build blog
```

- [ ] **Step 4: 查看 blog 启动日志**

```bash
docker compose logs blog --follow
```

看到 `Started BlogApplication` 或类似日志后按 Ctrl+C。

---

## Task 12：端对端验证博客功能

- [ ] **Step 1: 验证博客首页可访问**

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/article/list
```

预期：返回 `200`。

- [ ] **Step 2: 验证文章详情**

```bash
curl -s http://localhost:8080/api/article/1 | head -c 200
```

预期：返回包含 `title`、`content` 的 JSON，数据与迁移前一致。

- [ ] **Step 3: 浏览器测试**

打开博客前端，检查：
- 首页文章列表正常显示
- 文章详情页正常打开
- 评论区显示历史评论
- 用户登录功能正常

---

## Task 13：从 docker-compose.yml 移除 MySQL

> 确认 Task 12 全部通过后执行此步骤。

**Files:**
- Modify: `../docker-compose.yml`

- [ ] **Step 1: 删除 mysql 服务整块**

删除 docker-compose.yml 中从 `mysql:` 到 `    start_period: 30s` 之间的全部内容（第 57-82 行），即完整的 mysql 服务定义。

- [ ] **Step 2: 删除底部的 umami-db-data 卷声明**

删除文件末尾的：
```yaml
volumes:
  umami-db-data:
```

如果 `volumes:` 下还有其他卷声明需保留，只删 `umami-db-data:` 那一行。

- [ ] **Step 3: 重启验证**

```bash
cd /Users/Admin/Documents/InternProject/myproject/
docker compose up -d
docker compose ps
```

预期：无 mysql 容器；postgres、blog、redis、umami、nginx 全部为 running/healthy 状态。

- [ ] **Step 4: 提交后端代码变更**

```bash
cd /Users/Admin/Documents/InternProject/myproject/blog.hazenix.top/
git add backend/blog-server/pom.xml \
        backend/blog-server/src/main/resources/application.yml \
        backend/blog-server/src/main/resources/application-prod.yml \
        backend/blog-server/src/main/resources/application-test.yml
git commit -m "feat(backend): 数据库从 MySQL 迁移至 PostgreSQL 17"
```

- [ ] **Step 5: 清理 migrate.load（可选）**

迁移配置文件不需要长期保留：
```bash
rm /Users/Admin/Documents/InternProject/myproject/migrate.load
```

- [ ] **Step 6: 保留 MySQL 数据卷备份（暂不删除）**

`./mysql/data` 目录保留至少一周，确认生产环境稳定后再手动删除：
```bash
# 确认无误后手动执行：
# rm -rf /Users/Admin/Documents/InternProject/myproject/mysql/data
```
