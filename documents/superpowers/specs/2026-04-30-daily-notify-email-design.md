# 每日通知邮件 — 设计文档

- 日期：2026-04-30
- 范围：每日定时把「评论 / 树洞评论 / 友链申请」昨日明细汇总成一封 HTML 邮件，发送到管理员邮箱；收件邮箱、SMTP、发送时间均后台可改

## 1. 现状

- 项目无 `spring-boot-starter-mail`，无 `@EnableScheduling`，无任何定时任务/邮件相关代码
- 评论 [comments](backend/blog-server/src/main/resources/mapper/CommentsMapper.xml)、树洞 [tree_comments](backend/blog-server/src/main/resources/mapper/TreeCommentsMapper.xml)、友链 [link](backend/blog-server/src/main/resources/mapper/LinkMapper.xml) 数据模型与 Mapper 均已存在，三表均含 `create_time`
- 项目无按日 PV/UV 统计数据，本期不上报「今日浏览量 / 今日访客数」

## 2. 目标

- 每天定时（默认 08:00，后台可改）发送一封 HTML 通知邮件
- 邮件内容：「昨日 00:00:00 ~ 24:00:00」内
  - 新增评论（数量 + 明细）
  - 新增树洞评论（数量 + 明细）
  - 新增友链申请（数量 + 明细，仅 `status=pending`）
- 收件邮箱、SMTP host/port/账号/密码、发送时间均通过后台接口在线修改，无需重启
- 发送失败自动重试 3 次（+1 / +5 / +30 分钟），最终失败入库
- 提供后台「测试发送」「手动补发某日」「查看发送日志」接口

## 3. 关键决策

| 项 | 取值 | 说明 |
|---|---|---|
| 调度方式 | `ThreadPoolTaskScheduler` 动态注册 | 不用 `@Scheduled` 静态注解，便于运行时改时间 |
| 调度时间 | 单次 cron `0 m H * * ?` | `H:m` 来自 `notify_config.send_time` |
| 统计口径 | 昨日 `[今日 00:00:00, 今日 00:00:00)` 之前 24h | 数据完整、口径清晰 |
| 内容片段截取 | 80 字（超出加 …） | 评论/树洞内容字段 |
| 友链审核链接 | 不附（前端无现成路由） | 邮件正文文字提示「请进入后台审核」 |
| 明细带创建时间 | 是，格式 `HH:mm:ss` | 三类明细每行都带 |
| SMTP 密码加密 | AES，密钥从 yml 读 | `blog.notify.encrypt-key` |
| 配置存储 | `notify_config` 单行表 | id 固定为 1，UPSERT 语义 |
| 发送日志 | `notify_log` 表 | 记录每次尝试的成功/失败、重试次数、异常 |
| 重试 | 1 / 5 / 30 分钟 三次 | 由 `taskScheduler.schedule(retry, instant)` 触发 |
| 邮件模板 | 字符串拼装 HTML | 不引 Thymeleaf，避免新增依赖 |

## 4. 数据库

```sql
-- 通知配置（单行，id=1）
CREATE TABLE notify_config (
  id            BIGINT       PRIMARY KEY,
  recipient     VARCHAR(255) NOT NULL  COMMENT '收件人邮箱',
  smtp_host     VARCHAR(255) NOT NULL,
  smtp_port     INT          NOT NULL,
  smtp_username VARCHAR(255) NOT NULL,
  smtp_password VARCHAR(512) NOT NULL  COMMENT 'AES 加密后存储',
  smtp_ssl      TINYINT(1)   NOT NULL DEFAULT 1,
  send_time     VARCHAR(8)   NOT NULL DEFAULT '08:00' COMMENT 'HH:mm',
  enabled       TINYINT(1)   NOT NULL DEFAULT 1,
  update_time   DATETIME
);

-- 发送日志
CREATE TABLE notify_log (
  id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
  stat_date    DATE         NOT NULL  COMMENT '统计目标日（昨日）',
  send_time    DATETIME     NOT NULL  COMMENT '本次实际发送时间',
  status       TINYINT      NOT NULL  COMMENT '0 成功 / 1 失败',
  retry_count  INT          NOT NULL DEFAULT 0,
  error_msg    TEXT,
  recipient    VARCHAR(255),
  KEY idx_stat_date (stat_date)
);
```

