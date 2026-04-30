# 匿名评论设计文档

- 日期：2026-04-30
- 状态：已设计，待实现
- 目标：允许未登录用户在文章页发布评论，对齐主流博客（Hexo / Typecho 等）习惯。

## 背景

当前实现：
- `POST /user/comments` 在 `SecurityConfig` 中被列入 `authenticated()`，未登录请求会被 `JsonAuthenticationEntryPoint` 拦截返回 401。
- `CommentsServiceImpl.addComments` 通过 `BaseContext.getCurrentId()` 取登录用户 ID 写入 `comments.user_id`，并未提供匿名分支。
- 前端 `CommentList.vue` / `CommentItem.vue` 在未登录时隐藏或拦截评论表单。

需求：未登录访客可填写昵称（必填）+ 邮箱（可选）后直接发表评论，无需登录、无需审核。

## 设计选型

**方案 A（采用）**：保持单一接口 `POST /user/comments`，把它改为公开访问。Service 层根据 `BaseContext.getCurrentId()` 是否为空走两条分支。改动小，分歧仅在「`user_id` 是否填充」这一点。

未采用：
- 方案 B：新增独立的 `/user/comments/anonymous` 接口。多一份分支与维护成本。
- 方案 C：仅前端隐藏登录提示。后端校验仍 401，不可行。

## 设计

### 1. 数据模型

`comments` 表新增字段：

| 列 | 类型 | 是否可空 | 说明 |
|---|---|---|---|
| `email` | VARCHAR(100) | 是 | 匿名评论者邮箱，仅供后台审查；前端不展示 |

`user_id`、`username` 已为可空字段，无需变更。

DTO 与 VO：
- `CommentsDTO` 新增 `email`（可选，加 `@Email` 注解）。`username` 现有字段，匿名场景下必填校验在 Service 中完成（DTO 仍允许为空，因为登录场景不需要前端传）。
- `CommentsVO` 新增 `email`（仅后台用）和派生字段 `isAnonymous`（`userId == null` 时为 `true`）。

实体 `Comments` 同步增加 `email` 字段。

### 2. 后端权限

`SecurityConfig`：
- 从 `authenticated()` 列表中移除 `/user/comments`。
- `POST /user/comments` 与 `GET /user/comments/list` 走默认 `permitAll()`。
- `JwtAuthenticationFilter` 已是「有 token 则解析、无 token 则放行」，无需修改。

### 3. 业务逻辑

`CommentsServiceImpl.addComments` 改造：

```
Long userId = BaseContext.getCurrentId();
if (userId != null) {
    // 已登录分支（保持现状）
    comments.setUserId(userId);
    comments.setUsername(userMapper.getById(userId).getUsername()); // 防伪造
    // 不写入 email
} else {
    // 匿名分支
    if (StringUtils.isBlank(dto.getUsername()) || dto.getUsername().length() > 30) {
        throw new BussinessException(/* 复用现有参数错误码或在 ErrorCode 中新增 ANONYMOUS_NICKNAME_INVALID */, "昵称必填且长度 1-30");
    }
    comments.setUserId(null);
    comments.setUsername(dto.getUsername().trim());
    comments.setEmail(dto.getEmail()); // 可空
}
// replyId 解析逻辑保持不变
comments.setStatus(CommonStatusConstants.NORMAL); // 直接发布，不审核
```

### 4. IP 限流

匿名提交限流：同一 IP 1 分钟内最多 6 条评论。

实现：
- 在 `CommentsController.addComment` 入口处加切面或直接逻辑。
- 用 Caffeine 缓存（key=IP，value=滑动窗口计数器）或 `ConcurrentHashMap<String, Deque<Long>>`。
- 仅对 `BaseContext.getCurrentId() == null` 的请求生效。
- 超限抛业务异常，HTTP 200 但 `Result.code` 标识失败，前端展示「评论太频繁，请稍后再试」。
- 单机部署即可，未来多实例时再换 Redis。

### 5. 前端改动

**评论表单**（`CommentList.vue` 或 `CommentItem.vue`）：
- 移除「未登录禁止评论」的拦截 / 跳转。
- 根据登录态条件渲染：
  - 已登录：显示当前头像 + 昵称（只读）+ 内容框。
  - 未登录：显示昵称输入框（必填，1-30 字，前端校验）+ 邮箱输入框（可选，HTML5 `type=email` 校验）+ 内容框。
