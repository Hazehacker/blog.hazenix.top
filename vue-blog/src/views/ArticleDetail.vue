<template>
  <div class="article-detail">
    <div v-loading="loading" class="article-container">
      <!-- 文章内容 -->
      <main class="article-main">
        <article v-if="article" class="article-content">
          <!-- 文章标题 -->
          <header class="article-header">
            <h1 class="article-title">{{ article.title }}</h1>
            
            <!-- 文章元数据 -->
            <ArticleMetadata
              :author-name="'Hazenix'"
              :created-at="article.createTime || article.createdAt"
              :updated-at="article.updatedAt || article.updateTime"
              :content="article.content"
              :views="article.viewCount || article.views || 0"
              :category-name="getCategoryName(article)"
              :tags="article.tags"
            />
          </header>

          <!-- 文章摘要 -->
          <div v-if="article.summary" class="article-summary">
            <div class="summary-content">
              {{ article.summary }}
            </div>
          </div>

          <!-- AI摘要 -->
          <div v-if="article.summary" class="ai-summary">
            <div class="ai-summary-header">
              <div class="ai-summary-title">
                <el-icon class="ai-summary-icon"><Star /></el-icon>
                <span>AI Summary</span>
              </div>
              <div class="ai-summary-powered">
                Powered By DeepSeek-R1
              </div>
            </div>
            <div class="ai-summary-content">
              <p class="ai-summary-intro">以下是针对博客文章的深度解析与总结：</p>
              <div class="ai-summary-text">
                <el-icon class="ai-summary-bullet"><Star /></el-icon>
                <span>{{ generateAISummary(article.summary) }}</span>
              </div>
              <div class="ai-summary-expand" @click="toggleAISummary">
                <el-icon><ArrowDown /></el-icon>
                <span>{{ isAISummaryExpanded ? '收起' : '展开' }}</span>
              </div>
            </div>
          </div>

          <!-- 文章正文 -->
          <div class="article-body">
            <!-- 主要内容 -->
            <MarkdownRenderer :content="article.content" />
          </div>

        </article>

        <!-- 加载状态 -->
        <div v-else-if="!loading" class="empty-state">
          <el-empty description="文章不存在或已被删除">
            <el-button type="primary" @click="$router.push('/')">
              返回首页
            </el-button>
          </el-empty>
        </div>
      </main>

      <!-- 侧边栏 -->
      <aside class="article-sidebar">
        <!-- 目录 -->
        <div v-if="article?.content" class="sidebar-section">
          <TableOfContents 
            :content="article.content" 
            :is-mobile="isMobile"
            :article="article"
            @toc-click="handleTocClick"
            @like="likeArticle"
            @collect="collectArticle"
            @share="shareArticle"
          />
        </div>

        <!-- 相关文章 -->
        <div v-if="relatedArticles.length > 0" class="sidebar-section">
          <div class="section-header">
            <h3 class="section-title">相关文章</h3>
          </div>
          <div class="related-articles">
            <div
              v-for="related in relatedArticles"
              :key="related.id"
              class="related-item"
              @click="goToArticle(related)"
            >
              <h4 class="related-title">{{ related.title }}</h4>
              <p class="related-meta">
                {{ formatDate(related.createTime) }} · {{ related.viewCount || 0 }} 阅读
              </p>
            </div>
          </div>
        </div>

        <!-- 标签云 -->
        <div v-if="article?.tags && article.tags.length > 0" class="sidebar-section">
          <div class="section-header">
            <h3 class="section-title">标签</h3>
          </div>
          <div class="tag-cloud">
            <el-tag
              v-for="tag in article.tags"
              :key="tag.id || tag"
              size="small"
              class="tag-item"
              @click="searchByTag(getTagName(tag))"
            >
              {{ getTagName(tag) }}
            </el-tag>
          </div>
        </div>
      </aside>
    </div>

    <!-- 评论区 -->
    <div v-if="articleId" class="comments-section">
      <CommentList 
        :article-id="articleId" 
        @comment-added="handleCommentAdded"
      />
    </div>

    <!-- 固定互动按钮（右侧固定） -->
    <div v-if="article && !isMobile" class="fixed-actions">
      <div class="action-item" @click="likeArticle" :class="{ 'liked': article?.isLiked }">
        <el-icon class="action-icon" :class="{ 'text-yellow-500': article?.isLiked }">
          <Star />
        </el-icon>
        <span class="action-text">{{ article?.likeCount || article?.likes || 0 }}</span>
      </div>
      <div class="action-item" @click="collectArticle" :class="{ 'collected': article?.isCollected }">
        <el-icon class="action-icon" :class="{ 'text-green-500': article?.isCollected }">
          <Collection />
        </el-icon>
        <span class="action-text">{{ article?.isCollected ? '已收藏' : '收藏' }}</span>
      </div>
      <div class="action-item" @click="shareArticle">
        <el-icon class="action-icon">
          <Share />
        </el-icon>
        <span class="action-text">分享</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, Share, Collection, ArrowDown } from '@element-plus/icons-vue'
