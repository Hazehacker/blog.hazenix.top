# 个性化推荐系统 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在博客首页增加个性化推荐模块，匿名用户看综合热度推荐，登录用户看基于行为的个性化推荐。

**Architecture:** 三路混合推荐引擎（内容推荐 40% + 协同过滤 40% + 热度 20%），Redis 缓存预计算结果，定时任务 + 事件驱动异步更新。前端首页 Hero 下方全宽横向滚动卡片带。

**Tech Stack:** Spring Boot 2.7 + MyBatis + Redis + MySQL (backend), Vue 3 + Tailwind CSS + Element Plus (frontend)

---

## File Structure

### Backend — New Files

| File | Responsibility |
|------|---------------|
| `blog-pojo/.../entity/UserBehavior.java` | 用户行为记录实体 |
| `blog-pojo/.../entity/UserInterest.java` | 用户兴趣标签实体 |
| `blog-pojo/.../dto/UserBehaviorDTO.java` | 行为上报 DTO |
| `blog-pojo/.../dto/UserInterestDTO.java` | 兴趣标签设置 DTO |
| `blog-common/.../constant/RecommendConstants.java` | 推荐系统常量 |
| `blog-server/.../mapper/UserBehaviorMapper.java` | 行为数据 Mapper 接口 |
| `blog-server/.../mapper/UserInterestMapper.java` | 兴趣标签 Mapper 接口 |
| `blog-server/resources/mapper/UserBehaviorMapper.xml` | 行为数据 SQL |
| `blog-server/resources/mapper/UserInterestMapper.xml` | 兴趣标签 SQL |
| `blog-server/.../service/UserBehaviorService.java` | 行为记录服务接口 |
| `blog-server/.../service/impl/UserBehaviorServiceImpl.java` | 行为记录服务实现 |
| `blog-server/.../service/UserInterestService.java` | 兴趣标签服务接口 |
| `blog-server/.../service/impl/UserInterestServiceImpl.java` | 兴趣标签服务实现 |
| `blog-server/.../service/RecommendService.java` | 推荐入口服务接口 |
| `blog-server/.../service/impl/RecommendServiceImpl.java` | 推荐入口服务实现 |
| `blog-server/.../recommend/ContentBasedEngine.java` | 内容推荐引擎 |
| `blog-server/.../recommend/CollaborativeFilterEngine.java` | 协同过滤引擎 |
| `blog-server/.../recommend/PopularityEngine.java` | 热度引擎 |
| `blog-server/.../recommend/RecommendCacheService.java` | 推荐缓存服务 |
| `blog-server/.../controller/user/UserInterestController.java` | 兴趣标签 API |
| `blog-server/.../controller/user/UserBehaviorController.java` | 行为上报 API |
| `blog-server/.../task/RecommendTask.java` | 推荐定时任务 |
| `blog-server/.../config/AsyncConfig.java` | 异步线程池配置 |

### Backend — Modified Files

| File | Change |
|------|--------|
| `blog-server/.../controller/user/ArticleController.java` | 实现 `/recommended` 端点 |
| `blog-server/.../service/impl/ArticleServiceImpl.java` | 点赞/收藏时记录行为 |
| `blog-server/.../config/SecurityConfig.java` | 放行推荐和行为上报接口 |
| `blog-common/.../constant/DefaultConstants.java` | 添加推荐相关常量（或用新常量类） |

### Frontend — New Files

| File | Responsibility |
|------|---------------|
| `src/components/article/RecommendSection.vue` | 首页推荐横向滚动模块 |
| `src/components/article/RecommendCard.vue` | 推荐卡片组件 |
| `src/components/common/InterestTagSelector.vue` | 兴趣标签选择器 |
| `src/api/recommend.js` | 推荐相关 API |
| `src/composables/useBehaviorTracker.js` | 浏览行为上报 composable |

### Frontend — Modified Files

| File | Change |
|------|--------|
| `src/views/Home.vue` | 插入 RecommendSection 组件 |
| `src/views/Register.vue` | 注册成功后弹出兴趣选择 |
| `src/views/ArticleDetail.vue` | 集成行为上报 |

### SQL

| File | Content |
|------|---------|
| `sql/recommend_tables.sql` | user_behavior + user_interest 建表语句 |

---

## Task 1: Database Schema & Entity Classes

**Files:**
- Create: `sql/recommend_tables.sql`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/UserBehavior.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/entity/UserInterest.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/dto/UserBehaviorDTO.java`
- Create: `backend/blog-pojo/src/main/java/top/hazenix/dto/UserInterestDTO.java`
- Create: `backend/blog-common/src/main/java/top/hazenix/constant/RecommendConstants.java`

- [ ] **Step 1: Create SQL migration file**

```sql
-- sql/recommend_tables.sql

