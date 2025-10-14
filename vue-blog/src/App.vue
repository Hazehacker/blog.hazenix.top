<script setup>
import { useThemeStore } from '@/stores/theme'

// 初始化主题
const themeStore = useThemeStore()
</script>

<template>
  <router-view />
</template>

<style scoped></style>