初始化：插入 id=1 行（占位，需在后台首次配置后才会实际发送，`enabled=0` 时跳过）。

## 5. 架构

### 5.1 新增 / 修改组件

| 组件 | 路径 | 类型 |
|---|---|---|
| 启用调度 | `top.hazenix.BlogApplication` | 修改：加 `@EnableScheduling` |
| Mail 依赖 | `backend/blog-server/pom.xml` | 修改：加 `spring-boot-starter-mail` |
| 实体 | `top.hazenix.entity.NotifyConfig`、`NotifyLog` | 新增 |
| Mapper | `top.hazenix.mapper.NotifyConfigMapper`、`NotifyLogMapper` (+ xml) | 新增 |
| 配置 Service | `top.hazenix.service.NotifyConfigService` (+ Impl) | 新增；负责 SMTP 密码加解密 |
| 通知 Service | `top.hazenix.service.DailyNotifyService` (+ Impl) | 新增；收集数据 → 渲染 → 调 sender |
| 调度管理 | `top.hazenix.task.NotifyScheduleManager` | 新增；`@PostConstruct` 注册首次任务，配置变更时 reschedule |
| 邮件发送器 | `top.hazenix.notify.MailSender` | 新增；封装 `JavaMailSenderImpl`，支持运行时切换 SMTP；执行失败重试 |
| 加密工具 | `top.hazenix.utils.AesCryptoUtil` | 新增；密钥来自 `blog.notify.encrypt-key` |
| 数据收集 SQL | `CommentsMapper#listByCreateTimeBetween` 等三处 | 新增 |
| 后台 Controller | `top.hazenix.controller.admin.NotifyConfigController` | 新增 |
| DTO/VO | `dto.NotifyConfigDTO`、`vo.NotifyConfigVO`（不含密码）、`vo.NotifyLogVO` | 新增 |
| 配置项 | `application.yml` / `-dev.yml` | 加 `blog.notify.encrypt-key` |

### 5.2 数据流

```
ThreadPoolTaskScheduler (cron 触发)
        ↓
DailyNotifyService.run(date = yesterday)
        ├─ CommentsMapper.listByCreateTimeBetween(start, end)
        ├─ TreeCommentsMapper.listByCreateTimeBetween(start, end)
        └─ LinkMapper.listByCreateTimeBetween(start, end, status=pending)
        ↓
HtmlRenderer.render(...)        ← 字符串拼接
        ↓
MailSender.send(toRecipient, subject, html)
        ├─ 成功 → notify_log.status=0
        └─ 失败 → 1/5/30 分钟后重试，最终失败 → notify_log.status=1 + error_msg
```

### 5.3 调度生命周期

- 启动：`NotifyScheduleManager.@PostConstruct` 读 `notify_config`；`enabled=1` 时按 `send_time` 注册下一次触发
- 触发：执行完成后立即注册下一日的同一时间（保持单一 future 引用）
- 改配置：`PUT /admin/notify/config` → 更新表 → 调 `reschedule()` 取消旧 future、按新 `send_time` 注册
- 关闭：`enabled=0` → 取消 future，不再注册

### 5.4 Mapper 新增方法

```java
// CommentsMapper / TreeCommentsMapper
List<Comments> listByCreateTimeBetween(@Param("start") LocalDateTime start,
                                       @Param("end")   LocalDateTime end);

// LinkMapper：仅取待审核（status=pending）
List<Link> listPendingByCreateTimeBetween(@Param("start") LocalDateTime start,
                                          @Param("end")   LocalDateTime end);
```

XML 用 `BETWEEN #{start} AND #{end}`（半开区间用 `>= AND <`）。

## 6. 邮件内容

**主题：** `【Hazenix Blog】每日通知 yyyy-MM-dd`（yyyy-MM-dd 为昨日日期）

**正文 HTML 结构：**

