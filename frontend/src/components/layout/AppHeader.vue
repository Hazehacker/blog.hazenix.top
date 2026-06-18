<template>
  <header class="fixed top-0 left-0 right-0 z-50 flex justify-between items-center bg-white dark:bg-gray-900/90 backdrop-blur-sm shadow-md" style="height: 56px;">
    <div class="flex items-center space-x-2" style="margin-left: 16px;">
      <!-- Desktop avatar -->
      <img :src="avatarUrl" alt="User avatar"
           class="w-10 h-10 rounded-full hidden md:block"
           @error="onAvatarError" />

      <!-- Mobile: avatar as hamburger menu trigger -->
      <div class="flex items-center space-x-2 md:hidden cursor-pointer" @click="openMobileMenu">
        <img :src="avatarUrl" alt="Menu"
             class="w-8 h-8 rounded-full"
             @error="onAvatarError" />
        <span class="text-sm font-semibold text-gray-800 dark:text-gray-200">Hazenix 的后端札记</span>
      </div>
    </div>
    
    <nav class="hidden md:flex items-center space-x-8 text-gray-600 dark:text-gray-400">
      <router-link to="/home" class="hover:text-primary">首页</router-link>
      
      <!-- 文章下拉菜单 -->
      <el-dropdown @command="handleCategoryCommand" trigger="hover" class="article-dropdown">
        <div class="article-menu-wrapper">
          <router-link 
            to="/articles" 
            class="hover:text-primary article-link"
          >
            文章
          </router-link>
        </div>
        <template #dropdown>
          <el-dropdown-menu v-if="categories.length > 0">
            <el-dropdown-item 
              v-for="category in categories" 
              :key="category.id"
              :command="category.id"
              class="category-item"
            >
              <span class="category-name">{{ category.name }}</span>
              <span class="category-count">({{ category.articleCount }})</span>
            </el-dropdown-item>
          </el-dropdown-menu>
          <el-dropdown-menu v-else>
            <el-dropdown-item disabled>暂无分类</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      
      <router-link to="/moments" class="hover:text-primary">手记</router-link>
      <router-link to="/tags" class="hover:text-primary">标签</router-link>
      <router-link to="/tree-hole" class="hover:text-primary">树洞</router-link>
      <router-link to="/friend-links" class="hover:text-primary">友链</router-link>

      <!-- 更多下拉菜单 -->
      <el-dropdown @command="handleMoreCommand" trigger="hover" class="more-dropdown">
        <span class="hover:text-primary cursor-pointer flex items-center more-trigger ">
          更多
          <el-icon class="ml-1"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="monitor">
                <el-icon class="mr-2"><DataAnalysis /></el-icon>
                监控
            </el-dropdown-item>
