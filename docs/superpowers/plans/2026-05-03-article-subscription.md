# 文章订阅 + 首页三按钮 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 首页底部三按钮（喜欢/订阅/催更）+ 文章订阅邮件系统（邮箱直订、无确认、发布触发）+ 后台管理面板

**Architecture:** 后端 MySQL 存储订阅/催更/喜欢数据，SMTP 发送订阅邮件；前端三按钮组件独立；文章发布时异步触发批量邮件。

**Tech Stack:** Spring Boot, MyBatis, JavaMailSender, Vue3 (Element Plus dialog)

**Spec:** [2026-05-03-article-subscription-design.md](../specs/2026-05-03-article-subscription-design.md)

---

## File Map

### SQL
- Create: `documents/sql/article_subscription.sql`

### blog-pojo 实体
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleSubscription.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleUrge.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/SiteLike.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleNotifyLog.java`

### blog-server Mapper
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleSubscriptionMapper.java`
- Create: `backend/blog-server/src/main/resources/mapper/ArticleSubscriptionMapper.xml`
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleUrgeMapper.java`
- Create: `backend/blog-server/src/main/resources/mapper/ArticleUrgeMapper.xml`
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/SiteLikeMapper.java`
- Create: `backend/blog-server/src/main/resources/mapper/SiteLikeMapper.xml`

### blog-server Service
- Create: `backend/blog-server/src/main/java/top/hazenix/service/ArticleSubscriptionService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleSubscriptionServiceImpl.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/ArticleUrgeService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleUrgeServiceImpl.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/SiteLikeService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/SiteLikeServiceImpl.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/ArticleNotifyService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleNotifyServiceImpl.java`

### blog-server Controller
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleSubscriptionController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleUrgeController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/user/SiteLikeController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/web/UnsubscribeController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/admin/SubscriptionAdminController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/admin/UrgeAdminController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/admin/SiteLikeAdminController.java`
- Modify: `backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java` (放行 `/api/unsubscribe`)
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleServiceImpl.java` (发布时触发)

### blog-server notify
- Create: `backend/blog-server/src/main/java/top/hazenix/notify/ArticleMailRenderer.java`

### frontend
- Create: `frontend/src/components/common/SiteActionButtons.vue`
- Modify: `frontend/src/views/Home.vue` (加 `<SiteActionButtons />`)
- Modify: `frontend/src/api/frontend.js` (加 subscribeArticle, urgeArticle, likeSite)

---

## Task 1: SQL + 实体类

**Files:**
- Create: `documents/sql/article_subscription.sql`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleSubscription.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleUrge.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/SiteLike.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleNotifyLog.java`

### Step 1: 创建 SQL 脚本

```sql
CREATE TABLE IF NOT EXISTS article_subscription (
  id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
  email             VARCHAR(255) NOT NULL UNIQUE,
  unsubscribe_token VARCHAR(64)  NOT NULL UNIQUE,
  status            TINYINT      NOT NULL DEFAULT 1 COMMENT '1=已激活 2=已退订',
  subscribe_at      DATETIME     NOT NULL,
  unsubscribe_at    DATETIME,
  KEY idx_email (email),
  KEY idx_unsubscribe_token (unsubscribe_token)
);

CREATE TABLE IF NOT EXISTS article_urge (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  urge_month VARCHAR(7)  NOT NULL COMMENT 'YYYY-MM',
  count      INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_month (urge_month)
);

INSERT IGNORE INTO article_urge (urge_month, count)
VALUES (DATE_FORMAT(CURDATE(), '%Y-%m'), 0);

CREATE TABLE IF NOT EXISTS site_like (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  ip_hash    VARCHAR(64)  NOT NULL COMMENT 'IP 哈希值，防刷量',
  created_at DATETIME     NOT NULL,
  UNIQUE KEY uk_ip (ip_hash)
);

