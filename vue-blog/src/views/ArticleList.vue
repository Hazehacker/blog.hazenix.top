<template>
  <div class="article-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">文章列表</h1>
        <p class="page-description">探索我们的精彩文章，发现新的知识和见解</p>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <div class="search-container">
        <!-- 搜索框 -->
        <div class="search-box">
        <el-input
            v-model="searchForm.keyword"
            placeholder="搜索文章标题、内容或标签..."
            size="large"
            clearable
          @keyup.enter="handleSearch"
            @clear="handleSearch"
        >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          <template #append>
              <el-button @click="handleSearch" type="primary">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
          </template>
        </el-input>
        </div>

        <!-- 筛选条件 -->
        <div class="filter-container">
          <div class="filter-row">
            <!-- 分类筛选 -->
            <div class="filter-item">
              <label class="filter-label">分类</label>
              <el-select
                v-model="searchForm.categoryId"
                placeholder="选择分类"
                clearable
                @change="handleFilter"
                class="filter-select"
              >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
            </div>

            <!-- 标签筛选 -->
            <div class="filter-item">
              <label class="filter-label">标签</label>
              <el-select
                v-model="searchForm.tagId"
                placeholder="选择标签"
                clearable
                @change="handleFilter"
                class="filter-select"
              >
          <el-option
            v-for="tag in tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>
            </div>

            <!-- 排序方式 -->
            <div class="filter-item">
              <label class="filter-label">排序</label>
              <el-select
                v-model="searchForm.sortBy"
                placeholder="排序方式"
                @change="handleFilter"
                class="filter-select"
              >
                <el-option label="最新发布" value="createTime" />
                <el-option label="最多浏览" value="viewCount" />
                <el-option label="最多点赞" value="likeCount" />
                <el-option label="最多评论" value="commentCount" />
              </el-select>
            </div>

            <!-- 时间范围 -->
            <div class="filter-item">
              <label class="filter-label">时间</label>
              <el-select
                v-model="searchForm.timeRange"
                placeholder="时间范围"
                @change="handleFilter"
                class="filter-select"
              >
                <el-option label="全部时间" value="" />
                <el-option label="最近一周" value="week" />
                <el-option label="最近一月" value="month" />
                <el-option label="最近一年" value="year" />
              </el-select>
            </div>

            <!-- 重置按钮 -->
            <div class="filter-actions">
              <el-button @click="handleReset" :icon="Refresh">重置</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 文章列表 -->
    <div class="articles-section">
      <div class="articles-container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <!-- 空状态 -->
        <div v-else-if="articles.length === 0" class="empty-state">
          <el-empty description="暂无文章">
            <el-button type="primary" @click="handleReset">查看全部文章</el-button>
          </el-empty>
        </div>

        <!-- 文章列表 -->
        <div v-else class="articles-list">
          <div
            v-for="article in articles"
            :key="article.id"
            class="article-card"
            @click="goToArticle(article.id)"
          >
            <!-- 文章封面 -->
            <div v-if="article.coverImage || article.cover" class="article-cover">
              <img
                :src="article.coverImage || article.cover"
                :alt="article.title"
                class="cover-image"
              />
            </div>

            <!-- 文章内容 -->
            <div class="article-content">
              <!-- 文章标题 -->
              <h2 class="article-title">{{ article.title }}</h2>

              <!-- 文章摘要 -->
              <p class="article-summary">
                {{ article.summary || generateSummary(article.content) }}
              </p>

              <!-- 文章元信息 -->
              <div class="article-meta">
                <div class="meta-item">
                  <el-icon><User /></el-icon>
                  <span>{{ article.authorName || article.author }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(article.createTime || article.createdAt) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><View /></el-icon>
                  <span>{{ formatNumber(article.viewCount || article.views || 0) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ formatNumber(article.commentCount || article.comments || 0) }}</span>
                </div>
              </div>

              <!-- 标签 -->
              <div v-if="article.tags && article.tags.length > 0" class="article-tags">
                <el-tag
                  v-for="tag in article.tags.slice(0, 3)"
                  :key="tag.id || tag"
                  size="small"
                  type="info"
                  class="tag-item"
                  @click.stop="searchByTag(getTagName(tag))"
                >
                  {{ getTagName(tag) }}
                </el-tag>
                <span v-if="article.tags.length > 3" class="more-tags">
                  +{{ article.tags.length - 3 }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, User, Calendar, View, ChatDotRound } from '@element-plus/icons-vue'
import { getArticleList, searchArticles } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'

import { formatDate, formatNumber, generateSummary } from '@/utils/helpers'
import { setSEO } from '@/utils/seo'

const router = useRouter()


// 响应式数据
const articles = ref([])
const categories = ref([])
const tags = ref([])
const loading = ref(false)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: '',
  tagId: '',
  sortBy: 'createTime',
  timeRange: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10
})