<el-dropdown-item command="portfolio">
              <el-icon class="mr-2"><Collection /></el-icon>
              作品集
            </el-dropdown-item>
            <el-dropdown-item command="about">
              <el-icon class="mr-2"><InfoFilled /></el-icon>
              关于我
            </el-dropdown-item>
            
            <!-- 统计页面暂时不添加 -->
            <!-- <el-dropdown-item command="stats">
              <el-icon class="mr-2"><DataAnalysis /></el-icon>
              统计
            </el-dropdown-item> -->
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </nav>
    
    <div class="flex items-center space-x-3 md:space-x-4" style="margin-right: 20px;">
      <el-button link @click="openSearch" class="hidden md:inline-flex">
        <el-icon><Search /></el-icon>
      </el-button>
      <el-button link @click="openGithub">
          <img :src="github" alt="GitHub" class="w-5 h-5 rounded-full" />
        </el-button>
      <el-button link @click="openCSDN" class="hidden md:inline-flex">
          <img :src="csdn" alt="CSDN" class="w-5 h-5 rounded-full" />
        </el-button>
      <el-dropdown v-if="userStore.token && userStore.userInfo" @command="handleCommand" class="hidden md:block">
        <el-button link>
          <img :src="avatarUrl" 
               alt="User avatar" 
               class="w-8 h-8 rounded-full cursor-pointer"
               @error="onAvatarError" />
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <el-button v-else link @click="openLogin">
        <el-icon><User/></el-icon>
      </el-button>
      
      <ThemeToggle />
    </div>
    
  </header>

  <teleport to="body">
    <transition name="mobile-menu-fade">
      <div v-if="mobileMenuVisible" class="mobile-menu-overlay" @click.self="closeMobileMenu">
        <div class="mobile-menu-panel">
          <div class="flex justify-between items-center p-4">
            <span class="text-lg font-semibold text-gray-800 dark:text-gray-200">导航</span>
            <el-button link @click="closeMobileMenu" class="text-2xl">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>

          <div class="mobile-menu-items">
            <router-link to="/home" class="mobile-menu-item" @click="closeMobileMenu">🏠 首页</router-link>

            <div class="mobile-menu-item" @click="toggleMobileCategories">
              📝 文章
              <el-icon class="ml-auto transition-transform" :class="{ 'rotate-180': mobileCategoriesExpanded }"><ArrowDown /></el-icon>
            </div>
            <div v-show="mobileCategoriesExpanded" class="mobile-sub-items">
              <router-link v-for="cat in categories" :key="cat.id"
                :to="`/articles?categoryId=${cat.id}`"
                class="mobile-sub-item" @click="closeMobileMenu">
                {{ cat.name }} <span class="text-gray-400">({{ cat.articleCount }})</span>
              </router-link>
            </div>

            <router-link to="/moments" class="mobile-menu-item" @click="closeMobileMenu">📝 手记</router-link>
            <router-link to="/tags" class="mobile-menu-item" @click="closeMobileMenu">🏷️ 标签</router-link>
            <router-link to="/tree-hole" class="mobile-menu-item" @click="closeMobileMenu">🌳 树洞</router-link>
            <router-link to="/friend-links" class="mobile-menu-item" @click="closeMobileMenu">🔗 友链</router-link>
            <router-link to="/about" class="mobile-menu-item" @click="closeMobileMenu">👤 关于我</router-link>
            <a href="https://monitor.hazenix.top/share/UOi3M8l7j4ZSjDn3" target="_blank" rel="noopener noreferrer" class="mobile-menu-item" @click="closeMobileMenu">📊 监控</a>
            <a href="https://www.hazenix.top" target="_blank" rel="noopener noreferrer" class="mobile-menu-item" @click="closeMobileMenu">💼 作品集</a>
          </div>

          <div class="mobile-menu-footer">
            <div v-if="userStore.token && userStore.userInfo" class="mobile-menu-item" @click="goToProfile">
              <img :src="avatarUrl" class="w-6 h-6 rounded-full" @error="onAvatarError" />
              <span>个人中心</span>
            </div>
            <div v-else class="mobile-menu-item" @click="openLoginFromMobile">👤 登录</div>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'
import ThemeToggle from '@/components/common/ThemeToggle.vue'
import { Search, User, ArrowDown, InfoFilled, DataAnalysis, Collection, Close } from '@element-plus/icons-vue'
import { getCategoryList } from '@/api/category'
import { getArticleList } from '@/api/article'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const avatarFallback = '/avatar.webp'
import githubImg from '@/assets/img/githubLogo.webp'
import csdnImg from '@/assets/img/csdnLogo.webp'
const defaultAvatar = avatarFallback
const github = githubImg
const csdn = csdnImg

// 分类列表
const categories = ref([])
const loadingCategories = ref(false)

// 计算头像URL，确保响应式更新
const avatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar
  // 调试信息：检查用户信息和头像
  if (userStore.userInfo) {
    // console.log('用户信息:', userStore.userInfo)
    // console.log('头像URL:', avatar)
  }
  return avatar || defaultAvatar
})

const onAvatarError = (e) => {
  // console.warn('头像加载失败，使用默认头像')
  e.target.src = avatarFallback
}

// 监听用户信息变化，确保头像能够及时更新
watch(
  () => userStore.userInfo,
  (newInfo) => {
    if (newInfo) {
      // console.log('用户信息已更新:', newInfo)
      // console.log('头像字段:', newInfo.avatar)
    }
  },
  { deep: true, immediate: true }
)

const openSearch = () => {
  // 触发搜索对话框打开事件
  window.dispatchEvent(new CustomEvent('open-search-dialog'))
}

const openLogin = () => {
  // 触发登录对话框打开事件
  window.dispatchEvent(new CustomEvent('open-login-dialog'))
}

