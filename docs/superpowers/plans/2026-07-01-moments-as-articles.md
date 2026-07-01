# 手记并入文章模型 + 总列表去封面 —— 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让手记成为「手记」默认分类下的文章、自动接入推荐系统，保留 /moments 图片九宫格与匿名 IP 点赞，并让总文章列表去封面且排除手记。

**Architecture:** 手记不再是独立表，而是 `article` 表中 `category_id = 手记分类` 的记录。前端 `/moments` 与 `MomentController`/`MomentVO` 的 API 形状保留不变，`MomentService` 底层改为读写 `article` 表。推荐系统零改动接入（候选来自 `listAllForScoring`，手记天然在内）。浏览类查询新增手记排除子查询；点赞保留 `moment_like`（列改名 `article_id`）匿名 IP 幂等。

**Tech Stack:** Spring Boot 2.7 + MyBatis(XML) + PageHelper + PostgreSQL；Vue 3 + Element Plus + Vite；JUnit 5（仅纯逻辑单测）。

## Global Constraints

- 数据库为 PostgreSQL（`docs/nginx-blog.conf` 与现有 SQL 使用 `BIGSERIAL`/`TIMESTAMP`）。迁移 SQL 用 PostgreSQL 语法。
- 手记分类通过 `category.type = 1` 识别，**不硬编码分类 id**；后端解析用 `SELECT id FROM category WHERE type = 1 LIMIT 1`。
- 手记参与推荐依赖：`status = 0`（正常）且 `recommend_level != 0`。新建手记默认 `recommend_level = 3`。
- 手记点赞保持**匿名 IP 幂等**，走 `moment_like(article_id, ip)`；不使用登录态 `user_article`。
- 前端无测试框架（无 vitest/jest）：前端与 MyBatis/Controller 层验证用「编译 + 运行 + curl/肉眼」；仅纯逻辑（标题兜底）写 JUnit。
- **Bash 沙箱默认禁网**：所有联网/构建命令（mvn、npm install、npm run build）必须用 `dangerouslyDisableSandbox: true` 运行，否则连接被重置。
- **前端工具链**：`export PATH="$HOME/.nvm/versions/node/v24.17.0/bin:$PATH"`；npm 用公共镜像 `--registry=https://registry.npmmirror.com`（内网 `bnpm.byted.org` 不可用）。
- **后端工具链**（无 mvnw；已装 JDK17 + Maven 3.9.16 + Aliyun 镜像）：每次构建前先设置
  `export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"` 和
  `export PATH="$HOME/.local/maven/apache-maven-3.9.16/bin:$JAVA_HOME/bin:$PATH"`（或 `source .superpowers/build-env.sh`）。
  编译：`cd backend && mvn -q -DskipTests compile`；单测：`mvn -q -pl blog-server test -Dtest=<Test>`；打包：`mvn -q -DskipTests package`。
  依赖已缓存于 `~/.m2`；若遇偶发 "terminated the handshake"，重试即可（Maven 断点续传）。
- 提交信息结尾加 `Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>`。

---

## Phase 0 — 总文章列表去封面（独立）

### Task 0.1: ArticleList 去封面

**Files:**
- Modify: `frontend/src/views/ArticleList.vue`

**Interfaces:**
- Produces: 无对外接口，纯 UI。

- [ ] **Step 1: 删除模板中的封面块**

删除 `frontend/src/views/ArticleList.vue` 模板里这段（当前 33–42 行）：

```html
            <!-- 文章封面 -->
            <div v-if="(article.coverImage || article.cover) && !imageErrors[article.id]" class="article-cover">
              <img
                :src="getCoverUrl(article)"
                :alt="article.title"
                class="cover-image"
                loading="lazy" decoding="async"
                @error="imageErrors[article.id] = true"
              />
            </div>
```

- [ ] **Step 2: 删除封面相关的脚本引用**

删除 `const imageErrors = reactive({})`（134 行）与 `const getCoverUrl = (article) => getThumbnailUrl(article.coverImage || article.cover, 400)`（135 行）。删除顶部 `import { getThumbnailUrl } from '@/utils/apiConfig'`（121 行）——先确认该文件内无其它 `getThumbnailUrl` 使用（`grep -n getThumbnailUrl frontend/src/views/ArticleList.vue`，应只剩被删的两处）。若 `reactive` 在文件中已无其它使用，可从 `vue` import 中移除，否则保留。

- [ ] **Step 3: 删除封面样式**

删除样式段中的 `.article-cover { @apply h-48 overflow-hidden; }` 与 `.cover-image { ... }` 两条规则（约 575–581 行）。

- [ ] **Step 4: 构建验证**

```bash
cd frontend && export PATH="$HOME/.nvm/versions/node/v24.17.0/bin:$PATH"
npm run build 2>&1 | tail -5
```
Expected: 构建成功、无未定义变量/未使用 import 报错。肉眼确认列表项只剩标题+摘要+元信息+标签，无图片请求。

- [ ] **Step 5: 提交**

