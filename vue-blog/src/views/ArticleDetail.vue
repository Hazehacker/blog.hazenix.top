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
              :author-name="article.authorName || article.author"
              :created-at="article.createTime || article.createdAt"
              :updated-at="article.updatedAt || article.updateTime"
              :views="article.viewCount || article.views"
              :likes="article.likeCount || article.likes"
              :comments="article.commentCount || article.comments"
              :content="article.content"
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

          <!-- 文章正文 -->
          <div class="article-body">
            <MarkdownRenderer :content="article.content" />
          </div>

          <!-- 文章底部信息 -->
          <footer class="article-footer">
            <div class="footer-actions">
              <el-button 
                type="primary" 
                :icon="Star" 
                @click="likeArticle"
                :class="{ 'liked': article.isLiked }"
              >
                {{ article.isLiked ? '已点赞' : '点赞' }} ({{ article.likeCount || 0 }})
              </el-button>
              
              <el-button 
                type="default" 
                :icon="Share" 
                @click="shareArticle"
              >
                分享
              </el-button>
              
              <el-button 
                type="default" 
                :icon="Collection" 
                @click="collectArticle"
                :class="{ 'collected': article.isCollected }"
              >
                {{ article.isCollected ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </footer>
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
            @toc-click="handleTocClick"
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
              @click="$router.push(`/article/${related.id}`)"
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
    <div class="comments-section">
      <CommentList 
        :article-id="articleId" 
        @comment-added="handleCommentAdded"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, Share, Collection } from '@element-plus/icons-vue'
import { getArticleDetail, getRelatedArticles, likeArticle as likeArticleApi, collectArticle as collectArticleApi } from '@/api/article'
import { useArticleStore } from '@/stores/article'
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue'
import CommentList from '@/components/article/CommentList.vue'
import ArticleMetadata from '@/components/article/ArticleMetadata.vue'
import TableOfContents from '@/components/article/TableOfContents.vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const articleStore = useArticleStore()

const articleId = route.params.id
const article = ref(null)
const loading = ref(false)
const relatedArticles = ref([])
const isMobile = ref(false)

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
    const res = await getArticleDetail(articleId)
    article.value = res.data
    
    // 加载相关文章
    await loadRelatedArticles()
  } catch (error) {
    console.error('Failed to load article:', error)
    ElMessage.error('加载文章失败')
    
    // Mock数据作为fallback
    article.value = {
      id: articleId,
      title: 'Vue3博客系统开发指南',
      content: `# Vue3博客系统开发指南

本文介绍了如何使用Vue3、Element Plus和Tailwind CSS构建一个现代化的博客系统。

## 技术栈

- Vue 3
- Element Plus
- Tailwind CSS
- Pinia
- Vue Router

## 主要功能

1. 文章管理
2. 评论系统
3. 用户认证
4. 响应式设计

## 开发过程

### 1. 环境配置

首先需要安装必要的依赖包...

### 2. 组件开发

创建各种组件来构建用户界面...

## 总结

通过这个项目，我们可以学习到现代前端开发的最佳实践。`,
      author: 'Hazenix',
      createTime: '2025-01-01',
      viewCount: 100,
      tags: [{ id: 1, name: 'Vue3' }, { id: 2, name: '前端' }]
    }
  } finally {
    loading.value = false
  }
}

// 加载相关文章
const loadRelatedArticles = async () => {
  try {
    const res = await getRelatedArticles(articleId, { limit: 5 })
    relatedArticles.value = res.data
  } catch (error) {
    console.error('Failed to load related articles:', error)
  }
}

// 点赞文章
const likeArticle = async () => {
  if (!article.value) return
  
  try {
    await likeArticleApi(articleId)
    article.value.isLiked = !article.value.isLiked
    article.value.likeCount = (article.value.likeCount || 0) + (article.value.isLiked ? 1 : -1)
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
    await collectArticleApi(articleId)
    article.value.isCollected = !article.value.isCollected
    ElMessage.success(article.value.isCollected ? '收藏成功' : '取消收藏')
  } catch (error) {
    console.error('Collect article failed:', error)
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

// 按标签搜索
const searchByTag = (tagName) => {
  router.push(`/articles?tag=${encodeURIComponent(tagName)}`)
}

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
  @apply min-h-screen bg-gray-50 dark:bg-gray-900;
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
  @apply text-3xl md:text-4xl font-bold text-gray-900 dark:text-gray-100 mb-4 leading-tight;
}

.article-summary {
  @apply p-6 bg-blue-50 dark:bg-blue-900/20 border-b border-gray-200 dark:border-gray-700;
}

.summary-content {
  @apply text-gray-700 dark:text-gray-300 leading-relaxed text-lg;
}

.article-body {
  @apply p-6;
}

.article-footer {
  @apply p-6 border-t border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700/50;
}

.footer-actions {
  @apply flex flex-wrap gap-3;
}

.footer-actions .el-button.liked {
  @apply bg-yellow-500 border-yellow-500 text-white;
}

.footer-actions .el-button.collected {
  @apply bg-green-500 border-green-500 text-white;
}

.empty-state {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-12 text-center;
}

.article-sidebar {
  @apply space-y-6;
}

.sidebar-section {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700;
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

/* 移动端适配 */
@media (max-width: 1024px) {
  .article-container {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .article-sidebar {
    @apply order-first;
  }
}

@media (max-width: 768px) {
  .article-container {
    @apply px-2 py-4;
    gap: 1rem;
  }
  
  .article-header,
  .article-summary,
  .article-body,
  .article-footer {
    @apply p-4;
  }
  
  .article-title {
    @apply text-2xl;
  }
  
  .footer-actions {
    @apply flex-col;
  }
  
  .footer-actions .el-button {
    @apply w-full;
  }
}

/* 打印样式 */
@media print {
  .article-sidebar,
  .comments-section,
  .footer-actions {
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