//点击按钮之后，打开github
const openGithub = () => {
  window.open('https://github.com/HazeHacker', '_blank')
}
//点击按钮之后，打开csdn
const openCSDN = () => {
  window.open('https://blog.csdn.net/Hazehacker', '_blank')
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    await userStore.logout()
    // 退出登录后跳转到主页（index.vue），而不是 /home
    router.replace('/home')
  } else if (command === 'favorites') {
    router.push('/favorites')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

const handleMoreCommand = (command) => {
  if (command === 'about') {
    router.push('/about')
  } else if (command === 'monitor') {
    window.open('https://monitor.hazenix.top/share/UOi3M8l7j4ZSjDn3', '_blank')
  } else if (command === 'portfolio') {
    window.open('https://www.hazenix.top', '_blank')
  }
}

// 处理分类点击
const handleCategoryCommand = (categoryId) => {
  router.push(`/articles?categoryId=${categoryId}`)
}

// 加载分类列表
const loadCategories = async () => {
  loadingCategories.value = true
  try {
    // 获取分类列表
    const res = await getCategoryList()
    const allCategories = res.data || []
    
    // 获取所有文章列表
    let publishedArticles = []
    try {
      const articlesRes = await getArticleList()
      // 处理不同的响应格式
      if (articlesRes && articlesRes.data) {
        if (Array.isArray(articlesRes.data)) {
          publishedArticles = articlesRes.data
        } else if (articlesRes.data.list && Array.isArray(articlesRes.data.list)) {
          publishedArticles = articlesRes.data.list
        } else if (Array.isArray(articlesRes.data.records)) {
          publishedArticles = articlesRes.data.records
        }
      }
    } catch (articleError) {
      // console.warn('Failed to load articles for counting:', articleError)
    }
    
    // 过滤文章：只保留已发布的文章（status=0），排除留言板文章（id=1）
    const filteredArticles = publishedArticles.filter(article => {
      // 排除留言板文章
      if (article.id === 1 || article.id === '1') {
        return false
      }
      // 只显示已发布的文章（status=0）
      if (article.status !== 0 && article.status !== '0') {
        return false
      }
      return true
    })
    
    // 按分类统计已发布文章的数量
    const categoryCountMap = new Map()
    filteredArticles.forEach(article => {
      const categoryId = article.categoryId || article.category?.id
      if (categoryId) {
        categoryCountMap.set(categoryId, (categoryCountMap.get(categoryId) || 0) + 1)
      }
    })
    
    // 更新分类的文章数量，只显示有已发布文章的分类
    const categoriesWithCount = allCategories.map(category => ({
      ...category,
      articleCount: categoryCountMap.get(category.id) || 0
    })).filter(category => category.articleCount > 0)
    
    categories.value = categoriesWithCount
  } catch (error) {
    // console.error('Failed to load categories:', error)
    categories.value = []
  } finally {
    loadingCategories.value = false
  }
}

// 当 URL 上带有 ?login=1 时，自动打开登录弹窗，并清理一次性参数
const maybeOpenLoginFromQuery = () => {
  if (route.query && route.query.login === '1') {
    openLogin()
    const { login, ...rest } = route.query
    router.replace({ path: route.path, query: { ...rest } })
  }
}

onMounted(() => {
  maybeOpenLoginFromQuery()
  loadCategories()
})

watch(
  () => route.query.login,
  () => {
    maybeOpenLoginFromQuery()
  }
)

// Mobile menu state
const mobileMenuVisible = ref(false)
const mobileCategoriesExpanded = ref(false)

const openMobileMenu = () => {
  mobileMenuVisible.value = true
  document.body.style.overflow = 'hidden'
}

const closeMobileMenu = () => {
  mobileMenuVisible.value = false
  mobileCategoriesExpanded.value = false
  document.body.style.overflow = ''
}

const toggleMobileCategories = () => {
  mobileCategoriesExpanded.value = !mobileCategoriesExpanded.value
}

const openLoginFromMobile = () => {
  closeMobileMenu()
  window.dispatchEvent(new CustomEvent('open-login-dialog'))
}

const goToProfile = () => {
  closeMobileMenu()
  router.push('/profile')
}
</script>

<style scoped>
/* 导航链接样式 */
nav a, nav .more-trigger, nav .article-link {
  font-size: 17px;
  font-weight: 500;
}

/* 移除更多下拉菜单的默认边框样式 */
.more-dropdown :deep(.el-dropdown) {
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
}

.more-dropdown :deep(.el-dropdown__caret-button) {
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
}

/* 下拉菜单容器样式优化 */
.more-dropdown :deep(.el-dropdown-menu),
.article-dropdown :deep(.el-dropdown-menu) {
  padding: 10px 0;
  min-width: 200px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  border: 1px solid #e5e7eb;
}

.dark .more-dropdown :deep(.el-dropdown-menu),
.dark .article-dropdown :deep(.el-dropdown-menu) {
  background-color: #1f2937;
  border-color: #374151;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
}

/* 下拉菜单项字体大小和间距优化 */
.more-dropdown :deep(.el-dropdown-menu__item),
.article-dropdown :deep(.el-dropdown-menu__item) {
  font-size: 17px !important;
  font-weight: 500 !important;
  padding: 14px 24px !important;
  min-height: 52px !important;
  line-height: 1.6 !important;
  display: flex !important;
  align-items: center !important;
  transition: all 0.2s ease;
  margin: 3px 10px;
  border-radius: 8px;
}

/* 下拉菜单项悬停效果 */
.more-dropdown :deep(.el-dropdown-menu__item:hover),
.article-dropdown :deep(.el-dropdown-menu__item:hover) {
  background-color: #f3f4f6;
  color: var(--el-color-primary);
  transform: translateX(2px);
}

.dark .more-dropdown :deep(.el-dropdown-menu__item:hover),
.dark .article-dropdown :deep(.el-dropdown-menu__item:hover) {
  background-color: #374151;
  color: var(--el-color-primary);
}

/* 下拉菜单图标大小和间距 */
.more-dropdown :deep(.el-dropdown-menu__item .el-icon),
.article-dropdown :deep(.el-dropdown-menu__item .el-icon) {
  font-size: 20px !important;
  margin-right: 12px !important;
  transition: transform 0.2s ease;
}

.more-dropdown :deep(.el-dropdown-menu__item:hover .el-icon),
.article-dropdown :deep(.el-dropdown-menu__item:hover .el-icon) {
  transform: scale(1.1);
}

/* 下拉菜单项文字 */
.more-dropdown :deep(.el-dropdown-menu__item span),
.article-dropdown :deep(.el-dropdown-menu__item span) {
  font-weight: 500 !important;
  font-size: 17px !important;
}

.more-trigger {
  font-size: 17px; 
  font-weight: 500;
  color: #4B5563;
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
  background: transparent !important;
  padding: 0 !important;
}

/* 确保更多选项的样式与其他导航项一致 */
.more-dropdown {
  display: inline-block;
}

.more-dropdown:hover .more-trigger {
  color: var(--el-color-primary);
}

/* 文章下拉菜单样式 */
.article-dropdown {
  display: inline-block;
}

.article-menu-wrapper {
  display: inline-block;
}

.article-link {
  font-size: 17px;
  font-weight: 500;
  color: #4B5563;
  text-decoration: none;
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
  background: transparent !important;
  padding: 0 !important;
  cursor: pointer;
  display: inline-block;
}

.article-dropdown:hover .article-link,
.article-link:hover {
  color: var(--el-color-primary);
}

/* 确保 router-link 激活状态也显示主色 */
.article-link.router-link-active {
  color: var(--el-color-primary);
}

/* 分类菜单项样式 */
.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-width: 200px;
}

