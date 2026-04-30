# 每日通知邮件 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 每日定时发送 HTML 邮件，汇总昨日评论/树洞/友链申请明细，支持邮件内一键审核友链，SMTP 与收件人后台可改。

**Architecture:** 动态调度（`ThreadPoolTaskScheduler`）+ 数据库配置（`notify_config` 单行表）+ 一次性 token 友链审核。数据收集层查三张已有表的 `create_time` 范围，渲染层字符串拼 HTML，发送层封装 `JavaMailSenderImpl` 并支持运行时切 SMTP。失败重试 3 次（1/5/30 分钟），日志入 `notify_log`。

**Tech Stack:** Spring Boot, MyBatis, JavaMailSender, ThreadPoolTaskScheduler, AES-128 (javax.crypto)

**Spec:** [2026-04-30-daily-notify-email-design.md](../specs/2026-04-30-daily-notify-email-design.md)

---

## File Map

### blog-server/pom.xml (Modify)
- 加 `spring-boot-starter-mail` 依赖

### BlogApplication.java (Modify)
- 加 `@EnableScheduling`

### application.yml / application-dev.yml (Modify)
- 加 `blog.notify.encrypt-key`、`blog.notify.public-base-url`

### SecurityConfig.java (Modify)
- 放行 `/api/notify/link-action`

### blog-pojo 新增实体
- `entity/NotifyConfig.java`
- `entity/NotifyLog.java`
- `entity/NotifyActionToken.java`

### blog-pojo 新增 DTO/VO
- `dto/NotifyConfigDTO.java`
- `vo/NotifyConfigVO.java`
- `vo/NotifyLogVO.java`
- `vo/CommentNotifyVO.java`

### blog-common 新增工具
- `utils/AesCryptoUtil.java`

### blog-common 新增常量
- `constant/NotifyConstants.java`

### blog-server 新增 Mapper
- `mapper/NotifyConfigMapper.java` + `resources/mapper/NotifyConfigMapper.xml`
- `mapper/NotifyLogMapper.java` + `resources/mapper/NotifyLogMapper.xml`
- `mapper/NotifyActionTokenMapper.java` + `resources/mapper/NotifyActionTokenMapper.xml`

### blog-server 已有 Mapper 新增方法
- `CommentsMapper.java` + `CommentsMapper.xml` — `listByCreateTimeBetween`
- `TreeCommentsMapper.java` + `TreeCommentsMapper.xml` — `listByCreateTimeBetween`
- `LinkMapper.java` + `LinkMapper.xml` — `listPendingByCreateTimeBetween`

### blog-server 新增 Service
- `service/NotifyConfigService.java` + `service/impl/NotifyConfigServiceImpl.java`
- `service/DailyNotifyService.java` + `service/impl/DailyNotifyServiceImpl.java`
- `service/NotifyActionService.java` + `service/impl/NotifyActionServiceImpl.java`

### blog-server 新增基础设施
- `notify/BlogMailSender.java` — 封装 JavaMailSenderImpl
- `notify/NotifyHtmlRenderer.java` — HTML 拼装
- `task/NotifyScheduleManager.java` — 动态调度

### blog-server 新增 Controller
- `controller/admin/NotifyConfigController.java`
- `controller/web/NotifyActionController.java`

### SQL 脚本
- `documents/sql/notify_tables.sql`

---

## Task 1: 项目基础设施 — 依赖、配置、SQL

**Files:**
- Modify: `backend/blog-server/pom.xml:82-89` (在 redis 依赖后加 mail)
- Modify: `backend/blog-server/src/main/java/top/hazenix/BlogApplication.java:8` (加 import + 注解)
- Modify: `backend/blog-server/src/main/resources/application.yml:39` (blog 节点下加 notify)
- Modify: `backend/blog-server/src/main/resources/application-dev.yml` (加 notify 实际值)
- Create: `documents/sql/notify_tables.sql`

- [ ] **Step 1: 加 spring-boot-starter-mail 依赖**

