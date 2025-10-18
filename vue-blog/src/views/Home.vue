<template>
  <div class="py-16 md:py-24">
    <div class="flex flex-col md:flex-row justify-between items-start gap-12">
      <div class="md:w-2/3">
        <h1 class="text-5xl md:text-6xl font-bold text-gray-900 dark:text-white">
          Hazenix's Blog
        </h1>
        <p class="mt-4 text-2xl md:text-3xl text-primary font-semibold">
          Nothing but enthusiasm brightens up the endless years.
        </p>
        <p class="mt-6 text-lg text-gray-500 dark:text-gray-400">
         道阻且长，行则将至
        </p>
      </div>
      
      <div class="w-full md:w-1/3 mt-12 md:mt-0">
        <UserCard />
      </div>
    </div>
    
    <!-- 最新文章列表 -->
    <div class="mt-24">
      <h2 class="text-3xl font-bold mb-8">最新文章</h2>
      <ArticleList :articles="latestArticles" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticleList } from '@/api/article'
import ArticleList from '@/components/article/ArticleList.vue'
import UserCard from '@/components/common/UserCard.vue'

const latestArticles = ref([])

onMounted(async () => {
  try {
    const res = await getArticleList({ page: 1, pageSize: 10 })
    latestArticles.value = res.data.records || []
  } catch (error) {
    console.error('Failed to load articles:', error)
    // 使用mock数据作为fallback
    latestArticles.value = [
      {
        id: 1,
        title: 'Vue3博客系统开发指南',
        summary: '本文介绍了如何使用Vue3、Element Plus和Tailwind CSS构建一个现代化的博客系统...',
        author: 'Hazenix',
        createTime: '2025-01-01',
        viewCount: 100,
        tags: [{ id: 1, name: 'Vue3' }, { id: 2, name: '前端' }]
      },
      {
        id: 2,
        title: '现代前端开发最佳实践',
        summary: '探索现代前端开发中的最佳实践，包括组件设计、状态管理、性能优化等方面...',
        author: 'Hazenix',
        createTime: '2025-01-02',
        viewCount: 85,
        tags: [{ id: 3, name: 'JavaScript' }, { id: 4, name: '最佳实践' }]
      }
    ]
  }
})
</script>