CREATE TABLE IF NOT EXISTS article_notify_log (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  article_id    BIGINT       NOT NULL,
  send_time     DATETIME     NOT NULL,
  success_count INT          NOT NULL DEFAULT 0,
  fail_count    INT          NOT NULL DEFAULT 0,
  KEY idx_article (article_id)
);
```

### Step 2: ArticleSubscription 实体

```java
package top.hazenix.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ArticleSubscription {
    private Long id;
    private String email;
    private String unsubscribeToken;
    private Integer status;  // 1=已激活 2=已退订
    private LocalDateTime subscribeAt;
    private LocalDateTime unsubscribeAt;
}
```

### Step 3: ArticleUrge 实体

```java
package top.hazenix.entity;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ArticleUrge {
    private Long id;
    private String urgeMonth;  // YYYY-MM
    private Integer count;
}
```

### Step 4: SiteLike 实体

```java
package top.hazenix.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SiteLike {
    private Long id;
    private String ipHash;
    private LocalDateTime createdAt;
}
```

### Step 5: ArticleNotifyLog 实体

```java
package top.hazenix.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ArticleNotifyLog {
    private Long id;
    private Long articleId;
    private LocalDateTime sendTime;
    private Integer successCount;
    private Integer failCount;
}
```

### Step 6: Commit

```bash
git add documents/sql/article_subscription.sql backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleSubscription.java backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleUrge.java backend/blog-pojo/src/main/java/top/hazenix/entity/SiteLike.java backend/blog-pojo/src/main/java/top/hazenix/entity/ArticleNotifyLog.java
git commit -m "feat(subscription): add SQL schema and entity classes for article subscription"
```

---

## Task 2: Mapper 层

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleSubscriptionMapper.java`
- Create: `backend/blog-server/src/main/resources/mapper/ArticleSubscriptionMapper.xml`
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleUrgeMapper.java`
- Create: `backend/blog-server/src/main/resources/mapper/ArticleUrgeMapper.xml`
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/SiteLikeMapper.java`
- Create: `backend/blog-server/src/main/resources/mapper/SiteLikeMapper.xml`

### Step 1: ArticleSubscriptionMapper

```java
package top.hazenix.mapper;

import org.apache.ibatis.annotations.*;
import top.hazenix.entity.ArticleSubscription;
import java.util.List;

@Mapper
public interface ArticleSubscriptionMapper {
    void insert(ArticleSubscription sub);
    ArticleSubscription getByEmail(@Param("email") String email);
    List<ArticleSubscription> listActive();
    void updateStatus(@Param("email") String email, @Param("status") Integer status);
}
```