在 `blog-server/pom.xml` 的 `spring-boot-starter-data-redis` 依赖之后加：

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
```

- [ ] **Step 2: BlogApplication 加 @EnableScheduling**

```java
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableTransactionManagement
@Slf4j
@EnableKnife4j
@EnableSwagger2
@EnableCaching
@EnableScheduling
public class BlogApplication {
```

- [ ] **Step 3: application.yml 加 notify 占位**

在 `blog:` 节点末尾（`github` 之后）加：

```yaml
  notify:
    encrypt-key: ${blog.notify.encrypt-key}
    public-base-url: ${blog.notify.public-base-url}
```

- [ ] **Step 4: application-dev.yml 加 notify 实际值**

在 `blog:` 节点末尾加：

```yaml
  notify:
    encrypt-key: hazenix-notify-dev-key16
    public-base-url: http://localhost:9090
```

- [ ] **Step 5: 创建 SQL 脚本**

创建 `documents/sql/notify_tables.sql`：

```sql
CREATE TABLE IF NOT EXISTS notify_config (
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

INSERT IGNORE INTO notify_config (id, recipient, smtp_host, smtp_port, smtp_username, smtp_password, send_time, enabled)
VALUES (1, 'admin@example.com', 'smtp.qq.com', 465, 'your@qq.com', '', '08:00', 0);

CREATE TABLE IF NOT EXISTS notify_log (
  id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
  stat_date    DATE         NOT NULL  COMMENT '统计目标日（昨日）',
  send_time    DATETIME     NOT NULL  COMMENT '本次实际发送时间',
  status       TINYINT      NOT NULL  COMMENT '0 成功 / 1 失败',
  retry_count  INT          NOT NULL DEFAULT 0,
  error_msg    TEXT,
  recipient    VARCHAR(255),
  KEY idx_stat_date (stat_date)
);

CREATE TABLE IF NOT EXISTS notify_action_token (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  token       VARCHAR(64)  NOT NULL UNIQUE COMMENT '随机 token (URL-safe)',
  target_type VARCHAR(32)  NOT NULL COMMENT 'link',
  target_id   BIGINT       NOT NULL,
  action      VARCHAR(16)  NOT NULL COMMENT 'approve / reject',
  expires_at  DATETIME     NOT NULL,
  used_at     DATETIME,
  create_time DATETIME     NOT NULL,
  KEY idx_token (token),
  KEY idx_expires (expires_at)
);
```

- [ ] **Step 6: 手动执行 SQL 脚本建表**

```bash
mysql -u root -p1234 db_hazeblog < documents/sql/notify_tables.sql
```

- [ ] **Step 7: Commit**

```bash
git add backend/blog-server/pom.xml backend/blog-server/src/main/java/top/hazenix/BlogApplication.java backend/blog-server/src/main/resources/application.yml backend/blog-server/src/main/resources/application-dev.yml documents/sql/notify_tables.sql
git commit -m "feat(notify): add mail dependency, scheduling, config, and SQL schema"
```

---

## Task 2: 常量 + AES 加密工具 + 实体类

**Files:**
- Create: `backend/blog-common/src/main/java/top/hazenix/constant/NotifyConstants.java`
- Create: `backend/blog-common/src/main/java/top/hazenix/utils/AesCryptoUtil.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/NotifyConfig.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/NotifyLog.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/NotifyActionToken.java`

- [ ] **Step 1: 创建 NotifyConstants**

```java
package top.hazenix.constant;

public class NotifyConstants {
    public static final Long CONFIG_ID = 1L;
    public static final int CONTENT_SNIPPET_LENGTH = 80;
    public static final int TOKEN_BYTE_LENGTH = 32;
    public static final int TOKEN_EXPIRE_DAYS = 7;
    public static final int[] RETRY_DELAY_MINUTES = {1, 5, 30};
    public static final String PASSWORD_MASK = "********";

    private NotifyConstants() {}
}
```

- [ ] **Step 2: 创建 AesCryptoUtil**

```java
package top.hazenix.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AesCryptoUtil {

    private AesCryptoUtil() {}

    public static String encrypt(String plainText, String key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES encrypt failed", e);
        }
    }

    public static String decrypt(String cipherText, String key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decrypt failed", e);
        }
    }
}
```

- [ ] **Step 3: 创建 NotifyConfig 实体**

```java
package top.hazenix.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyConfig {
    private Long id;
    private String recipient;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private Integer smtpSsl;
    private String sendTime;
    private Integer enabled;
    private LocalDateTime updateTime;
}
```

- [ ] **Step 4: 创建 NotifyLog 实体**

```java
package top.hazenix.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyLog {
    private Long id;
    private LocalDate statDate;
    private LocalDateTime sendTime;
    private Integer status;
    private Integer retryCount;
    private String errorMsg;
    private String recipient;
}
```

- [ ] **Step 5: 创建 NotifyActionToken 实体**

```java
package top.hazenix.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyActionToken {
    private Long id;
    private String token;
    private String targetType;
    private Long targetId;
    private String action;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createTime;
}
```

- [ ] **Step 6: Commit**

```bash
git add backend/blog-common/src/main/java/top/hazenix/constant/NotifyConstants.java backend/blog-common/src/main/java/top/hazenix/utils/AesCryptoUtil.java backend/blog-pojo/src/main/java/top/hazenix/entity/NotifyConfig.java backend/blog-pojo/src/main/java/top/hazenix/entity/NotifyLog.java backend/blog-pojo/src/main/java/top/hazenix/entity/NotifyActionToken.java
git commit -m "feat(notify): add constants, AES util, and entity classes"
```

---

## Task 3: DTO / VO 类

**Files:**
- Create: `backend/blog-pojo/src/main/java/top/hazenix/dto/NotifyConfigDTO.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/vo/NotifyConfigVO.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/vo/NotifyLogVO.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/vo/CommentNotifyVO.java`

- [ ] **Step 1: 创建 NotifyConfigDTO**

```java
package top.hazenix.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class NotifyConfigDTO {
    @NotBlank @Email
    private String recipient;
    @NotBlank
    private String smtpHost;
    @NotNull
    private Integer smtpPort;
    @NotBlank
    private String smtpUsername;
    private String smtpPassword;  // 空字符串 = 不修改
    private Integer smtpSsl;
    @NotBlank
    private String sendTime;      // HH:mm
    @NotNull
    private Integer enabled;
}
```

- [ ] **Step 2: 创建 NotifyConfigVO**

```java
package top.hazenix.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotifyConfigVO {
    private String recipient;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;  // 永远返回 "********"
    private Integer smtpSsl;
    private String sendTime;
    private Integer enabled;
}
```

- [ ] **Step 3: 创建 NotifyLogVO**

```java
package top.hazenix.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class NotifyLogVO {
    private Long id;
    private LocalDate statDate;
    private LocalDateTime sendTime;
    private Integer status;
    private Integer retryCount;
    private String errorMsg;
    private String recipient;
}
```

- [ ] **Step 4: 创建 CommentNotifyVO**

用于评论明细查询，LEFT JOIN article 取标题。

```java
package top.hazenix.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentNotifyVO {
    private Long id;
    private String username;
    private String content;
    private String articleTitle;
    private LocalDateTime createTime;
}
```

- [ ] **Step 5: Commit**

```bash
git add backend/blog-pojo/src/main/java/top/hazenix/dto/NotifyConfigDTO.java backend/blog-pojo/src/main/java/top/hazenix/vo/NotifyConfigVO.java backend/blog-pojo/src/main/java/top/hazenix/vo/NotifyLogVO.java backend/blog-pojo/src/main/java/top/hazenix/vo/CommentNotifyVO.java
git commit -m "feat(notify): add DTOs and VOs for notify config, log, and comment detail"
```

---

## Task 4: Mapper 层 — 新增 Mapper + 已有 Mapper 加方法

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/NotifyConfigMapper.java`
- Create: `backend/blog-server/src/main/resources/mapper/NotifyConfigMapper.xml`
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/NotifyLogMapper.java`
- Create: `backend/blog-server/src/main/resources/mapper/NotifyLogMapper.xml`
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/NotifyActionTokenMapper.java`
- Create: `backend/blog-server/src/main/resources/mapper/NotifyActionTokenMapper.xml`
- Modify: `backend/blog-server/src/main/java/top/hazenix/mapper/CommentsMapper.java`
- Modify: `backend/blog-server/src/main/resources/mapper/CommentsMapper.xml`
- Modify: `backend/blog-server/src/main/java/top/hazenix/mapper/TreeCommentsMapper.java`
- Modify: `backend/blog-server/src/main/resources/mapper/TreeCommentsMapper.xml`
- Modify: `backend/blog-server/src/main/java/top/hazenix/mapper/LinkMapper.java`
- Modify: `backend/blog-server/src/main/resources/mapper/LinkMapper.xml`

