<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 页面头部 -->
    <div class="bg-white dark:bg-gray-800 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="text-center">
          <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-4">
            <el-icon class="mr-3 text-primary"><Document /></el-icon>
            关于此项目
          </h1>
          <p class="text-lg text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
            了解这个项目的详细信息
          </p>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-8">
        <div class="prose dark:prose-invert max-w-none">
          <!-- 这里可以添加项目相关的内容 -->
          <div class="text-center text-gray-500 dark:text-gray-400 py-12">
            <p class="text-lg">项目介绍内容待补充...</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Document } from '@element-plus/icons-vue'
</script>

<style scoped>
.text-primary {
  color: var(--el-color-primary);
}
</style>