```bash
git add frontend/src/views/ArticleList.vue
git commit -m "feat(article-list): 总文章列表去封面

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Phase 1 — 数据库迁移

### Task 1.1: 迁移 SQL 脚本

**Files:**
- Create: `docs/sql/2026-07-01-moments-as-articles.sql`

**Interfaces:**
- Produces: `category.type`、`article.images` 列；`type=1` 的手记分类种子；`moment_like.article_id` 列；删除 `moment`/`moment_tag` 表。

- [ ] **Step 1: 写迁移脚本**

```sql
-- 2026-07-01 手记并入文章模型
-- 1. 分类加类型列（0=普通，1=手记），并种子手记分类
ALTER TABLE category ADD COLUMN IF NOT EXISTS type SMALLINT NOT NULL DEFAULT 0;

INSERT INTO category (name, type)
SELECT '手记', 1
WHERE NOT EXISTS (SELECT 1 FROM category WHERE type = 1);

-- 2. 文章加图片列（仅手记填充，JSON 数组字符串）
ALTER TABLE article ADD COLUMN IF NOT EXISTS images TEXT;

-- 3. moment_like 列重命名：moment_id -> article_id（引用 article.id）
ALTER TABLE moment_like RENAME COLUMN moment_id TO article_id;

-- 4. 废弃旧表（确认无数据后）
DROP TABLE IF EXISTS moment_tag;
DROP TABLE IF EXISTS moment;
```

> 注意：`category` 表实际列名以现有 schema 为准；执行前先 `\d category` 核对是否有 `name` 列。若种子分类需要其它非空列（如 slug），在 INSERT 中补齐。

- [ ] **Step 2: 提交脚本（暂不执行）**

脚本随后随部署一起在服务器执行（见 Phase 7）。本地开发库若存在，可执行验证。

```bash
git add docs/sql/2026-07-01-moments-as-articles.sql
git commit -m "chore(sql): 手记并入文章模型迁移脚本

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Phase 2 — 后端：分类类型 + 文章图片列 + 浏览查询排除手记

### Task 2.1: Category 增加 type 字段与解析手记分类 id

**Files:**
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/entity/Category.java`
- Modify: `backend/blog-server/src/main/java/top/hazenix/mapper/CategoryMapper.java`
- Modify: `backend/blog-server/src/main/resources/mapper/CategoryMapper.xml`
- Create: `backend/blog-common/src/main/java/top/hazenix/constant/CategoryConstants.java`

**Interfaces:**
- Produces: `CategoryMapper.getMomentCategoryId(): Integer`；`CategoryConstants.MOMENT_CATEGORY_TYPE = 1`。

- [ ] **Step 1: Category 实体加 type 字段**

在 `Category.java` 加：
```java
@ApiModelProperty(value = "分类类型[0:普通 | 1:手记]", example = "0")
private Integer type;
```
确认查询 `select *` / resultMap 能带出 `type`（若用 resultMap 显式列，补充 `<result column="type" property="type"/>`）。

- [ ] **Step 2: 常量**

`CategoryConstants.java`：
```java
package top.hazenix.constant;

public class CategoryConstants {
    /** 手记分类类型 */
    public static final int MOMENT_CATEGORY_TYPE = 1;
}
```

- [ ] **Step 3: Mapper 方法**

`CategoryMapper.java` 加：
```java
Integer getMomentCategoryId();
```
`CategoryMapper.xml` 加：
```xml
<select id="getMomentCategoryId" resultType="java.lang.Integer">
    select id from category where type = 1 limit 1
</select>
```

- [ ] **Step 4: 编译**

```bash
cd backend && ./mvnw -q -pl blog-server -am -DskipTests compile 2>&1 | tail -15
```
Expected: BUILD SUCCESS。

- [ ] **Step 5: 提交**

```bash
git add backend/blog-pojo/.../Category.java backend/blog-server/.../mapper/CategoryMapper.java backend/blog-server/src/main/resources/mapper/CategoryMapper.xml backend/blog-common/.../CategoryConstants.java
git commit -m "feat(category): 增加 type 字段与手记分类解析

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

### Task 2.2: 禁止删除手记默认分类

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/CategoryServiceImpl.java`

**Interfaces:**
- Consumes: `CategoryMapper.getMomentCategoryId()`。

- [ ] **Step 1: 删除守卫**

在 `CategoryServiceImpl` 删除分类方法开头，加：
```java
Integer momentCatId = categoryMapper.getMomentCategoryId();
if (momentCatId != null && ids.contains(momentCatId /* 或 momentCatId.longValue()，按方法签名类型 */)) {
    throw new BussinessException(ErrorCode.A03003, "手记默认分类不可删除");
}
```
（`ErrorCode`/异常类型参照该文件已有用法；消息常量可加到 `MessageConstant`。）

- [ ] **Step 2: 编译**

```bash
cd backend && ./mvnw -q -pl blog-server -am -DskipTests compile 2>&1 | tail -15
```
Expected: BUILD SUCCESS。

- [ ] **Step 3: 提交**

```bash
git commit -am "feat(category): 禁止删除手记默认分类

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