CREATE TABLE IF NOT EXISTS user_behavior (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    article_id    BIGINT       NOT NULL,
    behavior_type TINYINT      NOT NULL COMMENT '1=浏览 2=点赞 3=收藏',
    duration      INT          DEFAULT NULL COMMENT '阅读时长(秒)，仅浏览行为',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_behavior (user_id, behavior_type),
    INDEX idx_article_behavior (article_id, behavior_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_interest (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT   NOT NULL,
    tag_id      BIGINT   NOT NULL,
    weight      DOUBLE   NOT NULL DEFAULT 0.5 COMMENT '兴趣权重 0-1',
    source      TINYINT  NOT NULL COMMENT '1=注册选择 2=行为推断',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_user_tag (user_id, tag_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: Run the SQL against the database**

Run: manually execute `sql/recommend_tables.sql` against the blog database.

- [ ] **Step 3: Create UserBehavior entity**

```java
// backend/blog-pojo/src/main/java/top/hazenix/entity/UserBehavior.java
package top.hazenix.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehavior implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long articleId;
    private Integer behaviorType; // 1=浏览 2=点赞 3=收藏
    private Integer duration;
    private LocalDateTime createTime;
}
```

- [ ] **Step 4: Create UserInterest entity**

```java
// backend/blog-pojo/src/main/java/top/hazenix/entity/UserInterest.java
package top.hazenix.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInterest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long tagId;
    private Double weight;
    private Integer source; // 1=注册选择 2=行为推断
    private LocalDateTime updateTime;
}
```

- [ ] **Step 5: Create DTOs**

```java
// backend/blog-pojo/src/main/java/top/hazenix/dto/UserBehaviorDTO.java
package top.hazenix.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserBehaviorDTO implements Serializable {
    private Long articleId;
    private Integer duration; // 阅读时长(秒)
}
```

```java
// backend/blog-pojo/src/main/java/top/hazenix/dto/UserInterestDTO.java
package top.hazenix.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class UserInterestDTO implements Serializable {
    private List<Long> tagIds;
}
```

- [ ] **Step 6: Create RecommendConstants**

```java
// backend/blog-common/src/main/java/top/hazenix/constant/RecommendConstants.java
package top.hazenix.constant;

public class RecommendConstants {
    // 行为类型
    public static final int BEHAVIOR_VIEW = 1;
    public static final int BEHAVIOR_LIKE = 2;
    public static final int BEHAVIOR_FAVORITE = 3;

    // 行为评分权重
    public static final int SCORE_VIEW = 1;
    public static final int SCORE_VIEW_LONG = 2; // 阅读 > 30s
    public static final int SCORE_LIKE = 3;
    public static final int SCORE_FAVORITE = 5;
    public static final int LONG_READ_THRESHOLD = 30; // 秒

    // 三路混合权重
    public static final double WEIGHT_CONTENT = 0.4;
    public static final double WEIGHT_CF = 0.4;
    public static final double WEIGHT_POPULARITY = 0.2;

    // 冷启动权重
    public static final double COLD_WEIGHT_CONTENT = 0.6;
    public static final double COLD_WEIGHT_POPULARITY = 0.4;

    // 匿名推荐权重
    public static final double ANON_WEIGHT_POPULARITY = 0.5;
    public static final double ANON_WEIGHT_FRESHNESS = 0.3;
    public static final double ANON_WEIGHT_DIVERSITY = 0.2;

    // 冷启动阈值
    public static final int COLD_START_THRESHOLD = 5;

    // 推荐数量
    public static final int RECOMMEND_SIZE = 10;

    // 同分类加分
    public static final double SAME_CATEGORY_BONUS = 0.2;

    // 时间衰减因子
    public static final double TIME_DECAY_LAMBDA = 0.01;

    // CF 最低行为阈值
    public static final int CF_MIN_BEHAVIOR_COUNT = 5;

    // 兴趣来源
    public static final int INTEREST_SOURCE_REGISTER = 1;
    public static final int INTEREST_SOURCE_BEHAVIOR = 2;

    // 默认兴趣权重
    public static final double DEFAULT_INTEREST_WEIGHT = 0.5;

    // Redis key 前缀
    public static final String KEY_REC_USER = "rec:user:";
    public static final String KEY_REC_ANONYMOUS = "rec:anonymous";
    public static final String KEY_REC_COLD = "rec:cold:";
    public static final String KEY_SIM_CONTENT = "sim:content:";
    public static final String KEY_SIM_CF = "sim:cf:";
    public static final String KEY_BEHAVIOR_SCORE = "behavior:score:";

    // TTL (秒)
    public static final long TTL_REC_USER = 7200;       // 2h
    public static final long TTL_REC_ANONYMOUS = 3600;   // 1h
    public static final long TTL_SIM_CONTENT = 86400;    // 24h
    public static final long TTL_SIM_CF = 43200;         // 12h
    public static final long TTL_BEHAVIOR_SCORE = 604800; // 7d

    // 同步计算超时 (ms)
    public static final long SYNC_COMPUTE_TIMEOUT = 500;

    private RecommendConstants() {}
}
```

- [ ] **Step 7: Commit**

```bash
git add sql/ backend/blog-pojo/src/main/java/top/hazenix/entity/UserBehavior.java backend/blog-pojo/src/main/java/top/hazenix/entity/UserInterest.java backend/blog-pojo/src/main/java/top/hazenix/dto/UserBehaviorDTO.java backend/blog-pojo/src/main/java/top/hazenix/dto/UserInterestDTO.java backend/blog-common/src/main/java/top/hazenix/constant/RecommendConstants.java
git commit -m "feat(recommend): add database schema, entities, DTOs, and constants"
```

---

## Task 2: MyBatis Mappers for Behavior & Interest

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/UserBehaviorMapper.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/mapper/UserInterestMapper.java`
- Create: `backend/blog-server/src/main/resources/mapper/UserBehaviorMapper.xml`
- Create: `backend/blog-server/src/main/resources/mapper/UserInterestMapper.xml`

- [ ] **Step 1: Create UserBehaviorMapper interface**

```java
// backend/blog-server/src/main/java/top/hazenix/mapper/UserBehaviorMapper.java
package top.hazenix.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hazenix.entity.UserBehavior;
import java.util.List;

@Mapper
public interface UserBehaviorMapper {
    void insert(UserBehavior behavior);

    List<UserBehavior> getByUserId(@Param("userId") Long userId);

    List<UserBehavior> getByArticleId(@Param("articleId") Long articleId);

    int countByUserId(@Param("userId") Long userId);

    /** 获取所有有行为的用户ID */
    List<Long> getAllActiveUserIds();

    /** 获取所有有行为的文章ID */
    List<Long> getAllActiveArticleIds();

    /** 获取指定文章的行为数量 */
    int countByArticleId(@Param("articleId") Long articleId);

    /** 获取用户对文章的最高评分行为 */
    List<UserBehavior> getByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /** 获取所有行为数据（用于协同过滤全量计算） */
    List<UserBehavior> listAll();
}
```

- [ ] **Step 2: Create UserBehaviorMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="top.hazenix.mapper.UserBehaviorMapper">

    <insert id="insert" parameterType="top.hazenix.entity.UserBehavior" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO user_behavior (user_id, article_id, behavior_type, duration, create_time)
        VALUES (#{userId}, #{articleId}, #{behaviorType}, #{duration}, #{createTime})
    </insert>

    <select id="getByUserId" resultType="top.hazenix.entity.UserBehavior">
        SELECT id, user_id, article_id, behavior_type, duration, create_time
        FROM user_behavior WHERE user_id = #{userId}
    </select>

    <select id="getByArticleId" resultType="top.hazenix.entity.UserBehavior">
        SELECT id, user_id, article_id, behavior_type, duration, create_time
        FROM user_behavior WHERE article_id = #{articleId}
    </select>

    <select id="countByUserId" resultType="int">
        SELECT COUNT(*) FROM user_behavior WHERE user_id = #{userId}
    </select>

    <select id="getAllActiveUserIds" resultType="java.lang.Long">
        SELECT DISTINCT user_id FROM user_behavior
    </select>

    <select id="getAllActiveArticleIds" resultType="java.lang.Long">
        SELECT DISTINCT article_id FROM user_behavior
    </select>

    <select id="countByArticleId" resultType="int">
        SELECT COUNT(*) FROM user_behavior WHERE article_id = #{articleId}
    </select>

    <select id="getByUserIdAndArticleId" resultType="top.hazenix.entity.UserBehavior">
        SELECT id, user_id, article_id, behavior_type, duration, create_time
        FROM user_behavior WHERE user_id = #{userId} AND article_id = #{articleId}
    </select>

    <select id="listAll" resultType="top.hazenix.entity.UserBehavior">
        SELECT id, user_id, article_id, behavior_type, duration, create_time
        FROM user_behavior
    </select>
</mapper>
```

- [ ] **Step 3: Create UserInterestMapper interface**

```java
// backend/blog-server/src/main/java/top/hazenix/mapper/UserInterestMapper.java
package top.hazenix.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hazenix.entity.UserInterest;
import java.util.List;

@Mapper
public interface UserInterestMapper {
    void insert(UserInterest interest);

    void insertBatch(@Param("list") List<UserInterest> list);

    void update(UserInterest interest);

    void deleteByUserId(@Param("userId") Long userId);

    List<UserInterest> getByUserId(@Param("userId") Long userId);

    UserInterest getByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    int countByUserId(@Param("userId") Long userId);
}
```

- [ ] **Step 4: Create UserInterestMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="top.hazenix.mapper.UserInterestMapper">

    <insert id="insert" parameterType="top.hazenix.entity.UserInterest" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO user_interest (user_id, tag_id, weight, source, update_time)
        VALUES (#{userId}, #{tagId}, #{weight}, #{source}, #{updateTime})
    </insert>

    <insert id="insertBatch">
        INSERT INTO user_interest (user_id, tag_id, weight, source, update_time)
        VALUES
        <foreach collection="list" item="item" separator=",">
            (#{item.userId}, #{item.tagId}, #{item.weight}, #{item.source}, #{item.updateTime})
        </foreach>
    </insert>

    <update id="update" parameterType="top.hazenix.entity.UserInterest">
        UPDATE user_interest
        <set>
            <if test="weight != null">weight = #{weight},</if>
            <if test="source != null">source = #{source},</if>
        </set>
        WHERE id = #{id}
    </update>

    <delete id="deleteByUserId">
        DELETE FROM user_interest WHERE user_id = #{userId}
    </delete>

    <select id="getByUserId" resultType="top.hazenix.entity.UserInterest">
        SELECT id, user_id, tag_id, weight, source, update_time
        FROM user_interest WHERE user_id = #{userId}
    </select>

    <select id="getByUserIdAndTagId" resultType="top.hazenix.entity.UserInterest">
        SELECT id, user_id, tag_id, weight, source, update_time
        FROM user_interest WHERE user_id = #{userId} AND tag_id = #{tagId}
    </select>

    <select id="countByUserId" resultType="int">
        SELECT COUNT(*) FROM user_interest WHERE user_id = #{userId}
    </select>
</mapper>
```

- [ ] **Step 5: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/mapper/UserBehaviorMapper.java backend/blog-server/src/main/java/top/hazenix/mapper/UserInterestMapper.java backend/blog-server/src/main/resources/mapper/UserBehaviorMapper.xml backend/blog-server/src/main/resources/mapper/UserInterestMapper.xml
git commit -m "feat(recommend): add MyBatis mappers for user behavior and interest"
```

---

## Task 3: UserBehaviorService & UserInterestService

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/service/UserBehaviorService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/UserBehaviorServiceImpl.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/UserInterestService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/UserInterestServiceImpl.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/config/AsyncConfig.java`
- Test: `backend/blog-server/src/test/java/top/hazenix/test/UserBehaviorServiceImplTest.java`

- [ ] **Step 1: Create AsyncConfig**

```java
// backend/blog-server/src/main/java/top/hazenix/config/AsyncConfig.java
package top.hazenix.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
}
```

- [ ] **Step 2: Create UserBehaviorService interface**

```java
// backend/blog-server/src/main/java/top/hazenix/service/UserBehaviorService.java
package top.hazenix.service;

import top.hazenix.dto.UserBehaviorDTO;

public interface UserBehaviorService {
    void recordView(Long userId, Long articleId, Integer duration);
    void recordLike(Long userId, Long articleId);
    void recordFavorite(Long userId, Long articleId);
    int getUserBehaviorCount(Long userId);
    double getUserArticleScore(Long userId, Long articleId);
}
```

- [ ] **Step 3: Create UserBehaviorServiceImpl**

```java
// backend/blog-server/src/main/java/top/hazenix/service/impl/UserBehaviorServiceImpl.java
package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.UserBehavior;
import top.hazenix.mapper.UserBehaviorMapper;
import top.hazenix.service.UserBehaviorService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserBehaviorServiceImpl implements UserBehaviorService {

    private final UserBehaviorMapper userBehaviorMapper;

    @Override
    public void recordView(Long userId, Long articleId, Integer duration) {
        UserBehavior behavior = UserBehavior.builder()
                .userId(userId)
                .articleId(articleId)
                .behaviorType(RecommendConstants.BEHAVIOR_VIEW)
                .duration(duration)
                .createTime(LocalDateTime.now())
                .build();
        userBehaviorMapper.insert(behavior);
    }

    @Override
    @Async
    public void recordLike(Long userId, Long articleId) {
        UserBehavior behavior = UserBehavior.builder()
                .userId(userId)
                .articleId(articleId)
                .behaviorType(RecommendConstants.BEHAVIOR_LIKE)
                .createTime(LocalDateTime.now())
                .build();
        userBehaviorMapper.insert(behavior);
    }

    @Override
    @Async
    public void recordFavorite(Long userId, Long articleId) {
        UserBehavior behavior = UserBehavior.builder()
                .userId(userId)
                .articleId(articleId)
                .behaviorType(RecommendConstants.BEHAVIOR_FAVORITE)
                .createTime(LocalDateTime.now())
                .build();
        userBehaviorMapper.insert(behavior);
    }

    @Override
    public int getUserBehaviorCount(Long userId) {
        return userBehaviorMapper.countByUserId(userId);
    }

    @Override
    public double getUserArticleScore(Long userId, Long articleId) {
        List<UserBehavior> behaviors = userBehaviorMapper.getByUserIdAndArticleId(userId, articleId);
        double maxScore = 0;
        for (UserBehavior b : behaviors) {
            double score = 0;
            switch (b.getBehaviorType()) {
                case RecommendConstants.BEHAVIOR_VIEW:
                    score = (b.getDuration() != null && b.getDuration() > RecommendConstants.LONG_READ_THRESHOLD)
                            ? RecommendConstants.SCORE_VIEW_LONG : RecommendConstants.SCORE_VIEW;
                    break;
                case RecommendConstants.BEHAVIOR_LIKE:
                    score = RecommendConstants.SCORE_LIKE;
                    break;
                case RecommendConstants.BEHAVIOR_FAVORITE:
                    score = RecommendConstants.SCORE_FAVORITE;
                    break;
            }
            maxScore = Math.max(maxScore, score);
        }
        return maxScore;
    }
}
```

- [ ] **Step 4: Create UserInterestService interface**

```java
// backend/blog-server/src/main/java/top/hazenix/service/UserInterestService.java
package top.hazenix.service;

import top.hazenix.entity.UserInterest;
import java.util.List;

public interface UserInterestService {
    void setInterests(Long userId, List<Long> tagIds);
    void updateInterests(Long userId, List<Long> tagIds);
    List<UserInterest> getInterests(Long userId);
    boolean hasInterests(Long userId);
}
```

- [ ] **Step 5: Create UserInterestServiceImpl**

```java
// backend/blog-server/src/main/java/top/hazenix/service/impl/UserInterestServiceImpl.java
package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.UserInterest;
import top.hazenix.mapper.UserInterestMapper;
import top.hazenix.service.UserInterestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInterestServiceImpl implements UserInterestService {

    private final UserInterestMapper userInterestMapper;

    @Override
    @Transactional
    public void setInterests(Long userId, List<Long> tagIds) {
        List<UserInterest> interests = tagIds.stream()
                .map(tagId -> UserInterest.builder()
                        .userId(userId)
                        .tagId(tagId)
                        .weight(RecommendConstants.DEFAULT_INTEREST_WEIGHT)
                        .source(RecommendConstants.INTEREST_SOURCE_REGISTER)
                        .updateTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        if (!interests.isEmpty()) {
            userInterestMapper.insertBatch(interests);
        }
    }

    @Override
    @Transactional
    public void updateInterests(Long userId, List<Long> tagIds) {
        userInterestMapper.deleteByUserId(userId);
        setInterests(userId, tagIds);
    }

    @Override
    public List<UserInterest> getInterests(Long userId) {
        return userInterestMapper.getByUserId(userId);
    }

    @Override
    public boolean hasInterests(Long userId) {
        return userInterestMapper.countByUserId(userId) > 0;
    }
}
```

- [ ] **Step 6: Write unit test for UserBehaviorServiceImpl**

```java
// backend/blog-server/src/test/java/top/hazenix/test/UserBehaviorServiceImplTest.java
package top.hazenix.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.UserBehavior;
import top.hazenix.mapper.UserBehaviorMapper;
import top.hazenix.service.impl.UserBehaviorServiceImpl;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserBehaviorServiceImplTest {

    private UserBehaviorMapper mapper;
    private UserBehaviorServiceImpl service;

    @BeforeEach
    public void setUp() {
        mapper = mock(UserBehaviorMapper.class);
        service = new UserBehaviorServiceImpl(mapper);
    }

    @Test
    public void recordView_should_insert_behavior_with_view_type() {
        service.recordView(1L, 100L, 45);
        verify(mapper).insert(argThat(b ->
                b.getUserId().equals(1L) &&
                b.getArticleId().equals(100L) &&
                b.getBehaviorType().equals(RecommendConstants.BEHAVIOR_VIEW) &&
                b.getDuration().equals(45)
        ));
    }

    @Test
    public void getUserArticleScore_should_return_max_score() {
        UserBehavior view = new UserBehavior();
        view.setBehaviorType(RecommendConstants.BEHAVIOR_VIEW);
        view.setDuration(10); // short read = 1

        UserBehavior like = new UserBehavior();
        like.setBehaviorType(RecommendConstants.BEHAVIOR_LIKE); // = 3

        when(mapper.getByUserIdAndArticleId(1L, 100L)).thenReturn(Arrays.asList(view, like));

        double score = service.getUserArticleScore(1L, 100L);
        assertEquals(3.0, score);
    }

    @Test
    public void getUserArticleScore_should_return_2_for_long_read() {
        UserBehavior view = new UserBehavior();
        view.setBehaviorType(RecommendConstants.BEHAVIOR_VIEW);
        view.setDuration(60); // > 30s = 2

        when(mapper.getByUserIdAndArticleId(1L, 100L)).thenReturn(Collections.singletonList(view));

        double score = service.getUserArticleScore(1L, 100L);
        assertEquals(2.0, score);
    }

    @Test
    public void getUserArticleScore_should_return_0_when_no_behaviors() {
        when(mapper.getByUserIdAndArticleId(1L, 100L)).thenReturn(Collections.emptyList());
        assertEquals(0.0, service.getUserArticleScore(1L, 100L));
    }
}
```

- [ ] **Step 7: Run tests**

Run: `cd backend && mvn test -pl blog-server -Dtest=UserBehaviorServiceImplTest -DfailIfNoTests=false`
Expected: All 4 tests PASS

- [ ] **Step 8: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/config/AsyncConfig.java backend/blog-server/src/main/java/top/hazenix/service/UserBehaviorService.java backend/blog-server/src/main/java/top/hazenix/service/impl/UserBehaviorServiceImpl.java backend/blog-server/src/main/java/top/hazenix/service/UserInterestService.java backend/blog-server/src/main/java/top/hazenix/service/impl/UserInterestServiceImpl.java backend/blog-server/src/test/java/top/hazenix/test/UserBehaviorServiceImplTest.java
git commit -m "feat(recommend): add UserBehaviorService, UserInterestService, and AsyncConfig"
```

---

## Task 4: Recommendation Engines (Content-Based + CF + Popularity)

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/recommend/ContentBasedEngine.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/recommend/CollaborativeFilterEngine.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/recommend/PopularityEngine.java`
- Test: `backend/blog-server/src/test/java/top/hazenix/test/ContentBasedEngineTest.java`
- Test: `backend/blog-server/src/test/java/top/hazenix/test/CollaborativeFilterEngineTest.java`

- [ ] **Step 1: Create ContentBasedEngine**

```java
// backend/blog-server/src/main/java/top/hazenix/recommend/ContentBasedEngine.java
package top.hazenix.recommend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.UserInterest;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.mapper.ArticleTagsMapper;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ContentBasedEngine {

    private final ArticleTagsMapper articleTagsMapper;
    private final ArticleMapper articleMapper;

    /**
     * 计算两篇文章的 Jaccard 相似度 + 同分类加分
     */
    public double articleSimilarity(Long articleA, Long articleB,
                                     Set<Integer> tagsA, Set<Integer> tagsB,
                                     Long categoryA, Long categoryB) {
        if (tagsA.isEmpty() && tagsB.isEmpty()) return 0.0;

        Set<Integer> intersection = new HashSet<>(tagsA);
        intersection.retainAll(tagsB);

        Set<Integer> union = new HashSet<>(tagsA);
        union.addAll(tagsB);

        double jaccard = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();

        if (categoryA != null && categoryA.equals(categoryB)) {
            jaccard = Math.min(1.0, jaccard + RecommendConstants.SAME_CATEGORY_BONUS);
        }
        return jaccard;
    }

    /**
     * 基于用户兴趣标签计算文章的内容推荐得分
     */
    public double scoreForUser(List<UserInterest> interests, Set<Integer> articleTags) {
        if (interests.isEmpty() || articleTags.isEmpty()) return 0.0;

        double totalWeight = 0.0;
        for (UserInterest interest : interests) {
            if (articleTags.contains(interest.getTagId().intValue())) {
                totalWeight += interest.getWeight();
            }
        }
        return totalWeight / articleTags.size();
    }
}
```

- [ ] **Step 2: Write test for ContentBasedEngine**

```java
// backend/blog-server/src/test/java/top/hazenix/test/ContentBasedEngineTest.java
package top.hazenix.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.hazenix.entity.UserInterest;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.mapper.ArticleTagsMapper;
import top.hazenix.recommend.ContentBasedEngine;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ContentBasedEngineTest {

    private ContentBasedEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new ContentBasedEngine(mock(ArticleTagsMapper.class), mock(ArticleMapper.class));
    }

    @Test
    public void jaccard_should_compute_correctly() {
        // {Java, Redis, Spring} vs {Java, Spring, MySQL}
        // intersection=2, union=4, jaccard=0.5
        Set<Integer> tagsA = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> tagsB = new HashSet<>(Arrays.asList(1, 3, 4));
        double sim = engine.articleSimilarity(1L, 2L, tagsA, tagsB, 1L, 2L);
        assertEquals(0.5, sim, 0.001);
    }

    @Test
    public void jaccard_should_add_category_bonus() {
        Set<Integer> tagsA = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> tagsB = new HashSet<>(Arrays.asList(1, 3, 4));
        double sim = engine.articleSimilarity(1L, 2L, tagsA, tagsB, 1L, 1L); // same category
        assertEquals(0.7, sim, 0.001);
    }

    @Test
    public void jaccard_should_cap_at_1() {
        Set<Integer> tagsA = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> tagsB = new HashSet<>(Arrays.asList(1, 2, 3)); // identical
        double sim = engine.articleSimilarity(1L, 2L, tagsA, tagsB, 1L, 1L);
        assertEquals(1.0, sim, 0.001);
    }

    @Test
    public void scoreForUser_should_match_interest_tags() {
        UserInterest i1 = UserInterest.builder().tagId(1L).weight(0.8).build();
        UserInterest i2 = UserInterest.builder().tagId(2L).weight(0.6).build();
        UserInterest i3 = UserInterest.builder().tagId(3L).weight(0.4).build();

        Set<Integer> articleTags = new HashSet<>(Arrays.asList(1, 3, 5));
        // matches: tag1(0.8) + tag3(0.4) = 1.2 / 3 tags = 0.4
        double score = engine.scoreForUser(Arrays.asList(i1, i2, i3), articleTags);
        assertEquals(0.4, score, 0.001);
    }

    @Test
    public void scoreForUser_should_return_0_when_no_match() {
        UserInterest i1 = UserInterest.builder().tagId(1L).weight(0.8).build();
        Set<Integer> articleTags = new HashSet<>(Arrays.asList(5, 6));
        assertEquals(0.0, engine.scoreForUser(Collections.singletonList(i1), articleTags));
    }
}
```

- [ ] **Step 3: Run ContentBasedEngine tests**

Run: `cd backend && mvn test -pl blog-server -Dtest=ContentBasedEngineTest -DfailIfNoTests=false`
Expected: All 5 tests PASS

- [ ] **Step 4: Create CollaborativeFilterEngine**

```java
// backend/blog-server/src/main/java/top/hazenix/recommend/CollaborativeFilterEngine.java
package top.hazenix.recommend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.UserBehavior;
import top.hazenix.mapper.UserBehaviorMapper;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class CollaborativeFilterEngine {

    private final UserBehaviorMapper userBehaviorMapper;

    /**
     * 构建用户-文章评分矩阵
     * key: userId -> (articleId -> score)
     */
    public Map<Long, Map<Long, Double>> buildRatingMatrix(List<UserBehavior> allBehaviors) {
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        for (UserBehavior b : allBehaviors) {
            double score = behaviorToScore(b);
            matrix.computeIfAbsent(b.getUserId(), k -> new HashMap<>())
                    .merge(b.getArticleId(), score, Math::max);
        }
        return matrix;
    }

    /**
     * 计算两篇文章的余弦相似度
     */
    public double cosineSimilarity(Map<Long, Double> ratingsA, Map<Long, Double> ratingsB) {
        Set<Long> commonUsers = new HashSet<>(ratingsA.keySet());
        commonUsers.retainAll(ratingsB.keySet());

        if (commonUsers.isEmpty()) return 0.0;

        double dotProduct = 0, normA = 0, normB = 0;
        for (Long userId : commonUsers) {
            double a = ratingsA.get(userId);
            double b = ratingsB.get(userId);
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0 ? 0.0 : dotProduct / denominator;
    }

    /**
     * 为用户生成 CF 推荐得分
     * userRatings: 该用户已交互的 articleId -> score
     * itemSimilarities: 候选文章 -> (已交互文章 -> similarity)
     */
    public double cfScoreForArticle(Map<Long, Double> userRatings,
                                     Long candidateArticleId,
                                     Map<Long, Double> candidateSimilarities) {
        double numerator = 0, denominator = 0;
        for (Map.Entry<Long, Double> entry : userRatings.entrySet()) {
            Long interactedArticleId = entry.getKey();
            double rating = entry.getValue();
            Double sim = candidateSimilarities.get(interactedArticleId);
            if (sim != null && sim > 0) {
                numerator += rating * sim;
                denominator += Math.abs(sim);
            }
        }
        return denominator == 0 ? 0.0 : numerator / denominator;
    }

    private double behaviorToScore(UserBehavior b) {
        switch (b.getBehaviorType()) {
            case RecommendConstants.BEHAVIOR_VIEW:
                return (b.getDuration() != null && b.getDuration() > RecommendConstants.LONG_READ_THRESHOLD)
                        ? RecommendConstants.SCORE_VIEW_LONG : RecommendConstants.SCORE_VIEW;
            case RecommendConstants.BEHAVIOR_LIKE:
                return RecommendConstants.SCORE_LIKE;
            case RecommendConstants.BEHAVIOR_FAVORITE:
                return RecommendConstants.SCORE_FAVORITE;
            default:
                return 0;
        }
    }
}
```

- [ ] **Step 5: Write test for CollaborativeFilterEngine**

```java
// backend/blog-server/src/test/java/top/hazenix/test/CollaborativeFilterEngineTest.java
package top.hazenix.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.hazenix.entity.UserBehavior;
import top.hazenix.mapper.UserBehaviorMapper;
import top.hazenix.recommend.CollaborativeFilterEngine;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class CollaborativeFilterEngineTest {

    private CollaborativeFilterEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new CollaborativeFilterEngine(mock(UserBehaviorMapper.class));
    }

    @Test
    public void buildRatingMatrix_should_take_max_score_per_user_article() {
        UserBehavior view = new UserBehavior();
        view.setUserId(1L); view.setArticleId(100L);
        view.setBehaviorType(1); view.setDuration(10); // score=1

        UserBehavior like = new UserBehavior();
        like.setUserId(1L); like.setArticleId(100L);
        like.setBehaviorType(2); // score=3

        Map<Long, Map<Long, Double>> matrix = engine.buildRatingMatrix(Arrays.asList(view, like));
        assertEquals(3.0, matrix.get(1L).get(100L));
    }

    @Test
    public void cosineSimilarity_should_compute_correctly() {
        // Article A: user1=3, user2=5
        // Article B: user1=3, user2=5
        // identical vectors -> similarity = 1.0
        Map<Long, Double> ratingsA = new HashMap<>();
        ratingsA.put(1L, 3.0); ratingsA.put(2L, 5.0);
        Map<Long, Double> ratingsB = new HashMap<>();
        ratingsB.put(1L, 3.0); ratingsB.put(2L, 5.0);

        assertEquals(1.0, engine.cosineSimilarity(ratingsA, ratingsB), 0.001);
    }

    @Test
    public void cosineSimilarity_should_return_0_when_no_common_users() {
        Map<Long, Double> ratingsA = new HashMap<>();
        ratingsA.put(1L, 3.0);
        Map<Long, Double> ratingsB = new HashMap<>();
        ratingsB.put(2L, 5.0);

        assertEquals(0.0, engine.cosineSimilarity(ratingsA, ratingsB));
    }

    @Test
    public void cfScoreForArticle_should_weight_by_similarity() {
        Map<Long, Double> userRatings = new HashMap<>();
        userRatings.put(10L, 5.0); // user rated article 10 = 5
        userRatings.put(20L, 1.0); // user rated article 20 = 1

        Map<Long, Double> candidateSim = new HashMap<>();
        candidateSim.put(10L, 0.8); // candidate similar to article 10
        candidateSim.put(20L, 0.2); // candidate less similar to article 20

        // (5*0.8 + 1*0.2) / (0.8 + 0.2) = 4.2 / 1.0 = 4.2
        double score = engine.cfScoreForArticle(userRatings, 30L, candidateSim);
        assertEquals(4.2, score, 0.001);
    }
}
```

- [ ] **Step 6: Run CF tests**

Run: `cd backend && mvn test -pl blog-server -Dtest=CollaborativeFilterEngineTest -DfailIfNoTests=false`
Expected: All 4 tests PASS

- [ ] **Step 7: Create PopularityEngine**

```java
// backend/blog-server/src/main/java/top/hazenix/recommend/PopularityEngine.java
package top.hazenix.recommend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.Article;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class PopularityEngine {

    public double score(Article article) {
        long v = article.getViewCount() == null ? 0 : article.getViewCount();
        long l = article.getLikeCount() == null ? 0 : article.getLikeCount();
        long f = article.getFavoriteCount() == null ? 0 : article.getFavoriteCount();

        double rawScore = v + l * 10.0 + f * 20.0;

        long daysSincePublish = 0;
        if (article.getCreateTime() != null) {
            daysSincePublish = ChronoUnit.DAYS.between(article.getCreateTime(), LocalDateTime.now());
        }

        return rawScore * Math.exp(-RecommendConstants.TIME_DECAY_LAMBDA * daysSincePublish);
    }
}
```

- [ ] **Step 8: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/recommend/ backend/blog-server/src/test/java/top/hazenix/test/ContentBasedEngineTest.java backend/blog-server/src/test/java/top/hazenix/test/CollaborativeFilterEngineTest.java
git commit -m "feat(recommend): add ContentBased, CollaborativeFilter, and Popularity engines with tests"
```

---

## Task 5: RecommendCacheService & RecommendService

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/recommend/RecommendCacheService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/RecommendService.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/service/impl/RecommendServiceImpl.java`
- Test: `backend/blog-server/src/test/java/top/hazenix/test/RecommendServiceImplTest.java`

- [ ] **Step 1: Create RecommendCacheService**

```java
// backend/blog-server/src/main/java/top/hazenix/recommend/RecommendCacheService.java
package top.hazenix.recommend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import top.hazenix.constant.RecommendConstants;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecommendCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void cacheUserRecommendations(Long userId, Map<Long, Double> articleScores) {
        String key = RecommendConstants.KEY_REC_USER + userId;
        cacheZSet(key, articleScores, RecommendConstants.TTL_REC_USER);
    }

    public void cacheAnonymousRecommendations(Map<Long, Double> articleScores) {
        cacheZSet(RecommendConstants.KEY_REC_ANONYMOUS, articleScores, RecommendConstants.TTL_REC_ANONYMOUS);
    }

    public void cacheColdStartRecommendations(Long userId, Map<Long, Double> articleScores) {
        String key = RecommendConstants.KEY_REC_COLD + userId;
        cacheZSet(key, articleScores, RecommendConstants.TTL_REC_USER);
    }

    public List<Long> getUserRecommendations(Long userId, int size) {
        String key = RecommendConstants.KEY_REC_USER + userId;
        return getFromZSet(key, size);
    }

    public List<Long> getAnonymousRecommendations(int size) {
        return getFromZSet(RecommendConstants.KEY_REC_ANONYMOUS, size);
    }

    public List<Long> getColdStartRecommendations(Long userId, int size) {
        String key = RecommendConstants.KEY_REC_COLD + userId;
        return getFromZSet(key, size);
    }

    public void cacheContentSimilarity(Long articleId, Map<Long, Double> similarities) {
        String key = RecommendConstants.KEY_SIM_CONTENT + articleId;
        cacheZSet(key, similarities, RecommendConstants.TTL_SIM_CONTENT);
    }

    public void cacheCFSimilarity(Long articleId, Map<Long, Double> similarities) {
        String key = RecommendConstants.KEY_SIM_CF + articleId;
        cacheZSet(key, similarities, RecommendConstants.TTL_SIM_CF);
    }

    public Map<Long, Double> getCFSimilarity(Long articleId) {
        String key = RecommendConstants.KEY_SIM_CF + articleId;
        return getZSetAsMap(key);
    }

    public void evictUserRecommendations(Long userId) {
        redisTemplate.delete(RecommendConstants.KEY_REC_USER + userId);
        redisTemplate.delete(RecommendConstants.KEY_REC_COLD + userId);
    }

    private void cacheZSet(String key, Map<Long, Double> scores, long ttlSeconds) {
        try {
            redisTemplate.delete(key);
            ZSetOperations<String, Object> zOps = redisTemplate.opsForZSet();
            for (Map.Entry<Long, Double> entry : scores.entrySet()) {
                zOps.add(key, entry.getKey(), entry.getValue());
            }
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("缓存写入失败: key={}", key, e);
        }
    }

    private List<Long> getFromZSet(String key, int size) {
        try {
            Set<Object> results = redisTemplate.opsForZSet().reverseRange(key, 0, size - 1);
            if (results == null || results.isEmpty()) return Collections.emptyList();
            return results.stream()
                    .map(o -> ((Number) o).longValue())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("缓存读取失败: key={}", key, e);
            return Collections.emptyList();
        }
    }

    private Map<Long, Double> getZSetAsMap(String key) {
        try {
            Set<ZSetOperations.TypedTuple<Object>> tuples =
                    redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
            if (tuples == null) return Collections.emptyMap();
            Map<Long, Double> result = new HashMap<>();
            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                result.put(((Number) tuple.getValue()).longValue(), tuple.getScore());
            }
            return result;
        } catch (Exception e) {
            log.error("缓存读取失败: key={}", key, e);
            return Collections.emptyMap();
        }
    }
}
```

- [ ] **Step 2: Create RecommendService interface**

```java
// backend/blog-server/src/main/java/top/hazenix/service/RecommendService.java
package top.hazenix.service;

import top.hazenix.vo.ArticleShortVO;
import java.util.List;

public interface RecommendService {
    List<ArticleShortVO> getRecommendations(Long userId, int size);
    void recomputeAnonymousRecommendations();
    void recomputeContentSimilarityMatrix();
    void recomputeCFSimilarityMatrix();
    void refreshUserRecommendations(Long userId);
}
```

- [ ] **Step 3: Create RecommendServiceImpl**

This is the core orchestrator. It determines user state (anonymous/cold-start/active) and dispatches to the appropriate engine combination.

```java
// backend/blog-server/src/main/java/top/hazenix/service/impl/RecommendServiceImpl.java
package top.hazenix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.Article;
import top.hazenix.entity.UserBehavior;
import top.hazenix.entity.UserInterest;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.mapper.ArticleTagsMapper;
import top.hazenix.mapper.UserBehaviorMapper;
import top.hazenix.recommend.CollaborativeFilterEngine;
import top.hazenix.recommend.ContentBasedEngine;
import top.hazenix.recommend.PopularityEngine;
import top.hazenix.recommend.RecommendCacheService;
import top.hazenix.service.PopularArticleService;
import top.hazenix.service.RecommendService;
import top.hazenix.service.UserBehaviorService;
import top.hazenix.service.UserInterestService;
import top.hazenix.vo.ArticleShortVO;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final ContentBasedEngine contentEngine;
    private final CollaborativeFilterEngine cfEngine;
    private final PopularityEngine popularityEngine;
    private final RecommendCacheService cacheService;
    private final UserBehaviorService behaviorService;
    private final UserInterestService interestService;
    private final PopularArticleService popularArticleService;
    private final ArticleMapper articleMapper;
    private final ArticleTagsMapper articleTagsMapper;
    private final UserBehaviorMapper userBehaviorMapper;

    @Override
    public List<ArticleShortVO> getRecommendations(Long userId, int size) {
        if (userId == null) {
            return getAnonymousRecommendations(size);
        }

        // Try cache first
        List<Long> cached = cacheService.getUserRecommendations(userId, size);
        if (!cached.isEmpty()) {
            return toArticleVOs(cached);
        }

        // Check cold start
        int behaviorCount = behaviorService.getUserBehaviorCount(userId);
        boolean hasInterests = interestService.hasInterests(userId);

        List<Long> articleIds;
        if (behaviorCount >= RecommendConstants.COLD_START_THRESHOLD) {
            articleIds = computeFullRecommendations(userId, size);
        } else if (hasInterests) {
            articleIds = computeColdStartRecommendations(userId, size);
        } else {
            return getAnonymousRecommendations(size);
        }

        return toArticleVOs(articleIds);
    }

    private List<ArticleShortVO> getAnonymousRecommendations(int size) {
        List<Long> cached = cacheService.getAnonymousRecommendations(size);
        if (!cached.isEmpty()) {
            return toArticleVOs(cached);
        }
        // Fallback to popular articles
        return popularArticleService.getCachedOrFallback(size);
    }

    private List<Long> computeFullRecommendations(Long userId, int size) {
        List<Article> allArticles = articleMapper.listAllForScoring();
        List<UserInterest> interests = interestService.getInterests(userId);
        List<UserBehavior> userBehaviors = userBehaviorMapper.getByUserId(userId);

        Set<Long> viewedArticleIds = userBehaviors.stream()
                .map(UserBehavior::getArticleId)
                .collect(Collectors.toSet());

        // Build user ratings
        Map<Long, Double> userRatings = new HashMap<>();
        for (UserBehavior b : userBehaviors) {
            double score = behaviorService.getUserArticleScore(userId, b.getArticleId());
            userRatings.merge(b.getArticleId(), score, Math::max);
        }

        // Get popular article IDs to exclude from anonymous recommendations
        List<ArticleShortVO> popularList = popularArticleService.getCachedOrFallback(5);
        Set<Long> popularIds = popularList.stream()
                .map(ArticleShortVO::getId)
                .collect(Collectors.toSet());

        Map<Long, Double> finalScores = new HashMap<>();

        for (Article article : allArticles) {
            if (viewedArticleIds.contains(article.getId())) continue;

            // Content score
            Set<Integer> articleTags = new HashSet<>(articleTagsMapper.getListByArticleId(article.getId()));
            double contentScore = contentEngine.scoreForUser(interests, articleTags);

            // CF score
            Map<Long, Double> cfSim = cacheService.getCFSimilarity(article.getId());
            double cfScore = cfEngine.cfScoreForArticle(userRatings, article.getId(), cfSim);

            // Popularity score
            double popScore = popularityEngine.score(article);

            finalScores.put(article.getId(), contentScore * RecommendConstants.WEIGHT_CONTENT
                    + cfScore * RecommendConstants.WEIGHT_CF
                    + popScore * RecommendConstants.WEIGHT_POPULARITY);
        }

        // Normalize and get top N
        List<Long> result = normalizeAndTopN(finalScores, size);

        // Cache
        Map<Long, Double> cacheMap = new LinkedHashMap<>();
        for (int i = 0; i < result.size(); i++) {
            cacheMap.put(result.get(i), (double)(result.size() - i));
        }
        cacheService.cacheUserRecommendations(userId, cacheMap);

        return result;
    }

    private List<Long> computeColdStartRecommendations(Long userId, int size) {
        List<Article> allArticles = articleMapper.listAllForScoring();
        List<UserInterest> interests = interestService.getInterests(userId);

        Map<Long, Double> finalScores = new HashMap<>();
        for (Article article : allArticles) {
            Set<Integer> articleTags = new HashSet<>(articleTagsMapper.getListByArticleId(article.getId()));
            double contentScore = contentEngine.scoreForUser(interests, articleTags);
            double popScore = popularityEngine.score(article);

            finalScores.put(article.getId(),
                    contentScore * RecommendConstants.COLD_WEIGHT_CONTENT
                    + popScore * RecommendConstants.COLD_WEIGHT_POPULARITY);
        }

        List<Long> result = normalizeAndTopN(finalScores, size);

        Map<Long, Double> cacheMap = new LinkedHashMap<>();
        for (int i = 0; i < result.size(); i++) {
            cacheMap.put(result.get(i), (double)(result.size() - i));
        }
        cacheService.cacheColdStartRecommendations(userId, cacheMap);

        return result;
    }

    @Override
    public void recomputeAnonymousRecommendations() {
        log.info("开始重算匿名用户推荐");
        List<Article> allArticles = articleMapper.listAllForScoring();

        // Get popular top 5 IDs to exclude
        List<ArticleShortVO> popularList = popularArticleService.getCachedOrFallback(5);
        Set<Long> excludeIds = popularList.stream()
                .map(ArticleShortVO::getId)
                .collect(Collectors.toSet());

        Map<Long, Double> scores = new HashMap<>();
        for (Article article : allArticles) {
            if (excludeIds.contains(article.getId())) continue;
            double popScore = popularityEngine.score(article);
            // Diversity: count distinct tags
            int tagCount = articleTagsMapper.getListByArticleId(article.getId()).size();
            double diversityScore = Math.min(1.0, tagCount / 5.0);

            scores.put(article.getId(),
                    popScore * RecommendConstants.ANON_WEIGHT_POPULARITY
                    + popScore * RecommendConstants.ANON_WEIGHT_FRESHNESS // freshness already in popScore via decay
                    + diversityScore * RecommendConstants.ANON_WEIGHT_DIVERSITY);
        }

        Map<Long, Double> topScores = new LinkedHashMap<>();
        normalizeAndTopN(scores, RecommendConstants.RECOMMEND_SIZE)
                .forEach(id -> topScores.put(id, scores.get(id)));

        cacheService.cacheAnonymousRecommendations(topScores);
        log.info("匿名用户推荐重算完成，共 {} 条", topScores.size());
    }

    @Override
    public void recomputeContentSimilarityMatrix() {
        log.info("开始重算内容相似度矩阵");
        List<Article> allArticles = articleMapper.listAllForScoring();
        Map<Long, Set<Integer>> articleTagsMap = new HashMap<>();
        Map<Long, Long> articleCategoryMap = new HashMap<>();

        for (Article a : allArticles) {
            articleTagsMap.put(a.getId(), new HashSet<>(articleTagsMapper.getListByArticleId(a.getId())));
            articleCategoryMap.put(a.getId(), a.getCategoryId());
        }

        for (Article a : allArticles) {
            Map<Long, Double> similarities = new HashMap<>();
            for (Article b : allArticles) {
                if (a.getId().equals(b.getId())) continue;
                double sim = contentEngine.articleSimilarity(
                        a.getId(), b.getId(),
                        articleTagsMap.get(a.getId()), articleTagsMap.get(b.getId()),
                        articleCategoryMap.get(a.getId()), articleCategoryMap.get(b.getId()));
                if (sim > 0) {
                    similarities.put(b.getId(), sim);
                }
            }
            cacheService.cacheContentSimilarity(a.getId(), similarities);
        }
        log.info("内容相似度矩阵重算完成");
    }

    @Override
    public void recomputeCFSimilarityMatrix() {
        log.info("开始重算协同过滤相似度矩阵");
        List<UserBehavior> allBehaviors = userBehaviorMapper.listAll();
        Map<Long, Map<Long, Double>> ratingMatrix = cfEngine.buildRatingMatrix(allBehaviors);

        // Transpose: articleId -> (userId -> score)
        Map<Long, Map<Long, Double>> articleRatings = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> userEntry : ratingMatrix.entrySet()) {
            Long userId = userEntry.getKey();
            for (Map.Entry<Long, Double> articleEntry : userEntry.getValue().entrySet()) {
                articleRatings.computeIfAbsent(articleEntry.getKey(), k -> new HashMap<>())
                        .put(userId, articleEntry.getValue());
            }
        }

        List<Long> articleIds = new ArrayList<>(articleRatings.keySet());
        for (int i = 0; i < articleIds.size(); i++) {
            Long articleA = articleIds.get(i);
            if (userBehaviorMapper.countByArticleId(articleA) < RecommendConstants.CF_MIN_BEHAVIOR_COUNT) {
                continue;
            }
            Map<Long, Double> similarities = new HashMap<>();
            for (int j = 0; j < articleIds.size(); j++) {
                if (i == j) continue;
                Long articleB = articleIds.get(j);
                double sim = cfEngine.cosineSimilarity(
                        articleRatings.get(articleA), articleRatings.get(articleB));
                if (sim > 0) {
                    similarities.put(articleB, sim);
                }
            }
            cacheService.cacheCFSimilarity(articleA, similarities);
        }
        log.info("协同过滤相似度矩阵重算完成");
    }

    @Override
    @Async
    public void refreshUserRecommendations(Long userId) {
        log.info("异步刷新用户 {} 的推荐缓存", userId);
        cacheService.evictUserRecommendations(userId);
        int behaviorCount = behaviorService.getUserBehaviorCount(userId);
        boolean hasInterests = interestService.hasInterests(userId);

        if (behaviorCount >= RecommendConstants.COLD_START_THRESHOLD) {
            computeFullRecommendations(userId, RecommendConstants.RECOMMEND_SIZE);
        } else if (hasInterests) {
            computeColdStartRecommendations(userId, RecommendConstants.RECOMMEND_SIZE);
        }
    }

    private List<Long> normalizeAndTopN(Map<Long, Double> scores, int topN) {
        if (scores.isEmpty()) return Collections.emptyList();

        double min = Collections.min(scores.values());
        double max = Collections.max(scores.values());
        double range = max - min;

        return scores.entrySet().stream()
                .sorted((a, b) -> Double.compare(
                        range == 0 ? 0 : (b.getValue() - min) / range,
                        range == 0 ? 0 : (a.getValue() - min) / range))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<ArticleShortVO> toArticleVOs(List<Long> articleIds) {
        if (articleIds.isEmpty()) return Collections.emptyList();
        List<ArticleShortVO> result = new ArrayList<>();
        for (Long id : articleIds) {
            Article article = articleMapper.getById(id);
            if (article != null) {
                ArticleShortVO vo = new ArticleShortVO();
                BeanUtils.copyProperties(article, vo);
                result.add(vo);
            }
        }
        return result;
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/recommend/RecommendCacheService.java backend/blog-server/src/main/java/top/hazenix/service/RecommendService.java backend/blog-server/src/main/java/top/hazenix/service/impl/RecommendServiceImpl.java
git commit -m "feat(recommend): add RecommendCacheService and RecommendService orchestrator"
```

---

## Task 6: Controllers, Security Config & Integration

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/user/UserBehaviorController.java`
- Create: `backend/blog-server/src/main/java/top/hazenix/controller/user/UserInterestController.java`
- Modify: `backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleController.java:88-97`
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleServiceImpl.java:254-320,327-372`
- Modify: `backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java:61-70`

- [ ] **Step 1: Create UserBehaviorController**

```java
// backend/blog-server/src/main/java/top/hazenix/controller/user/UserBehaviorController.java
package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.UserBehaviorDTO;
import top.hazenix.result.Result;
import top.hazenix.service.RecommendService;
import top.hazenix.service.UserBehaviorService;

@RestController
@RequestMapping("/user/behavior")
@Slf4j
@RequiredArgsConstructor
public class UserBehaviorController {

    private final UserBehaviorService userBehaviorService;
    private final RecommendService recommendService;

    @PostMapping
    public Result reportBehavior(@RequestBody UserBehaviorDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.success(); // 匿名用户不记录
        }
        log.info("用户 {} 上报浏览行为: articleId={}, duration={}", userId, dto.getArticleId(), dto.getDuration());
        userBehaviorService.recordView(userId, dto.getArticleId(), dto.getDuration());
        recommendService.refreshUserRecommendations(userId);
        return Result.success();
    }
}
```

- [ ] **Step 2: Create UserInterestController**

```java
// backend/blog-server/src/main/java/top/hazenix/controller/user/UserInterestController.java
package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.UserInterestDTO;
import top.hazenix.entity.UserInterest;
import top.hazenix.result.Result;
import top.hazenix.service.UserInterestService;

import java.util.List;

@RestController
@RequestMapping("/user/interests")
@Slf4j
@RequiredArgsConstructor
public class UserInterestController {

    private final UserInterestService userInterestService;

    @GetMapping
    public Result getInterests() {
        Long userId = BaseContext.getCurrentId();
        List<UserInterest> interests = userInterestService.getInterests(userId);
        return Result.success(interests);
    }

    @PostMapping
    public Result setInterests(@RequestBody UserInterestDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户 {} 设置兴趣标签: {}", userId, dto.getTagIds());
        userInterestService.setInterests(userId, dto.getTagIds());
        return Result.success();
    }

    @PutMapping
    public Result updateInterests(@RequestBody UserInterestDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户 {} 更新兴趣标签: {}", userId, dto.getTagIds());
        userInterestService.updateInterests(userId, dto.getTagIds());
        return Result.success();
    }
}
```

- [ ] **Step 3: Modify ArticleController — implement /recommended endpoint**

Replace the existing stub at line 88-97 in `backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleController.java`:

```java
    /**
     * 获取推荐文章
     * @return
     */
    @GetMapping("/recommended")
    public Result getRecommendedArticles(@RequestParam(defaultValue = "10") Integer size){
        log.info("获取推荐文章");
        Long userId = BaseContext.getCurrentId();
        List<ArticleShortVO> list = recommendService.getRecommendations(userId, size);
        return Result.success(list);
    }
```

Add `RecommendService` as a dependency:
```java
private final RecommendService recommendService;
```

Add import:
```java
import top.hazenix.context.BaseContext;
import top.hazenix.service.RecommendService;
```

- [ ] **Step 4: Modify ArticleServiceImpl — add behavior recording to like/favorite**

In `likeArticle()` method, after the like is successfully recorded (inside the `else` block at line 289 and line 311), add:

```java
userBehaviorService.recordLike(userId, id);
recommendService.refreshUserRecommendations(userId);
```

In `favoriteArticle()` method, after the favorite is successfully recorded (inside the `else` block at line 355 and line 370), add:

```java
userBehaviorService.recordFavorite(userId, id);
recommendService.refreshUserRecommendations(userId);
```

Add dependencies to `ArticleServiceImpl`:
```java
private final UserBehaviorService userBehaviorService;
private final RecommendService recommendService;
```

Add imports:
```java
import top.hazenix.service.UserBehaviorService;
import top.hazenix.service.RecommendService;
```

- [ ] **Step 5: Modify SecurityConfig — add permit rules**

In `SecurityConfig.java`, add to the authenticated endpoints list:

```java
.antMatchers(
        "/user/user/logout",
        "/user/user/userinfo",
        "/user/user/stats",
        "/user/user/profile",
        "/user/user/password",
        "/user/user/favorite",
        "/user/tree/**",
        "/user/articles/*/favorite",
        "/user/interests/**"  // 兴趣标签需要登录
).authenticated()
```

The `/user/behavior` and `/user/articles/recommended` endpoints are already covered by `.anyRequest().permitAll()` so no additional changes needed for those.

- [ ] **Step 6: Verify compilation**

Run: `cd backend && mvn compile -pl blog-server -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/controller/user/UserBehaviorController.java backend/blog-server/src/main/java/top/hazenix/controller/user/UserInterestController.java backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleController.java backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleServiceImpl.java backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java
git commit -m "feat(recommend): add controllers, security config, and integrate with existing like/favorite"
```

---

## Task 7: Scheduled Tasks

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/task/RecommendTask.java`

- [ ] **Step 1: Create RecommendTask**

```java
// backend/blog-server/src/main/java/top/hazenix/task/RecommendTask.java
package top.hazenix.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.hazenix.service.RecommendService;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecommendTask {

    private final RecommendService recommendService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void recomputeCFMatrix() {
        log.info("【定时任务】开始重算协同过滤相似度矩阵");
        try {
            recommendService.recomputeCFSimilarityMatrix();
        } catch (Exception e) {
            log.error("协同过滤矩阵重算失败", e);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void recomputeContentMatrix() {
        log.info("【定时任务】开始重算内容相似度矩阵");
        try {
            recommendService.recomputeContentSimilarityMatrix();
        } catch (Exception e) {
            log.error("内容相似度矩阵重算失败", e);
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void refreshAnonymousRecommendations() {
        log.info("【定时任务】刷新匿名用户推荐");
        try {
            recommendService.recomputeAnonymousRecommendations();
        } catch (Exception e) {
            log.error("匿名推荐刷新失败", e);
        }
    }

    @PostConstruct
    public void warmUpOnStartup() {
        log.info("【启动预热】初始化推荐系统缓存");
        try {
            recommendService.recomputeAnonymousRecommendations();
            recommendService.recomputeContentSimilarityMatrix();
        } catch (Exception e) {
            log.error("推荐系统启动预热失败", e);
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd backend && mvn compile -pl blog-server -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 3: Run all tests**

Run: `cd backend && mvn test -pl blog-server -DfailIfNoTests=false`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add backend/blog-server/src/main/java/top/hazenix/task/RecommendTask.java
git commit -m "feat(recommend): add scheduled tasks for similarity matrix and anonymous recommendations"
```

---

## Task 8: Frontend — API Layer & Behavior Tracker

**Files:**
- Create: `frontend/src/api/recommend.js`
- Create: `frontend/src/composables/useBehaviorTracker.js`

- [ ] **Step 1: Create recommend API**

```javascript
// frontend/src/api/recommend.js
import request from '@/utils/request'

export function getRecommendedArticles(params = {}) {
    return request({
        url: '/user/articles/recommended',
        method: 'get',
        isPublicResource: true,
        params
    })
}

export function getUserInterests() {
    return request({
        url: '/user/interests',
        method: 'get'
    })
}

export function setUserInterests(tagIds) {
    return request({
        url: '/user/interests',
        method: 'post',
        data: { tagIds }
    })
}

export function updateUserInterests(tagIds) {
    return request({
        url: '/user/interests',
        method: 'put',
        data: { tagIds }
    })
}

export function reportBehavior(articleId, duration) {
    return request({
        url: '/user/behavior',
        method: 'post',
        data: { articleId, duration }
    })
}
```

- [ ] **Step 2: Create behavior tracker composable**

```javascript
// frontend/src/composables/useBehaviorTracker.js
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useUserStore } from '@/stores/user'
import { reportBehavior } from '@/api/recommend'

export function useBehaviorTracker(articleId) {
    const startTime = ref(null)
    const userStore = useUserStore()

    const sendBehavior = (duration) => {
        if (!userStore.token || !articleId) return
        const url = `/user/behavior`
        const data = JSON.stringify({ articleId: Number(articleId), duration })

        if (navigator.sendBeacon) {
            const blob = new Blob([data], { type: 'application/json' })
            // sendBeacon doesn't support custom headers, fall back to regular request
            reportBehavior(Number(articleId), duration).catch(() => {})
        } else {
            reportBehavior(Number(articleId), duration).catch(() => {})
        }
    }

    onMounted(() => {
        startTime.value = Date.now()
    })

    onBeforeUnmount(() => {
        if (startTime.value) {
            const duration = Math.floor((Date.now() - startTime.value) / 1000)
            if (duration > 0) {
                sendBehavior(duration)
            }
        }
    })

    return { startTime }
}
```

- [ ] **Step 3: Integrate behavior tracker into ArticleDetail view**

Find the article detail view (likely `frontend/src/views/ArticleDetail.vue` or similar) and add:

```javascript
import { useBehaviorTracker } from '@/composables/useBehaviorTracker'

// Inside <script setup>, after getting the article ID from route:
const route = useRoute()
const articleId = computed(() => route.params.id)
useBehaviorTracker(articleId.value)
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/api/recommend.js frontend/src/composables/useBehaviorTracker.js
git commit -m "feat(recommend): add frontend API layer and behavior tracking composable"
```

---

## Task 9: Frontend — RecommendSection & RecommendCard Components

**Files:**
- Create: `frontend/src/components/article/RecommendCard.vue`
- Create: `frontend/src/components/article/RecommendSection.vue`
- Modify: `frontend/src/views/Home.vue`

**Note:** Use the `frontend-design` skill for the visual design of these components. The components should match the existing blog's Tailwind CSS + dark mode patterns.

- [ ] **Step 1: Create RecommendCard.vue**

```vue
<!-- frontend/src/components/article/RecommendCard.vue -->
<template>
  <div
    class="flex-shrink-0 w-72 bg-white dark:bg-gray-800 rounded-lg shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-1 cursor-pointer overflow-hidden"
    @click="$emit('click', article)"
  >
    <div class="h-36 bg-gray-100 dark:bg-gray-700 overflow-hidden" v-if="article.coverImage">
      <img :src="article.coverImage" :alt="article.title" class="w-full h-full object-cover" />
    </div>
    <div class="h-36 bg-gradient-to-br from-primary/10 to-primary/5 dark:from-primary/20 dark:to-primary/10 flex items-center justify-center" v-else>
      <span class="text-4xl text-primary/30">📄</span>
    </div>
    <div class="p-4">
      <h3 class="text-sm font-semibold text-gray-900 dark:text-white line-clamp-2 leading-snug">
        {{ article.title }}
      </h3>
      <div class="mt-2 flex items-center gap-3 text-xs text-gray-400">
        <span>{{ article.viewCount || 0 }} 阅读</span>
        <span>{{ article.likeCount || 0 }} 点赞</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  article: { type: Object, required: true }
})
defineEmits(['click'])
</script>
```

- [ ] **Step 2: Create RecommendSection.vue**

```vue
<!-- frontend/src/components/article/RecommendSection.vue -->
<template>
  <div class="w-full" v-if="articles.length > 0">
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center gap-3">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white">
          {{ isLoggedIn ? '为你推荐' : '热门推荐' }}
        </h2>
        <span v-if="!isLoggedIn" class="text-sm text-gray-400 dark:text-gray-500">
          登录获取个性化推荐
        </span>
      </div>
      <button
        v-if="isLoggedIn"
        @click="refresh"
        class="text-sm text-primary hover:text-primary/80 transition-colors"
        :disabled="loading"
      >
        换一批
      </button>
    </div>

    <div class="relative group">
      <!-- Left arrow -->
      <button
        v-show="canScrollLeft"
        @click="scrollLeft"
        class="absolute left-0 top-1/2 -translate-y-1/2 z-10 w-8 h-8 bg-white/90 dark:bg-gray-800/90 rounded-full shadow-md flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
      >
        <span class="text-gray-600 dark:text-gray-300">&lt;</span>
      </button>

      <!-- Scrollable container -->
      <div
        ref="scrollContainer"
        class="flex gap-4 overflow-x-auto scrollbar-hide scroll-smooth pb-2"
        @scroll="updateScrollState"
      >
        <RecommendCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
          @click="goToArticle(article)"
        />
      </div>

      <!-- Right arrow -->
      <button
        v-show="canScrollRight"
        @click="scrollRight"
        class="absolute right-0 top-1/2 -translate-y-1/2 z-10 w-8 h-8 bg-white/90 dark:bg-gray-800/90 rounded-full shadow-md flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
      >
        <span class="text-gray-600 dark:text-gray-300">&gt;</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getRecommendedArticles } from '@/api/recommend'
import RecommendCard from './RecommendCard.vue'

const router = useRouter()
const userStore = useUserStore()
const articles = ref([])
const loading = ref(false)
const scrollContainer = ref(null)
const canScrollLeft = ref(false)
const canScrollRight = ref(false)

const isLoggedIn = ref(!!userStore.token)

const fetchRecommendations = async () => {
  loading.value = true
  try {
    const res = await getRecommendedArticles({ size: 10 })
    if (res && res.data) {
      articles.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (error) {
    articles.value = []
  } finally {
    loading.value = false
    await nextTick()
    updateScrollState()
  }
}

const refresh = () => {
  fetchRecommendations()
}

const goToArticle = (article) => {
  router.push({ name: 'ArticleDetail', params: { id: article.id } })
}

const scrollLeft = () => {
  if (scrollContainer.value) {
    scrollContainer.value.scrollBy({ left: -300, behavior: 'smooth' })
  }
}

const scrollRight = () => {
  if (scrollContainer.value) {
    scrollContainer.value.scrollBy({ left: 300, behavior: 'smooth' })
  }
}

const updateScrollState = () => {
  if (!scrollContainer.value) return
  const el = scrollContainer.value
  canScrollLeft.value = el.scrollLeft > 0
  canScrollRight.value = el.scrollLeft + el.clientWidth < el.scrollWidth - 1
}

onMounted(() => {
  fetchRecommendations()
})
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
```

- [ ] **Step 3: Modify Home.vue — insert RecommendSection**

In `frontend/src/views/Home.vue`, add the import:

```javascript
import RecommendSection from '@/components/article/RecommendSection.vue'
```

In the template, insert between the Hero section and the content grid (after the closing `</div>` of the hero section, before `<div class="mt-24 grid ..."`):

```html
    <!-- 推荐文章 -->
    <div class="mt-16">
      <RecommendSection />
    </div>
```

- [ ] **Step 4: Verify frontend builds**

Run: `cd frontend && npm run build`
Expected: Build succeeds with no errors

- [ ] **Step 5: Commit**

```bash
git add frontend/src/components/article/RecommendCard.vue frontend/src/components/article/RecommendSection.vue frontend/src/views/Home.vue
git commit -m "feat(recommend): add RecommendSection and RecommendCard components to homepage"
```

---

## Task 10: Frontend — Interest Tag Selector & Registration Integration

**Files:**
- Create: `frontend/src/components/common/InterestTagSelector.vue`
- Modify: `frontend/src/views/Register.vue`

- [ ] **Step 1: Create InterestTagSelector.vue**

```vue
<!-- frontend/src/components/common/InterestTagSelector.vue -->
<template>
  <el-dialog
    v-model="visible"
    title="选择你感兴趣的标签"
    width="500px"
    :close-on-click-modal="false"
  >
    <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">
      选择感兴趣的标签，我们会为你推荐相关文章（可跳过）
    </p>
    <div class="flex flex-wrap gap-2">
      <el-tag
        v-for="tag in allTags"
        :key="tag.id"
        :type="selectedIds.includes(tag.id) ? '' : 'info'"
        :effect="selectedIds.includes(tag.id) ? 'dark' : 'plain'"
        class="cursor-pointer text-sm"
        @click="toggleTag(tag.id)"
      >
        {{ tag.name }}
      </el-tag>
    </div>
    <p class="mt-3 text-xs text-gray-400">
      已选 {{ selectedIds.length }} / 10 个标签
    </p>
    <template #footer>
      <el-button @click="skip">跳过</el-button>
      <el-button type="primary" @click="confirm" :disabled="selectedIds.length === 0">
        确认 ({{ selectedIds.length }})
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTagList } from '@/api/tag'
import { setUserInterests } from '@/api/recommend'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'done'])

const visible = ref(props.modelValue)
const allTags = ref([])
const selectedIds = ref([])

const toggleTag = (id) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else if (selectedIds.value.length < 10) {
    selectedIds.value.push(id)
  } else {
    ElMessage.warning('最多选择 10 个标签')
  }
}

const confirm = async () => {
  try {
    await setUserInterests(selectedIds.value)
    ElMessage.success('兴趣标签设置成功')
    visible.value = false
    emit('update:modelValue', false)
    emit('done')
  } catch (error) {
    ElMessage.error('设置失败，请稍后重试')
  }
}

const skip = () => {
  visible.value = false
  emit('update:modelValue', false)
  emit('done')
}

onMounted(async () => {
  try {
    const res = await getTagList()
    if (res && res.data) {
      allTags.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (error) {
    allTags.value = []
  }
})

// Watch prop changes
import { watch } from 'vue'
watch(() => props.modelValue, (val) => { visible.value = val })
watch(visible, (val) => { emit('update:modelValue', val) })
</script>
```

- [ ] **Step 2: Modify Register.vue — show interest selector after registration**

In `frontend/src/views/Register.vue`, add imports:

```javascript
import InterestTagSelector from '@/components/common/InterestTagSelector.vue'
```

Add state:

```javascript
const showInterestSelector = ref(false)
```

Modify `handleRegister` — replace `router.push('/')` with:

```javascript
showInterestSelector.value = true
```

Add handler:

```javascript
const onInterestDone = () => {
  router.push('/')
}
```

Add to template (before closing `</div>`):

```html
<InterestTagSelector v-model="showInterestSelector" @done="onInterestDone" />
```

- [ ] **Step 3: Verify frontend builds**

Run: `cd frontend && npm run build`
Expected: Build succeeds

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/common/InterestTagSelector.vue frontend/src/views/Register.vue
git commit -m "feat(recommend): add interest tag selector and integrate with registration flow"
```

---

## Task 11: End-to-End Verification

- [ ] **Step 1: Start backend and verify API**

Run: `cd backend && mvn spring-boot:run -pl blog-server`

Test endpoints:
```bash
# Anonymous recommendations
curl http://localhost:8080/user/articles/recommended?size=10

# Should return popular articles as fallback
```

- [ ] **Step 2: Start frontend and verify UI**

Run: `cd frontend && npm run dev`

Open browser and verify:
1. Homepage shows recommendation section below Hero
2. Anonymous user sees "热门推荐" title with "登录获取个性化推荐" hint
3. Horizontal scroll works with mouse drag
4. Cards display article info correctly
5. Dark mode works

- [ ] **Step 3: Test logged-in flow**

1. Register a new user
2. Interest tag selector appears after registration
3. Select some tags and confirm
4. Homepage shows "为你推荐" title
5. "换一批" button works

- [ ] **Step 4: Test behavior tracking**

1. Open an article detail page while logged in
2. Stay for > 30 seconds
3. Navigate away
4. Check backend logs for behavior recording
5. Refresh homepage — recommendations should reflect the behavior

- [ ] **Step 5: Final commit**

```bash
git add -A
git commit -m "feat(recommend): complete personalized recommendation system"
```

---

## Summary

| Task | Description | Estimated Time |
|------|-------------|---------------|
| 1 | Database schema, entities, DTOs, constants | 10 min |
| 2 | MyBatis mappers | 10 min |
| 3 | UserBehaviorService, UserInterestService, AsyncConfig | 15 min |
| 4 | Three recommendation engines + tests | 20 min |
| 5 | RecommendCacheService, RecommendService orchestrator | 20 min |
| 6 | Controllers, security config, integration | 15 min |
| 7 | Scheduled tasks | 5 min |
| 8 | Frontend API layer, behavior tracker | 10 min |
| 9 | RecommendSection, RecommendCard, Home.vue | 15 min |
| 10 | InterestTagSelector, Register.vue | 10 min |
| 11 | End-to-end verification | 15 min |
| **Total** | | **~2.5 hours** |
