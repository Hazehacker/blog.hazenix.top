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
          Only those who keep walking light up the long road of time
        </p>
        <p class="mt-6 text-lg text-gray-500 dark:text-gray-400">
         我会一直走，走到灯火通明
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

// 处理OAuth回调 - 统一处理，通过 source 参数或 sessionStorage 区分来源
const handleOAuthCallback = async () => {
  const code = route.query.code
  // 优先使用 URL 参数中的 source，如果没有则从 sessionStorage 读取
  // Google 登录的 redirect_uri 不能包含查询参数，所以需要从 sessionStorage 读取
  let source = route.query.source || sessionStorage.getItem('oauth_source') || null
  
  // 如果没有 code 参数，不处理
  if (!code) {
    return
  }
  
  // 标记是否正在处理，避免重复处理
  if (window._oauthCallbackProcessing) {
    // console.log('OAuth 回调正在处理中，跳过')
    return
  }
  
  window._oauthCallbackProcessing = true
  
  try {
    // 根据 source 参数决定调用哪个登录方法
    if (source === 'github') {
      // console.log('处理 GitHub OAuth 回调')
      await userStore.githubLogin(code)
      ElMessage.success('GitHub登录成功')
      // 清除 sessionStorage
      sessionStorage.removeItem('oauth_source')
    } else if (source === 'google') {
      // console.log('处理 Google OAuth 回调')
      await userStore.googleLogin(code)
      ElMessage.success('Google登录成功')
      // 清除 sessionStorage
      sessionStorage.removeItem('oauth_source')
    } else {
      // 如果没有 source 参数，尝试通过 code 的特征判断
      // 但为了安全，建议明确指定 source
      // console.warn('未指定 OAuth 来源，默认尝试 GitHub')
      await userStore.githubLogin(code)
      ElMessage.success('登录成功')
      // 清除 sessionStorage
      sessionStorage.removeItem('oauth_source')
    }
    
    // 登录成功后，获取完整的用户信息（包括头像）
    try {
      await userStore.getUserInfo()
    } catch (error) {
      // console.warn('获取用户信息失败，但登录已成功:', error)
    }
    
    // 清除URL中的回调参数
    const cleanQuery = { ...route.query }
    delete cleanQuery.code
    delete cleanQuery.source
    router.replace({ path: '/home', query: cleanQuery })
  } catch (error) {
    ElMessage.error('第三方登录失败: ' + (error.message || '未知错误'))
    // console.error('OAuth callback error:', error)
    // 清除 sessionStorage
    sessionStorage.removeItem('oauth_source')
    // 即使登录失败，也清除URL中的回调参数
    const cleanQuery = { ...route.query }
    delete cleanQuery.code
    delete cleanQuery.source
    router.replace({ path: '/home', query: cleanQuery })
  } finally {
    // 清除处理标记
    window._oauthCallbackProcessing = false
  }
}

onMounted(async () => {
  // 先处理 OAuth 回调（如果有）
  await handleOAuthCallback()
  
  // 先保存 fromIndex 参数，因为回调可能会清除 query 参数
  const fromIndex = route.query.fromIndex === 'true'
  
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
  }
  // 移除else分支 - 不再强制切换到亮色模式，保持用户当前的主题设置
  try {
    const res = await getArticleList({ 
      status: '0', // 只显示正常状态的文章
      page: 1,
      pageSize: 6 // 首页只显示6篇文章
    })
    // console.log('Home page articles response:', res)
    
    // 处理不同的响应格式
    let articleList = []
    if (res && res.data) {
      if (Array.isArray(res.data)) {
        articleList = res.data
      } else if (res.data.records) {
        articleList = res.data.records
      } else if (res.data.list) {
        articleList = res.data.list
      } else {
        articleList = Array.isArray(res.data) ? res.data : [res.data]
      }
    } else {
      articleList = []
    }
    
    // 过滤掉 id=1 的文章（留言板专用）
    articleList = articleList.filter(article => article.id !== 1 && article.id !== '1')
    
    // 排序：置顶文章优先，然后按创建时间降序排序
    articleList.sort((a, b) => {
      // 获取置顶状态（兼容 0/1 和 true/false）
      const isTopA = a.isTop === 1 || a.isTop === true
      const isTopB = b.isTop === 1 || b.isTop === true
      
      // 如果一个是置顶，一个不是，置顶的排在前面
      if (isTopA && !isTopB) {
        return -1
      }
      if (!isTopA && isTopB) {
        return 1
      }
      
      // 如果都是置顶或都不是置顶，按创建时间降序排序（最新的在前）
      const timeA = new Date(a.createTime || a.createdAt || 0).getTime()
      const timeB = new Date(b.createTime || b.createdAt || 0).getTime()
      return timeB - timeA
    })
    
    // 只取前6篇
    latestArticles.value = articleList.slice(0, 6)
  } catch (error) {
    // console.error('Failed to load articles:', error)
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
