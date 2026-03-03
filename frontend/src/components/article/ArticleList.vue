<template>
  <div class="space-y-6">
    <ArticleCard 
      v-for="article in articles" 
      :key="article.id" 
      :article="article"
      @click="handleArticleClick"
    />
    
    <div v-if="articles.length === 0" class="text-center py-12 text-gray-500 dark:text-gray-400">
      暂无文章
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import ArticleCard from './ArticleCard.vue'

defineProps({
  articles: {
    type: Array,
    default: () => []
  }
})

const router = useRouter()

// 跳转到文章详情（优先使用slug）
const handleArticleClick = (article) => {
  // 优先使用slug，如果没有slug则使用id
  const identifier = article.slug || article.id
  // 使用路由名称跳转，更可靠
  router.push({ name: 'ArticleDetail', params: { id: identifier } })
}
</script>