import { getArticleDetail, getArticleBySlug, getRelatedArticles, likeArticle as likeArticleApi, collectArticle as favoriteArticleApi, incrementViewCount } from '@/api/article'
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue'
import CommentList from '@/components/article/CommentList.vue'
import ArticleMetadata from '@/components/article/ArticleMetadata.vue'
import TableOfContents from '@/components/article/TableOfContents.vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

// 路由参数（可能是ID或slug）
const routeParam = computed(() => route.params.id)
const article = ref(null)
const loading = ref(false)
const relatedArticles = ref([])
const isMobile = ref(false)
const isAISummaryExpanded = ref(false)
const shouldScrollToTop = ref(false)

// 判断路由参数是ID还是slug（ID是纯数字，slug包含字母或特殊字符）
const isSlug = computed(() => {
  const id = routeParam.value
  // 如果包含非数字字符，认为是slug
  return id && !/^\d+$/.test(id)
})

// 文章的实际ID（从文章数据中获取，如果是slug则从加载后的文章数据中获取）
const articleId = computed(() => {
  // 如果文章已加载，优先使用文章的实际ID
  if (article.value && article.value.id) {
    return article.value.id
  }
  // 如果路由参数是数字ID，直接使用
  if (!isSlug.value) {
    return routeParam.value
  }
  // 如果是slug但文章还没加载，返回null（等待文章加载）
  return null
})

// 响应式检测
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

// 获取分类名称（兼容新的API格式）
const getCategoryName = (article) => {
  // 如果直接有categoryName字段，返回它
  if (article.categoryName) {
    return article.categoryName
  }
  // 如果有category对象，返回其name
  if (article.category && article.category.name) {
    return article.category.name
  }
  // 如果只有categoryId，需要从其他地方获取分类名称
  // 这里可以根据需要实现从分类ID获取名称的逻辑
  return ''
}

// 获取标签名称（兼容新的API格式）
const getTagName = (tag) => {
  // 如果tag是对象，返回name字段
  if (typeof tag === 'object' && tag !== null) {
    return tag.name
  }
  // 如果tag是ID，这里需要从标签列表中查找对应的名称
  // 由于ArticleDetail页面可能没有加载所有标签，这里暂时返回ID
  return tag
}

