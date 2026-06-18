# 匿名评论实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让未登录用户通过填写昵称（必填）+ 邮箱（可选）直接发表评论，无需登录或审核。

**Architecture:** 单一接口方案——把 `POST /user/comments` 改为公开访问，Service 层根据 `BaseContext.getCurrentId()` 分登录 / 匿名两条分支。匿名请求用内存限流（同 IP 1 分钟 6 条）。前端根据登录态切换表单形态，匿名用户头像用 `minidenticons` 本地生成。

**Tech Stack:** Spring Security、Spring Boot、MyBatis（后端）；Vue 3 + Element Plus（前端）；`minidenticons` (~1KB 前端依赖)。

---

## 参考文件（请先阅读再动手）

- 设计文档：`documents/superpowers/specs/2026-04-30-anonymous-comments-design.md`
- 现有实现：
  - `backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java`
  - `backend/blog-server/src/main/java/top/hazenix/controller/user/CommentsController.java`
  - `backend/blog-server/src/main/java/top/hazenix/service/impl/CommentsServiceImpl.java`
  - `backend/blog-server/src/main/resources/mapper/CommentsMapper.xml`
  - `backend/blog-pojo/src/main/java/top/hazenix/entity/Comments.java`
  - `backend/blog-pojo/src/main/java/top/hazenix/dto/CommentsDTO.java`
  - `backend/blog-pojo/src/main/java/top/hazenix/vo/CommentsVO.java`
  - `backend/blog-common/src/main/java/top/hazenix/constant/ErrorCode.java`
  - `backend/blog-common/src/main/java/top/hazenix/constant/MessageConstant.java`
  - `frontend/src/components/article/CommentList.vue`
  - `frontend/src/components/article/CommentItem.vue`

---

## File Structure（本次改动的文件一览）

**后端改动**
- Modify: `backend/blog-common/src/main/java/top/hazenix/constant/ErrorCode.java` — 新增 `A03002` / `A03003`。
- Modify: `backend/blog-common/src/main/java/top/hazenix/constant/MessageConstant.java` — 新增错误消息常量。
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/entity/Comments.java` — 新增 `email` 字段。
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/dto/CommentsDTO.java` — 新增 `email` 字段。
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/vo/CommentsVO.java` — 新增 `email`、`isAnonymous` 字段。
- Modify: `backend/blog-server/src/main/resources/mapper/CommentsMapper.xml` — INSERT 增加 `email` 列。
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/CommentsServiceImpl.java` — `addComments` 分登录/匿名分支；`getCommentsList` 设置 `isAnonymous`。
- Create: `backend/blog-server/src/main/java/top/hazenix/security/CommentRateLimiter.java` — 内存滑动窗口限流组件。
- Modify: `backend/blog-server/src/main/java/top/hazenix/controller/user/CommentsController.java` — 注入限流器 + 传递 IP。
- Modify: `backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java` — 把 `/user/comments` 从 `authenticated()` 移除。
- Create: `backend/blog-server/src/test/java/top/hazenix/test/CommentsServiceImplAnonymousTest.java` — Service 单元测试。
- Create: `backend/blog-server/src/test/java/top/hazenix/test/CommentRateLimiterTest.java` — 限流器单元测试。
- 数据库迁移：`comments` 表 `ADD COLUMN email`（本计划附一个 SQL 任务，由人工在数据库执行）。

**前端改动**
- Modify: `frontend/package.json` — 新增 `minidenticons` 依赖。
- Modify: `frontend/src/components/article/CommentList.vue` — 移除登录拦截、增加匿名字段、Identicon 渲染。
- Modify: `frontend/src/components/article/CommentItem.vue` — 内联回复表单支持匿名字段、Identicon 渲染。

---

## Task 1: 数据库迁移 + 实体字段扩展

**Files:**
- Manual SQL (由人工执行): 无文件
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/entity/Comments.java`
- Modify: `backend/blog-server/src/main/resources/mapper/CommentsMapper.xml`

- [ ] **Step 1：执行数据库迁移（人工）**

在数据库执行：

```sql
ALTER TABLE comments
  ADD COLUMN email VARCHAR(100) NULL COMMENT '匿名评论者邮箱，可空' AFTER reply_username;