// 计算属性
const hasSearchParams = computed(() => {
  return searchForm.keyword || searchForm.categoryId || searchForm.tagId || searchForm.timeRange
})

// 加载文章列表
const loadArticles = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchForm.keyword,
      categoryId: searchForm.categoryId,
      tagId: searchForm.tagId,
      status: '0' // 只显示正常状态的文章
    }

    const response = await getArticleList(params)
    articles.value = response.data || []
    total.value = articles.value.length
  } catch (error) {
    console.error('Failed to load articles:', error)
    ElMessage.error('加载文章列表失败')
    
    // Mock数据作为fallback
    articles.value = [
      {
        id: 1,
        title: 'Vue3博客系统开发指南',
        summary: '本文介绍了如何使用Vue3、Element Plus和Tailwind CSS构建一个现代化的博客系统...',
        content: '# Vue3博客系统开发指南\n\n本文介绍了如何使用Vue3、Element Plus和Tailwind CSS构建一个现代化的博客系统...',
        author: 'Hazenix',
        createTime: '2025-01-01',
        viewCount: 100,
        tags: [{ id: 1, name: 'Vue3' }, { id: 2, name: '前端' }]
      },
      {
        id: 2,
        title: 'Element Plus组件库使用技巧',
        summary: '深入解析Element Plus组件库的高级用法和最佳实践...',
        content: '# Element Plus组件库使用技巧\n\n深入解析Element Plus组件库的高级用法和最佳实践...',
        author: 'Hazenix',
        createTime: '2025-01-02',
        viewCount: 80,
        tags: [{ id: 3, name: 'Element Plus' }, { id: 2, name: '前端' }]
      }
    ]
    total.value = 2
  } finally {
    loading.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await getCategoryList()
    categories.value = response.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
    // Mock数据
    categories.value = [
      { id: 1, name: '技术' },
      { id: 2, name: '生活' },
      { id: 3, name: '随笔' }
    ]
  }
}

// 加载标签列表
const loadTags = async () => {
  try {
    const response = await getTagList()
    tags.value = response.data || []
  } catch (error) {
    console.error('Failed to load tags:', error)
    // Mock数据
    tags.value = [
      { id: 1, name: 'Vue3' },
      { id: 2, name: '前端' },
      { id: 3, name: 'Element Plus' },
      { id: 4, name: 'JavaScript' }
    ]
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadArticles()
}

// 筛选处理
const handleFilter = () => {
  pagination.page = 1
  loadArticles()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    categoryId: '',
    tagId: '',
    sortBy: 'createTime',
    timeRange: ''
  })
  pagination.page = 1
  loadArticles()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadArticles()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadArticles()
}

// 跳转到文章详情
const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

// 获取标签名称（兼容新的API格式）
const getTagName = (tag) => {
  // 如果tag是对象，返回name字段
  if (typeof tag === 'object' && tag !== null) {
    return tag.name
  }
  // 如果tag是ID，从tags数组中查找对应的名称
  const tagObj = tags.value.find(t => t.id === tag)
  return tagObj ? tagObj.name : tag
}

// 按标签搜索
const searchByTag = (tagName) => {
  searchForm.tagId = tags.value.find(tag => tag.name === tagName)?.id || ''
  handleFilter()
}

