<template>
  <div class="space-y-6">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-blue-100 dark:bg-blue-900">
            <el-icon class="text-blue-600 dark:text-blue-400" size="24">
              <Document />
            </el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600 dark:text-gray-400">总文章数</p>
            <p class="text-2xl font-semibold text-gray-900 dark:text-gray-100">{{ stats.totalArticles }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-green-100 dark:bg-green-900">
            <el-icon class="text-green-600 dark:text-green-400" size="24">
              <Collection />
            </el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600 dark:text-gray-400">分类数量</p>
            <p class="text-2xl font-semibold text-gray-900 dark:text-gray-100">{{ stats.totalCategories }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-yellow-100 dark:bg-yellow-900">
            <el-icon class="text-yellow-600 dark:text-yellow-400" size="24">
              <PriceTag />
            </el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600 dark:text-gray-400">标签数量</p>
            <p class="text-2xl font-semibold text-gray-900 dark:text-gray-100">{{ stats.totalTags }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-purple-100 dark:bg-purple-900">
            <el-icon class="text-purple-600 dark:text-purple-400" size="24">
              <ChatDotRound />
            </el-icon>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600 dark:text-gray-400">评论数量</p>
            <p class="text-2xl font-semibold text-gray-900 dark:text-gray-100">{{ stats.totalComments }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 最新文章 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100">最新文章</h3>
        </div>
        <div class="p-6">
          <div v-if="recentArticles.length === 0" class="text-center text-gray-500 dark:text-gray-400 py-8">
            暂无文章
          </div>
          <div v-else class="space-y-4">
            <div v-for="article in recentArticles" :key="article.id" 
                 class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <div class="flex-1">
                <h4 class="font-medium text-gray-900 dark:text-gray-100">{{ article.title }}</h4>
                <p class="text-sm text-gray-500 dark:text-gray-400">{{ formatDate(article.createTime) }}</p>
              </div>
              <el-tag :type="article.status === 'published' ? 'success' : 'warning'" size="small">
                {{ article.status === 0 ? '已发布' : '草稿' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 最新评论 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100">最新评论</h3>
        </div>
        <div class="p-6">
          <div v-if="recentComments.length === 0" class="text-center text-gray-500 dark:text-gray-400 py-8">
            暂无评论
          </div>
          <div v-else class="space-y-4">
            <div v-for="comment in recentComments" :key="comment.id" 
                 class="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <div class="flex items-center justify-between mb-2">
                <span class="font-medium text-gray-900 dark:text-gray-100">{{ comment.username }}</span>
                <span class="text-sm text-gray-500 dark:text-gray-400">{{ formatDate(comment.createTime) }}</span>
              </div>
              <p class="text-sm text-gray-600 dark:text-gray-300 line-clamp-2">{{ comment.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
      <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
        <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100">快速操作</h3>
      </div>
      <div class="p-6">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <el-button @click="$router.push('/admin/articles')" type="primary" class="h-16">
            <div class="text-center">
              <el-icon size="24" class="mb-2"><Document /></el-icon>
              <div class="text-sm">写文章</div>
            </div>
          </el-button>
          <el-button @click="$router.push('/admin/categories')" class="h-16">
            <div class="text-center">
              <el-icon size="24" class="mb-2"><Collection /></el-icon>
              <div class="text-sm">管理分类</div>
            </div>
          </el-button>
          <el-button @click="$router.push('/admin/tags')" class="h-16">
            <div class="text-center">
              <el-icon size="24" class="mb-2"><PriceTag /></el-icon>
              <div class="text-sm">管理标签</div>
            </div>
          </el-button>
          <el-button @click="$router.push('/admin/comments')" class="h-16">
            <div class="text-center">
              <el-icon size="24" class="mb-2"><ChatDotRound /></el-icon>
              <div class="text-sm">管理评论</div>
            </div>
          </el-button>
          <el-button @click="$router.push('/admin/links')" class="h-16">
            <div class="text-center">
              <el-icon size="24" class="mb-2"><Link /></el-icon>
              <div class="text-sm">友链管理</div>
            </div>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, Collection, PriceTag, ChatDotRound, Link } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

// 统计数据
const stats = ref({
  totalArticles: 0,
  totalCategories: 0,
  totalTags: 0,
  totalComments: 0
})

// 最近文章
const recentArticles = ref([])

// 最近评论
const recentComments = ref([])

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载数据
const loadDashboardData = async () => {
  try {
    // 加载统计数据
    const statsRes = await adminApi.getStats()
    stats.value = statsRes.data

    // 加载最新文章
    const articlesRes = await adminApi.getRecentArticles({ limit: 5 })
    recentArticles.value = articlesRes.data

    // 加载最新评论
    const commentsRes = await adminApi.getRecentComments({ limit: 5 })
    recentComments.value = commentsRes.data
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