```

Expected：`email` 列已添加；现有行 `email` 为 `NULL`。

- [ ] **Step 2：给 `Comments` 实体加 `email` 字段**

编辑 `backend/blog-pojo/src/main/java/top/hazenix/entity/Comments.java`，在 `content` 字段之后、`status` 字段之前插入：

```java
@ApiModelProperty(value = "匿名评论者邮箱（可空）", example = "guest@example.com")
private String email;
```

- [ ] **Step 3：更新 `CommentsMapper.xml` 的 INSERT**

在 `backend/blog-server/src/main/resources/mapper/CommentsMapper.xml` 中把 `<insert id="insert">` 改为：

```xml
<insert id="insert">
    insert into comments
    (article_id,parent_id,user_id,username,reply_id,reply_username,content,email,status,create_time)
    values
        (#{articleId},#{parentId},#{userId},#{username},#{replyId},#{replyUsername},#{content},#{email},#{status},#{createTime})
</insert>
```

- [ ] **Step 4：编译后端，确认通过**

Run：`cd backend && mvn -q -pl blog-pojo,blog-server -am compile`
Expected：BUILD SUCCESS。

- [ ] **Step 5：提交**

```bash
git add backend/blog-pojo/src/main/java/top/hazenix/entity/Comments.java \
        backend/blog-server/src/main/resources/mapper/CommentsMapper.xml
git commit -m "feat(comments): add email column for anonymous comments"
```

---

## Task 2: DTO / VO 字段扩展

**Files:**
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/dto/CommentsDTO.java`
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/vo/CommentsVO.java`

- [ ] **Step 1：在 `CommentsDTO` 加 `email` 字段**

在 `CommentsDTO.java` 的 `replyId` 字段之后加：

```java
@ApiModelProperty(value = "匿名评论者邮箱（可选）", example = "guest@example.com")
@javax.validation.constraints.Email(message = "邮箱格式不正确")
@javax.validation.constraints.Size(max = 100, message = "邮箱长度不能超过100个字符")
private String email;
```

注意：`username` 字段保持现状（依旧是 `@Size(max = 30)` 且允许为空——登录场景无需前端传），匿名场景的必填校验放在 Service 里统一做，避免影响登录场景。

- [ ] **Step 2：在 `CommentsVO` 加字段**

在 `backend/blog-pojo/src/main/java/top/hazenix/vo/CommentsVO.java` 中新增两个字段（如已有字段保持风格一致）：

```java
@ApiModelProperty(value = "匿名评论者邮箱（仅后台可见）", example = "guest@example.com")
private String email;

@ApiModelProperty(value = "是否匿名评论", example = "true")
private Boolean isAnonymous;
```

- [ ] **Step 3：编译通过**

Run：`cd backend && mvn -q -pl blog-pojo -am compile`
Expected：BUILD SUCCESS。

- [ ] **Step 4：提交**

```bash
git add backend/blog-pojo/src/main/java/top/hazenix/dto/CommentsDTO.java \
        backend/blog-pojo/src/main/java/top/hazenix/vo/CommentsVO.java
git commit -m "feat(comments): add email and isAnonymous fields in DTO/VO"
```

---

## Task 3: 错误码与消息常量

**Files:**
- Modify: `backend/blog-common/src/main/java/top/hazenix/constant/ErrorCode.java`
- Modify: `backend/blog-common/src/main/java/top/hazenix/constant/MessageConstant.java`

- [ ] **Step 1：新增错误码**

在 `ErrorCode.java` 第 74 行 `A03001` 之后追加：

```java
/** 匿名评论昵称必填/长度不合法 */
public static final String A03002 = "A03002";
/** 评论提交过于频繁（限流） */
public static final String A03003 = "A03003";
```

- [ ] **Step 2：新增消息常量**

在 `MessageConstant.java` 中（`REPLYER_NOT_FOUND` 附近）追加：

```java
public static final String ANONYMOUS_NICKNAME_REQUIRED = "昵称必填且长度 1-30";
public static final String COMMENT_RATE_LIMIT_EXCEEDED = "评论太频繁，请稍后再试";
```

- [ ] **Step 3：编译通过**

Run：`cd backend && mvn -q -pl blog-common -am compile`
Expected：BUILD SUCCESS。

- [ ] **Step 4：提交**

```bash
git add backend/blog-common/src/main/java/top/hazenix/constant/ErrorCode.java \
        backend/blog-common/src/main/java/top/hazenix/constant/MessageConstant.java
git commit -m "feat(comments): add anonymous comment error codes"
```

---

## Task 4: 匿名限流组件（内存滑动窗口）

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/security/CommentRateLimiter.java`
- Test: `backend/blog-server/src/test/java/top/hazenix/test/CommentRateLimiterTest.java`

- [ ] **Step 1：写失败的测试**

创建 `backend/blog-server/src/test/java/top/hazenix/test/CommentRateLimiterTest.java`：

```java
package top.hazenix.test;

import org.junit.jupiter.api.Test;
import top.hazenix.security.CommentRateLimiter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommentRateLimiterTest {

    @Test
    public void tryAcquire_allowsUpToLimit_thenRejects() {
        // maxPerMinute = 6
        CommentRateLimiter limiter = new CommentRateLimiter(6, 60_000L);
        String ip = "1.2.3.4";
        for (int i = 0; i < 6; i++) {
            assertTrue(limiter.tryAcquire(ip), "第 " + (i + 1) + " 次应允许");
        }
        assertFalse(limiter.tryAcquire(ip), "第 7 次应被限流");
    }

    @Test
    public void tryAcquire_differentIpsIndependent() {
        CommentRateLimiter limiter = new CommentRateLimiter(2, 60_000L);
        assertTrue(limiter.tryAcquire("ip-a"));
        assertTrue(limiter.tryAcquire("ip-a"));
        assertFalse(limiter.tryAcquire("ip-a"));
        assertTrue(limiter.tryAcquire("ip-b"));
    }

    @Test
    public void tryAcquire_slidesWindow_afterExpiry() throws InterruptedException {
        // 100ms 窗口，方便测试
        CommentRateLimiter limiter = new CommentRateLimiter(2, 100L);
        String ip = "1.2.3.4";
        assertTrue(limiter.tryAcquire(ip));
        assertTrue(limiter.tryAcquire(ip));
        assertFalse(limiter.tryAcquire(ip));
        Thread.sleep(150L);
        assertTrue(limiter.tryAcquire(ip), "窗口过期后应重新允许");
    }
}
```

- [ ] **Step 2：运行测试确认失败**

Run：`cd backend && mvn -q -pl blog-server -am test -Dtest=CommentRateLimiterTest`
Expected：FAIL（`CommentRateLimiter` 类不存在）。

- [ ] **Step 3：实现限流器**

创建 `backend/blog-server/src/main/java/top/hazenix/security/CommentRateLimiter.java`：

```java
package top.hazenix.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 匿名评论的内存滑动窗口限流器。
 * 单机部署；多实例部署时应替换为 Redis 实现。
 */
@Component
public class CommentRateLimiter {

    private final int maxPerWindow;
    private final long windowMs;
    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public CommentRateLimiter() {
        this(6, 60_000L);
    }

    public CommentRateLimiter(
            @Value("${blog.comments.anonymous.rate-limit.max-per-minute:6}") int maxPerWindow,
            @Value("${blog.comments.anonymous.rate-limit.window-ms:60000}") long windowMs) {
        this.maxPerWindow = maxPerWindow;
        this.windowMs = windowMs;
    }

    public boolean tryAcquire(String key) {
        if (key == null || key.isEmpty()) {
            return true; // 无法识别来源时不限流，避免误伤
        }
        long now = System.currentTimeMillis();
        long cutoff = now - windowMs;
        Deque<Long> queue = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst() < cutoff) {
                queue.pollFirst();
            }
            if (queue.size() >= maxPerWindow) {
                return false;
            }
            queue.addLast(now);
            return true;
        }
    }
}
```

注意：构造函数有两个——`@Autowired` 会选择参数多的那个（Spring 默认规则），Spring Boot 会把 `@Value` 值注入。无参构造仅用于测试便利时使用默认值——但由于 Spring 优先选有 `@Autowired`/更多参数的构造器，实际运行时会用到带参数的构造器。为了保险，改用单构造器+测试中直接用带参数版本：

删除无参构造器，只保留：

```java
public CommentRateLimiter(
        @Value("${blog.comments.anonymous.rate-limit.max-per-minute:6}") int maxPerWindow,
        @Value("${blog.comments.anonymous.rate-limit.window-ms:60000}") long windowMs) {
    this.maxPerWindow = maxPerWindow;
    this.windowMs = windowMs;
}
```

测试里已经用 `new CommentRateLimiter(6, 60_000L)` 显式传参，这样不需要无参构造。

- [ ] **Step 4：运行测试确认通过**

Run：`cd backend && mvn -q -pl blog-server -am test -Dtest=CommentRateLimiterTest`
Expected：3 tests PASS。

- [ ] **Step 5：提交**

```bash
git add backend/blog-server/src/main/java/top/hazenix/security/CommentRateLimiter.java \
        backend/blog-server/src/test/java/top/hazenix/test/CommentRateLimiterTest.java
git commit -m "feat(comments): add in-memory sliding-window rate limiter"
```

---

## Task 5: Service 层增加匿名分支

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/CommentsServiceImpl.java`
- Test: `backend/blog-server/src/test/java/top/hazenix/test/CommentsServiceImplAnonymousTest.java`

- [ ] **Step 1：写失败的测试**

创建 `backend/blog-server/src/test/java/top/hazenix/test/CommentsServiceImplAnonymousTest.java`：

```java
package top.hazenix.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.CommentsDTO;
import top.hazenix.entity.Comments;
import top.hazenix.entity.User;
import top.hazenix.exception.BussinessException;
import top.hazenix.mapper.ArticleMapper;
import top.hazenix.mapper.CommentsMapper;
import top.hazenix.mapper.UserMapper;
import top.hazenix.service.impl.CommentsServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommentsServiceImplAnonymousTest {

    private CommentsMapper commentsMapper;
    private ArticleMapper articleMapper;
    private UserMapper userMapper;
    private CommentsServiceImpl service;

    @BeforeEach
    public void setUp() {
        commentsMapper = mock(CommentsMapper.class);
        articleMapper = mock(ArticleMapper.class);
        userMapper = mock(UserMapper.class);
        service = new CommentsServiceImpl(commentsMapper, articleMapper, userMapper);
        BaseContext.removeCurrentId();
    }

    @AfterEach
    public void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    public void addComments_anonymous_savesNicknameAndNullUserId() {
        CommentsDTO dto = new CommentsDTO();
        dto.setArticleId(1L);
        dto.setContent("hello");
        dto.setUsername("访客甲");
        dto.setEmail("g@example.com");

        service.addComments(dto);

        ArgumentCaptor<Comments> captor = ArgumentCaptor.forClass(Comments.class);
        verify(commentsMapper).insert(captor.capture());
        Comments saved = captor.getValue();
        assertNull(saved.getUserId(), "匿名评论 user_id 应为 null");
        assertEquals("访客甲", saved.getUsername());
        assertEquals("g@example.com", saved.getEmail());
    }

    @Test
    public void addComments_anonymous_blankNicknameThrows() {
        CommentsDTO dto = new CommentsDTO();
        dto.setArticleId(1L);
        dto.setContent("hello");
        dto.setUsername("   ");

        assertThrows(BussinessException.class, () -> service.addComments(dto));
        verify(commentsMapper, never()).insert(any());
    }

    @Test
    public void addComments_loggedIn_overridesDtoUsername() {
        BaseContext.setCurrentId(42L);
        User dbUser = new User();
        dbUser.setId(42L);
        dbUser.setUsername("realUser");
        when(userMapper.getById(42L)).thenReturn(dbUser);

        CommentsDTO dto = new CommentsDTO();
        dto.setArticleId(1L);
        dto.setContent("hello");
        dto.setUsername("伪造昵称");
        dto.setEmail("spoof@example.com");

        service.addComments(dto);

        ArgumentCaptor<Comments> captor = ArgumentCaptor.forClass(Comments.class);
        verify(commentsMapper).insert(captor.capture());
        Comments saved = captor.getValue();
        assertEquals(Long.valueOf(42L), saved.getUserId());
        assertEquals("realUser", saved.getUsername(), "登录用户 username 必须从 user 表覆盖 DTO 值");
        assertNull(saved.getEmail(), "登录用户不应写入 email");
    }
}
```

注意：`CommentsServiceImpl` 当前用 `@RequiredArgsConstructor` 自动生成构造器，参数顺序为 `commentsMapper, articleMapper, userMapper`；如果 Lombok 构造器参数顺序不同，请调整测试里 `new CommentsServiceImpl(...)` 的参数顺序为字段声明顺序。

- [ ] **Step 2：运行测试确认失败**

Run：`cd backend && mvn -q -pl blog-server -am test -Dtest=CommentsServiceImplAnonymousTest`
Expected：FAIL（第 1 个测试断言 `userId == null` 失败——当前实现总是调用 `BaseContext.getCurrentId()` 并设置，即便是 null 也会写 null，但第 2 个测试「匿名空昵称抛异常」一定失败——当前实现没有这层校验。）

- [ ] **Step 3：修改 `CommentsServiceImpl.addComments` 加入匿名分支**

将 `addComments` 方法替换为：

```java
@Override
public void addComments(CommentsDTO commentsDTO) {
    Comments comments = new Comments();
    BeanUtils.copyProperties(commentsDTO, comments);

    Long userId = BaseContext.getCurrentId();
    if (userId != null) {
        // 已登录：用登录身份，忽略 DTO 传入的 username/email
        comments.setUserId(userId);
        User dbUser = userMapper.getById(userId);
        if (dbUser != null) {
            comments.setUsername(dbUser.getUsername());
        }
        comments.setEmail(null);
    } else {
        // 匿名：昵称必填、长度 1-30
        String nickname = commentsDTO.getUsername();
        if (nickname == null || nickname.trim().isEmpty() || nickname.trim().length() > 30) {
            throw new BussinessException(
                    ErrorCode.A03002,
                    MessageConstant.ANONYMOUS_NICKNAME_REQUIRED
            );
        }
        comments.setUserId(null);
        comments.setUsername(nickname.trim());
        comments.setEmail(commentsDTO.getEmail());
    }

    if (commentsDTO.getReplyId() != null) {
        User replyUser = userMapper.getById(commentsDTO.getReplyId());
        if (replyUser != null) {
            comments.setReplyUsername(replyUser.getUsername());
        } else {
            throw new BussinessException(ErrorCode.A03001, MessageConstant.REPLYER_NOT_FOUND);
        }
    }
    comments.setStatus(CommonStatusConstants.NORMAL);
    comments.setCreateTime(LocalDateTime.now());
    commentsMapper.insert(comments);
}
```

别忘了在文件顶部 import：

```java
import top.hazenix.entity.User;
```

- [ ] **Step 4：在 `getCommentsList` 设置 `isAnonymous`**

`CommentsServiceImpl.getCommentsList` 现有循环内，把 `BeanUtils.copyProperties(comments, commentsVO)` 之后追加：

```java
commentsVO.setIsAnonymous(comments.getUserId() == null);
// 前端不需要 email，这里主动清空避免泄露
commentsVO.setEmail(null);
```

- [ ] **Step 5：运行测试确认通过**

Run：`cd backend && mvn -q -pl blog-server -am test -Dtest=CommentsServiceImplAnonymousTest`
Expected：3 tests PASS。

- [ ] **Step 6：跑全部相关测试确认没有回归**

Run：`cd backend && mvn -q -pl blog-server -am test -Dtest='Comments*'`
Expected：所有 tests PASS。

- [ ] **Step 7：提交**

```bash
git add backend/blog-server/src/main/java/top/hazenix/service/impl/CommentsServiceImpl.java \
        backend/blog-server/src/test/java/top/hazenix/test/CommentsServiceImplAnonymousTest.java
git commit -m "feat(comments): allow anonymous submissions with nickname validation"
```

---

## Task 6: Controller 集成限流 + 打开权限

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/controller/user/CommentsController.java`
- Modify: `backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java`

- [ ] **Step 1：修改 `CommentsController`**

把 `backend/blog-server/src/main/java/top/hazenix/controller/user/CommentsController.java` 整个类替换为：

```java
package top.hazenix.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import top.hazenix.constant.ErrorCode;
import top.hazenix.constant.MessageConstant;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.CommentsDTO;
import top.hazenix.exception.BussinessException;
import top.hazenix.result.Result;
import top.hazenix.security.CommentRateLimiter;
import top.hazenix.service.CommentsService;
import top.hazenix.vo.CommentsVO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController("UserCommentsController")
@RequestMapping("/user/comments")
@Slf4j
@RequiredArgsConstructor
public class CommentsController {

    private final CommentsService commentsService;
    private final CommentRateLimiter commentRateLimiter;

    /**
     * 获取评论列表（公开）
     */
    @GetMapping("/list")
    @Cacheable(cacheNames = "commentsCache", key = "#root.args[0].articleId+ '_' + #root.args[0].status")
    public Result<List<CommentsVO>> getCommentsList(CommentsDTO commentsDTO) {
        log.info("获取评论列表:{}", commentsDTO);
        List<CommentsVO> commentTree = commentsService.getCommentsList(commentsDTO);
        return Result.success(commentTree);
    }

    /**
     * 新增评论（支持匿名）
     */
    @PostMapping
    @CacheEvict(cacheNames = "commentsCache", key = "#root.args[0].articleId+ '_' + #root.args[0].status")
    public Result addComment(@Valid @RequestBody CommentsDTO commentsDTO, HttpServletRequest request) {
        log.info("新增评论：{}", commentsDTO);
        // 仅对匿名请求限流
        if (BaseContext.getCurrentId() == null) {
            String ip = resolveClientIp(request);
            if (!commentRateLimiter.tryAcquire(ip)) {
                throw new BussinessException(ErrorCode.A03003, MessageConstant.COMMENT_RATE_LIMIT_EXCEEDED);
            }
        }
        commentsService.addComments(commentsDTO);
        return Result.success();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String real = request.getHeader("X-Real-IP");
        if (real != null && !real.isEmpty()) {
            return real.trim();
        }
        return request.getRemoteAddr();
    }
}
```

- [ ] **Step 2：修改 `SecurityConfig`**

在 `backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java` 中，把 `authenticated()` 列表里的 `"/user/comments"` 移除；并把 `POST /user/comments` 及其 list 接口显式放行，避免依赖「未列 = permitAll」的隐式语义。

找到：

```java
.antMatchers(
        "/user/user/logout",
        "/user/user/userinfo",
        "/user/user/stats",
        "/user/user/profile",
        "/user/user/password",
        "/user/user/favorite",
        "/user/comments",
        "/user/tree/**",
        "/user/articles/*/favorite"
).authenticated()
```

改为：

```java
.antMatchers(
        "/user/user/logout",
        "/user/user/userinfo",
        "/user/user/stats",
        "/user/user/profile",
        "/user/user/password",
        "/user/user/favorite",
        "/user/tree/**",
        "/user/articles/*/favorite"
).authenticated()
```

并在上方 `.permitAll()` 的第二块（Swagger 那块）下面新增一块显式放行：

```java
// 评论接口对外开放（支持匿名评论）
.antMatchers(
        org.springframework.http.HttpMethod.GET, "/user/comments/list"
).permitAll()
.antMatchers(
        org.springframework.http.HttpMethod.POST, "/user/comments"
).permitAll()
```

- [ ] **Step 3：编译全后端**

Run：`cd backend && mvn -q compile`
Expected：BUILD SUCCESS。

- [ ] **Step 4：跑所有测试**

Run：`cd backend && mvn -q test`
Expected：所有 tests PASS。

- [ ] **Step 5：提交**

```bash
git add backend/blog-server/src/main/java/top/hazenix/controller/user/CommentsController.java \
        backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java
git commit -m "feat(comments): open comment endpoints to anonymous users with IP rate limit"
```

---

## Task 7: 后端冒烟测试（手动）

**Files:** 无文件改动

- [ ] **Step 1：启动后端**

Run：`cd backend && mvn -pl blog-server spring-boot:run`
Expected：应用启动成功。

- [ ] **Step 2：未登录匿名评论（昵称必填，成功）**

Run：

```bash
curl -s -X POST http://localhost:8080/user/comments \
  -H "Content-Type: application/json" \
  -d '{"articleId":1,"content":"anonymous hello","username":"访客甲","email":"g@example.com"}'
```

Expected：返回 `{"code":1,...}`（业务成功），数据库 `comments` 新增一行，`user_id = NULL`、`username = '访客甲'`、`email = 'g@example.com'`。

- [ ] **Step 3：未登录 + 空昵称（失败）**

Run：

```bash
curl -s -X POST http://localhost:8080/user/comments \
  -H "Content-Type: application/json" \
  -d '{"articleId":1,"content":"hello","username":""}'
```

Expected：返回业务错误，code `A03002`、msg `昵称必填且长度 1-30`。

- [ ] **Step 4：同 IP 限流**

连续 7 次发 `curl` 匿名评论，第 7 次返回 code `A03003`、msg `评论太频繁，请稍后再试`。

- [ ] **Step 5：已登录用户提交伪造昵称（应被覆盖）**

先登录拿到 token，然后：

```bash
curl -s -X POST http://localhost:8080/user/comments \
  -H "Content-Type: application/json" \
  -H "token: <real-token>" \
  -d '{"articleId":1,"content":"logged","username":"伪造昵称","email":"spoof@example.com"}'
```

Expected：入库 `username` = 真实登录用户名，`email = NULL`。

- [ ] **Step 6：原来需要登录的接口仍然需要登录**

Run：

```bash
curl -s http://localhost:8080/user/user/userinfo
```

Expected：返回 401 / 未登录错误（证明其他接口不受影响）。

- [ ] **Step 7：停止后端**

Ctrl+C。无需提交。

---

## Task 8: 前端加 minidenticons 依赖

**Files:**
- Modify: `frontend/package.json`

- [ ] **Step 1：安装依赖**

Run：`cd frontend && npm install minidenticons --save`
Expected：`package.json` 中 `dependencies` 新增 `minidenticons`。

- [ ] **Step 2：验证包可导入**

Run：`cd frontend && node -e "const m = require('minidenticons'); console.log(typeof m.minidenticon)"`
Expected：输出 `function`。

- [ ] **Step 3：提交**

```bash
git add frontend/package.json frontend/package-lock.json
git commit -m "feat(frontend): add minidenticons for anonymous avatars"
```

---

## Task 9: 前端 CommentList 支持匿名评论

**Files:**
- Modify: `frontend/src/components/article/CommentList.vue`

- [ ] **Step 1：在 `<script setup>` 最上方 imports 处加 minidenticons**

在 `frontend/src/components/article/CommentList.vue` 顶部 `import CommentItem from './CommentItem.vue'` 之后加：

```js
import { minidenticon } from 'minidenticons'
```

- [ ] **Step 2：新增匿名表单字段和头像生成工具**

在 `commentForm` reactive 定义的下面追加：

```js
// 匿名评论者信息（仅未登录时使用）
const anonymousForm = reactive({
  nickname: '',
  email: ''
})

// 生成 Identicon SVG 的 data URL
const identiconDataUrl = (seed) => {
  const svg = minidenticon(seed || 'anonymous', 95, 45)
  return 'data:image/svg+xml;utf8,' + encodeURIComponent(svg)
}
```

- [ ] **Step 3：修改 `commentRules`，加入匿名昵称校验**

把 `commentRules` 替换为：

```js
const commentRules = {
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 1, max: 500, message: '评论长度在 1 到 500 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 30, message: '昵称长度 1-30 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}
```

- [ ] **Step 4：替换模板——去掉"未登录提示"，改为"匿名表单"**

把模板里第 10-19 行（`<!-- 未登录用户提示 -->` 那整块）删除。

把第 21 行的 `v-if="isLoggedIn && showCommentForm && !activeReplyCommentId"` 改为 `v-if="showCommentForm && !activeReplyCommentId"`。

把第 24-32 行的 `<div class="form-user-info">` 块替换为：

```html
<div class="form-user-info">
  <template v-if="isLoggedIn">
    <img
      :src="getUserAvatar(userInfo)"
      :alt="userInfo?.username || '用户'"
      class="w-8 h-8 rounded-full object-cover border border-gray-200 dark:border-gray-600"
      @error="handleAvatarError"
    />
    <span class="form-username">{{ userInfo?.username || '用户' }}</span>
  </template>
  <template v-else>
    <img
      :src="identiconDataUrl(anonymousForm.nickname || 'anonymous')"
      alt="访客头像"
      class="w-8 h-8 rounded-full border border-gray-200 dark:border-gray-600 bg-gray-100"
    />
    <span class="form-username">访客</span>
  </template>
</div>
```

在 `<el-form :model="commentForm" ...>` 内部、`<el-form-item prop="content">` 之前新增一块：

```html
<template v-if="!isLoggedIn">
  <el-form-item prop="nickname">
    <el-input
      v-model="anonymousForm.nickname"
      placeholder="昵称（必填，1-30字）"
      maxlength="30"
      show-word-limit
    />
  </el-form-item>
  <el-form-item prop="email">
    <el-input
      v-model="anonymousForm.email"
      placeholder="邮箱（可选）"
      type="email"
      maxlength="100"
    />
  </el-form-item>
</template>
```

并把表单的 `:rules="commentRules"` 和 `:model` 改为：

```html
<el-form :model="{ ...commentForm, nickname: anonymousForm.nickname, email: anonymousForm.email }" :rules="commentRules" :ref="setCommentFormRef">
```

把第 87-92 行「发表评论按钮（仅登录用户）」的 `v-if="isLoggedIn && !showCommentForm"` 改为 `v-if="!showCommentForm"`。

把第 100-109 行空评论 `<el-empty>` 块里的 `<el-button v-if="isLoggedIn" ...>` 去掉 `v-if="isLoggedIn"`。

- [ ] **Step 5：修改 `submitComment`——允许匿名，构造 payload**

把 `submitComment` 函数整体替换为：

```js
const submitComment = async () => {
  if (!commentForm.content || commentForm.content.trim() === '') {
    ElMessage.warning('请输入评论内容')
    return
  }
  if (!isLoggedIn.value) {
    if (!anonymousForm.nickname || !anonymousForm.nickname.trim()) {
      ElMessage.warning('请填写昵称')
      return
    }
  }

  if (commentFormRef.value) {
    try {
      await commentFormRef.value.validate()
    } catch (error) {
      if (error === false) return
      return
    }
  }

  try {
    submitting.value = true
    const payload = {
      articleId: props.articleId,
      content: commentForm.content.trim()
    }
    if (isLoggedIn.value) {
      payload.username = userInfo.value?.username || userInfo.value?.nickname
    } else {
      payload.username = anonymousForm.nickname.trim()
      if (anonymousForm.email && anonymousForm.email.trim()) {
        payload.email = anonymousForm.email.trim()
      }
    }
    if (commentForm.parentId) {
      payload.parentId = commentForm.parentId
      if (commentForm.replyUserId) {
        payload.replyId = commentForm.replyUserId
      }
    }
    const response = await createComment(payload)
    ElMessage.success('评论发表成功')
    resetCommentForm()
    showCommentForm.value = false
    activeReplyCommentId.value = null
    await loadComments()
    emit('comment-added', response.data)
  } catch (error) {
    const code = error.response?.data?.code
    const msg = error.response?.data?.msg
    if (code === 'A03003') {
      ElMessage.error(msg || '评论太频繁，请稍后再试')
    } else if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      openLoginDialog()
    } else if (msg) {
      ElMessage.error(msg)
    } else {
      ElMessage.error('发表评论失败')
    }
  } finally {
    submitting.value = false
  }
}
```

- [ ] **Step 6：`replyToComment` 去掉登录拦截**

把 `replyToComment` 函数开头的：

```js
if (!isLoggedIn.value) {
  ElMessage.warning('登录后才能发表评论')
  return
}
```

整段删除。

- [ ] **Step 7：`resetCommentForm` 顺带清空匿名表单**

替换为：

```js
const resetCommentForm = () => {
  Object.assign(commentForm, {
    content: '',
    parentId: null,
    replyUserId: null,
    replyTo: ''
  })
  anonymousForm.nickname = ''
  anonymousForm.email = ''
  commentFormRef.value?.clearValidate()
}
```

- [ ] **Step 8：前端编译检查**

Run：`cd frontend && npm run build`
Expected：构建成功，无报错。

- [ ] **Step 9：提交**

```bash
git add frontend/src/components/article/CommentList.vue
git commit -m "feat(frontend): allow anonymous commenting with identicon avatar"
```

---

## Task 10: CommentItem 内联回复支持匿名 + 列表头像 Identicon

**Files:**
- Modify: `frontend/src/components/article/CommentItem.vue`

- [ ] **Step 1：阅读 `CommentItem.vue` 全文**

Run：`sed -n '1,300p' frontend/src/components/article/CommentItem.vue`
（目的：确定评论渲染区用的是哪个字段取头像；回复输入框在什么位置；是否也有 `isLoggedIn` 分支）

- [ ] **Step 2：导入 minidenticon**

在 `frontend/src/components/article/CommentItem.vue` 的 `<script setup>` 顶部 imports 区增加：

```js
import { minidenticon } from 'minidenticons'
```

以及一个工具函数：

```js
const identiconDataUrl = (seed) => {
  const svg = minidenticon(seed || 'anonymous', 95, 45)
  return 'data:image/svg+xml;utf8,' + encodeURIComponent(svg)
}
```

- [ ] **Step 3：渲染评论项头像分支**

找到评论项里渲染头像的 `<img>` 或 `<el-avatar>`（通常绑定 `comment.avatar`）。把 src 改为：

```html
:src="comment.isAnonymous || !comment.avatar
        ? identiconDataUrl(comment.username || 'anonymous')
        : getAvatarUrl(comment.avatar, avatarFallback)"
```

如果当前没有导入 `getAvatarUrl` 或 `avatarFallback`，按 `CommentList.vue` 的 import 对应补上。

- [ ] **Step 4：内联回复表单也允许匿名**

如果 `CommentItem.vue` 里有独立的"回复输入框"（常见做法是复用 `commentForm` 和提交函数），**注意**：该组件通过 props 接收 `commentForm`、`isLoggedIn`、表单 ref 等，不直接持有匿名 form。最简单的做法：
- 把 `anonymousForm` 也通过 prop 从 `CommentList` 传下来。
- `CommentList` 的 `<CommentItem>` 渲染处新增 `:anonymous-form="anonymousForm"`。
- `CommentItem.vue` 的 `defineProps` 增加 `anonymousForm: Object`。
- 在 `CommentItem.vue` 的回复输入区，如果 `!isLoggedIn`，先展示两个 input（绑定 `anonymousForm.nickname` 与 `anonymousForm.email`），再展示内容框。

示例（请根据实际模板位置插入）：

```html
<template v-if="!isLoggedIn">
  <el-form-item prop="nickname">
    <el-input v-model="anonymousForm.nickname" placeholder="昵称（必填）" maxlength="30" show-word-limit />
  </el-form-item>
  <el-form-item prop="email">
    <el-input v-model="anonymousForm.email" placeholder="邮箱（可选）" type="email" maxlength="100" />
  </el-form-item>
</template>
```

并去掉组件内任何 `v-if="isLoggedIn"` 对回复按钮 / 表单的拦截；`submit` 事件向上抛，交给 `CommentList.submitComment` 统一处理（已在 Task 9 支持匿名）。

- [ ] **Step 5：更新 `CommentList.vue` 的 `<CommentItem>` 调用**

在 `frontend/src/components/article/CommentList.vue` 把：

```html
<CommentItem
  v-for="comment in comments"
  :key="comment.id"
  :comment="comment"
  :depth="0"
  :is-logged-in="isLoggedIn"
  ...
/>
```

中新增 prop：

```html
:anonymous-form="anonymousForm"
```

- [ ] **Step 6：前端构建**

Run：`cd frontend && npm run build`
Expected：构建成功。

- [ ] **Step 7：启动 dev 服务器手动验证**

Run：`cd frontend && npm run dev`

浏览器访问文章详情页，**不登录**的情况下：
1. 评论表单可见，昵称+邮箱字段显示。
2. 填昵称（不填邮箱）、内容，提交 → 成功；列表新条目显示 Identicon 头像、`@昵称`。
3. 空昵称提交 → 前端提示"请填写昵称"。
4. 连续提交 7 条 → 第 7 条提示"评论太频繁，请稍后再试"。
5. 点击某条评论的"回复"→ 内联出现匿名回复表单，能提交回复。
6. 登录状态下再回到页面，表单切换为已登录模式，行为与现状一致。

- [ ] **Step 8：提交**

```bash
git add frontend/src/components/article/CommentItem.vue \
        frontend/src/components/article/CommentList.vue
git commit -m "feat(frontend): support anonymous replies and identicon in comment item"
```

---

## Task 11: 整体回归与清单确认

**Files:** 无文件改动

- [ ] **Step 1：后端全量测试**

Run：`cd backend && mvn -q test`
Expected：所有 tests PASS。

- [ ] **Step 2：前端构建 + 基本 lint**

Run：`cd frontend && npm run build`
Expected：构建成功。

- [ ] **Step 3：git log 检查**

Run：`git log --oneline -20`
Expected：按 Task 顺序看到约 9-10 条 commit。

- [ ] **Step 4：在 spec 文档里打勾实现状态（可选）**

在 `documents/superpowers/specs/2026-04-30-anonymous-comments-design.md` 文件末尾追加一节：

```markdown
## 实现状态（2026-04-30）

- [x] 数据库迁移 + 实体/Mapper
- [x] DTO / VO 扩展
- [x] 错误码
- [x] 限流器
- [x] Service 匿名分支
- [x] Controller 限流 + 放行
- [x] 前端 CommentList 匿名表单
- [x] 前端 CommentItem 匿名回复 + Identicon
- [x] 手动回归
```

- [ ] **Step 5：提交**

```bash
git add documents/superpowers/specs/2026-04-30-anonymous-comments-design.md
git commit -m "docs(comments): mark anonymous comments implementation complete"
```

---

## 完成标准

1. 未登录用户能发表评论（必填昵称、可选邮箱），新条目以 Identicon 显示。
2. 同 IP 1 分钟内 ≥ 7 次匿名提交被限流。
3. 已登录用户行为不变，`username` 仍来自服务端（防伪造）。
4. 其他需要登录的接口（`/user/user/*`、`/user/tree/**` 等）仍然 401。
5. 后端所有 test PASS；前端构建成功。
