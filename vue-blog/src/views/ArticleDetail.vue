<template>
  <div class="max-w-4xl mx-auto">
    <div v-loading="loading">
      <article v-if="article" class="bg-white dark:bg-gray-800 rounded-lg p-8 shadow-lg">
        <h1 class="text-4xl font-bold mb-4">{{ article.title }}</h1>
        
        <div class="flex items-center gap-4 text-gray-500 dark:text-gray-400 mb-8">
          <span>{{ article.author }}</span>
          <span>{{ formatDate(article.createTime) }}</span>
          <span>浏览 {{ article.viewCount }}</span>
        </div>
        
        <div class="mb-4">
          <el-tag v-for="tag in article.tags" :key="tag.id" class="mr-2">
            {{ tag.name }}
          </el-tag>
        </div>
        
        <MarkdownRenderer :content="article.content" />
      </article>
      
      <!-- 评论区 -->
      <div class="mt-12">
        <h2 class="text-2xl font-bold mb-6">评论</h2>
        <CommentList :article-id="articleId" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleDetail } from '@/api/article'
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue'
import CommentList from '@/components/article/CommentList.vue'
import dayjs from 'dayjs'

const route = useRoute()
const articleId = route.params.id
const article = ref(null)
const loading = ref(false)

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(articleId)
    article.value = res.data
  } catch (error) {
    console.error('Failed to load article:', error)
    // Mock数据作为fallback
    article.value = {
      id: articleId,
      title: 'Vue3博客系统开发指南',
      content: `# Vue3博客系统开发指南

本文介绍了如何使用Vue3、Element Plus和Tailwind CSS构建一个现代化的博客系统。

## 技术栈

- Vue 3
- Element Plus
- Tailwind CSS
- Pinia
- Vue Router

## 主要功能

1. 文章管理
2. 评论系统
3. 用户认证
4. 响应式设计

## 开发过程

### 1. 环境配置

首先需要安装必要的依赖包...

### 2. 组件开发

创建各种组件来构建用户界面...

## 总结

通过这个项目，我们可以学习到现代前端开发的最佳实践。`,
      author: 'Hazenix',
      createTime: '2025-01-01',
      viewCount: 100,
      tags: [{ id: 1, name: 'Vue3' }, { id: 2, name: '前端' }]
    }
  } finally {
    loading.value = false
  }
})
</script>
