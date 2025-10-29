<template>
  <div class="py-16 md:py-24">
    <!-- 主题切换引导 -->
    <ThemeGuide v-model="showThemeGuide" />
    <div class="flex flex-col md:flex-row justify-between items-start gap-12">
      <div class="md:w-2/3">
        <h1 class="text-5xl md:text-6xl font-bold text-gray-900 dark:text-white">
          Hazenix's Blog
        </h1>
        <p class="mt-4 text-2xl md:text-3xl text-primary font-semibold">
          Nothing but enthusiasm brightens up the endless years.
        </p>
        <p class="mt-6 text-lg text-gray-500 dark:text-gray-400">
         道阻且长，行则将至
        </p>
      </div>
      
      <div class="w-full md:w-1/3 mt-12 md:mt-0">
        <UserCard />
      </div>
    </div>
    
    <!-- 内容区域 -->
    <div class="mt-24 grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- 最新文章列表 -->
      <div class="lg:col-span-2">
        <h2 class="text-3xl font-bold mb-8">最新文章</h2>
        <ArticleList :articles="latestArticles" />
      </div>
      
      <!-- 侧边栏 -->
      <div class="lg:col-span-1">
        <div class="space-y-8">
          <!-- 热门文章 -->
          <PopularArticles />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { ElMessage } from 'element-plus'
import { getArticleList } from '@/api/article'
import ArticleList from '@/components/article/ArticleList.vue'
import UserCard from '@/components/common/UserCard.vue'
import PopularArticles from '@/components/article/PopularArticles.vue'
import ThemeGuide from '@/components/common/ThemeGuide.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
const latestArticles = ref([])
const showThemeGuide = ref(false)

// 处理Google登录回调
const handleGoogleCallback = async () => {
  const code = route.query.code
  if (code) {
    try {
      await userStore.googleLogin(code)
      ElMessage.success('Google登录成功')
      // 清除URL中的code参数
      router.replace({ path: '/home', query: {} })
    } catch (error) {
      ElMessage.error('Google登录失败: ' + (error.message || '未知错误'))
      console.error('Google login callback error:', error)
    }
  }
}

onMounted(async () => {
  // 先保存 fromIndex 参数，因为 Google 回调可能会清除 query 参数
  const fromIndex = route.query.fromIndex === 'true'
  
  // 首先处理Google回调（可能会清除 query 参数）
  await handleGoogleCallback()
  
  // 检查是否从主页面跳转过来
  if (fromIndex) {
    // 如果是从主页面跳转过来的，设置为黑色模式
    if (!themeStore.isDark) {
      themeStore.toggleTheme()
    }
    // 清除查询参数，避免刷新页面时仍然保持黑色模式
    router.replace({ path: '/home', query: {} })
    
    // 每次从主页面进入时都显示引导（延迟显示，给用户一些时间看到页面）
    // 确保只在黑色模式下显示引导
    setTimeout(() => {
      if (themeStore.isDark) {
        showThemeGuide.value = true
      }
    }, 1000)
  } else {
    // 如果不是从主页面跳转过来的，确保是白色模式（如果之前是黑色模式，则切换）
    if (themeStore.isDark) {
      themeStore.toggleTheme()
    }
  }
  try {
    const res = await getArticleList({ 
      status: '0', // 只显示正常状态的文章
      page: 1,
      pageSize: 6 // 首页只显示6篇文章
    })
    console.log('Home page articles response:', res)
    
    // 处理不同的响应格式
    if (res && res.data) {
      if (Array.isArray(res.data)) {
        latestArticles.value = res.data
      } else if (res.data.records) {
        latestArticles.value = res.data.records
      } else if (res.data.list) {
        latestArticles.value = res.data.list
      } else {
        latestArticles.value = res.data
      }
    } else {
      latestArticles.value = []
    }
  } catch (error) {
    console.error('Failed to load articles:', error)
    // 使用mock数据作为fallback
    latestArticles.value = [
      {
        id: 1,
        title: 'Vue3博客系统开发指南',
        summary: '本文介绍了如何使用Vue3、Element Plus和Tailwind CSS构建一个现代化的博客系统...',
        author: 'Hazenix',
        createTime: '2025-01-01',
        viewCount: 100,
        tags: [{ id: 1, name: 'Vue3' }, { id: 2, name: '前端' }]
      },
      {
        id: 2,
        title: '现代前端开发最佳实践',
        summary: '探索现代前端开发中的最佳实践，包括组件设计、状态管理、性能优化等方面...',
        author: 'Hazenix',
        createTime: '2025-01-02',
        viewCount: 85,
        tags: [{ id: 3, name: 'JavaScript' }, { id: 4, name: '最佳实践' }]
      }
    ]
  }
})
</script>
