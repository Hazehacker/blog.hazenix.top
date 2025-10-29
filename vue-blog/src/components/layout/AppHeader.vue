<template>
  <header class="fixed top-0 left-0 right-0 z-50 flex justify-between items-center py-6 bg-white dark:bg-gray-900/90 backdrop-blur-sm shadow-md" style="height: 70px;">
    <div class="flex items-center space-x-2" style="margin-left: 30px;">
      <img :src="userStore.userInfo?.avatar || defaultAvatar" 
           alt="User avatar" 
           class="w-10 h-10 rounded-full"
           @error="onAvatarError" />
    </div>
    
    <nav class="hidden md:flex items-center space-x-8 text-gray-600 dark:text-gray-400">
      <router-link to="/home" class="hover:text-primary">首页</router-link>
      <router-link to="/articles" class="hover:text-primary">文章</router-link>
      <router-link to="/categories" class="hover:text-primary">分类</router-link>
      <router-link to="/tags" class="hover:text-primary">标签</router-link>
      <router-link to="/album" class="hover:text-primary">相册</router-link>
      
      <!-- 更多下拉菜单 -->
      <el-dropdown @command="handleMoreCommand" trigger="hover" class="more-dropdown">
        <span class="hover:text-primary cursor-pointer flex items-center more-trigger ">
          更多
          <el-icon class="ml-1"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="friend-links">
              <el-icon class="mr-2"><Link /></el-icon>
              友情链接
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
    
    <div class="flex items-center space-x-4" style="margin-right: 20px;">
      <el-button link @click="openSearch">
        <el-icon><Search /></el-icon>
      </el-button>
      <el-button link @click="openGithub">
          <img :src="github" alt="GitHub" class="w-5 h-5 rounded-full" />
        </el-button>
      <el-button link @click="openCSDN">
          <img :src="csdn" alt="CSDN" class="w-5 h-5 rounded-full" />
        </el-button>
      <el-dropdown v-if="userStore.token" @command="handleCommand">
        <el-button link>
          <el-icon><User/></el-icon>
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
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'
import ThemeToggle from '@/components/common/ThemeToggle.vue'
import { Search, User, ArrowDown, Link, InfoFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
import avatarFallback from '@/assets/img/avatar.jpg'
import githubImg from '@/assets/img/githubLogo.png'
import csdnImg from '@/assets/img/csdnLogo.png'
const defaultAvatar = avatarFallback
const github = githubImg
const csdn = csdnImg

const onAvatarError = (e) => {
  e.target.src = avatarFallback
}

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
  window.open('https://github.com/HazeHacker', '_blank')
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/')
  } else if (command === 'favorites') {
    router.push('/favorites')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

const handleMoreCommand = (command) => {
  if (command === 'friend-links') {
    router.push('/friend-links')
  } else if (command === 'about') {
    router.push('/about')
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
})

watch(
  () => route.query.login,
  () => {
    maybeOpenLoginFromQuery()
  }
)
</script>

<style scoped>
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

.more-trigger {
  font-size:16px; 
  color:#4B5563;
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
</style>