- [ ] **Step 1: 创建 NotifyConfigMapper**

```java
package top.hazenix.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.hazenix.entity.NotifyConfig;

@Mapper
public interface NotifyConfigMapper {
    NotifyConfig getById(Long id);
    void update(NotifyConfig config);
}
```

- [ ] **Step 2: 创建 NotifyConfigMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="top.hazenix.mapper.NotifyConfigMapper">
    <select id="getById" resultType="top.hazenix.entity.NotifyConfig">
        SELECT id, recipient, smtp_host, smtp_port, smtp_username, smtp_password,
               smtp_ssl, send_time, enabled, update_time
        FROM notify_config WHERE id = #{id}
    </select>
    <update id="update">
        UPDATE notify_config
        <set>
            <if test="recipient != null">recipient = #{recipient},</if>
            <if test="smtpHost != null">smtp_host = #{smtpHost},</if>
            <if test="smtpPort != null">smtp_port = #{smtpPort},</if>
            <if test="smtpUsername != null">smtp_username = #{smtpUsername},</if>
            <if test="smtpPassword != null">smtp_password = #{smtpPassword},</if>
            <if test="smtpSsl != null">smtp_ssl = #{smtpSsl},</if>
            <if test="sendTime != null">send_time = #{sendTime},</if>
            <if test="enabled != null">enabled = #{enabled},</if>
            update_time = NOW()
        </set>
        WHERE id = #{id}
    </update>
</mapper>
```

- [ ] **Step 3: 创建 NotifyLogMapper**

```java
package top.hazenix.mapper;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import top.hazenix.entity.NotifyLog;

@Mapper
public interface NotifyLogMapper {
    void insert(NotifyLog log);
    Page<NotifyLog> pageQuery();
}
```

- [ ] **Step 4: 创建 NotifyLogMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="top.hazenix.mapper.NotifyLogMapper">
    <insert id="insert">
        INSERT INTO notify_log (stat_date, send_time, status, retry_count, error_msg, recipient)
        VALUES (#{statDate}, #{sendTime}, #{status}, #{retryCount}, #{errorMsg}, #{recipient})
    </insert>
    <select id="pageQuery" resultType="top.hazenix.entity.NotifyLog">
        SELECT id, stat_date, send_time, status, retry_count, error_msg, recipient
        FROM notify_log ORDER BY send_time DESC
    </select>
</mapper>
```

- [ ] **Step 5: 创建 NotifyActionTokenMapper**

```java
package top.hazenix.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hazenix.entity.NotifyActionToken;

@Mapper
public interface NotifyActionTokenMapper {
    void insert(NotifyActionToken token);
    NotifyActionToken getByToken(@Param("token") String token);
    void markUsed(@Param("token") String token);
    void markUsedByTargetId(@Param("targetType") String targetType, @Param("targetId") Long targetId);
}
```

