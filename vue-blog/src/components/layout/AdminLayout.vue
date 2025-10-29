<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 侧边栏 -->
    <aside class="fixed inset-y-0 left-0 z-50 w-64 bg-white dark:bg-gray-800 shadow-lg transform transition-transform duration-300 ease-in-out"
           :class="{ '-translate-x-full': !sidebarOpen, 'translate-x-0': sidebarOpen }">
      <!-- Logo -->
      <div class="flex items-center justify-center h-16 px-4 bg-primary text-white">
        <h1 class="text-xl font-bold">管理后台</h1>
      </div>
      
      <!-- 导航菜单 -->
      <nav class="mt-8">
        <div class="px-4 space-y-2">
          <router-link
            v-for="item in menuItems"
            :key="item.name"
            :to="item.path"
            class="flex items-center px-4 py-3 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            :class="{ 'bg-primary text-white': $route.path === item.path }"
          >
            <el-icon class="mr-3">
              <component :is="item.icon" />
            </el-icon>
            {{ item.label }}
          </router-link>
        </div>
      </nav>
    </aside>

    <!-- 主内容区域 -->
    <div class="flex flex-col flex-1 ml-0 transition-all duration-300"
         :class="{ 'ml-64': sidebarOpen }">
      <!-- 顶部导航栏 -->
      <header class="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
        <div class="flex items-center justify-between px-6 py-4">
          <!-- 左侧：菜单切换按钮 -->
          <div class="flex items-center">
            <el-button
              @click="toggleSidebar"
              class="mr-4"
              :icon="sidebarOpen ? 'ArrowLeft' : 'Menu'"
              circle
            />
            <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-200">
              {{ currentPageTitle }}
            </h2>
          </div>

          <!-- 右侧：用户信息和操作 -->
          <div class="flex items-center space-x-4">
            <el-button @click="goToHome" type="primary" plain>
              返回前台
            </el-button>
            <el-dropdown @command="handleUserCommand">
              <div class="flex items-center space-x-2 cursor-pointer">
                <img :src="userStore.userInfo?.avatar || defaultAvatar" 
                     alt="User avatar" 
                     class="w-8 h-8 rounded-full" />
                <span class="text-gray-700 dark:text-gray-300">{{ userStore.userInfo?.username || 'Admin' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人设置</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="flex-1 p-6">
        <router-view />
      </main>
    </div>

    <!-- 移动端遮罩 -->
    <div v-if="sidebarOpen" 
         class="fixed inset-0 z-40 bg-black bg-opacity-50 lg:hidden"
         @click="toggleSidebar">
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  House, 
  Document, 
  Collection, 
  PriceTag, 
  ChatDotRound, 
  Clock,
  Link,
  ArrowLeft,
  Menu,
  ArrowDown
} from '@element-plus/icons-vue'
import avatarFallback from '@/assets/img/avatar.jpg'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarOpen = ref(true)
const defaultAvatar = avatarFallback

// 菜单项配置
const menuItems = [
  { name: 'dashboard', path: '/admin', label: '仪表盘', icon: 'House' },
  { name: 'articles', path: '/admin/articles', label: '文章管理', icon: 'Document' },
  { name: 'categories', path: '/admin/categories', label: '分类管理', icon: 'Collection' },
  { name: 'tags', path: '/admin/tags', label: '标签管理', icon: 'PriceTag' },
  { name: 'comments', path: '/admin/comments', label: '评论管理', icon: 'ChatDotRound' },
  { name: 'updates', path: '/admin/updates', label: '更新记录', icon: 'Clock' },
  { name: 'links', path: '/admin/links', label: '友链管理', icon: 'Link' },
]

// 当前页面标题
const currentPageTitle = computed(() => {
  const currentItem = menuItems.find(item => item.path === route.path)
  return currentItem ? currentItem.label : '管理后台'
})

// 切换侧边栏
const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

// 返回前台
const goToHome = () => {
  router.push('/index')
}

// 用户操作
const handleUserCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    // 跳转到个人设置页面
    console.log('跳转到个人设置')
  }
}

// 响应式处理
const handleResize = () => {
  if (window.innerWidth < 1024) {
    sidebarOpen.value = false
  } else {
    sidebarOpen.value = true
  }
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.router-link-active {
  background-color: var(--el-color-primary);
  color: white;
}
</style>
