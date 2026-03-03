<template>
  <div class="tag-detail">
    <div class="container mx-auto px-4 py-8">
      <!-- 标签信息头部 -->
      <div class="mb-8">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
          <div class="flex items-center mb-4">
            <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 mr-4">
              <i class="fas fa-tag mr-1"></i>
              标签
            </span>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white">
              {{ tag?.name || '加载中...' }}
            </h1>
          </div>
          <p v-if="tag?.description" class="text-gray-600 dark:text-gray-300 text-lg">
            {{ tag.description }}
          </p>
          <div class="flex items-center mt-4 text-sm text-gray-500 dark:text-gray-400">
            <span class="mr-4">
              <i class="fas fa-file-alt mr-1"></i>
              {{ totalArticles }} 篇文章
            </span>
            <span class="mr-4">
              <i class="fas fa-calendar mr-1"></i>
              创建于 {{ formatDate(tag?.createdAt) }}
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
            该标签下还没有任何文章
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTagArticles } from '@/api/tag'
import ArticleCard from '@/components/article/ArticleCard.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const route = useRoute()

// 响应式数据
const loading = ref(false)
const tag = ref(null)
const articles = ref([])
const totalArticles = ref(0)

// 方法
const fetchTagArticles = async () => {
  if (!route.params.id) {
    // console.warn('标签ID不存在，无法获取文章')
    return
  }
  
  loading.value = true
  try {
    // console.log('开始获取标签文章，标签ID:', route.params.id)
    
    // 根据图片中的接口，后端返回的是列表，不需要分页参数
    const response = await getTagArticles(route.params.id)
    // console.log('标签文章接口响应:', response)
    
    // 处理响应数据：后端返回格式为 { code, msg, data: [...] }
    let articleList = []
    if (response && response.data) {
      if (Array.isArray(response.data)) {
        articleList = response.data
      } else if (response.data.list && Array.isArray(response.data.list)) {
        articleList = response.data.list
      } else if (Array.isArray(response.data.records)) {
        articleList = response.data.records
      }
    } else if (Array.isArray(response)) {
      // 如果响应直接是数组
      articleList = response
    }
    
    // 直接保存所有文章
    articles.value = articleList
    totalArticles.value = articleList.length
    
    // 设置标签信息（从第一篇文章获取或使用默认值）
    if (articles.value.length > 0 && articles.value[0].tags) {
      const tagInfo = articles.value[0].tags.find(t => t.id == route.params.id || t.id == String(route.params.id))
      if (tagInfo) {
        tag.value = {
          name: tagInfo.name,
          id: route.params.id
        }
      } else {
        // 如果没找到，尝试从标签列表中获取
        tag.value = {
          name: '标签',
          id: route.params.id
        }
      }
    } else {
      tag.value = {
        name: '标签',
        id: route.params.id
      }
    }
    
    // console.log('成功获取标签文章，数量:', articles.value.length)
  } catch (error) {
    // console.error('获取标签文章失败:', error)
    ElMessage.error('获取标签文章失败，请稍后重试')
    articles.value = []
    tag.value = {
      name: '标签',
      id: route.params.id
    }
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 监听路由变化
watch(() => route.params.id, () => {
  fetchTagArticles()
}, { immediate: true })

// 生命周期
onMounted(() => {
  fetchTagArticles()
})
</script>

<style scoped>
.tag-detail {
  min-height: 100vh;
  background-color: #f8fafc;
}

.dark .tag-detail {
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