// 加载文章详情
const loadArticle = async () => {
  loading.value = true
  try {
    const identifier = routeParam.value
    console.log('Loading article with identifier:', identifier, 'isSlug:', isSlug.value)
    console.log('API Base URL:', import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090')
    
    // 根据是slug还是ID调用不同的API
    let res
    if (isSlug.value) {
      res = await getArticleBySlug(identifier)
    } else {
      res = await getArticleDetail(identifier)
    }
    console.log('Article detail response:', res)
    
    // 处理不同的响应格式
    if (res && res.data) {
      article.value = res.data
      
      // 如果文章有slug且当前URL使用的是ID，更新URL为slug
      if (article.value.slug && !isSlug.value) {
        router.replace({ name: 'ArticleDetail', params: { id: article.value.slug } })
      }
      
      // 增加浏览量（使用文章ID，不是slug）
      try {
        const actualId = article.value.id || identifier
        await incrementViewCount(actualId)
      } catch (viewError) {
        console.warn('Failed to increment view count:', viewError)
      }
      
      // 加载相关文章
      await loadRelatedArticles()
      
      // 如果是从路由变化触发的，滚动到顶部
      if (shouldScrollToTop.value) {
        window.scrollTo({ top: 0, behavior: 'smooth' })
        shouldScrollToTop.value = false
      }
    } else {
      throw new Error('文章数据为空')
    }
  } catch (error) {
    console.error('Failed to load article:', error)
    
    // 显示更详细的错误信息
    let errorMessage = '加载文章失败'
    if (error.response) {
      if (error.response.status === 404) {
        errorMessage = '文章不存在或已被删除'
      } else if (error.response.status === 500) {
        errorMessage = '服务器内部错误，请稍后重试'
      } else {
        errorMessage = `请求失败 (${error.response.status})`
      }
    } else if (error.code === 'ECONNREFUSED') {
      errorMessage = '无法连接到服务器，请检查后端服务是否启动'
    } else if (error.message.includes('timeout')) {
      errorMessage = '请求超时，请检查网络连接'
    }
    
    ElMessage.error(errorMessage)
    
    // 使用Mock数据作为fallback
    console.log('Using mock data as fallback')
    article.value = {
      id: articleId.value,
      title: 'Vue3博客系统开发指南 (Mock数据)',
      summary: '本文介绍了如何使用Vue3、Element Plus和Tailwind CSS构建一个现代化的博客系统。',
      content: `# Vue3博客系统开发指南

本文介绍了如何使用Vue3、Element Plus和Tailwind CSS构建一个现代化的博客系统。

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Element Plus** - Vue 3组件库
- **Tailwind CSS** - 实用优先的CSS框架
- **Pinia** - Vue状态管理
- **Vue Router** - Vue官方路由管理器

## 主要功能

1. **文章管理** - 创建、编辑、删除文章
2. **评论系统** - 用户评论和回复
3. **用户认证** - 登录、注册、权限管理
4. **响应式设计** - 适配各种设备

## 开发过程

### 1. 环境配置

首先需要安装必要的依赖包：

\`\`\`bash
npm install vue@next
npm install element-plus
npm install tailwindcss
npm install pinia
npm install vue-router@4
\`\`\`

### 2. 组件开发

创建各种组件来构建用户界面：

\`\`\`vue
&lt;template&gt;
  &lt;div class="article-card"&gt;
    &lt;h3 class="title"&gt;{{ article.title }}&lt;/h3&gt;
    &lt;p class="content"&gt;{{ article.summary }}&lt;/p&gt;
    &lt;div class="meta"&gt;
      &lt;span class="author"&gt;{{ article.author }}&lt;/span&gt;
      &lt;span class="date"&gt;{{ formatDate(article.createdAt) }}&lt;/span&gt;
    &lt;/div&gt;
  &lt;/div&gt;
&lt;/template&gt;

&lt;script setup&gt;
import { computed } from 'vue'

const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

const formatDate = (date) => {
  return new Date(date).toLocaleDateString()
}
&lt;/script&gt;
\`\`\`

## 代码示例

### JavaScript代码

\`\`\`javascript
// 文章API服务
class ArticleService {
  constructor() {
    this.baseURL = '/api/articles'
  }
  
  async getArticles(params = {}) {
    const queryString = new URLSearchParams(params).toString()
    const response = await fetch(this.baseURL + '?' + queryString)
    return response.json()
  }
  
  async getArticle(id) {
    const response = await fetch(this.baseURL + '/' + id)
    return response.json()
  }
}
\`\`\`

## 表格示例

| 功能 | 技术栈 | 状态 |
|------|--------|------|
| 前端框架 | Vue 3 | ✅ 完成 |
| UI组件库 | Element Plus | ✅ 完成 |
| 样式框架 | Tailwind CSS | ✅ 完成 |
| 状态管理 | Pinia | ✅ 完成 |
| 路由管理 | Vue Router | ✅ 完成 |

## 引用块示例

> **重要提示**: 这是一个Mock数据示例，用于测试Markdown渲染功能。
> 
> 实际项目中，请确保：
> 1. 后端服务正常运行
> 2. API接口正确配置
> 3. 数据库连接正常

## 总结

通过这个项目，我们可以学习到现代前端开发的最佳实践：

- **组件化开发** - 提高代码复用性
- **响应式设计** - 适配各种设备
- **状态管理** - 统一管理应用状态
- **路由管理** - 实现单页应用导航

---

**注意**: 这是Mock数据，实际文章加载失败。请检查：
1. 后端服务是否启动 (默认端口: 9090)
2. API接口是否正确配置
3. 网络连接是否正常`,
      author: 'Hazenix',
      authorName: 'Hazenix',
      createTime: '2025-01-01',
      createdAt: '2025-01-01',
      viewCount: 100,
      views: 100,
      commentCount: 5,
      comments: 5,
      tags: [{ id: 1, name: 'Vue3' }, { id: 2, name: '前端' }, { id: 3, name: 'JavaScript' }],
      category: { id: 1, name: '技术' },
      categoryName: '技术'
    }
  } finally {
    loading.value = false
  }
}

// 加载相关文章
const loadRelatedArticles = async () => {
  try {
    const res = await getRelatedArticles(articleId.value, { limit: 5 })
    relatedArticles.value = res.data
  } catch (error) {
    console.error('Failed to load related articles:', error)
  }
}

// 点赞文章
const likeArticle = async () => {
  if (!article.value) return
  
  try {
    await likeArticleApi(articleId.value)
    article.value.isLiked = !article.value.isLiked
    
    // 确保点赞数的一致性更新
    const currentLikeCount = article.value.likeCount || article.value.likes || 0
    article.value.likeCount = currentLikeCount + (article.value.isLiked ? 1 : -1)
    article.value.likes = article.value.likeCount // 保持两个字段同步
    
    ElMessage.success(article.value.isLiked ? '点赞成功' : '取消点赞')
  } catch (error) {
    console.error('Like article failed:', error)
    ElMessage.error('操作失败')
  }
}

// 分享文章
const shareArticle = async () => {
  if (!article.value) return
  
  try {
    if (navigator.share) {
      await navigator.share({
        title: article.value.title,
        text: article.value.summary || '',
        url: window.location.href
      })
    } else {
      // 复制链接到剪贴板
      await navigator.clipboard.writeText(window.location.href)
      ElMessage.success('链接已复制到剪贴板')
    }
  } catch (error) {
    console.error('Share article failed:', error)
    ElMessage.error('分享失败')
  }
}

// 收藏文章
const collectArticle = async () => {
  if (!article.value) return
  
  try {
    await favoriteArticleApi(articleId.value)
    article.value.isCollected = !article.value.isCollected
    ElMessage.success(article.value.isCollected ? '收藏成功' : '取消收藏')
  } catch (error) {
    console.error('Favorite article failed:', error)
    ElMessage.error('操作失败')
  }
}

// 处理目录点击
const handleTocClick = (id) => {
  // 目录点击处理逻辑已在TableOfContents组件中实现
  console.log('TOC clicked:', id)
}

// 处理评论添加
const handleCommentAdded = (comment) => {
  // 更新文章评论数
  if (article.value) {
    article.value.commentCount = (article.value.commentCount || 0) + 1
  }
}

// 跳转到指定文章
// 跳转到文章详情（优先使用slug）
const goToArticle = (article) => {
  // 如果传入的是对象，优先使用slug
  if (typeof article === 'object' && article !== null) {
    const identifier = article.slug || article.id
    router.push(`/article/${identifier}`)
  } else {
    // 如果传入的是ID，直接使用
    router.push(`/article/${article}`)
  }
}

// 按标签搜索
const searchByTag = (tagName) => {
  // 从当前文章的标签中找到对应的标签对象
  const tag = article.value?.tags?.find(t => {
    if (typeof t === 'object' && t !== null) {
      return t.name === tagName
    }
    return t === tagName
  })
  
  if (tag) {
    // 如果找到标签对象，使用其ID跳转到标签详情页
    const tagId = typeof tag === 'object' ? tag.id : tag
    router.push(`/tag/${tagId}`)
  } else {
    // 如果没找到，跳转到文章列表页进行搜索
    router.push(`/articles?tag=${encodeURIComponent(tagName)}`)
  }
}

// 生成AI摘要
const generateAISummary = (summary) => {
  if (!summary) return '暂无摘要'
  
  // 简单的AI摘要生成逻辑，实际项目中可以调用AI API
  const words = summary.split('')
  if (words.length <= 20) {
    return summary
  }
  
  // 生成一个诗意的摘要
  const aiSummaries = [
    '代码孤岛漫行苦,星火聚源破迷途',
    '技术探索无止境,创新思维启新程',
    '知识分享传智慧,社区共建创未来',
    '学习路上有你我,技术成长共前行'
  ]
  
  return aiSummaries[Math.floor(Math.random() * aiSummaries.length)]
}

// 切换AI摘要展开状态
const toggleAISummary = () => {
  isAISummaryExpanded.value = !isAISummaryExpanded.value
}

// 监听路由参数变化
watch(() => route.params.id, (newId, oldId) => {
  console.log('Route params id changed:', oldId, '->', newId)
  if (newId && newId !== oldId) {
    console.log('Loading new article due to route change')
    // 清空旧数据
    article.value = null
    relatedArticles.value = []
    // 标记需要滚动到顶部
    shouldScrollToTop.value = true
    loadArticle()
  }
})

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadArticle()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.article-detail {
  @apply min-h-screen;
  background-color: rgb(255, 255, 255);
}

.article-container {
  @apply max-w-7xl mx-auto px-4 py-8;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 2rem;
}

.article-main {
  @apply min-w-0;
}

.article-content {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden;
}

.article-header {
  @apply p-6 border-b border-gray-200 dark:border-gray-700;
}

.article-title {
  @apply text-4xl md:text-5xl font-bold text-gray-900 dark:text-gray-100 mb-6 leading-tight text-center;
}

.article-summary {
  @apply p-6 bg-blue-50 dark:bg-blue-900/20 border-b border-gray-200 dark:border-gray-700;
}

.summary-content {
  @apply text-gray-700 dark:text-gray-300 leading-relaxed text-lg;
}

.ai-summary {
  @apply p-6 bg-gray-50 dark:bg-gray-700/50 border-b border-gray-200 dark:border-gray-700;
}

.ai-summary-header {
  @apply flex justify-between items-start mb-4;
}

.ai-summary-title {
  @apply flex items-center text-lg font-semibold text-gray-900 dark:text-gray-100;
}

.ai-summary-icon {
  @apply mr-2 text-purple-500;
}

.ai-summary-powered {
  @apply text-xs text-gray-400 dark:text-gray-500;
}

.ai-summary-content {
  @apply space-y-3;
}

.ai-summary-intro {
  @apply text-gray-700 dark:text-gray-300 text-sm m-0;
}

.ai-summary-text {
  @apply flex items-start text-gray-800 dark:text-gray-200;
}

.ai-summary-bullet {
  @apply mr-2 text-purple-500 mt-0.5 flex-shrink-0;
}

.ai-summary-expand {
  @apply flex items-center text-sm text-blue-600 dark:text-blue-400 cursor-pointer hover:text-blue-700 dark:hover:text-blue-300 transition-colors;
}

.article-body {
  @apply p-6;
}

.content-section {
  @apply mb-8;
}

.section-title {
  @apply text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4 relative pl-4;
}

.section-title::before {
  content: '';
  @apply absolute left-0 top-0 bottom-0 w-1 bg-gray-300 dark:bg-gray-600 rounded;
}

.problem-list {
  @apply space-y-4;
}

.problem-items {
  @apply list-none p-0 m-0 space-y-3;
}

.problem-item {
  @apply text-gray-700 dark:text-gray-300 leading-relaxed relative pl-6;
}

.problem-item::before {
  content: '•';
  @apply absolute left-0 text-gray-900 dark:text-gray-100 font-bold;
}

.problem-conclusion {
  @apply text-gray-700 dark:text-gray-300 leading-relaxed mt-4;
}


.empty-state {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-12 text-center;
}

.article-sidebar {
  @apply space-y-6;
  position: sticky;
  top: 2rem;
  max-height: calc(100vh - 4rem);
  overflow-y: auto;
}

.sidebar-section {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-sm border-none ;
}

.section-header {
  @apply p-4 border-b border-gray-200 dark:border-gray-700;
}

.section-title {
  @apply text-lg font-semibold text-gray-900 dark:text-gray-100 m-0;
}

.related-articles {
  @apply p-4 space-y-4;
}

.related-item {
  @apply cursor-pointer p-3 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors;
}

.related-title {
  @apply text-sm font-medium text-gray-900 dark:text-gray-100 mb-1 line-clamp-2;
}

.related-meta {
  @apply text-xs text-gray-500 dark:text-gray-400 m-0;
}

.tag-cloud {
  @apply p-4 flex flex-wrap gap-2;
}

.tag-item {
  @apply cursor-pointer transition-colors hover:bg-blue-100 dark:hover:bg-blue-900/30;
}

.comments-section {
  @apply max-w-7xl mx-auto px-4 pb-8;
}

/* 固定互动按钮 */
.fixed-actions {
  position: fixed;
  right: 2rem;
  top: 50%;
  transform: translateY(-50%);
  z-index: 100;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background: white;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(229, 231, 235, 1);
}

.dark .fixed-actions {
  background: rgb(31, 41, 55);
  border-color: rgba(55, 65, 81, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.fixed-actions .action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0.75rem;
  border-radius: 8px;
  transition: all 0.2s;
  min-width: 60px;
  color: rgb(107, 114, 128);
}

.fixed-actions .action-item:hover {
  background-color: rgb(243, 244, 246);
  color: rgb(37, 99, 235);
}

.dark .fixed-actions .action-item:hover {
  background-color: rgb(55, 65, 81);
  color: rgb(96, 165, 250);
}

.fixed-actions .action-item.liked {
  color: rgb(234, 179, 8);
}

.fixed-actions .action-item.liked:hover {
  color: rgb(202, 138, 4);
}

.fixed-actions .action-item.collected {
  color: rgb(34, 197, 94);
}

.fixed-actions .action-item.collected:hover {
  color: rgb(22, 163, 74);
}

.fixed-actions .action-icon {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}

.fixed-actions .action-text {
  font-size: 0.75rem;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
}

/* 移动端适配 */
@media (max-width: 1024px) {
  .article-container {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .article-sidebar {
    @apply order-first;
    position: static;
    max-height: none;
    overflow-y: visible;
  }
}

@media (max-width: 768px) {
  .article-container {
    @apply px-2 py-4;
    gap: 1rem;
  }
  
  .article-header,
  .article-summary,
  .article-body {
    @apply p-4;
  }
  
  .article-title {
    @apply text-2xl;
  }
  
  .fixed-actions {
    display: none;
  }
}

/* 打印样式 */
@media print {
  .article-sidebar,
  .comments-section {
    @apply hidden;
  }
  
  .article-container {
    grid-template-columns: 1fr;
    max-width: none;
    padding: 0;
  }
  
  .article-content {
    @apply shadow-none border-none;
  }
}
</style>