- [ ] **Step 6: 创建 NotifyActionTokenMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="top.hazenix.mapper.NotifyActionTokenMapper">
    <insert id="insert">
        INSERT INTO notify_action_token (token, target_type, target_id, action, expires_at, create_time)
        VALUES (#{token}, #{targetType}, #{targetId}, #{action}, #{expiresAt}, #{createTime})
    </insert>
    <select id="getByToken" resultType="top.hazenix.entity.NotifyActionToken">
        SELECT id, token, target_type, target_id, action, expires_at, used_at, create_time
        FROM notify_action_token WHERE token = #{token}
    </select>
    <update id="markUsed">
        UPDATE notify_action_token SET used_at = NOW() WHERE token = #{token} AND used_at IS NULL
    </update>
    <update id="markUsedByTargetId">
        UPDATE notify_action_token SET used_at = NOW()
        WHERE target_type = #{targetType} AND target_id = #{targetId} AND used_at IS NULL
    </update>
</mapper>
```

- [ ] **Step 7: CommentsMapper 加 listByCreateTimeBetween**

在 `CommentsMapper.java` 接口末尾加：

```java
import org.apache.ibatis.annotations.Param;
import top.hazenix.vo.CommentNotifyVO;

List<CommentNotifyVO> listByCreateTimeBetween(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
```

在 `CommentsMapper.xml` 末尾 `</mapper>` 前加：

```xml
    <select id="listByCreateTimeBetween" resultType="top.hazenix.vo.CommentNotifyVO">
        SELECT c.id, c.username, c.content, a.title AS article_title, c.create_time
        FROM comments c
        LEFT JOIN article a ON c.article_id = a.id
        WHERE c.create_time >= #{start} AND c.create_time &lt; #{end}
          AND c.status = 0
        ORDER BY c.create_time ASC
    </select>
```

- [ ] **Step 8: TreeCommentsMapper 加 listByCreateTimeBetween**

在 `TreeCommentsMapper.java` 接口末尾加：

```java
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

List<TreeComments> listByCreateTimeBetween(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);
```

在 `TreeCommentsMapper.xml` 末尾 `</mapper>` 前加：

```xml
    <select id="listByCreateTimeBetween" resultType="top.hazenix.entity.TreeComments">
        SELECT id, username, content, create_time
        FROM tree_comments
        WHERE create_time >= #{start} AND create_time &lt; #{end}
          AND status = 0
        ORDER BY create_time ASC
    </select>
```

- [ ] **Step 9: LinkMapper 加 listPendingByCreateTimeBetween**

在 `LinkMapper.java` 接口末尾加：

```java
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

List<Link> listPendingByCreateTimeBetween(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);
```

在 `LinkMapper.xml` 末尾 `</mapper>` 前加：

```xml
    <select id="listPendingByCreateTimeBetween" resultType="top.hazenix.entity.Link">
        SELECT id, name, description, url, avatar, status, create_time
        FROM link
        WHERE create_time >= #{start} AND create_time &lt; #{end}
          AND status = 1
        ORDER BY create_time ASC
    </select>
```

注意：`link.status = 1` 对应 `CommonStatusConstants.PENDING`（待审核）。

- [ ] **Step 10: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/mapper/NotifyConfigMapper.java backend/blog-server/src/main/resources/mapper/NotifyConfigMapper.xml backend/blog-server/src/main/java/top/hazenix/mapper/NotifyLogMapper.java backend/blog-server/src/main/resources/mapper/NotifyLogMapper.xml backend/blog-server/src/main/java/top/hazenix/mapper/NotifyActionTokenMapper.java backend/blog-server/src/main/resources/mapper/NotifyActionTokenMapper.xml backend/blog-server/src/main/java/top/hazenix/mapper/CommentsMapper.java backend/blog-server/src/main/resources/mapper/CommentsMapper.xml backend/blog-server/src/main/java/top/hazenix/mapper/TreeCommentsMapper.java backend/blog-server/src/main/resources/mapper/TreeCommentsMapper.xml backend/blog-server/src/main/java/top/hazenix/mapper/LinkMapper.java backend/blog-server/src/main/resources/mapper/LinkMapper.xml
git commit -m "feat(notify): add notify mappers and date-range queries for comments/tree/link"
```

---

## Task 5: NotifyConfigService — 配置读写 + SMTP 密码加解密

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/service/NotifyConfigService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/NotifyConfigServiceImpl.java`

- [ ] **Step 1: 创建 NotifyConfigService 接口**

```java
package top.hazenix.service;

import top.hazenix.dto.NotifyConfigDTO;
import top.hazenix.entity.NotifyConfig;
import top.hazenix.vo.NotifyConfigVO;

public interface NotifyConfigService {
    NotifyConfigVO getConfig();
    NotifyConfig getRawConfig();
    void updateConfig(NotifyConfigDTO dto);
    String decryptPassword(String encrypted);
}
```

- [ ] **Step 2: 创建 NotifyConfigServiceImpl**

```java
package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.hazenix.constant.NotifyConstants;
import top.hazenix.dto.NotifyConfigDTO;
import top.hazenix.entity.NotifyConfig;
import top.hazenix.mapper.NotifyConfigMapper;
import top.hazenix.service.NotifyConfigService;
import top.hazenix.utils.AesCryptoUtil;
import top.hazenix.vo.NotifyConfigVO;

@Service
@RequiredArgsConstructor
public class NotifyConfigServiceImpl implements NotifyConfigService {

    private final NotifyConfigMapper notifyConfigMapper;

    @Value("${blog.notify.encrypt-key}")
    private String encryptKey;

    @Override
    public NotifyConfigVO getConfig() {
        NotifyConfig config = notifyConfigMapper.getById(NotifyConstants.CONFIG_ID);
        if (config == null) return null;
        return NotifyConfigVO.builder()
                .recipient(config.getRecipient())
                .smtpHost(config.getSmtpHost())
                .smtpPort(config.getSmtpPort())
                .smtpUsername(config.getSmtpUsername())
                .smtpPassword(NotifyConstants.PASSWORD_MASK)
                .smtpSsl(config.getSmtpSsl())
                .sendTime(config.getSendTime())
                .enabled(config.getEnabled())
                .build();
    }

    @Override
    public NotifyConfig getRawConfig() {
        return notifyConfigMapper.getById(NotifyConstants.CONFIG_ID);
    }

    @Override
    public void updateConfig(NotifyConfigDTO dto) {
        NotifyConfig config = NotifyConfig.builder()
                .id(NotifyConstants.CONFIG_ID)
                .recipient(dto.getRecipient())
                .smtpHost(dto.getSmtpHost())
                .smtpPort(dto.getSmtpPort())
                .smtpUsername(dto.getSmtpUsername())
                .smtpSsl(dto.getSmtpSsl())
                .sendTime(dto.getSendTime())
                .enabled(dto.getEnabled())
                .build();
        if (dto.getSmtpPassword() != null && !dto.getSmtpPassword().isEmpty()) {
            config.setSmtpPassword(AesCryptoUtil.encrypt(dto.getSmtpPassword(), encryptKey));
        }
        notifyConfigMapper.update(config);
    }

    @Override
    public String decryptPassword(String encrypted) {
        return AesCryptoUtil.decrypt(encrypted, encryptKey);
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/service/NotifyConfigService.java backend/blog-server/src/main/java/top/hazenix/service/impl/NotifyConfigServiceImpl.java
git commit -m "feat(notify): add NotifyConfigService with AES password encryption"
```

---

## Task 6: BlogMailSender — 运行时切换 SMTP 的邮件发送器

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/notify/BlogMailSender.java`

- [ ] **Step 1: 创建 BlogMailSender**

```java
package top.hazenix.notify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import top.hazenix.entity.NotifyConfig;
import top.hazenix.service.NotifyConfigService;

import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogMailSender {

    private final NotifyConfigService notifyConfigService;

    public void send(String to, String subject, String htmlBody) {
        NotifyConfig config = notifyConfigService.getRawConfig();
        JavaMailSenderImpl sender = buildSender(config);
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(config.getSmtpUsername());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            sender.send(message);
            log.info("Mail sent to {} subject={}", to, subject);
        } catch (Exception e) {
            throw new RuntimeException("Mail send failed: " + e.getMessage(), e);
        }
    }

    private JavaMailSenderImpl buildSender(NotifyConfig config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getSmtpHost());
        sender.setPort(config.getSmtpPort());
        sender.setUsername(config.getSmtpUsername());
        sender.setPassword(notifyConfigService.decryptPassword(config.getSmtpPassword()));
        sender.setDefaultEncoding("UTF-8");
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        if (config.getSmtpSsl() != null && config.getSmtpSsl() == 1) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");
        return sender;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/notify/BlogMailSender.java
git commit -m "feat(notify): add BlogMailSender with runtime SMTP config"
```

---

## Task 7: NotifyActionService — 一次性 token 生成与消费

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/service/NotifyActionService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/NotifyActionServiceImpl.java`

- [ ] **Step 1: 创建 NotifyActionService 接口**

```java
package top.hazenix.service;

public interface NotifyActionService {
    String issueToken(Long targetId, String targetType, String action);
    String consumeToken(String token);
}
```

`issueToken` 返回生成的 token 字符串。`consumeToken` 返回结果 HTML 字符串。

- [ ] **Step 2: 创建 NotifyActionServiceImpl**

```java
package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hazenix.constant.NotifyConstants;
import top.hazenix.entity.Link;
import top.hazenix.entity.NotifyActionToken;
import top.hazenix.mapper.LinkMapper;
import top.hazenix.mapper.NotifyActionTokenMapper;
import top.hazenix.service.NotifyActionService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyActionServiceImpl implements NotifyActionService {

    private final NotifyActionTokenMapper tokenMapper;
    private final LinkMapper linkMapper;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String issueToken(Long targetId, String targetType, String action) {
        byte[] bytes = new byte[NotifyConstants.TOKEN_BYTE_LENGTH];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        NotifyActionToken entity = NotifyActionToken.builder()
                .token(token)
                .targetType(targetType)
                .targetId(targetId)
                .action(action)
                .expiresAt(LocalDateTime.now().plusDays(NotifyConstants.TOKEN_EXPIRE_DAYS))
                .createTime(LocalDateTime.now())
                .build();
        tokenMapper.insert(entity);
        return token;
    }

    @Override
    @Transactional
    public String consumeToken(String token) {
        NotifyActionToken entity = tokenMapper.getByToken(token);
        if (entity == null || entity.getUsedAt() != null || entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            return renderHtml("链接无效或已失效", "该链接已使用、已过期或不存在。");
        }

        // 标记同一 targetId 的所有 token 为已使用（互斥）
        tokenMapper.markUsedByTargetId(entity.getTargetType(), entity.getTargetId());

        if ("link".equals(entity.getTargetType())) {
            Link link = linkMapper.getLinkById(entity.getTargetId());
            if (link == null) {
                return renderHtml("操作失败", "友链不存在。");
            }
            String linkName = link.getName();
            if ("approve".equals(entity.getAction())) {
                Link update = Link.builder().id(entity.getTargetId()).status(0).build();
                linkMapper.updateLink(update);
                log.info("Link approved via token: id={} name={}", entity.getTargetId(), linkName);
                return renderHtml("审核通过", "已通过友链申请：" + linkName);
            } else if ("reject".equals(entity.getAction())) {
                Link update = Link.builder().id(entity.getTargetId()).status(2).build();
                linkMapper.updateLink(update);
                log.info("Link rejected via token: id={} name={}", entity.getTargetId(), linkName);
                return renderHtml("已拒绝", "已拒绝友链申请：" + linkName);
            }
        }
        return renderHtml("操作失败", "不支持的操作类型。");
    }

    private String renderHtml(String title, String message) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title
                + "</title><style>body{font-family:sans-serif;display:flex;justify-content:center;"
                + "align-items:center;height:100vh;margin:0;background:#f5f5f5}"
                + ".card{background:#fff;padding:40px;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,.1);"
                + "text-align:center;max-width:400px}</style></head><body><div class='card'><h2>"
                + title + "</h2><p>" + message + "</p></div></body></html>";
    }
}
```

注意：`approve` 时 `link.status = 0`（`CommonStatusConstants.NORMAL`），`reject` 时 `status = 2`（新增的拒绝状态）。

- [ ] **Step 3: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/service/NotifyActionService.java backend/blog-server/src/main/java/top/hazenix/service/impl/NotifyActionServiceImpl.java
git commit -m "feat(notify): add NotifyActionService for one-time token link approval"
```

---

## Task 8: NotifyHtmlRenderer — 邮件 HTML 拼装

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/notify/NotifyHtmlRenderer.java`

- [ ] **Step 1: 创建 NotifyHtmlRenderer**

```java
package top.hazenix.notify;

import top.hazenix.constant.NotifyConstants;
import top.hazenix.entity.Link;
import top.hazenix.entity.TreeComments;
import top.hazenix.vo.CommentNotifyVO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotifyHtmlRenderer {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private NotifyHtmlRenderer() {}

    public static String render(LocalDate statDate,
                                List<CommentNotifyVO> comments,
                                List<TreeComments> treeComments,
                                List<Link> pendingLinks,
                                java.util.Map<Long, String[]> linkTokens,
                                String publicBaseUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head>");
        sb.append("<body style='font-family:sans-serif;background:#f5f5f5;padding:20px'>");
        sb.append("<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:8px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.08)'>");

        // 标题
        sb.append("<h2 style='color:#333;border-bottom:2px solid #4A90D9;padding-bottom:8px'>")
          .append("每日通知 · ").append(statDate.format(DATE_FMT)).append("</h2>");

        // 概览
        sb.append("<div style='display:flex;gap:16px;margin:16px 0'>");
        appendBadge(sb, "评论", comments.size(), "#4A90D9");
        appendBadge(sb, "树洞", treeComments.size(), "#7B68EE");
        appendBadge(sb, "友链申请", pendingLinks.size(), "#E67E22");
        sb.append("</div>");

        // 评论明细
        sb.append("<h3 style='color:#4A90D9'>新增评论（").append(comments.size()).append("）</h3>");
        if (comments.isEmpty()) {
            sb.append("<p style='color:#999'>无新增</p>");
        } else {
            for (CommentNotifyVO c : comments) {
                sb.append("<div style='padding:8px 0;border-bottom:1px solid #eee'>");
                sb.append("<span style='color:#999;font-size:12px'>").append(c.getCreateTime().format(TIME_FMT)).append("</span> ");
                sb.append("<b>").append(escapeHtml(c.getUsername())).append("</b>：");
                sb.append(escapeHtml(truncate(c.getContent())));
                if (c.getArticleTitle() != null) {
                    sb.append(" <span style='color:#888'>｜ 文章《").append(escapeHtml(c.getArticleTitle())).append("》</span>");
                }
                sb.append("</div>");
            }
        }

        // 树洞明细
        sb.append("<h3 style='color:#7B68EE'>新增树洞评论（").append(treeComments.size()).append("）</h3>");
        if (treeComments.isEmpty()) {
            sb.append("<p style='color:#999'>无新增</p>");
        } else {
            for (TreeComments t : treeComments) {
                sb.append("<div style='padding:8px 0;border-bottom:1px solid #eee'>");
                sb.append("<span style='color:#999;font-size:12px'>").append(t.getCreateTime().format(TIME_FMT)).append("</span> ");
                sb.append("<b>").append(escapeHtml(t.getUsername())).append("</b>：");
                sb.append(escapeHtml(truncate(t.getContent())));
                sb.append("</div>");
            }
        }

        // 友链明细
        sb.append("<h3 style='color:#E67E22'>待审核友链申请（").append(pendingLinks.size()).append("）</h3>");
        if (pendingLinks.isEmpty()) {
            sb.append("<p style='color:#999'>无新增</p>");
        } else {
            for (Link l : pendingLinks) {
                sb.append("<div style='padding:12px 0;border-bottom:1px solid #eee'>");
                sb.append("<span style='color:#999;font-size:12px'>").append(l.getCreateTime().format(TIME_FMT)).append("</span> ");
                sb.append("<b>").append(escapeHtml(l.getName())).append("</b>");
                sb.append(" / <a href='").append(escapeHtml(l.getUrl())).append("'>").append(escapeHtml(l.getUrl())).append("</a>");
                if (l.getDescription() != null) {
                    sb.append("<br><span style='color:#666'>").append(escapeHtml(truncate(l.getDescription()))).append("</span>");
                }
                String[] tokens = linkTokens.get(l.getId());
                if (tokens != null) {
                    String approveUrl = publicBaseUrl + "/api/notify/link-action?token=" + tokens[0];
                    String rejectUrl = publicBaseUrl + "/api/notify/link-action?token=" + tokens[1];
                    sb.append("<div style='margin-top:8px'>");
                    sb.append("<a href='").append(approveUrl).append("' style='display:inline-block;padding:6px 16px;background:#27ae60;color:#fff;text-decoration:none;border-radius:4px;margin-right:8px'>✅ 通过</a>");
                    sb.append("<a href='").append(rejectUrl).append("' style='display:inline-block;padding:6px 16px;background:#e74c3c;color:#fff;text-decoration:none;border-radius:4px'>❌ 拒绝</a>");
                    sb.append("</div>");
                }
                sb.append("</div>");
            }
        }

        // 页脚
        sb.append("<p style='color:#bbb;font-size:12px;margin-top:24px;text-align:center'>此邮件由系统自动发送，请勿回复</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private static void appendBadge(StringBuilder sb, String label, int count, String color) {
        sb.append("<div style='background:").append(color).append("1a;padding:8px 16px;border-radius:6px;text-align:center'>");
        sb.append("<div style='font-size:24px;font-weight:bold;color:").append(color).append("'>").append(count).append("</div>");
        sb.append("<div style='font-size:12px;color:#666'>").append(label).append("</div></div>");
    }

    private static String truncate(String text) {
        if (text == null) return "";
        return text.length() > NotifyConstants.CONTENT_SNIPPET_LENGTH
                ? text.substring(0, NotifyConstants.CONTENT_SNIPPET_LENGTH) + "…"
                : text;
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/notify/NotifyHtmlRenderer.java
git commit -m "feat(notify): add HTML email renderer with comment/tree/link sections"
```

---

## Task 9: DailyNotifyService — 数据收集 + 渲染 + 发送 + 重试

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/service/DailyNotifyService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/DailyNotifyServiceImpl.java`

- [ ] **Step 1: 创建 DailyNotifyService 接口**

```java
package top.hazenix.service;

import java.time.LocalDate;

public interface DailyNotifyService {
    void runForDate(LocalDate statDate);
    void sendTestMail();
}
```

- [ ] **Step 2: 创建 DailyNotifyServiceImpl**

```java
package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import top.hazenix.constant.NotifyConstants;
import top.hazenix.entity.Link;
import top.hazenix.entity.NotifyConfig;
import top.hazenix.entity.NotifyLog;
import top.hazenix.entity.TreeComments;
import top.hazenix.mapper.CommentsMapper;
import top.hazenix.mapper.LinkMapper;
import top.hazenix.mapper.NotifyLogMapper;
import top.hazenix.mapper.TreeCommentsMapper;
import top.hazenix.notify.BlogMailSender;
import top.hazenix.notify.NotifyHtmlRenderer;
import top.hazenix.service.DailyNotifyService;
import top.hazenix.service.NotifyActionService;
import top.hazenix.service.NotifyConfigService;
import top.hazenix.vo.CommentNotifyVO;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyNotifyServiceImpl implements DailyNotifyService {

    private final CommentsMapper commentsMapper;
    private final TreeCommentsMapper treeCommentsMapper;
    private final LinkMapper linkMapper;
    private final NotifyLogMapper notifyLogMapper;
    private final NotifyConfigService notifyConfigService;
    private final NotifyActionService notifyActionService;
    private final BlogMailSender blogMailSender;
    private final TaskScheduler taskScheduler;

    @Value("${blog.notify.public-base-url}")
    private String publicBaseUrl;

    @Override
    public void runForDate(LocalDate statDate) {
        NotifyConfig config = notifyConfigService.getRawConfig();
        if (config == null || config.getEnabled() == 0) {
            log.info("Notify disabled or not configured, skip");
            return;
        }

        LocalDateTime start = statDate.atStartOfDay();
        LocalDateTime end = statDate.plusDays(1).atStartOfDay();

        List<CommentNotifyVO> comments = commentsMapper.listByCreateTimeBetween(start, end);
        List<TreeComments> treeComments = treeCommentsMapper.listByCreateTimeBetween(start, end);
        List<Link> pendingLinks = linkMapper.listPendingByCreateTimeBetween(start, end);

        Map<Long, String[]> linkTokens = new HashMap<>();
        for (Link link : pendingLinks) {
            String approveToken = notifyActionService.issueToken(link.getId(), "link", "approve");
            String rejectToken = notifyActionService.issueToken(link.getId(), "link", "reject");
            linkTokens.put(link.getId(), new String[]{approveToken, rejectToken});
        }

        String subject = "【Hazenix Blog】每日通知 " + statDate;
        String html = NotifyHtmlRenderer.render(statDate, comments, treeComments, pendingLinks, linkTokens, publicBaseUrl);

        sendWithRetry(config.getRecipient(), subject, html, statDate, 0);
    }

    @Override
    public void sendTestMail() {
        NotifyConfig config = notifyConfigService.getRawConfig();
        if (config == null) {
            throw new RuntimeException("通知配置不存在");
        }
        String html = "<!DOCTYPE html><html><body><h2>Hazenix Blog 邮件测试</h2>"
                + "<p>如果你收到这封邮件，说明 SMTP 配置正确。</p>"
                + "<p style='color:#999'>发送时间：" + LocalDateTime.now() + "</p></body></html>";
        blogMailSender.send(config.getRecipient(), "【Hazenix Blog】测试邮件", html);
    }

    private void sendWithRetry(String recipient, String subject, String html, LocalDate statDate, int attempt) {
        try {
            blogMailSender.send(recipient, subject, html);
            notifyLogMapper.insert(NotifyLog.builder()
                    .statDate(statDate)
                    .sendTime(LocalDateTime.now())
                    .status(0)
                    .retryCount(attempt)
                    .recipient(recipient)
                    .build());
            log.info("Daily notify sent for {} attempt={}", statDate, attempt);
        } catch (Exception e) {
            log.error("Daily notify failed for {} attempt={}: {}", statDate, attempt, e.getMessage());
            if (attempt < NotifyConstants.RETRY_DELAY_MINUTES.length) {
                int delayMinutes = NotifyConstants.RETRY_DELAY_MINUTES[attempt];
                Instant retryAt = Instant.now().plusSeconds(delayMinutes * 60L);
                taskScheduler.schedule(
                        () -> sendWithRetry(recipient, subject, html, statDate, attempt + 1),
                        retryAt
                );
            } else {
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 2000) {
                    errorMsg = errorMsg.substring(0, 2000);
                }
                notifyLogMapper.insert(NotifyLog.builder()
                        .statDate(statDate)
                        .sendTime(LocalDateTime.now())
                        .status(1)
                        .retryCount(attempt)
                        .errorMsg(errorMsg)
                        .recipient(recipient)
                        .build());
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/service/DailyNotifyService.java backend/blog-server/src/main/java/top/hazenix/service/impl/DailyNotifyServiceImpl.java
git commit -m "feat(notify): add DailyNotifyService with data collection, rendering, and retry"
```

---

## Task 10: NotifyScheduleManager — 动态调度

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/task/NotifyScheduleManager.java`

- [ ] **Step 1: 创建 NotifyScheduleManager**

```java
package top.hazenix.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import top.hazenix.constant.NotifyConstants;
import top.hazenix.entity.NotifyConfig;
import top.hazenix.service.DailyNotifyService;
import top.hazenix.service.NotifyConfigService;

import javax.annotation.PostConstruct;
import java.time.*;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyScheduleManager {

    private final NotifyConfigService notifyConfigService;
    private final DailyNotifyService dailyNotifyService;
    private final TaskScheduler taskScheduler;

    private volatile ScheduledFuture<?> currentFuture;

    @Configuration
    static class SchedulerConfig {
        @Bean
        public TaskScheduler notifyTaskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(2);
            scheduler.setThreadNamePrefix("notify-");
            scheduler.initialize();
            return scheduler;
        }
    }

    @PostConstruct
    public void init() {
        reschedule();
    }

    public synchronized void reschedule() {
        if (currentFuture != null) {
            currentFuture.cancel(false);
            currentFuture = null;
        }
        NotifyConfig config = notifyConfigService.getRawConfig();
        if (config == null || config.getEnabled() == 0) {
            log.info("Notify scheduling disabled");
            return;
        }
        scheduleNext(config.getSendTime());
    }

    private void scheduleNext(String sendTime) {
        String[] parts = sendTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        LocalDateTime nextRun = LocalDate.now().atTime(hour, minute);
        if (!nextRun.isAfter(LocalDateTime.now())) {
            nextRun = nextRun.plusDays(1);
        }

        Instant runInstant = nextRun.atZone(ZoneId.systemDefault()).toInstant();
        currentFuture = taskScheduler.schedule(() -> {
            try {
                LocalDate yesterday = LocalDate.now().minusDays(1);
                dailyNotifyService.runForDate(yesterday);
            } catch (Exception e) {
                log.error("Notify task execution error", e);
            } finally {
                NotifyConfig latest = notifyConfigService.getRawConfig();
                if (latest != null && latest.getEnabled() == 1) {
                    scheduleNext(latest.getSendTime());
                }
            }
        }, runInstant);

        log.info("Next notify scheduled at {}", nextRun);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/task/NotifyScheduleManager.java
git commit -m "feat(notify): add dynamic NotifyScheduleManager with reschedule support"
```

---

## Task 11: Controller 层 — 后台配置 + 公开审核接口

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/admin/NotifyConfigController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/web/NotifyActionController.java`
- Modify: `backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java`

- [ ] **Step 1: 创建 NotifyConfigController**

```java
package top.hazenix.controller.admin;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import top.hazenix.dto.NotifyConfigDTO;
import top.hazenix.entity.NotifyLog;
import top.hazenix.mapper.NotifyLogMapper;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.DailyNotifyService;
import top.hazenix.service.NotifyConfigService;
import top.hazenix.task.NotifyScheduleManager;
import top.hazenix.vo.NotifyConfigVO;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@Slf4j
@RequestMapping("/admin/notify")
@RequiredArgsConstructor
public class NotifyConfigController {

    private final NotifyConfigService notifyConfigService;
    private final DailyNotifyService dailyNotifyService;
    private final NotifyScheduleManager notifyScheduleManager;
    private final NotifyLogMapper notifyLogMapper;

    @GetMapping("/config")
    public Result<NotifyConfigVO> getConfig() {
        return Result.success(notifyConfigService.getConfig());
    }

    @PutMapping("/config")
    public Result<Void> updateConfig(@Valid @RequestBody NotifyConfigDTO dto) {
        notifyConfigService.updateConfig(dto);
        notifyScheduleManager.reschedule();
        return Result.success();
    }

    @PostMapping("/test")
    public Result<Void> testSend() {
        dailyNotifyService.sendTestMail();
        return Result.success();
    }

    @PostMapping("/trigger")
    public Result<Void> trigger(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        dailyNotifyService.runForDate(date);
        return Result.success();
    }

    @GetMapping("/log")
    public Result<PageResult> getLogs(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "20") Integer size) {
        PageHelper.startPage(page, size);
        Page<NotifyLog> logs = notifyLogMapper.pageQuery();
        return Result.success(new PageResult(logs.getTotal(), logs.getResult()));
    }
}
```

- [ ] **Step 2: 创建 NotifyActionController**

```java
package top.hazenix.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hazenix.service.NotifyActionService;

@RestController
@RequiredArgsConstructor
public class NotifyActionController {

    private final NotifyActionService notifyActionService;

    @GetMapping(value = "/api/notify/link-action", produces = MediaType.TEXT_HTML_VALUE)
    public String linkAction(@RequestParam String token) {
        return notifyActionService.consumeToken(token);
    }
}
```

- [ ] **Step 3: SecurityConfig 放行一键审核路径**

在 `SecurityConfig.java` 的 `.antMatchers("/doc.html", ...)` 放行块中追加 `/api/notify/link-action`：

```java
                .antMatchers(
                        "/doc.html",
                        "/webjars/**",
                        "/v2/api-docs",
                        "/swagger-resources/**",
                        "/swagger-ui.html",
                        "user/tree/list",
                        "/api/notify/link-action"
                ).permitAll()
```

- [ ] **Step 4: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/controller/admin/NotifyConfigController.java backend/blog-server/src/main/java/top/hazenix/controller/web/NotifyActionController.java backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java
git commit -m "feat(notify): add admin config controller, public link-action endpoint, and security config"
```

---

## Task 12: 编译验证 + 手动冒烟测试

**Files:** (no new files)

- [ ] **Step 1: Maven 编译**

```bash
cd backend && mvn clean compile -q
```

预期：BUILD SUCCESS，无编译错误。

- [ ] **Step 2: 启动应用**

```bash
cd backend && mvn spring-boot:run -pl blog-server
```

预期：启动日志中出现 `Notify scheduling disabled`（因为初始 `enabled=0`）或 `Next notify scheduled at ...`。

- [ ] **Step 3: 测试后台接口 — 获取配置**

```bash
curl -s http://localhost:9090/admin/notify/config -H "token: <admin-jwt>" | python -m json.tool
```

预期：返回 `smtpPassword: "********"`。

- [ ] **Step 4: 测试后台接口 — 更新配置**

用实际 SMTP 信息更新配置（以 QQ 邮箱为例）：

```bash
curl -X PUT http://localhost:9090/admin/notify/config \
  -H "Content-Type: application/json" \
  -H "token: <admin-jwt>" \
  -d '{"recipient":"your@email.com","smtpHost":"smtp.qq.com","smtpPort":465,"smtpUsername":"your@qq.com","smtpPassword":"your-auth-code","smtpSsl":1,"sendTime":"08:00","enabled":1}'
```

预期：200，日志出现 `Next notify scheduled at ...`。

- [ ] **Step 5: 测试发送**

```bash
curl -X POST http://localhost:9090/admin/notify/test -H "token: <admin-jwt>"
```

预期：收到测试邮件。

- [ ] **Step 6: 手动触发某日通知**

```bash
curl -X POST "http://localhost:9090/admin/notify/trigger?date=2026-04-29" -H "token: <admin-jwt>"
```

预期：收到包含昨日数据的完整通知邮件。

- [ ] **Step 7: 测试一键审核链接**

从邮件中复制友链审核链接，在浏览器中打开。

预期：显示「审核通过」或「已拒绝」页面；再次点击同一链接显示「链接无效或已失效」。

- [ ] **Step 8: 查看发送日志**

```bash
curl -s "http://localhost:9090/admin/notify/log?page=1&size=10" -H "token: <admin-jwt>" | python -m json.tool
```

预期：返回发送记录列表。

- [ ] **Step 9: Final commit**

```bash
git add -A
git commit -m "feat(notify): complete daily notification email with one-click link approval"
```
