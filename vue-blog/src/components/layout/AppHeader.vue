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
    </nav>
    
    <div class="flex items-center space-x-4">
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
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import ThemeToggle from '@/components/common/ThemeToggle.vue'
import { Search, User } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
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
</script>