XML:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="top.hazenix.mapper.ArticleSubscriptionMapper">
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO article_subscription (email, unsubscribe_token, status, subscribe_at)
        VALUES (#{email}, #{unsubscribeToken}, #{status}, #{subscribeAt})
    </insert>
    <select id="getByEmail" resultType="top.hazenix.entity.ArticleSubscription">
        SELECT * FROM article_subscription WHERE email = #{email}
    </select>
    <select id="listActive" resultType="top.hazenix.entity.ArticleSubscription">
        SELECT * FROM article_subscription WHERE status = 1
    </select>
    <update id="updateStatus">
        UPDATE article_subscription SET status = #{status} WHERE email = #{email}
    </update>
</mapper>
```

### Step 2: ArticleUrgeMapper

```java
package top.hazenix.mapper;

import org.apache.ibatis.annotations.*;
import top.hazenix.entity.ArticleUrge;

@Mapper
public interface ArticleUrgeMapper {
    ArticleUrge getByMonth(@Param("month") String month);
    void insert(ArticleUrge urge);
    void incrementCount(@Param("month") String month);
    java.util.List<ArticleUrge> listAll();
}
```

XML:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="top.hazenix.mapper.ArticleUrgeMapper">
    <select id="getByMonth" resultType="top.hazenix.entity.ArticleUrge">
        SELECT * FROM article_urge WHERE urge_month = #{month}
    </select>
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO article_urge (urge_month, count) VALUES (#{urgeMonth}, #{count})
    </insert>
    <update id="incrementCount">
        UPDATE article_urge SET count = count + 1 WHERE urge_month = #{month}
    </update>
    <select id="listAll" resultType="top.hazenix.entity.ArticleUrge">
        SELECT * FROM article_urge ORDER BY urge_month DESC
    </select>
</mapper>
```

### Step 3: SiteLikeMapper

```java
package top.hazenix.mapper;

import org.apache.ibatis.annotations.*;
import top.hazenix.entity.SiteLike;
import java.util.List;

@Mapper
public interface SiteLikeMapper {
    void insert(SiteLike like);
    SiteLike getByIpHash(@Param("ipHash") String ipHash);
    Long countAll();
    Long countToday();
    List<SiteLike> listRecent(@Param("limit") Integer limit);
}
```

XML:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="top.hazenix.mapper.SiteLikeMapper">
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO site_like (ip_hash, created_at) VALUES (#{ipHash}, #{createdAt})
    </insert>
    <select id="getByIpHash" resultType="top.hazenix.entity.SiteLike">
        SELECT * FROM site_like WHERE ip_hash = #{ipHash}
    </select>
    <select id="countAll" resultType="java.lang.Long">
        SELECT COUNT(*) FROM site_like
    </select>
    <select id="countToday" resultType="java.lang.Long">
        SELECT COUNT(*) FROM site_like WHERE DATE(created_at) = CURDATE()
    </select>
    <select id="listRecent" resultType="top.hazenix.entity.SiteLike">
        SELECT * FROM site_like ORDER BY created_at DESC LIMIT #{limit}
    </select>
</mapper>
```

### Step 4: Commit

```bash
git add backend/blog-server/src/main/java/top/hazenix/mapper/ArticleSubscriptionMapper.java backend/blog-server/src/main/resources/mapper/ArticleSubscriptionMapper.xml backend/blog-server/src/main/java/top/hazenix/mapper/ArticleUrgeMapper.java backend/blog-server/src/main/resources/mapper/ArticleUrgeMapper.xml backend/blog-server/src/main/java/top/hazenix/mapper/SiteLikeMapper.java backend/blog-server/src/main/resources/mapper/SiteLikeMapper.xml
git commit -m "feat(subscription): add mappers for subscription, urge, and site-like"
```

---

## Task 3: Service 层 — 订阅 + 催更 + 喜欢

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/service/ArticleSubscriptionService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleSubscriptionServiceImpl.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/ArticleUrgeService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleUrgeServiceImpl.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/SiteLikeService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/SiteLikeServiceImpl.java`

### Step 1: ArticleSubscriptionService 接口

```java
package top.hazenix.service;

public interface ArticleSubscriptionService {
    void subscribe(String email);
    void unsubscribe(String token);
    boolean isSubscribed(String email);
}
```

### Step 2: ArticleSubscriptionServiceImpl

```java
package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.hazenix.entity.ArticleSubscription;
import top.hazenix.mapper.ArticleSubscriptionMapper;
import top.hazenix.service.ArticleSubscriptionService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ArticleSubscriptionServiceImpl implements ArticleSubscriptionService {

    private final ArticleSubscriptionMapper mapper;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void subscribe(String email) {
        ArticleSubscription existing = mapper.getByEmail(email);
        if (existing != null && existing.getStatus() == 1) {
            throw new RuntimeException("该邮箱已订阅");
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        ArticleSubscription sub = ArticleSubscription.builder()
                .email(email)
                .unsubscribeToken(token)
                .status(1)
                .subscribeAt(LocalDateTime.now())
                .build();
        mapper.insert(sub);
    }

    @Override
    public void unsubscribe(String token) {
        // 根据 token 找 email 再退订
        // unsubscribeToken 不在 mapper 里，需要查全部遍历，或加一个 getByUnsubscribeToken 方法
        // 用现有 mapper 方法：查 listActive 后匹配 token（订阅量小时可用）
        throw new UnsupportedOperationException("实现时需加 getByUnsubscribeToken 到 mapper");
    }

    @Override
    public boolean isSubscribed(String email) {
        ArticleSubscription sub = mapper.getByEmail(email);
        return sub != null && sub.getStatus() == 1;
    }
}
```

> **注意：** ArticleSubscriptionMapper 需要加 `getByUnsubscribeToken` 方法，XML 里加对应 select，insert 时记录 unsubscribe_token。

### Step 3: ArticleUrgeService 接口

```java
package top.hazenix.service;

public interface ArticleUrgeService {
    int urgeAndGetCount();
    java.util.List<top.hazenix.entity.ArticleUrge> getStats();
}
```

### Step 4: ArticleUrgeServiceImpl

```java
package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.hazenix.entity.ArticleUrge;
import top.hazenix.mapper.ArticleUrgeMapper;
import top.hazenix.service.ArticleUrgeService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleUrgeServiceImpl implements ArticleUrgeService {

    private final ArticleUrgeMapper mapper;
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public int urgeAndGetCount() {
        String month = LocalDate.now().format(MONTH_FMT);
        ArticleUrge record = mapper.getByMonth(month);
        if (record == null) {
            mapper.insert(ArticleUrge.builder().urgeMonth(month).count(1).build());
            return 1;
        }
        mapper.incrementCount(month);
        return record.getCount() + 1;
    }

    @Override
    public List<ArticleUrge> getStats() {
        return mapper.listAll();
    }
}
```

### Step 5: SiteLikeService 接口

```java
package top.hazenix.service;

public interface SiteLikeService {
    long likeAndGetCount(String ipHash);
    long getTotalCount();
    long getTodayCount();
}
```

### Step 6: SiteLikeServiceImpl

```java
package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.hazenix.entity.SiteLike;
import top.hazenix.mapper.SiteLikeMapper;
import top.hazenix.service.SiteLikeService;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SiteLikeServiceImpl implements SiteLikeService {

    private final SiteLikeMapper mapper;

    @Override
    public long likeAndGetCount(String ipHash) {
        SiteLike existing = mapper.getByIpHash(ipHash);
        if (existing != null) {
            throw new RuntimeException("已喜欢过");
        }
        mapper.insert(SiteLike.builder().ipHash(ipHash).createdAt(LocalDateTime.now()).build());
        return mapper.countAll();
    }

    @Override
    public long getTotalCount() {
        return mapper.countAll();
    }

    @Override
    public long getTodayCount() {
        return mapper.countToday();
    }
}
```

### Step 7: Commit（注意 Mapper 方法补充）

先在 ArticleSubscriptionMapper 加 `getByUnsubscribeToken` 方法和 XML 对应 select，然后提交。

```bash
git add backend/blog-server/src/main/java/top/hazenix/service/ArticleSubscriptionService.java backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleSubscriptionServiceImpl.java backend/blog-server/src/main/java/top/hazenix/service/ArticleUrgeService.java backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleUrgeServiceImpl.java backend/blog-server/src/main/java/top/hazenix/service/SiteLikeService.java backend/blog-server/src/main/java/top/hazenix/service/impl/SiteLikeServiceImpl.java backend/blog-server/src/main/java/top/hazenix/mapper/ArticleSubscriptionMapper.java backend/blog-server/src/main/resources/mapper/ArticleSubscriptionMapper.xml
git commit -m "feat(subscription): add services for subscription, urge, and site-like"
```

---

## Task 4: Controller 层 — 用户端 + 公开接口

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleSubscriptionController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleUrgeController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/user/SiteLikeController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/web/UnsubscribeController.java`
- Modify: `backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java`

### Step 1: ArticleSubscriptionController

```java
package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleSubscriptionService;

@RestController
@RequestMapping("/user/subscription")
@RequiredArgsConstructor
public class ArticleSubscriptionController {

    private final ArticleSubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public Result<Void> subscribe(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        try {
            subscriptionService.subscribe(email);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error("400", e.getMessage());
        }
    }
}
```

### Step 2: ArticleUrgeController

```java
package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleUrgeService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ArticleUrgeController {

    private final ArticleUrgeService urgeService;

    @PostMapping("/urge")
    public Result<java.util.Map<String, Integer>> urge() {
        int count = urgeService.urgeAndGetCount();
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        result.put("currentCount", count);
        return Result.success(result);
    }
}
```

### Step 3: SiteLikeController

```java
package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.result.Result;
import top.hazenix.service.SiteLikeService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SiteLikeController {

    private final SiteLikeService siteLikeService;

    @PostMapping("/site-like")
    public Result<java.util.Map<String, Long>> like(HttpServletRequest request) {
        String ip = getClientIp(request);
        try {
            long total = siteLikeService.likeAndGetCount(ip);
            java.util.Map<String, Long> result = new java.util.HashMap<>();
            result.put("totalCount", total);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error("409", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        // 简单哈希
        return String.valueOf(ip.hashCode());
    }
}
```

### Step 4: UnsubscribeController

```java
package top.hazenix.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import top.hazenix.service.ArticleSubscriptionService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UnsubscribeController {

    private final ArticleSubscriptionService subscriptionService;

    @GetMapping(value = "/unsubscribe", produces = MediaType.TEXT_HTML_VALUE)
    public String unsubscribe(@RequestParam String token) {
        try {
            subscriptionService.unsubscribe(token);
        } catch (Exception e) {
            return "<html><body><h2>退订失败</h2><p>链接无效或已过期。</p></body></html>";
        }
        return "<html><body><h2>退订成功</h2><p>已取消文章订阅通知。</p></body></html>";
    }
}
```

### Step 5: SecurityConfig — 放行 unsubscribe

在 permitAll block 里加 `"/api/unsubscribe"`。

### Step 6: Commit

```bash
git add backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleSubscriptionController.java backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleUrgeController.java backend/blog-server/src/main/java/top/hazenix/controller/user/SiteLikeController.java backend/blog-server/src/main/java/top/hazenix/controller/web/UnsubscribeController.java backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java
git commit -m "feat(subscription): add user controllers and public unsubscribe endpoint"
```

---

## Task 5: 文章发布触发 + 订阅邮件发送

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/service/ArticleNotifyService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleNotifyServiceImpl.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/notify/ArticleMailRenderer.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleNotifyLogMapper.java`
- Create: `backend/blog-server/src/main/resources/mapper/ArticleNotifyLogMapper.xml`
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleServiceImpl.java`

### Step 1: ArticleNotifyService 接口

```java
package top.hazenix.service;

import top.hazenix.entity.Article;

public interface ArticleNotifyService {
    void notifySubscribers(Article article);
}
```

### Step 2: ArticleMailRenderer

```java
package top.hazenix.notify;

public class ArticleMailRenderer {
    private static final int SUMMARY_LENGTH = 120;

    private ArticleMailRenderer() {}

    public static String render(String title, String summary, Long articleId, String unsubscribeToken) {
        String articleUrl = "https://blog.hazenix.top/article/" + articleId;
        String unsubscribeUrl = "https://blog.hazenix.top/api/unsubscribe?token=" + unsubscribeToken;
        String truncated = summary != null && summary.length() > SUMMARY_LENGTH
                ? summary.substring(0, SUMMARY_LENGTH) + "…" : (summary != null ? summary : "");
        return "<!DOCTYPE html><html><body style='font-family:sans-serif;max-width:600px;margin:0 auto;padding:20px'>"
                + "<h2 style='color:#333'>《" + escapeHtml(title) + "》</h2>"
                + "<p style='color:#555;font-size:16px'>" + escapeHtml(truncated) + "</p>"
                + "<p><a href='" + articleUrl + "' style='display:inline-block;padding:10px 24px;background:#4A90D9;color:#fff;text-decoration:none;border-radius:4px'>阅读全文 →</a></p>"
                + "<hr style='margin:24px 0;border:none;border-top:1px solid #eee'>"
                + "<p style='font-size:12px;color:#999'>"
                + "<a href='https://blog.hazenix.top/feed' style='color:#4A90D9'>RSS 订阅</a> | "
                + "<a href='" + unsubscribeUrl + "' style='color:#999'>退订通知</a>"
                + "</p></body></html>";
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
```

### Step 3: ArticleNotifyServiceImpl

```java
package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hazenix.entity.Article;
import top.hazenix.entity.ArticleNotifyLog;
import top.hazenix.entity.ArticleSubscription;
import top.hazenix.mapper.ArticleNotifyLogMapper;
import top.hazenix.mapper.ArticleSubscriptionMapper;
import top.hazenix.notify.ArticleMailRenderer;
import top.hazenix.notify.BlogMailSender;
import top.hazenix.service.ArticleNotifyService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleNotifyServiceImpl implements ArticleNotifyService {

    private final ArticleSubscriptionMapper subscriptionMapper;
    private final ArticleNotifyLogMapper notifyLogMapper;
    private final BlogMailSender mailSender;

    @Override
    @Async
    public void notifySubscribers(Article article) {
        var subscribers = subscriptionMapper.listActive();
        if (subscribers.isEmpty()) return;

        String subject = "【Hazenix Blog】新文章：" + article.getTitle();
        int success = 0, fail = 0;
        for (ArticleSubscription sub : subscribers) {
            try {
                String html = ArticleMailRenderer.render(
                        article.getTitle(),
                        article.getContent(),
                        article.getId(),
                        sub.getUnsubscribeToken()
                );
                mailSender.send(sub.getEmail(), subject, html);
                success++;
            } catch (Exception e) {
                fail++;
                log.error("Notify subscriber {} failed: {}", sub.getEmail(), e.getMessage());
            }
        }
        notifyLogMapper.insert(ArticleNotifyLog.builder()
                .articleId(article.getId())
                .sendTime(LocalDateTime.now())
                .successCount(success)
                .failCount(fail)
                .build());
        log.info("Article {} notified: success={} fail={}", article.getId(), success, fail);
    }
}
```

### Step 4: ArticleServiceImpl — 发布时触发

Read `ArticleServiceImpl.java` first. Find the method that handles article creation or status update to published (status 2 → 0). Inject `ArticleNotifyService` and call `notifySubscribers(article)` after successful publish.

### Step 5: Commit

```bash
git add backend/blog-server/src/main/java/top/hazenix/service/ArticleNotifyService.java backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleNotifyServiceImpl.java backend/blog-server/src/main/java/top/hazenix/notify/ArticleMailRenderer.java backend/blog-server/src/main/java/top/hazenix/mapper/ArticleNotifyLogMapper.java backend/blog-server/src/main/resources/mapper/ArticleNotifyLogMapper.xml backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleServiceImpl.java
git commit -m "feat(subscription): add article notify service with async email dispatch"
```

---

## Task 6: 后台管理 Controller

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/admin/SubscriptionAdminController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/admin/UrgeAdminController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/admin/SiteLikeAdminController.java`

### Step 1: SubscriptionAdminController

```java
package top.hazenix.controller.admin;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.entity.ArticleSubscription;
import top.hazenix.mapper.ArticleSubscriptionMapper;
import top.hazenix.result.PageResult;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleSubscriptionService;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/subscription")
@RequiredArgsConstructor
public class SubscriptionAdminController {

    private final ArticleSubscriptionMapper mapper;
    private final ArticleSubscriptionService service;

    @GetMapping("/list")
    public Result<PageResult> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "20") Integer size) {
        PageHelper.startPage(page, size);
        Page<ArticleSubscription> p = mapper.pageQuery();
        return Result.success(new PageResult(p.getTotal(), p.getResult()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 根据 id 查 email，再退订
        // 需要加 mapper.getById 或在 service 层处理
        return Result.success();
    }

    @GetMapping("/export")
    public Result<java.util.List<String>> export() {
        var subs = mapper.listActive();
        var emails = subs.stream().map(ArticleSubscription::getEmail).collect(Collectors.toList());
        return Result.success(emails);
    }
}
```

> **注意：** `ArticleSubscriptionMapper` 需要加 `pageQuery()` 方法（XML）和 `getById(id)` 方法。

### Step 2: UrgeAdminController

```java
package top.hazenix.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.entity.ArticleUrge;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleUrgeService;

import java.util.List;

@RestController
@RequestMapping("/admin/urge")
@RequiredArgsConstructor
public class UrgeAdminController {

    private final ArticleUrgeService urgeService;

    @GetMapping("/stats")
    public Result<List<ArticleUrge>> stats() {
        return Result.success(urgeService.getStats());
    }
}
```

### Step 3: SiteLikeAdminController

```java
package top.hazenix.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.hazenix.result.Result;
import top.hazenix.service.SiteLikeService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/site-like")
@RequiredArgsConstructor
public class SiteLikeAdminController {

    private final SiteLikeService siteLikeService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", siteLikeService.getTotalCount());
        result.put("todayCount", siteLikeService.getTodayCount());
        return Result.success(result);
    }
}
```

### Step 4: Commit

```bash
git add backend/blog-server/src/main/java/top/hazenix/controller/admin/SubscriptionAdminController.java backend/blog-server/src/main/java/top/hazenix/controller/admin/UrgeAdminController.java backend/blog-server/src/main/java/top/hazenix/controller/admin/SiteLikeAdminController.java
git commit -m "feat(subscription): add admin controllers for subscription, urge, and site-like stats"
```

---

## Task 7: 前端 — 三按钮组件 + API

**Files:**
- Create: `frontend/src/components/common/SiteActionButtons.vue`
- Modify: `frontend/src/views/Home.vue`
- Modify: `frontend/src/api/frontend.js`

### Step 1: 前端 API

在 `frontend/src/api/frontend.js` 末尾加：

```js
subscribeArticle(data) {
  return request.post('/user/subscription/subscribe', data)
},
urgeArticle() {
  return request.post('/user/urge')
},
likeSite() {
  return request.post('/user/site-like')
}
```

### Step 2: SiteActionButtons.vue

```vue
<template>
  <div class="flex gap-4 w-full mt-8">
    <!-- 喜欢本站 -->
    <button
      class="flex-1 h-16 rounded-full flex items-center justify-center gap-2 transition-all"
      :class="liked
        ? 'bg-gray-200 dark:bg-gray-700 cursor-not-allowed opacity-60'
        : 'bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700'"
      :disabled="liked"
      @click="handleLike"
    >
      <span class="text-lg">❤️</span>
      <div class="flex flex-col items-start">
        <span class="text-sm font-bold" :class="liked ? 'text-gray-500' : 'text-gray-700 dark:text-gray-200'">
          {{ liked ? '已喜欢' : '喜欢本站' }}
        </span>
        <span class="text-xs text-gray-400">{{ likeCount }} 人</span>
      </div>
    </button>

    <!-- 订阅文章 -->
    <button
      class="flex-1 h-16 rounded-full bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 flex items-center justify-center gap-2 transition-all"
      @click="showSubscribeDialog = true"
    >
      <span class="text-lg">✉️</span>
      <div class="flex flex-col items-start">
        <span class="text-sm font-bold text-gray-700 dark:text-gray-200">订阅文章</span>
        <span class="text-xs text-gray-400">实时推送</span>
      </div>
    </button>

    <!-- 催更 -->
    <button
      class="flex-1 h-16 rounded-full bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 flex items-center justify-center gap-2 transition-all"
      @click="handleUrge"
    >
      <span class="text-lg">⚡</span>
      <div class="flex flex-col items-start">
        <span class="text-sm font-bold text-gray-700 dark:text-gray-200">催更</span>
        <span class="text-xs text-gray-400">本月 {{ urgeCount }} 人催更</span>
      </div>
    </button>

    <!-- 订阅弹窗 -->
    <el-dialog v-model="showSubscribeDialog" title="订阅文章更新" width="400px">
      <div class="space-y-4">
        <p class="text-sm text-gray-500">留下邮箱，新文章发布第一时间通知你</p>
        <el-input v-model="subscribeEmail" placeholder="your@email.com" />
        <el-button type="primary" class="w-full" @click="handleSubscribe" :loading="subscribing">
          订阅
        </el-button>
        <div class="flex items-center gap-2 text-sm text-gray-400">
          <span>— 或 —</span>
        </div>
        <div class="flex items-center gap-2">
          <el-input :value="feedUrl" readonly class="text-xs" />
          <el-button size="small" @click="copyFeed">复制</el-button>
        </div>
        <p class="text-xs text-gray-400">
          <a :href="feedUrl" target="_blank" class="text-primary hover:underline">打开 /feed</a>
        </p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { frontendApi } from '@/api/frontend'

const liked = ref(localStorage.getItem('site_liked') === '1')
const likeCount = ref(0)
const urgeCount = ref(0)
const showSubscribeDialog = ref(false)
const subscribeEmail = ref('')
const subscribing = ref(false)
const feedUrl = ref('https://blog.hazenix.top/feed')

const handleLike = async () => {
  if (liked.value) { ElMessage.warning('你已喜欢过啦！'); return }
  try {
    const res = await frontendApi.likeSite()
    liked.value = true
    localStorage.setItem('site_liked', '1')
    likeCount.value = res.data?.totalCount || likeCount.value + 1
    ElMessage.success('感谢你的支持！')
  } catch (e) {
    if (e.response?.data?.code === '409') ElMessage.warning('你已喜欢过啦！')
    else ElMessage.error('操作失败')
  }
}

const handleSubscribe = async () => {
  if (!subscribeEmail.value) { ElMessage.warning('请输入邮箱'); return }
  try {
    await frontendApi.subscribeArticle({ email: subscribeEmail.value })
    ElMessage.success('订阅成功！')
    showSubscribeDialog.value = false
    subscribeEmail.value = ''
  } catch (e) {
    if (e.response?.data?.code === '400') ElMessage.error('邮箱格式错误')
    else if (e.response?.data?.code === '409') ElMessage.warning('该邮箱已订阅')
    else ElMessage.error('订阅失败')
  }
}

const handleUrge = async () => {
  try {
    const res = await frontendApi.urgeArticle()
    urgeCount.value = res.data?.currentCount || urgeCount.value + 1
    ElMessage({ message: `本月已有 ${urgeCount.value} 人催更，快马加鞭更新中！`, duration: 3000 })
  } catch (e) {
    ElMessage.error('催更失败')
  }
}

const copyFeed = () => {
  navigator.clipboard.writeText(feedUrl.value).then(() => ElMessage.success('已复制'))
}
</script>
```

### Step 3: Home.vue — 加组件

Read `Home.vue` first. After the `</div>` closing the `<div class="mt-24 grid...">` block, add `<SiteActionButtons />`.

```html
    <!-- 内容区域 -->
    <div class="mt-24 grid grid-cols-1 lg:grid-cols-3 gap-8">
      ...（现有内容）...
    </div>

    <!-- 三按钮 -->
    <SiteActionButtons />
  </div>
</template>
```

Add import:
```js
import SiteActionButtons from '@/components/common/SiteActionButtons.vue'
```

### Step 4: Commit

```bash
git add frontend/src/components/common/SiteActionButtons.vue frontend/src/views/Home.vue frontend/src/api/frontend.js
git commit -m "feat(subscription): add SiteActionButtons component and frontend APIs"
```

---

## Task 8: 编译验证

**Files:** (no new files)

### Step 1: Maven 编译

```bash
cd backend && mvn clean compile -q
```

### Step 2: 验证清单（手动冒烟）

- [ ] 三按钮在首页渲染
- [ ] 喜欢按钮点一次 disabled
- [ ] 订阅弹窗可输入邮箱
- [ ] 催更显示本月人数
- [ ] 后台订阅列表可访问
- [ ] 文章发布后邮件发送

### Step 3: Final commit

```bash
git add -A
git commit -m "feat: complete article subscription email and homepage 3-button feature"
```
