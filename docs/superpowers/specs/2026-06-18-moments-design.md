# 手记（Moments）功能设计规格

**日期：** 2026-06-18  
**状态：** 已确认  
**参考：** https://blog.grtsinry43.com/moments/

---

## 概述

在博客中新增"手记"页面（`/moments`），用于发布图文混合的短篇记录，类似微信朋友圈风格。只有博主（管理员）可以发布，访客可以浏览、点赞、评论。

---

## 功能需求

### 前台（`/moments`）
- 按时间倒序展示所有已发布手记
- 顶部标签筛选栏，点击标签过滤列表
- 左侧竖向时间轴线 + 节点，右侧卡片
- 每条手记展示：标题（可选）、正文、图片、标签、浏览数、评论数、点赞数
- 点赞按钮：无需登录，基于 IP 幂等防重复
- "查看详情"展开/跳转至详情页，支持评论
- 分页加载（每页 10 条）

### 图片展示规则
| 图片数 | 布局 |
|--------|------|
| 0 | 纯文字卡片 |
| 1 | 左图右文（横向排列） |
| 2 | 左右各半 |
| 3 | 三等分横排 |
| 4+ | 2×2 网格（多余张数显示 +N） |

### 评论
复用现有 `Comments` 表，`ref_type = 'moment'`，`ref_id = moment_id`。复用前台现有 `CommentList` / `CommentItem` 组件。

### 后台（`/admin/moments`）
- 手记列表：标题、标签、浏览数、点赞数、状态（草稿/已发布）
- 支持搜索（按标题）
- 新建/编辑：以 Drawer 抽屉形式打开编辑器（不跳新页面）
- 批量删除
- 编辑器字段：标题（选填）、正文（纯文本）、图片上传（最多 9 张，复用 AliOSS）、标签（输入回车添加）、状态（草稿/发布）

### 导航栏
顶部导航顺序：**首页 → 文章 → 手记 → 标签 → 树洞 → 友链 → 更多**

---

## 数据模型

### `moment` 表

```sql
CREATE TABLE moment (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(100),
    content      TEXT NOT NULL,
    images       TEXT,                     -- JSON 数组，存储 OSS URL 列表
    like_count   INT NOT NULL DEFAULT 0,
    view_count   INT NOT NULL DEFAULT 0,
    status       SMALLINT NOT NULL DEFAULT 0,  -- 0=正常 1=草稿
    create_time  TIMESTAMP NOT NULL,
    update_time  TIMESTAMP NOT NULL
);
```

### `moment_tag` 表

```sql
CREATE TABLE moment_tag (
    moment_id  BIGINT NOT NULL,
    tag_name   VARCHAR(30) NOT NULL,
    PRIMARY KEY (moment_id, tag_name)
);
```

手记标签与文章标签独立存储，不共用 `tags` 表。

### 点赞防重

复用 `SiteLike` 机制模式：新建 `moment_like` 表，以 `(moment_id, ip)` 做唯一约束，INSERT 冲突时返回"已点赞"。

```sql
CREATE TABLE moment_like (
    moment_id  BIGINT NOT NULL,
    ip         VARCHAR(50) NOT NULL,
    create_time TIMESTAMP NOT NULL,
    PRIMARY KEY (moment_id, ip)
);
```

### 评论扩展

`Comments` 表已有 `ref_type` / `ref_id` 字段，无需改表，新增枚举值 `'moment'` 即可。

---

## 后端架构

### 新增文件（blog-pojo 模块）

| 文件 | 说明 |
|------|------|
| `entity/Moment.java` | 实体类 |
| `entity/MomentTag.java` | 标签实体 |
| `entity/MomentLike.java` | 点赞实体 |
| `dto/MomentDTO.java` | 创建/编辑入参（含 imageUrls 列表、tags 列表） |
| `vo/MomentVO.java` | 前端返回（含 tags 列表、liked 布尔、images 解析后列表） |

### 新增文件（blog-server 模块）

| 文件 | 说明 |
|------|------|
| `mapper/MomentMapper.java` + `.xml` | 基础 CRUD + 分页查询（支持标签筛选） |
| `mapper/MomentTagMapper.java` | 标签批量插入/查询 |
| `mapper/MomentLikeMapper.java` | 点赞插入/查询 |
| `service/MomentService.java` | 接口 |
| `service/impl/MomentServiceImpl.java` | 实现 |
| `controller/user/MomentController.java` | 公开端点 |
| `controller/admin/MomentAdminController.java` | 管理端点 |

### API 端点

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/user/moment/page` | 分页列表（支持 `tagName` 参数过滤） | 公开 |
| GET | `/user/moment/{id}` | 详情（+view_count+1） | 公开 |
| POST | `/user/moment/{id}/like` | 点赞（IP 幂等） | 公开 |
| GET | `/user/moment/tags` | 所有标签及用量（用于筛选栏） | 公开 |
| POST | `/admin/moment` | 新建手记 | 管理员 |
| PUT | `/admin/moment/{id}` | 编辑手记 | 管理员 |
| DELETE | `/admin/moment` | 批量删除（RequestBody id 列表） | 管理员 |
| GET | `/admin/moment/page` | 后台列表（支持标题搜索） | 管理员 |

---

## 前端架构

### 新增文件

| 文件 | 说明 |
|------|------|
| `views/Moments.vue` | 手记列表主页面（时间轴布局） |
| `components/moments/MomentCard.vue` | 单条手记卡片（含图片布局逻辑） |
| `components/moments/MomentImageGrid.vue` | 根据图片数量自动选择布局的图片网格组件 |
| `components/moments/MomentLikeButton.vue` | 点赞按钮（含动画，本地防重） |
| `views/admin/MomentManagement.vue` | 后台手记管理列表页 |
| `components/admin/MomentEditor.vue` | 手记编辑器（Drawer，含图片上传） |
| `api/moment.js` | 前台 API 封装 |
| `api/admin/moment.js` | 后台 API 封装 |

### 路由

```js
// 前台
{ path: '/moments', name: 'Moments', component: () => import('@/views/Moments.vue'), meta: { title: '手记' } }

// 后台
{ path: 'moments', name: 'AdminMoments', component: () => import('@/views/admin/MomentManagement.vue'), meta: { title: '手记管理' } }
```

### 导航修改

`AppHeader.vue` 在"文章"和"标签"之间插入：
```html
<router-link to="/moments" class="hover:text-primary">手记</router-link>
```

移动端侧边栏同步添加入口。

---

## UI 设计要点

- 主色：`#6366f1`（indigo，与现有 primary 色系一致）
- 时间轴线：左侧竖线渐变（`#6366f1` → `#a5b4fc` → transparent）
- 节点：已发布为实心紫色圆点 + 光晕；草稿为灰色
- 卡片圆角 `16px`，`box-shadow: 0 2px 12px rgba(0,0,0,0.08)`
- 暗色模式适配：背景 `gray-800`，卡片 `gray-700`
- 图片点击支持灯箱放大（使用 Element Plus `el-image` 的 preview 功能）

---

## 不在本期范围

- 手记详情独立页（点击"查看详情"用 Drawer 展开，不单独路由）
- 手记 RSS/订阅
- 访客评论审核（复用文章评论的现有审核流程）
