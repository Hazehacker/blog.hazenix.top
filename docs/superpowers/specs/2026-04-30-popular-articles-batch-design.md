# 热门文章离线批量计算 — 设计文档

- 日期：2026-04-30
- 范围：将「热门文章」从每次请求实时查 DB 改为「凌晨 1 点定时批量计算 → 写 Redis → 请求直读 Redis」

## 1. 现状

- `ArticleController#getPopularArticles` 调用 `ArticleService#getPopularArticles(8)`
- mapper SQL：`SELECT * FROM article ORDER BY (like_count + view_count) DESC LIMIT ?`
- 每次请求都打 DB；无缓存；公式简陋

相关文件：

- [ArticleController.java:75-83](backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleController.java#L75-L83)
- [ArticleServiceImpl.java:534-552](backend/blog-server/src/main/java/top/hazenix/service/impl/ArticleServiceImpl.java#L534-L552)
- [ArticleMapper.xml:100-102](backend/blog-server/src/main/resources/mapper/ArticleMapper.xml#L100-L102)

## 2. 目标

- 凌晨 1:00 定时跑一次，对**所有** `status=0` 的文章按热度公式排序，取 Top N 写入 Redis
- HTTP 请求只读 Redis（cache miss 时降级原 SQL，并触发一次重算）
- 已有 Redis 与序列化配置，沿用 `RedisTemplate`

## 3. 关键决策

| 项 | 取值 | 说明 |
|---|---|---|
| 调度时间 | `cron = "0 0 1 * * ?"` | 凌晨 1 点 |
| 计算范围 | 所有 `status=0` 的文章（全量） | 不做时间窗口过滤 |
| 热度公式 | `score = view*1 + like*3 + favorite*5` | 权重以常量提取 |
| Top N | 8 | 与现有保持一致，常量化便于调整 |
| Redis key | `popular:articles` | 字符串 key |
| Redis value | `List<ArticleShortVO>` 的 JSON | 直接整体读写 |
| TTL | 无 | 每天覆盖；防任务挂掉时旧榜单被擦掉 |
| 启动预热 | `@PostConstruct` 调一次 `recompute()` | 防冷启动 / Redis 清档后空窗 |
| Cache miss | 降级到原 SQL 并异步重算 | 兜底保证可用性 |

## 4. 架构

### 4.1 新增 / 修改组件

| 组件 | 路径 | 类型 |
|---|---|---|
| 启用调度 | `top.hazenix.BlogApplication` | 修改：加 `@EnableScheduling` |
| 定时任务 | `top.hazenix.task.PopularArticleTask` | 新增 |
| 业务接口 | `top.hazenix.service.PopularArticleService` | 新增 |
| 业务实现 | `top.hazenix.service.impl.PopularArticleServiceImpl` | 新增 |
| 全量查询 SQL | `ArticleMapper#listAllForScoring` + xml | 新增 |
| Controller 切换数据源 | `ArticleController#getPopularArticles` | 修改：调用 `PopularArticleService.getCachedOrFallback(8)` |
| 常量 | `DefaultConstants` | 新增 Redis key / Top N / 权重常量 |
| 旧 service 方法 | `ArticleServiceImpl#getPopularArticles` | **保留**作为 fallback |

### 4.2 数据流

#### 写路径（定时 + 启动预热）

```
PopularArticleTask.run()         <- @Scheduled(cron="0 0 1 * * ?")
   │                             <- @PostConstruct 启动也跑一次
   ▼
PopularArticleServiceImpl.recompute()
   │
   ├─► articleMapper.listAllForScoring()         // status=0 全量
   ├─► 内存计算 score = view*1 + like*3 + favorite*5
   ├─► 排序 desc，截 Top N
   ├─► BeanUtils 转 List<ArticleShortVO>
   └─► redisTemplate.opsForValue().set("popular:articles", list)
```

#### 读路径

```
GET /article/popular
   │
   ▼
ArticleController.getPopularArticles()
   │
   ▼
PopularArticleService.getCachedOrFallback(8)
   │
   ├─ Redis hit  → 返回缓存
   └─ Redis miss
        ├─► articleMapper.getPopularArticles(8)        // 原 SQL，兜底
        └─► 异步触发 recompute() 回填 Redis
```

## 5. 数据结构

- Redis key：`popular:articles`
- Value 类型：`List<ArticleShortVO>`（依赖现有 `RedisConfiguration` 的 Jackson 序列化）
- 新增 mapper 返回字段：`id, title, cover_image, like_count, favorite_count, view_count, create_time`
  - 复用 `Article` 实体接收即可

## 6. 错误处理

| 场景 | 处理 |
|---|---|
| 任务计算抛异常 | `try/catch` 全包，`log.error`，下一轮重试 |
| Redis 写失败 | log，不影响 DB / 不抛 |
| Redis 读失败 | 走 mapper fallback |
| 全表无 `status=0` 文章 | 写入空列表，前端正常处理空态 |
| 异步 recompute 自身失败 | log，不阻塞当前请求响应 |

## 7. 测试策略

### 单元测试

- `PopularArticleServiceImplTest`
  - `recompute_should_apply_weighted_formula_and_topN()`：mock mapper 返回若干文章，验证排序结果与截断
  - `recompute_should_handle_empty_list()`：mapper 返回空，应写空 list 不抛
  - `getCachedOrFallback_should_return_redis_when_hit()`
  - `getCachedOrFallback_should_query_db_when_miss()` 并验证触发了一次 recompute

### 手动验证

- 本地启动后 `redis-cli get popular:articles` 看是否有数据（启动预热生效）
- 临时把 cron 改为 `*/30 * * * * ?` 观察周期性刷新
- 命中 / miss 两条路径分别用 `redis-cli del popular:articles` 触发

## 8. 风险与权衡

- **全表扫描**：当前文章规模下成本可忽略；若未来扩展到数万级文章再考虑加 `(status, like_count+view_count)` 之类的索引或限制时间窗口
- **跨进程一致性**：单实例部署足够；多实例时多个节点会重复跑一次，写入幂等无副作用，可接受。如未来上集群可考虑分布式锁
- **公式调参**：权重、Top N、cron 全部常量化，后期改 `DefaultConstants` 即可

## 9. 改动文件清单

新增：

- `backend/blog-server/src/main/java/top/hazenix/task/PopularArticleTask.java`
- `backend/blog-server/src/main/java/top/hazenix/service/PopularArticleService.java`
- `backend/blog-server/src/main/java/top/hazenix/service/impl/PopularArticleServiceImpl.java`

修改：

- `backend/blog-server/src/main/java/top/hazenix/BlogApplication.java`（加 `@EnableScheduling`）
- `backend/blog-server/src/main/java/top/hazenix/controller/user/ArticleController.java`（调用新 service）
- `backend/blog-server/src/main/java/top/hazenix/mapper/ArticleMapper.java`（加 `listAllForScoring`）
- `backend/blog-server/src/main/resources/mapper/ArticleMapper.xml`（对应 SQL）
- `backend/blog-common/src/main/java/top/hazenix/constant/DefaultConstants.java`（Redis key / Top N / 权重常量）

保留：

- `ArticleServiceImpl#getPopularArticles` 与原 mapper SQL，作为 cache miss 的 fallback