### Task 2.3: Article 加 images 列（实体/DTO/详情VO/Mapper）

**Files:**
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/entity/Article.java`
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/dto/ArticleDTO.java`
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/vo/ArticleDetailVO.java`
- Modify: `backend/blog-server/src/main/resources/mapper/ArticleMapper.xml`
- Modify: `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleMapper.java`

**Interfaces:**
- Produces: `Article.images: String`（JSON）；`ArticleMapper.incrementLikeCount(Long id)`。

- [ ] **Step 1: 实体/DTO/VO 加 images**

- `Article.java` 加 `private String images;`
- `ArticleDTO.java` 加 `private List<String> imageUrls;`（可空，仅手记用）
- `ArticleDetailVO.java` 加 `private List<String> images;`（详情页手记九宫格用）

- [ ] **Step 2: insert/update 带 images**

`ArticleMapper.xml` 的 `insert`：列加 `,images`，values 加 `,#{images}`：
```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    insert into article (user_id,title,content,cover_image,category_id,like_count,favorite_count,view_count,slug,status,create_time,update_time,recommend_level,images)
    values (#{userId},#{title},#{content},#{coverImage},#{categoryId},#{likeCount},#{favoriteCount},#{viewCount},#{slug},#{status},#{createTime},#{updateTime},#{recommendLevel},#{images})
</insert>
```
`update` 的 `<set>` 加：
```xml
<if test="images != null">
    images = #{images},
</if>
```

- [ ] **Step 3: 加 incrementLikeCount（手记匿名点赞用）**

`ArticleMapper.java` 加 `void incrementLikeCount(Long id);`；`ArticleMapper.xml` 加：
```xml
<update id="incrementLikeCount">
    update article set like_count = coalesce(like_count,0) + 1 where id = #{id}
</update>
```

- [ ] **Step 4: 详情查询带出 images**

`getById` 用 `select *`，实体已含 images；确认 `ArticleServiceImpl.getArticleDetail` 把 `article.getImages()` 反序列化后 set 到 `ArticleDetailVO.images`（用现有 ObjectMapper，与 MomentServiceImpl 反序列化一致）。若详情走 `ArticleShortVO` 则无需。

- [ ] **Step 5: 编译**

```bash
cd backend && ./mvnw -q -pl blog-server -am -DskipTests compile 2>&1 | tail -15
```
Expected: BUILD SUCCESS。

- [ ] **Step 6: 提交**

```bash
git commit -am "feat(article): 增加 images 列与手记匿名点赞计数

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

### Task 2.4: 浏览类查询排除手记

**Files:**
- Modify: `backend/blog-server/src/main/resources/mapper/ArticleMapper.xml`

**Interfaces:**
- 无新接口，只改 SQL 行为。

- [ ] **Step 1: 在浏览查询加手记排除子查询**

给下列查询的 `where` 追加 `and category_id not in (select id from category where type = 1)`：
- `getRecentArticles`（先加 `where` 子句再排序）：
```xml
<select id="getRecentArticles" resultType="top.hazenix.vo.ArticleShortVO">
    select id,title,status,create_time,cover_image from article
    where category_id not in (select id from category where type = 1)
    order by update_time desc limit #{i}
</select>
```
- `pageQuery`：在 `<where>` 内首行加 `and category_id not in (select id from category where type = 1)`。
- `getArticleList`：同上，在 `<where>` 内加同一条件。
- `getPopularArticles`：`where status = 0 AND (recommend_level != 0 OR recommend_level IS NULL) AND category_id not in (select id from category where type = 1)`。

**不改**：`listAllForScoring`（推荐候选，手记要参与）、`getPublishedSlugs`（手记有详情页，保留在 sitemap）。

- [ ] **Step 2: 编译并（若有本地库）冒烟**

```bash
cd backend && ./mvnw -q -pl blog-server -am -DskipTests compile 2>&1 | tail -8
```
Expected: BUILD SUCCESS。

- [ ] **Step 3: 提交**

```bash
git commit -am "feat(article): 浏览列表排除手记分类（保留推荐/站点地图）

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Phase 3 — 后端：MomentService 改为读写文章表

### Task 3.1: MomentDTO 改整型标签 + 手记分页查询 Mapper

**Files:**
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/dto/MomentDTO.java`
- Modify: `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleMapper.java`
- Modify: `backend/blog-server/src/main/resources/mapper/ArticleMapper.xml`

**Interfaces:**
- Produces: `MomentDTO.tagIds: List<Integer>`（替换旧 `tags`）；`ArticleMapper.pageMoments(keyword, status): List<MomentVO>`（含 content、images_json）。

- [ ] **Step 1: MomentDTO 换标签类型**

把 `MomentDTO` 的 `private List<String> tags;` 改为：
```java
@ApiModelProperty(value = "标签ID列表")
private List<Integer> tagIds;
```

- [ ] **Step 2: 手记分页查询**

`ArticleMapper.java` 加：
```java
com.github.pagehelper.Page<top.hazenix.vo.MomentVO> pageMoments(
    @org.apache.ibatis.annotations.Param("keyword") String keyword,
    @org.apache.ibatis.annotations.Param("status") Integer status,
    @org.apache.ibatis.annotations.Param("momentCategoryId") Integer momentCategoryId);
