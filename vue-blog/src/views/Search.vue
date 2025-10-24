<template>
  <div class="search-page">
    <div class="page-header">
      <h1 class="page-title">搜索结果</h1>
    </div>
    
    <div class="content">
      <p>搜索功能正在开发中...</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { setSEO } from '@/utils/seo'

onMounted(() => {
  setSEO({
    title: '搜索',
    description: '在博客中搜索内容',
    keywords: ['搜索', '查找', '博客'],
  })
})
</script>

<style scoped>
.search-page {
  @apply min-h-screen bg-gray-50 dark:bg-gray-900 py-16;
}

.page-header {
  @apply text-center mb-12;
}

.page-title {
  @apply text-3xl font-bold text-gray-900 dark:text-white mb-4;
}

.content {
  @apply max-w-4xl mx-auto px-4;
}
</style>