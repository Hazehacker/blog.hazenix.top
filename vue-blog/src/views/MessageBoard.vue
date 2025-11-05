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

          <!-- 文章正文 -->
          <div class="article-body">
            <!-- 主要内容 -->
            <MarkdownRenderer :content="article.content" />
          </div>

        </article>

        <!-- 加载状态 -->
        <div v-else-if="!loading" class="empty-state">
          <el-empty description="留言板加载中...">
            <el-button type="primary" @click="loadArticle">
              重新加载
            </el-button>
          </el-empty>
        </div>
      </main>

      <!-- 侧边栏 -->
      <aside class="article-sidebar">
        <!-- 目录和操作按钮 -->
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
      </aside>
    </div>

    <!-- 评论区 -->
    <div class="comments-section">
      <CommentList 
        :article-id="1" 
        @comment-added="handleCommentAdded"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getArticleDetail, likeArticle as likeArticleApi, collectArticle as favoriteArticleApi, incrementViewCount } from '@/api/article'
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue'
import CommentList from '@/components/article/CommentList.vue'
import ArticleMetadata from '@/components/article/ArticleMetadata.vue'
import TableOfContents from '@/components/article/TableOfContents.vue'
import dayjs from 'dayjs'

const router = useRouter()

const article = ref(null)
const loading = ref(false)
const isMobile = ref(false)

// 响应式检测
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
}

// 获取分类名称（兼容新的API格式）
const getCategoryName = (article) => {
  if (article.categoryName) {
    return article.categoryName
  }
  if (article.category && article.category.name) {
    return article.category.name
  }
  return ''
}

// 加载文章详情（留言板固定使用 id=1）
const loadArticle = async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(1)
    
    if (res && res.data) {
      article.value = res.data
      
      // 增加浏览量
      try {
        await incrementViewCount(1)
      } catch (viewError) {
        console.warn('Failed to increment view count:', viewError)
      }
    } else {
      throw new Error('文章数据为空')
    }
  } catch (error) {
    console.error('Failed to load message board:', error)
    
    // 如果文章不存在，显示默认内容
    article.value = {
      id: 1,
      title: '留言板',
      content: `# 留言板

远方的朋友，想说点什么吗？

从我的故事，到技术问题，或是日常闲聊

这里永远欢迎你的评论！

在这里留下你的文字吧~`,
      author: 'Hazenix',
      authorName: 'Hazenix',
      createTime: new Date().toISOString(),
      createdAt: new Date().toISOString(),
      viewCount: 0,
      views: 0,
      commentCount: 0,
      comments: 0,
      tags: [],
      category: null,
      categoryName: ''
    }
    
    ElMessage.warning('留言板内容加载失败，显示默认内容')
  } finally {
    loading.value = false
  }
}

// 点赞文章
const likeArticle = async () => {
  if (!article.value) return
  
  try {
    await likeArticleApi(1)
    article.value.isLiked = !article.value.isLiked
    
    const currentLikeCount = article.value.likeCount || article.value.likes || 0
    article.value.likeCount = currentLikeCount + (article.value.isLiked ? 1 : -1)
    article.value.likes = article.value.likeCount
    
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
    await favoriteArticleApi(1)
    article.value.isCollected = !article.value.isCollected
    ElMessage.success(article.value.isCollected ? '收藏成功' : '取消收藏')
  } catch (error) {
    console.error('Favorite article failed:', error)
    ElMessage.error('操作失败')
  }
}

// 处理目录点击
const handleTocClick = (id) => {
  console.log('TOC clicked:', id)
}

// 处理评论添加
const handleCommentAdded = (comment) => {
  if (article.value) {
    article.value.commentCount = (article.value.commentCount || 0) + 1
  }
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

.article-body {
  @apply p-6;
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
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-sm border-none;
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
  .article-body {
    @apply p-4;
  }
  
  .article-title {
    @apply text-2xl;
  }
}
</style>


