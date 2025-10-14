<template>
  <el-button link @click="themeStore.toggleTheme()">
    <el-icon v-if="themeStore.isDark"><Moon /></el-icon>
    <el-icon v-else><Sunny /></el-icon>
  </el-button>
</template>

<script setup>
import { useThemeStore } from '@/stores/theme'
import { Moon, Sunny } from '@element-plus/icons-vue'

const themeStore = useThemeStore()
</script>
