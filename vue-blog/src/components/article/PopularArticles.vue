<template>
  <div class="popular-articles">
    <div class="section-header">
      <h3 class="section-title">
        <el-icon class="mr-2"><TrendCharts /></el-icon>
        热门文章
      </h3>
    </div>
    
    <div v-loading="loading" class="articles-list">
      <div
        v-for="(article, index) in articles"
        :key="article.id"
        class="article-item"
        @click="goToArticle(article.id)"
      >
        <div class="article-rank">{{ index + 1 }}</div>
        <div class="article-content">
          <h4 class="article-title">{{ article.title }}</h4>
          <div class="article-meta">
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ article.viewCount || 0 }}
            </span>
            <span class="meta-item">
              <el-icon><Star /></el-icon>
              {{ article.likeCount || 0 }}
            </span>
            <span class="meta-item">
              <el-icon><ChatDotRound /></el-icon>
              {{ article.commentCount || 0 }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && articles.length === 0" class="empty-state">
      <el-empty description="暂无热门文章" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { TrendCharts, View, Star, ChatDotRound } from '@element-plus/icons-vue'
import { getPopularArticles } from '@/api/article'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const articles = ref([])

// 加载热门文章
const loadPopularArticles = async () => {
  loading.value = true
  try {
    const response = await getPopularArticles({
      limit: 10,
      timeRange: 'week'
    })
    
    console.log('Popular articles response:', response)
    
    // 处理不同的响应格式
    if (response && response.data) {
      if (Array.isArray(response.data)) {
        articles.value = response.data
      } else if (response.data.records) {
        articles.value = response.data.records
      } else if (response.data.list) {
        articles.value = response.data.list
      } else {
        articles.value = response.data
      }
    } else {
      articles.value = []
    }
  } catch (error) {
    console.error('加载热门文章失败:', error)
    ElMessage.error('加载热门文章失败')
    
    // 使用Mock数据作为fallback
    articles.value = [
      {
        id: 1,
        title: 'Vue3博客系统开发指南',
        viewCount: 100,
        likeCount: 15,
        commentCount: 8
      },
      {
        id: 2,
        title: 'Element Plus组件库使用技巧',
        viewCount: 85,
        likeCount: 12,
        commentCount: 6
      },
      {
        id: 3,
        title: '现代前端开发最佳实践',
        viewCount: 75,
        likeCount: 10,
        commentCount: 5
      }
    ]
  } finally {
    loading.value = false
  }
}

// 跳转到文章详情
const goToArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

// 组件挂载时加载数据
onMounted(() => {
  loadPopularArticles()
})
</script>

<style scoped>
.popular-articles {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-md p-6;
}

.section-header {
  @apply mb-6;
}

.section-title {
  @apply text-lg font-semibold text-gray-900 dark:text-white flex items-center;
}

.articles-list {
  @apply space-y-3;
}

.article-item {
  @apply flex items-center space-x-3 p-3 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer transition-colors duration-200;
}

.article-rank {
  @apply w-8 h-8 bg-blue-100 dark:bg-blue-900 text-blue-600 dark:text-blue-400 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0;
}

.article-content {
  @apply flex-1 min-w-0;
}

.article-title {
  @apply text-sm font-medium text-gray-900 dark:text-white truncate mb-1;
}

.article-meta {
  @apply flex items-center space-x-3 text-xs text-gray-500 dark:text-gray-400;
}

.meta-item {
  @apply flex items-center space-x-1;
}

.empty-state {
  @apply py-8;
}

@media (max-width: 768px) {
  .article-meta {
    @apply flex-col space-x-0 space-y-1;
  }
}
</style>
