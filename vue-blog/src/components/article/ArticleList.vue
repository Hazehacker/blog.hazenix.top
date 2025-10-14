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

const handleArticleClick = (article) => {
  router.push(`/article/${article.id}`)
}
</script>