```
[标题] 每日通知 · 2026-04-29

[概览卡片]
  评论：N1 条    树洞：N2 条    友链申请：N3 条（待审核）

[分块 1] 新增评论（N1）
  - HH:mm:ss  username：content（截断 80 字…）  ｜ 文章《title》
  - ... （0 条则显示「无新增」）

[分块 2] 新增树洞评论（N2）
  - HH:mm:ss  username：content（截断 80 字…）

[分块 3] 待审核友链申请（N3）
  - HH:mm:ss  站点名 / URL / description（截断 80 字…）
  - 请登录后台审核（前端暂无独立审核页面，请在管理后台「友链管理」中处理）

[页脚] 自动发送，请勿回复
```

- 评论的「文章标题」需 join `article` 表查；为最小化 SQL 改动，可在 `CommentsMapper#listByCreateTimeBetween` 内 LEFT JOIN article 取 title，返回新 VO `CommentNotifyVO { id, username, content, articleTitle, createTime }`
- 截断逻辑放在渲染层，不污染 SQL/VO

## 7. 后台接口

均挂在 `/admin/notify/**`，走已有 admin JWT 拦截器。

```
GET  /admin/notify/config
     → NotifyConfigVO（不返回 smtp_password，返回 "********"）

PUT  /admin/notify/config
     body: NotifyConfigDTO { recipient, smtpHost, smtpPort, smtpUsername,
                              smtpPassword?, smtpSsl, sendTime, enabled }
     → 200；smtpPassword 为空表示不修改；保存后调 reschedule()

POST /admin/notify/test
     → 立即按当前配置组装一封"测试邮件"发送（不读业务数据，正文写明"测试"）

POST /admin/notify/trigger?date=yyyy-MM-dd
     → 异步按指定日期跑一次完整流程（用于补发/调试）

GET  /admin/notify/log?page=&size=
     → 分页 NotifyLogVO 列表
```

## 8. 失败与重试

- `MailSender.sendWithRetry(req)`：
  - 第 0 次立即发；失败抛异常
  - 捕获后用 `taskScheduler.schedule(retry, now+1min)`；同样失败则 `+5min`、`+30min`
  - 最多 3 次重试（即总共 4 次尝试）；最后一次失败写 `notify_log` status=1
  - 任意一次成功立即写 `notify_log` status=0，并停止后续重试
- 不跨天补发：失败后不会自动累积到次日

## 9. 配置项

`application.yml` 新增：

```yaml
blog:
  notify:
    encrypt-key: ${blog.notify.encrypt-key}   # 16/24/32 字节
```

`application-dev.yml` 新增：

```yaml
blog:
  notify:
    encrypt-key: hazenix-notify-dev-key16   # 16 字节示例
```

## 10. 安全

- SMTP 密码 AES-128 加密入库；接口返回时永远脱敏为 `********`
- `PUT` 时 `smtpPassword` 为空字符串视为「保持不变」；非空则覆盖
- 测试邮件接口与手动触发接口均走 admin JWT
- `encrypt-key` 不入库；prod 环境通过环境变量注入，不写 git

## 11. 不在范围内（Out of Scope）

- 今日浏览量 / 今日访客数（无数据来源，待埋点完成后再加）
- 多收件人 / 多通知渠道（短信、Bark、Webhook 等）
- 邮件模板可视化编辑
- 友链申请的「邮件内一键审核」链接（前端暂无对应路由）

## 12. 验证清单

- [ ] 启动时 `enabled=1` 能注册任务；`enabled=0` 不注册
- [ ] 改 `send_time` 后下次触发时间正确变化
- [ ] 三类数据为 0 时邮件正文显示「无新增」而非崩溃
- [ ] 内容超过 80 字被正确截断并加省略号
- [ ] SMTP 错误时 1/5/30 分钟重试三次，日志记录正确
- [ ] `GET /admin/notify/config` 返回的 `smtpPassword` 是 `********`
- [ ] `PUT` 时 `smtpPassword` 留空不会清空数据库密码
- [ ] 手动触发指定日期能补发
