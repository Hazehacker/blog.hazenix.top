<template>
  <div class="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-lg">
    <div class="text-center">
      <img :src="avatarUrl" 
           alt="User avatar" 
           class="w-20 h-20 rounded-full mx-auto mb-4" />
      <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-2">
        {{ userInfo?.username || 'Hazenix' }}
      </h3>
      <p class="text-gray-600 dark:text-gray-400 mb-4">
        {{ userInfo?.bio || '热爱技术，享受生活' }}
      </p>
      
      <div class="flex justify-center space-x-4 mb-4">
        <div class="text-center">
          <div class="text-2xl font-bold text-primary">{{ userInfo?.articleCount || 0 }}</div>
          <div class="text-sm text-gray-500">文章</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-primary">{{ userInfo?.viewCount || 0 }}</div>
          <div class="text-sm text-gray-500">浏览</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-primary">{{ userInfo?.commentCount || 0 }}</div>
          <div class="text-sm text-gray-500">评论</div>
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
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { Link, Message } from '@element-plus/icons-vue'

const userStore = useUserStore()

// 正确的默认头像路径
// 使用 import() 导入图片资源
import avatarImg from '@/assets/img/avatar.jpg';
const defaultAvatar = avatarImg;

// 计算头像URL，优先使用用户头像，否则使用默认头像
const avatarUrl = defaultAvatar


// 如果没有登录用户信息，使用默认信息
const userInfo = computed(() => {
  return userStore.userInfo || {
    username: 'Hazenix',
    bio: '心有所向，何惧山海',
    articleCount: 12,
    viewCount: 1280,
    commentCount: 56,
    github: 'https://github.com/HazeHacker',
    email: 'L3542495583@outlook.com'
  }
})
</script>