.category-name {
  flex: 1;
  font-size: 17px !important;
  font-weight: 500 !important;
}

.category-count {
  color: #909399;
  font-size: 15px !important;
  margin-left: 12px;
  font-weight: 400 !important;
}

.dark .category-count {
  color: #9ca3af;
}

/* Mobile fullscreen overlay menu */
.mobile-menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(255, 255, 255, 0.98);
}

.dark .mobile-menu-overlay {
  background: rgba(17, 24, 39, 0.98);
}

.mobile-menu-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: env(safe-area-inset-top) 0 env(safe-area-inset-bottom);
}

.mobile-menu-items {
  flex: 1;
  overflow-y: auto;
  padding: 0 24px;
}

.mobile-menu-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  text-decoration: none;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: color 0.2s;
}

.dark .mobile-menu-item {
  color: #e5e7eb;
  border-bottom-color: #1f2937;
}

.mobile-menu-item:active {
  color: var(--el-color-primary);
}

.dark .mobile-menu-item:active {
  color: var(--el-color-primary-light-3);
}

.mobile-sub-items {
  padding-left: 40px;
  padding-bottom: 8px;
}

.mobile-sub-item {
  display: block;
  padding: 10px 0;
  font-size: 15px;
  color: #6b7280;
  text-decoration: none;
  border-bottom: 1px solid #f9fafb;
}

.dark .mobile-sub-item {
  color: #9ca3af;
  border-bottom-color: #1f2937;
}

.mobile-menu-footer {
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
}

.dark .mobile-menu-footer {
  border-top-color: #374151;
}

.rotate-180 {
  transform: rotate(180deg);
}

/* Transition */
.mobile-menu-fade-enter-active {
  transition: opacity 0.25s ease;
}
.mobile-menu-fade-leave-active {
  transition: opacity 0.2s ease;
}
.mobile-menu-fade-enter-from,
.mobile-menu-fade-leave-to {
  opacity: 0;
}
</style>