```
`ArticleMapper.xml` 加（映射到 MomentVO：images 列映射到 imagesJson 由 Service 反序列化）：
```xml
<select id="pageMoments" resultType="top.hazenix.vo.MomentVO">
    select id, title, content, images as images_json,
           like_count, view_count, status, create_time, update_time
    from article
    where category_id = #{momentCategoryId}
    <if test="keyword != null and keyword != ''">
        and (title like concat('%',#{keyword},'%') or content like concat('%',#{keyword},'%'))
    </if>
    <if test="status != null">
        and status = #{status}
    </if>
    order by create_time desc
</select>
```
> `images as images_json` 需与 MyBatis 驼峰映射匹配（`images_json` → `imagesJson`）。若项目未开启 `map-underscore-to-camel-case`，改用 `images as imagesJson` 并加引号视方言而定；PostgreSQL 下用别名 `imagesJson` 需双引号，稳妥用 resultMap。执行时以能正确映射为准。

- [ ] **Step 3: 编译**

```bash
cd backend && ./mvnw -q -pl blog-server -am -DskipTests compile 2>&1 | tail -15
```
Expected: BUILD SUCCESS。

- [ ] **Step 4: 提交**

```bash
git commit -am "feat(moment): DTO 改整型标签 + 手记分页查询走文章表

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

### Task 3.2: 标题兜底工具（纯逻辑 + JUnit）

**Files:**
- Create: `backend/blog-server/src/main/java/top/hazenix/util/MomentTitleUtil.java`
- Test: `backend/blog-server/src/test/java/top/hazenix/test/MomentTitleUtilTest.java`

**Interfaces:**
- Produces: `MomentTitleUtil.fallbackTitle(String title, String content): String`。

- [ ] **Step 1: 写失败测试**

```java
package top.hazenix.test;

import org.junit.jupiter.api.Test;
import top.hazenix.util.MomentTitleUtil;
import static org.junit.jupiter.api.Assertions.*;

public class MomentTitleUtilTest {
    @Test
    public void keepsExplicitTitle() {
        assertEquals("你好", MomentTitleUtil.fallbackTitle("你好", "正文很长很长"));
    }
    @Test
    public void fallsBackToContentExcerptWhenTitleBlank() {
        String r = MomentTitleUtil.fallbackTitle("  ", "今天写了并入文章的方案，感觉不错");
        assertEquals("今天写了并入文章的方案，感觉不错", r);
    }
    @Test
    public void truncatesLongContentTo30Chars() {
        String content = "一二三四五六七八九十".repeat(4); // 40 字
        String r = MomentTitleUtil.fallbackTitle(null, content);
        assertEquals(30, r.length() - 1); // 30 + 省略号
        assertTrue(r.endsWith("…"));
    }
    @Test
    public void handlesNullContent() {
        assertEquals("无标题", MomentTitleUtil.fallbackTitle(null, null));
    }
}
```

- [ ] **Step 2: 运行确认失败**

```bash
cd backend && ./mvnw -q -pl blog-server test -Dtest=MomentTitleUtilTest 2>&1 | tail -20
```
Expected: 编译失败（`MomentTitleUtil` 不存在）。

- [ ] **Step 3: 实现**

```java
package top.hazenix.util;

public final class MomentTitleUtil {
    private MomentTitleUtil() {}

    /** 手记标题兜底：优先显式标题，其次正文首 30 字摘要，末尾加省略号 */
    public static String fallbackTitle(String title, String content) {
        if (title != null && !title.isBlank()) return title;
        if (content == null || content.isBlank()) return "无标题";
        String plain = content.strip();
        if (plain.length() <= 30) return plain;
        return plain.substring(0, 30) + "…";
    }
}
```

- [ ] **Step 4: 运行确认通过**

```bash
cd backend && ./mvnw -q -pl blog-server test -Dtest=MomentTitleUtilTest 2>&1 | tail -20
```
Expected: Tests run: 4, Failures: 0。

- [ ] **Step 5: 提交**

```bash
git add backend/blog-server/src/main/java/top/hazenix/util/MomentTitleUtil.java backend/blog-server/src/test/java/top/hazenix/test/MomentTitleUtilTest.java
git commit -m "feat(moment): 标题兜底工具 + 单测

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

### Task 3.3: 重写 MomentServiceImpl 读写文章表

**Files:**
- Modify: `backend/blog-server/src/main/java/top/hazenix/service/impl/MomentServiceImpl.java`
- Modify: `backend/blog-server/src/main/java/top/hazenix/mapper/MomentLikeMapper.java`
- Modify: `backend/blog-server/src/main/resources/mapper/MomentLikeMapper.xml`
- Modify: `backend/blog-pojo/src/main/java/top/hazenix/entity/MomentLike.java`
- Delete: `backend/.../entity/Moment.java`, `entity/MomentTag.java`, `mapper/MomentMapper.java`, `mapper/MomentTagMapper.java`, `resources/mapper/MomentMapper.xml`, `resources/mapper/MomentTagMapper.xml`

**Interfaces:**
- Consumes: `ArticleMapper.insert/update/getById/deleteByIds/updateArticleView(或 incrementViewCount)/incrementLikeCount/pageMoments`、`ArticleTagsMapper`（saveTags/getListByArticleId/getTagNamesByArticleId）、`CategoryMapper.getMomentCategoryId`、`MomentTitleUtil.fallbackTitle`。
- Produces: `MomentService` 接口方法签名不变（`createMoment`/`updateMoment`/`deleteMoments`/`getById`/`pageQueryUser`/`pageQueryAdmin`/`likeMoment`/`getAllTags`）。

- [ ] **Step 1: MomentLike 实体与 Mapper 改 article_id**

- `MomentLike.java`：把 `momentId` 字段改名 `articleId`。
- `MomentLikeMapper.java`：`getByMomentIdAndIp` → `getByArticleIdAndIp(Long articleId, String ip)`；`insert` 参数不变（用改名后字段）。
- `MomentLikeMapper.xml`：列 `moment_id` → `article_id`，方法 id/参数同步。

- [ ] **Step 2: ArticleTagsMapper 确认标签能力**

确认 `ArticleTagsMapper` 具备：按 articleId 保存整型标签、按 articleId 删除、按 articleId 取 tagId 列表、按 articleId 取标签名列表。若缺「取标签名列表」，参照 `getRelatedArticlesByTags` 的联表在 `ArticleTagsMapper.xml` 加：
```xml
<select id="getTagNamesByArticleId" resultType="java.lang.String">
    select t.name from tags t
    join article_tags at on at.tags_id = t.id
    where at.article_id = #{articleId}
</select>
```
（表名 `tags`/列名以现有 schema 为准。）

- [ ] **Step 3: 重写 MomentServiceImpl**

用文章表实现，保留 `serializeImages`/`deserializeImages`/`fillVO`/`buildVO` 思路，核心改动：

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MomentServiceImpl implements MomentService {

    private final ArticleMapper articleMapper;
    private final ArticleTagsMapper articleTagsMapper;
    private final MomentLikeMapper momentLikeMapper;
    private final CategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;

    private Integer momentCategoryId() {
        Integer id = categoryMapper.getMomentCategoryId();
        if (id == null) throw new BussinessException(ErrorCode.A04001, "手记分类未初始化");
        return id;
    }

    @Override
    @Transactional
    public void createMoment(MomentDTO dto) {
        Article a = Article.builder()
                .userId(1L)                       // 站长单人博客，固定作者
                .title(dto.getTitle())
                .content(dto.getContent())
                .images(serializeImages(dto.getImageUrls()))
                .coverImage(null)
                .categoryId(momentCategoryId())
                .likeCount(0).favoriteCount(0).viewCount(0)
                .slug(null)
                .status(dto.getStatus() != null ? dto.getStatus() : 0)
                .recommendLevel(3)                // 默认可推荐
                .isTop(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        articleMapper.insert(a);
        saveTags(a.getId(), dto.getTagIds());
    }

    @Override
    @Transactional
    public void updateMoment(Long id, MomentDTO dto) {
        Article a = Article.builder()
                .id(id)
                .title(dto.getTitle())
                .content(dto.getContent())
                .images(serializeImages(dto.getImageUrls()))
                .status(dto.getStatus())
                .updateTime(LocalDateTime.now())
                .build();
        articleMapper.update(a);
        articleTagsMapper.deleteByArticleId(id);   // 方法名以现有为准
        saveTags(id, dto.getTagIds());
    }

    @Override
    @Transactional
    public void deleteMoments(DeleteMomentsDTO dto) {
        articleTagsMapper.deleteByArticleIds(dto.getIds()); // 或逐个删；以现有 API 为准
        articleMapper.deleteByIds(dto.getIds());
    }

    @Override
    public MomentVO getById(Long id, String clientIp) {
        Article a = articleMapper.getById(id);
        if (a == null) throw new BussinessException(ErrorCode.A04001, MessageConstant.NOT_FOUND);
        articleMapper.updateArticleView(id); // 或现有的浏览+1方法名
        return buildVO(a, clientIp);
    }

    @Override
    public PageResult pageQueryUser(Integer page, Integer pageSize, String tagName, String clientIp) {
        PageHelper.startPage(page, pageSize);
        Page<MomentVO> res = articleMapper.pageMoments(null, 0, momentCategoryId());
        List<MomentVO> list = res.getResult();
        list.forEach(vo -> fillVO(vo, clientIp));
        return new PageResult(res.getTotal(), list);
    }

    @Override
    public PageResult pageQueryAdmin(Integer page, Integer pageSize, String keyword) {
        PageHelper.startPage(page, pageSize);
        Page<MomentVO> res = articleMapper.pageMoments(keyword, null, momentCategoryId());
        List<MomentVO> list = res.getResult();
        list.forEach(vo -> fillVO(vo, null));
        return new PageResult(res.getTotal(), list);
    }

    @Override
    public void likeMoment(Long id, String clientIp) {
        if (momentLikeMapper.getByArticleIdAndIp(id, clientIp) != null) {
            throw new BussinessException(ErrorCode.A03003, MessageConstant.ALREADY_LIKED);
        }
        momentLikeMapper.insert(MomentLike.builder()
                .articleId(id).ip(clientIp).createTime(LocalDateTime.now()).build());
        articleMapper.incrementLikeCount(id);
    }

    @Override
    public List<String> getAllTags() {
        // 复用文章标签体系：返回手记分类下用过的标签名（或全部标签名）
        return articleTagsMapper.getAllTagNames(); // 若无则参照 tags 表实现
    }

    // saveTags 改为整型标签
    private void saveTags(Long articleId, List<Integer> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        articleTagsMapper.insertBatch(articleId, tagIds); // 以现有 article_tags 保存 API 为准
    }

    // fillVO：标题兜底 + 标签名 + liked
    private void fillVO(MomentVO vo, String clientIp) {
        if (vo.getImages() == null) vo.setImages(deserializeImages(vo.getImagesJson()));
        vo.setTitle(MomentTitleUtil.fallbackTitle(vo.getTitle(), vo.getContent()));
        vo.setTags(articleTagsMapper.getTagNamesByArticleId(vo.getId()));
        vo.setLiked(clientIp != null && momentLikeMapper.getByArticleIdAndIp(vo.getId(), clientIp) != null);
    }

    // buildVO(Article) 映射 id/title/content/images/likeCount/viewCount/status/createTime/updateTime，再 fillVO
    // serializeImages/deserializeImages 保持原实现
}
```
> 以现有 `ArticleTagsMapper` / `ArticleMapper` 的真实方法名为准（`deleteByArticleId`、`insertBatch`、浏览+1 方法名等）；缺失的补齐并加对应 XML。作者 `userId` 固定 1L（单人博客）。

- [ ] **Step 4: 删除旧 Moment 表相关类与 XML**

```bash
cd backend
git rm blog-pojo/src/main/java/top/hazenix/entity/Moment.java \
       blog-pojo/src/main/java/top/hazenix/entity/MomentTag.java \
       blog-server/src/main/java/top/hazenix/mapper/MomentMapper.java \
       blog-server/src/main/java/top/hazenix/mapper/MomentTagMapper.java \
       blog-server/src/main/resources/mapper/MomentMapper.xml \
       blog-server/src/main/resources/mapper/MomentTagMapper.xml
```
`grep -rn "MomentMapper\|MomentTagMapper\|entity.Moment\b\|MomentTag" backend/blog-server/src` 应无残留引用。

- [ ] **Step 5: 编译**

```bash
cd backend && ./mvnw -q -pl blog-server -am -DskipTests compile 2>&1 | tail -20
```
Expected: BUILD SUCCESS，无未解析符号。

- [ ] **Step 6: 提交**

```bash
git add -A backend
git commit -m "refactor(moment): MomentService 改为读写文章表，废弃 moment/moment_tag

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

### Task 3.4: 后端集成冒烟（本地或服务器）

**Files:** 无（验证）。

- [ ] **Step 1: 起服务 + 执行迁移 SQL（在有库的环境）**

先执行 `docs/sql/2026-07-01-moments-as-articles.sql`，再启动后端。

- [ ] **Step 2: curl 冒烟**

```bash
# 新建手记（后台接口，带鉴权头视项目而定）
curl -s -X POST localhost:8080/admin/moment -H 'Content-Type: application/json' \
  -d '{"content":"第一条手记","imageUrls":["https://x/a.jpg"],"tagIds":[1],"status":0}'
# 用户端分页
curl -s 'localhost:8080/user/moment/page?page=1&pageSize=10' | head -c 400
# 点赞幂等（同 IP 第二次应报已点赞）
curl -s -X POST localhost:8080/user/moment/1/like
```
Expected: 分页返回该手记（title 为兜底摘要、images 数组、tags）；重复点赞返回“已点赞”。

- [ ] **Step 3: 确认手记进入推荐候选、未进总列表**

```bash
curl -s 'localhost:8080/user/articles' | grep -c '第一条手记'      # 期望 0（总列表排除）
curl -s 'localhost:8080/api/articles/recommended?size=20'          # 期望可含手记
```

---

## Phase 4 — 前端：/moments 与编辑器整型标签

### Task 4.1: MomentEditor 改用整型标签

**Files:**
- Modify: `frontend/src/components/admin/MomentEditor.vue`
- Modify: `frontend/src/api/moment.js`

**Interfaces:**
- Consumes: 文章标签下拉数据（复用 `ArticleEditor` 的标签获取方式，如 `getTagList()`）。
- Produces: 提交体字段 `tagIds: number[]`（替换旧 `tags: string[]`）。

- [ ] **Step 1: 标签控件换整型**

参照 `frontend/src/components/admin/ArticleEditor.vue` 的标签选择器（`el-select multiple` 绑定 tag id 列表 + `getTagList()`），替换 MomentEditor 里原字符串标签输入；提交时字段名用 `tagIds`。保留图片九宫格上传（OSS）与标题(可选)/正文。

- [ ] **Step 2: 管理端 API 增删改**

`api/moment.js` 增加后台方法（对应 `MomentAdminController`）：
```js
create(data)      { return request({ url: '/admin/moment', method: 'post', data }) },
update(id, data)  { return request({ url: `/admin/moment/${id}`, method: 'put', data }) },
batchDelete(ids)  { return request({ url: '/admin/moment/batch', method: 'delete', data: { ids } }) },
adminPage(params) { return request({ url: '/admin/moment/page', method: 'get', params }) },
```

- [ ] **Step 3: 构建验证**

```bash
cd frontend && export PATH="$HOME/.nvm/versions/node/v24.17.0/bin:$PATH"
npm run build 2>&1 | tail -5
```
Expected: 构建成功。

- [ ] **Step 4: 提交**

```bash
git commit -am "feat(moment-editor): 改用整型标签 + 后台管理 API

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

### Task 4.2: /moments 页与 MomentCard 回归验证

**Files:** `frontend/src/views/Moments.vue`, `frontend/src/components/moments/MomentCard.vue`（预期无需改动）。

- [ ] **Step 1: 验证形状兼容**

因 `MomentVO` 字段形状不变，`/moments` 分页、点赞、九宫格应正常。运行 `npm run build` 与本地 `npm run dev` 肉眼确认：列表展示九宫格、匿名点赞可用、标题为兜底摘要。若 `MomentCard` 依赖了已删字段，按需微调。

- [ ] **Step 2: 提交（如有改动）**

```bash
git commit -am "fix(moments): 适配手记文章化后的展示

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Phase 5 — 前端：手记管理台补齐文章管理能力

### Task 5.1: 抽共享行操作 composable

**Files:**
- Create: `frontend/src/composables/useArticleAdminActions.js`

**Interfaces:**
- Produces: `useArticleAdminActions()` 返回 `{ setRecommendLevel(id, level), toggleStatus(row), toggleTop(row), remove(id), batchRemove(ids), recommendLevelMap }`，内部调用 `articleApi`。

- [ ] **Step 1: 实现 composable**

抽取 `ArticleManagement.vue` 现有的推荐度更新（`recommendLevelMap = {0:'屏蔽',1:'弱',2:'较弱',3:'默认',4:'推荐',5:'精华'}`）、状态切换、置顶、删除、批量删除逻辑，封装为可复用函数，均走 `@/api/article` 的 `updateRecommendLevel/toggleArticleStatus/deleteArticle/batchDeleteArticles`（置顶若无接口则新增 `toggleArticleTop`，后端加 `PUT /admin/articles/{id}/top` 或复用 update）。含 `ElMessage`/`ElMessageBox` 交互。

- [ ] **Step 2: ArticleManagement 改用 composable**

将 `ArticleManagement.vue` 中对应内联逻辑替换为 `useArticleAdminActions()` 的调用，保证行为不变。

- [ ] **Step 3: 构建验证**

```bash
cd frontend && export PATH="$HOME/.nvm/versions/node/v24.17.0/bin:$PATH"
npm run build 2>&1 | tail -5
```
Expected: 构建成功、文章管理台功能不回归。

- [ ] **Step 4: 提交**

```bash
git commit -am "refactor(admin): 抽共享文章管理行操作 composable

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

### Task 5.2: MomentManagement 补齐能力

**Files:**
- Modify: `frontend/src/views/admin/MomentManagement.vue`

**Interfaces:**
- Consumes: `useArticleAdminActions()`、`momentApi.adminPage`。

- [ ] **Step 1: 列表补列与操作**

在 `MomentManagement.vue`：
- 搜索：改为按标题+内容（后端 `pageMoments` 已支持）。
- 表格新增列：推荐度（`el-select` 0–5，调 `setRecommendLevel`）、状态（发布/下架切换 `toggleStatus`）、置顶（`toggleTop`）、查看（跳文章详情/预览）。保留内容摘要 + 图片缩略 + 浏览/点赞 + 批量删除。
- 行操作与批量删除改用 `useArticleAdminActions()`（手记就是文章，接口通用）。
- 列表数据源从 `momentApi.adminPage` 获取（返回 MomentVO，含 likeCount/viewCount/status；推荐度/置顶若 MomentVO 未含，需在 `pageMoments` 查询补 `recommend_level, is_top` 并加到 `MomentVO`）。

- [ ] **Step 2: 若需要，补 MomentVO 字段**

如列表要显示推荐度/置顶，给 `MomentVO` 加 `recommendLevel`、`isTop`，并在 `pageMoments` select 中补 `recommend_level, is_top`。

- [ ] **Step 3: 构建验证**

```bash
cd frontend && export PATH="$HOME/.nvm/versions/node/v24.17.0/bin:$PATH"
npm run build 2>&1 | tail -5
```
Expected: 构建成功。本地 `npm run dev` 确认手记管理台具备推荐度/置顶/状态切换/查看/内容搜索/批量删除。

- [ ] **Step 4: 提交**

```bash
git commit -am "feat(admin-moment): 手记管理补齐推荐度/置顶/状态/查看/内容搜索

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Phase 6 — 前端：手记详情九宫格 + 分类管理禁删

### Task 6.1: ArticleDetail 手记分类渲染九宫格 + 标题兜底

**Files:**
- Modify: `frontend/src/views/ArticleDetail.vue`

**Interfaces:**
- Consumes: 文章详情返回的 `images: string[]`、`category.type`、`title`。

- [ ] **Step 1: 手记详情图片九宫格**

当详情 `category?.type === 1`（手记）且 `images?.length` 时，在正文上方/下方用 `MomentImageGrid`（`frontend/src/components/moments/MomentImageGrid.vue`）渲染 `images`。标题为空时用正文摘要兜底（与后端 `fallbackTitle` 规则一致，可前端简单截断）。确认后端 `ArticleDetailVO` 返回了 `images` 与 `category`（含 type）。

- [ ] **Step 2: 构建验证**

```bash
cd frontend && export PATH="$HOME/.nvm/versions/node/v24.17.0/bin:$PATH"
npm run build 2>&1 | tail -5
```
Expected: 构建成功；推荐里的手记点进详情能看到九宫格。

- [ ] **Step 3: 提交**

```bash
git commit -am "feat(article-detail): 手记分类渲染图片九宫格 + 标题兜底

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

### Task 6.2: CategoryManagement 禁止删除手记分类

**Files:**
- Modify: `frontend/src/views/admin/CategoryManagement.vue`

- [ ] **Step 1: 前端禁删**

分类列表中 `type === 1` 的行，禁用/隐藏删除按钮，并加“默认分类”标记 tag。后端已在 Task 2.2 兜底拦截。

- [ ] **Step 2: 构建验证**

```bash
cd frontend && export PATH="$HOME/.nvm/versions/node/v24.17.0/bin:$PATH"
npm run build 2>&1 | tail -5
```
Expected: 构建成功。

- [ ] **Step 3: 提交**

```bash
git commit -am "feat(admin-category): 手记默认分类禁止删除

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Phase 7 — 端到端验证与部署

### Task 7.1: 全量构建 + 推荐链路核对

- [ ] **Step 1: 后端全量测试 + 打包**

```bash
cd backend && ./mvnw -q test 2>&1 | tail -15 && ./mvnw -q -DskipTests package 2>&1 | tail -5
```
Expected: 测试全绿、打包成功。

- [ ] **Step 2: 前端全量构建**

```bash
cd frontend && export PATH="$HOME/.nvm/versions/node/v24.17.0/bin:$PATH"
npm run build 2>&1 | tail -8
```
Expected: 构建 + 92 页预渲染成功。

- [ ] **Step 3: 验收清单逐条核对**（对照 spec 验收标准）

- 总列表无封面、无图片请求、不含手记。
- /moments 九宫格 + 匿名 IP 点赞（重复点赞被拒）。
- 手记管理台具备推荐度/置顶/状态/查看/内容搜索/批量删除；编辑器九宫格 + 整型标签。
- 新手记出现在推荐、点进详情为九宫格；浏览/点赞写入 `user_behavior`（查库确认）影响后续推荐。
- 手记默认分类不可删除（前端禁用 + 后端拦截）。

### Task 7.2: 部署（网络恢复后）

- [ ] **Step 1: 执行迁移 SQL**（在服务器 PostgreSQL）执行 `docs/sql/2026-07-01-moments-as-articles.sql`。
- [ ] **Step 2: 部署后端 jar + 前端 dist**（参照既有部署方式：SCP `frontend/dist` 到 nginx root；重启后端；reload nginx）。
- [ ] **Step 3: 线上抽查**总列表/`/moments`/推荐/后台手记管理各一次。

---

## Self-Review 记录

- **Spec 覆盖**：去封面(P0)、默认手记分类+禁删(2.1/2.2/6.2)、article.images(2.3)、浏览排除手记(2.4)、MomentService 文章化(3.x)、匿名 IP 点赞(3.1/3.3)、编辑器整型标签(4.1)、九宫格保留(4.2/6.1)、后台补齐能力+共享 composable(5.x)、推荐零改动核对(3.4/7.1)、迁移与部署(1.1/7.2) —— 均有对应任务。
- **占位符**：无 TODO/TBD；不确定处（真实方法名、resultMap 映射、种子分类必填列）均标注“以现有为准/执行时核对”，属实现细节而非空缺。
- **类型一致**：`MomentDTO.tagIds:List<Integer>`、`MomentLike.articleId`、`ArticleMapper.pageMoments/incrementLikeCount`、`CategoryMapper.getMomentCategoryId`、`MomentTitleUtil.fallbackTitle` 在定义与调用处保持一致。
