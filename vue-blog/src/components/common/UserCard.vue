<template>
  <div class="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-lg">
    <div class="text-center">
      <img :src="avatarUrl" 
           alt="User avatar" 
           class="w-20 h-20 rounded-full mx-auto mb-4" />
      <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-2">
        Hazenix 
      </h3>
      <p class="text-gray-600 dark:text-gray-400 mb-4">
        心有所向，何惧山海
      </p>
      
      <div class="flex justify-center space-x-4 mb-4">
        <div class="text-center">
          <div class="text-2xl font-bold text-primary">{{ stats.articleCount }}</div>
          <div class="text-sm text-gray-500">文章</div>
        </div>
        <!-- <div class="text-center">
          <div class="text-2xl font-bold text-primary">{{ stats.totalViewCount }}</div>
          <div class="text-sm text-gray-500">浏览</div>
        </div> -->
        <div class="text-center">
          <div class="text-2xl font-bold text-primary">{{ stats.totalLikeCount }}</div>
          <div class="text-sm text-gray-500">点赞</div>
        </div>
      </div>
      
      <div class="space-y-2">
        <a v-if="userInfo?.github" :href="userInfo.github" target="_blank" 
           class="block text-sm text-gray-600 dark:text-gray-400 hover:text-primary">
          <el-icon class="mr-1"><Link /></el-icon>
          GitHub
        </a>
        <a v-if="userInfo?.email" :href="`mailto:${userInfo.email}`" 
           class="block text-sm text-gray-600 dark:text-gray-400 hover:text-primary">
          <el-icon class="mr-1"><Message /></el-icon>
          联系我
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { Link, Message } from '@element-plus/icons-vue'
import { articleApi } from '@/api/article'

const userStore = useUserStore()

// 正确的默认头像路径
// 使用 import() 导入图片资源
import avatarImg from '@/assets/img/avatar.jpg';
const defaultAvatar = avatarImg;

// 计算头像URL，优先使用用户头像，否则使用默认头像
const avatarUrl = defaultAvatar

// 网站统计数据
const stats = ref({
  articleCount: 0,
  totalViewCount: 0,
  totalLikeCount: 0
})

// 如果没有登录用户信息，使用默认信息
const userInfo = computed(() => {
  return userStore.userInfo || {
    username: 'Hazenix',
    bio: '心有所向，何惧山海',
    github: 'https://github.com/HazeHacker',
    email: 'L3542495583@outlook.com'
  }
})

// 获取网站统计数据
const fetchSiteStats = async () => {
  try {
    // 获取所有文章列表
    const response = await articleApi.getArticleList()
    
    if (response && response.code === 200 && response.data) {
      // 处理不同的响应格式
      let articles = []
      if (Array.isArray(response.data)) {
        // 如果data直接是数组
        articles = response.data
      } else if (response.data.list && Array.isArray(response.data.list)) {
        // 如果data包含list字段
        articles = response.data.list
      } else if (Array.isArray(response.data.records)) {
        // 兼容分页格式
        articles = response.data.records
      } else if (Array.isArray(response.data)) {
        articles = response.data
      } else {
        articles = []
      }
      
      // 过滤掉 id=1 的文章（留言板专用）
      articles = articles.filter(article => article.id !== 1 && article.id !== '1')
      
      // 计算统计数据
      stats.value = {
        articleCount: articles.length,
        totalViewCount: articles.reduce((sum, article) => sum + (article.viewCount || 0), 0),
        totalLikeCount: articles.reduce((sum, article) => sum + (article.likeCount || 0), 0)
      }
    }
  } catch (error) {
    // console.error('获取网站统计数据失败:', error)
    // 出错时保持默认值 0
  }
}

// 组件挂载时获取统计数据
onMounted(() => {
  fetchSiteStats()
})
</script>