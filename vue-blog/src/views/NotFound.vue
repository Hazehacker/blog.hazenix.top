<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
    <div class="text-center">
      <h1 class="text-9xl font-bold text-gray-300 dark:text-gray-600">404</h1>
      <h2 class="text-2xl font-semibold text-gray-900 dark:text-white mt-4">页面未找到</h2>
      <p class="text-gray-600 dark:text-gray-400 mt-2 mb-8">抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>
