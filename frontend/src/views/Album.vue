<template>
  <div class="album-page">
    <div class="page-header">
      <h1 class="page-title">相册</h1>
      <p class="page-description">欢迎来到我的相册</p>
    </div>
    
    <div class="content">
      <p>暂未添加相片...</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { setSEO } from '@/utils/seo'

onMounted(() => {
  setSEO({
    title: '相册',
    description: '查看Hazelnix的相册',
    keywords: ['相册', '图片', '照片'],
  })
})
</script>

<style scoped>
.album-page {
  @apply min-h-screen bg-gray-50 dark:bg-gray-900 py-16;
}

.page-header {
  @apply text-center mb-12;
}

.page-title {
  @apply text-3xl font-bold text-gray-900 dark:text-white mb-4;
}

.page-description {
  @apply text-gray-600 dark:text-gray-400;
}

.content {
  @apply max-w-4xl mx-auto px-4;
}
</style>