- 提交时未登录场景把 `username`、`email` 一起放入请求 body；已登录场景仅传 `articleId`、`content`、`parentId`、`replyId`。

**Identicon 头像**：
- 引入 `minidenticons`（轻量纯 JS，~1KB）。
- 评论列表渲染：`isAnonymous` 为 `true` → 用 `username`（或 `username+email`）生成 Identicon SVG 作为 `<img>` 的 src（`data:image/svg+xml;...`）；否则使用 user 表 `avatar`。

**API 层**（`frontend/src/api/comment.js`）：`createComment` 不变，DTO 自动带上新字段。

**错误处理**：限流场景前端提示「评论太频繁，请稍后再试」。

### 6. 安全

**关于「无 token 放行」的澄清**：`JwtAuthenticationFilter` 仅负责「认证」——有 token 就解析、无 token 就不设置 `SecurityContext`。**它本身不拒绝请求**。真正的访问控制由 `SecurityConfig.authorizeRequests()` 完成：`authenticated()` 规则会拦截未认证请求，`hasRole("ADMIN")` 会检查权限。所以本次改动只把 `/user/comments` 从 `authenticated()` 列表中移除，**其他需要登录的接口（如 `/user/user/userinfo`、`/user/user/favorite`、`/user/tree/**`、`/admin/**`）规则不变，安全性不受影响**。这是 Spring Security 的标准两层模型（认证 / 授权分离）。

唯一需要关注的副作用：之前依赖「请求能进 Service 就一定登录了」这一隐式假设的代码，现在 `BaseContext.getCurrentId()` 可能返回 `null`。本设计仅修改 `addComments`，需要确认 `getCommentsList` 等其他评论接口不依赖该假设（应该不依赖，因为本来就是公开查询）。

**其他风险**：
- **XSS**：前端用 `{{ }}` 插值渲染 `username`、`content`，不使用 `v-html`。后端入库时复用现有清洗逻辑（如已有），否则用基本的 `<script>` 标签剥离。
- **邮箱泄露**：`email` 字段不在前端任何位置展示，仅后台管理可见。
- **昵称冒充**：已登录用户的 `username` 始终从 user 表覆盖 DTO 值，避免登录用户被冒名顶替；匿名昵称无法冒充已注册用户名（因为存的是 `username` 字段，不影响登录态识别）。

## 测试

后端：
- 未登录 + 带昵称 → 成功，`user_id` 为 null。
- 未登录 + 不带昵称 → 400/业务异常。
- 已登录提交（DTO 传伪造 `username`） → 入库 `username` 应是登录用户名，非 DTO 值。
- 同 IP 1 分钟内 7 次匿名提交 → 第 7 次失败，提示限流。

前端：
- 未登录访问文章页可见评论表单。
- 昵称为空时不允许提交。
- 提交成功后表单清空、列表刷新。
- 匿名评论项 Identicon 正常渲染。

## 范围外（YAGNI）

- 评论审核流程
- Gravatar / 邮件通知 / 邮件验证
- 敏感词过滤
- 图形/文字验证码
- Redis 限流（仅在多实例部署时考虑）

## 影响面

- `backend/blog-server/src/main/java/top/hazenix/config/SecurityConfig.java`
- `backend/blog-server/src/main/java/top/hazenix/service/impl/CommentsServiceImpl.java`
- `backend/blog-server/src/main/java/top/hazenix/controller/user/CommentsController.java`（限流入口）
- `backend/blog-pojo/src/main/java/top/hazenix/dto/CommentsDTO.java`
- `backend/blog-pojo/src/main/java/top/hazenix/vo/CommentsVO.java`
- `backend/blog-pojo/src/main/java/top/hazenix/entity/Comments.java`
- `backend/blog-server/src/main/resources/mapper/CommentsMapper.xml`（INSERT 增加 email 列）
- 数据库迁移：`comments` 表 ADD COLUMN `email`
- `frontend/src/components/article/CommentList.vue` 与 `CommentItem.vue`
- `frontend/package.json`（新增 `minidenticons` 依赖）
