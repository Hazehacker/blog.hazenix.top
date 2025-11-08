<script setup>
import { useThemeStore } from '@/stores/theme'
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

// 初始化主题
const themeStore = useThemeStore()
const userStore = useUserStore()

onMounted(async () => {
  if (userStore.token && !userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch (err) {
      // 获取用户信息失败时，清除本地登录状态
      await userStore.logout()
    }
  }
})
</script>

<template>
  <router-view />
</template>

<style scoped></style>
