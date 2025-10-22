<template>
  <div class="category-detail">
    <div class="container mx-auto px-4 py-8">
      <!-- 分类信息头部 -->
      <div class="mb-8">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-4">
            {{ category?.name || '加载中...' }}
          </h1>
          <p v-if="category?.description" class="text-gray-600 dark:text-gray-300 text-lg">
            {{ category.description }}
          </p>
          <div class="flex items-center mt-4 text-sm text-gray-500 dark:text-gray-400">
            <span class="mr-4">
              <i class="fas fa-file-alt mr-1"></i>
              {{ totalArticles }} 篇文章
            </span>
            <span class="mr-4">
              <i class="fas fa-calendar mr-1"></i>
              创建于 {{ formatDate(category?.createdAt) }}
            </span>
          </div>
        </div>
      </div>

      <!-- 文章列表 -->
      <div class="mb-8">
        <h2 class="text-2xl font-semibold text-gray-900 dark:text-white mb-6">
          相关文章
        </h2>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="flex justify-center py-8">
          <LoadingSpinner />
        </div>

        <!-- 文章列表 -->
        <div v-else-if="articles.length > 0" class="space-y-6">
          <ArticleCard
            v-for="article in articles"
            :key="article.id"
            :article="article"
            class="hover:shadow-lg transition-shadow duration-300"
          />
        </div>

        <!-- 空状态 -->
        <div v-else class="text-center py-12">
          <div class="text-gray-400 dark:text-gray-500 mb-4">
            <i class="fas fa-inbox text-6xl"></i>
          </div>
          <h3 class="text-xl font-medium text-gray-900 dark:text-white mb-2">
            暂无文章
          </h3>
          <p class="text-gray-600 dark:text-gray-400">
            该分类下还没有任何文章
          </p>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="mt-8 flex justify-center">
          <nav class="flex items-center space-x-2">
            <button
              v-for="page in visiblePages"
              :key="page"
              @click="goToPage(page)"
              :class="[
                'px-4 py-2 rounded-md text-sm font-medium transition-colors',
                page === currentPage
                  ? 'bg-blue-600 text-white'
                  : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
              ]"
            >
              {{ page }}
            </button>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import ArticleCard from '@/components/article/ArticleCard.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const route = useRoute()
const articleStore = useArticleStore()

// 响应式数据
const loading = ref(false)
const category = ref(null)
const articles = ref([])
const currentPage = ref(1)
const totalPages = ref(1)
const totalArticles = ref(0)
const pageSize = 10

// 计算属性
const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// 方法
const fetchCategoryArticles = async () => {
  if (!route.params.id) return
  
  loading.value = true
  try {
    const response = await articleStore.getArticlesByCategory({
      categoryId: route.params.id,
      page: currentPage.value,
      pageSize: pageSize
    })
    
    articles.value = response.data || []
    totalPages.value = response.totalPages || 1
    totalArticles.value = response.total || 0
    
    // 获取分类信息
    if (response.category) {
      category.value = response.category
    }
  } catch (error) {
    console.error('获取分类文章失败:', error)
    articles.value = []
  } finally {
    loading.value = false
  }
}

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value && page !== currentPage.value) {
    currentPage.value = page
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 监听路由变化
watch(() => route.params.id, () => {
  currentPage.value = 1
  fetchCategoryArticles()
}, { immediate: true })

// 监听页码变化
watch(currentPage, () => {
  fetchCategoryArticles()
})

// 生命周期
onMounted(() => {
  fetchCategoryArticles()
})
</script>

<style scoped>
.category-detail {
  min-height: 100vh;
  background-color: #f8fafc;
}

.dark .category-detail {
  background-color: #1a202c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding-left: 1rem;
    padding-right: 1rem;
  }
}
</style>