// 设置SEO信息
const setPageSEO = () => {
  setSEO({
    title: '文章列表',
    description: '探索我们的精彩文章，发现新的知识和见解',
    keywords: ['文章', '博客', '技术', '前端', 'Vue3'],
    structuredData: {
      '@context': 'https://schema.org',
      '@type': 'CollectionPage',
      name: '文章列表',
      description: '探索我们的精彩文章，发现新的知识和见解',
      url: window.location.href
    }
  })
}

onMounted(() => {
  setPageSEO()
  loadArticles()
  loadCategories()
  loadTags()
})
</script>

<style scoped>
.article-list-page {
  @apply min-h-screen bg-gray-50 dark:bg-gray-900;
}

.page-header {
  @apply bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700;
}

.header-content {
  @apply max-w-7xl mx-auto px-4 py-8;
}

.page-title {
  @apply text-3xl md:text-4xl font-bold text-gray-900 dark:text-gray-100 mb-2;
}

.page-description {
  @apply text-gray-600 dark:text-gray-400 text-lg;
}

.search-section {
  @apply bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700;
}

.search-container {
  @apply max-w-7xl mx-auto px-4 py-6;
}

.search-box {
  @apply mb-6;
}

.filter-container {
  @apply space-y-4;
}

.filter-row {
  @apply flex flex-wrap gap-4 items-end;
}

.filter-item {
  @apply flex flex-col space-y-2;
}

.filter-label {
  @apply text-sm font-medium text-gray-700 dark:text-gray-300;
}

.filter-select {
  @apply w-40;
}

.filter-actions {
  @apply flex items-end;
}

.articles-section {
  @apply max-w-7xl mx-auto px-4 py-8;
}

.articles-container {
  @apply mb-8;
}

.loading-container {
  @apply space-y-4;
}

.empty-state {
  @apply text-center py-12;
}

.articles-list {
  @apply space-y-6;
}

.article-card {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden cursor-pointer transition-all duration-200 hover:shadow-md hover:border-gray-300 dark:hover:border-gray-600;
}

.article-cover {
  @apply h-48 overflow-hidden;
}

.cover-image {
  @apply w-full h-full object-cover transition-transform duration-200 hover:scale-105;
}

.article-content {
  @apply p-6;
}

.article-title {
  @apply text-xl font-semibold text-gray-900 dark:text-gray-100 mb-3 line-clamp-2 hover:text-blue-600 dark:hover:text-blue-400 transition-colors;
}

.article-summary {
  @apply text-gray-600 dark:text-gray-400 mb-4 line-clamp-3 leading-relaxed;
}

.article-meta {
  @apply flex flex-wrap gap-4 mb-4 text-sm text-gray-500 dark:text-gray-400;
}

.meta-item {
  @apply flex items-center space-x-1;
}

.article-tags {
  @apply flex flex-wrap gap-2;
}

.tag-item {
  @apply cursor-pointer transition-colors hover:bg-blue-100 dark:hover:bg-blue-900/30;
}

.more-tags {
  @apply text-sm text-gray-500 dark:text-gray-400;
}

.pagination-container {
  @apply flex justify-center;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .header-content {
    @apply py-6;
  }
  
  .page-title {
    @apply text-2xl;
  }
  
  .search-container {
    @apply py-4;
  }
  
  .filter-row {
    @apply flex-col items-stretch;
  }
  
  .filter-item {
    @apply w-full;
  }
  
  .filter-select {
    @apply w-full;
  }
  
  .article-content {
    @apply p-4;
  }
  
  .article-meta {
    @apply flex-col gap-2;
  }
}

/* 深色模式优化 */
.dark .article-card:hover {
  @apply shadow-lg;
}

.dark .article-title:hover {
  @apply text-blue-400;
}

/* 加载动画 */
.loading-container .el-skeleton {
  @apply bg-white dark:bg-gray-800 rounded-lg p-6;
}

/* 空状态样式 */
.empty-state .el-empty {
  @apply py-12;
}

/* 分页样式优化 */
.pagination-container .el-pagination {
  @apply justify-center;
}
</style>
