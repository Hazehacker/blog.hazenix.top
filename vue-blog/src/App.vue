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
      userStore.logout()
    }
  }
})
</script>

<template>
  <router-view />
</template>

<style scoped></style>
