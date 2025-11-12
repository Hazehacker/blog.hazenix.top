<template>
  <div class="search-page">
    <div class="page-header">
      <h1 class="page-title">搜索结果</h1>
      <div class="search-info">
        <span v-if="keyword">关键词: "{{ keyword }}"</span>
        <span v-if="results.length > 0">找到 {{ results.length }} 个结果</span>
      </div>
    </div>
    
    <div class="content">
      <!-- 搜索框 -->
      <div class="search-box-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文章标题、内容或标签..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleClear"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch" type="primary">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- 搜索结果 -->
      <div v-loading="loading" class="search-results">
        <div v-if="results.length > 0" class="results-list">
          <div
            v-for="article in results"
            :key="article.id"
            class="result-item"
            @click="goToArticle(article)"
          >
            <div class="result-content">
              <h3 class="result-title">{{ article.title }}</h3>
              <p class="result-meta">
                <span class="meta-item">
                  <el-icon><View /></el-icon>
                  {{ article.viewCount || 0 }} 阅读
                </span>
                <span class="meta-item">
                  <el-icon><Star /></el-icon>
                  {{ article.likeCount || 0 }} 点赞
                </span>
                <span class="meta-item">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ article.commentCount || 0 }} 评论
                </span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ formatDate(article.createTime) }}
                </span>
              </p>
              <div class="result-tags">
                <el-tag v-if="article.categoryName" size="small" type="primary">
                  {{ article.categoryName }}
                </el-tag>
                <el-tag
                  v-for="tag in article.tags"
                  :key="tag.id || tag"
                  size="small"
                  class="tag-item"
                  @click.stop="searchByTag(getTagName(tag), tag)"
                >
                  {{ getTagName(tag) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading && keyword" class="empty-state">
          <el-empty description="没有找到相关文章">
            <el-button type="primary" @click="handleClear">
              重新搜索
            </el-button>
          </el-empty>
        </div>

        <!-- 初始状态 -->
        <div v-else-if="!loading && !keyword" class="initial-state">
          <div class="initial-content">
            <el-icon class="search-icon"><Search /></el-icon>
            <h3>开始搜索</h3>
            <p>输入关键词来搜索文章</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, View, Star, ChatDotRound, Clock } from '@element-plus/icons-vue'
import { getArticleList } from '@/api/article'
import { setSEO } from '@/utils/seo'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const keyword = ref('')
const results = ref([])

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

// 获取标签名称
const getTagName = (tag) => {
  if (typeof tag === 'object' && tag !== null) {
    return tag.name
  }
  return tag
}

// 按标签搜索
const searchByTag = (tagName, tag) => {
  // 如果tag是对象，直接使用其ID
  if (tag && typeof tag === 'object' && tag.id) {
    router.push(`/tag/${tag.id}`)
  } else {
    // 如果没找到，跳转到文章列表页进行搜索
    router.push(`/articles?tag=${encodeURIComponent(tagName)}`)
  }
}

// 执行搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  keyword.value = searchKeyword.value.trim()
  loading.value = true

  try {
    const response = await getArticleList({
      keyword: keyword.value,
      status: '0' // 只搜索正常状态的文章
    })
    
    results.value = response.data || []
  } catch (error) {
    // console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
    results.value = []
  } finally {
    loading.value = false
  }
}

// 清空搜索
const handleClear = () => {
  searchKeyword.value = ''
  keyword.value = ''
  results.value = []
}

// 跳转到文章详情（优先使用slug）
const goToArticle = (article) => {
  // 如果传入的是对象，优先使用slug
  if (typeof article === 'object' && article !== null) {
    const identifier = article.slug || article.id
    // 使用路由名称跳转，更可靠
    router.push({ name: 'ArticleDetail', params: { id: identifier } })
  } else {
    // 如果传入的是ID，直接使用
    router.push({ name: 'ArticleDetail', params: { id: article } })
  }
}

// 初始化
onMounted(() => {
  // 从URL参数获取搜索关键词
  if (route.query.q) {
    searchKeyword.value = route.query.q
    handleSearch()
  }

  setSEO({
    title: '搜索',
    description: '在博客中搜索内容',
    keywords: ['搜索', '查找', '博客'],
  })
})
</script>

<style scoped>
.search-page {
  @apply min-h-screen bg-gray-50 dark:bg-gray-900 py-16;
}

.page-header {
  @apply text-center mb-12;
}

.page-title {
  @apply text-3xl font-bold text-gray-900 dark:text-white mb-4;
}

.search-info {
  @apply text-gray-600 dark:text-gray-400;
}

.content {
  @apply max-w-4xl mx-auto px-4;
}

.search-box-container {
  @apply mb-8;
}

.search-results {
  @apply min-h-96;
}

.results-list {
  @apply space-y-4;
}

.result-item {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 cursor-pointer hover:shadow-lg transition-shadow duration-300;
}

.result-content {
  @apply space-y-3;
}

.result-title {
  @apply text-xl font-semibold text-gray-900 dark:text-white hover:text-blue-600 dark:hover:text-blue-400;
}

.result-meta {
  @apply flex flex-wrap gap-4 text-sm text-gray-500 dark:text-gray-400;
}

.meta-item {
  @apply flex items-center gap-1;
}

.result-tags {
  @apply flex flex-wrap gap-2;
}

.tag-item {
  @apply m-0;
}

.empty-state,
.initial-state {
  @apply flex justify-center items-center py-16;
}

.initial-content {
  @apply text-center;
}

.search-icon {
  @apply text-6xl text-gray-400 dark:text-gray-500 mb-4;
}

.initial-content h3 {
  @apply text-xl font-semibold text-gray-700 dark:text-gray-300 mb-2;
}

.initial-content p {
  @apply text-gray-500 dark:text-gray-400;
}

@media (max-width: 768px) {
  .result-meta {
    @apply flex-col gap-2;
  }
}
</style>