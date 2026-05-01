<template>
  <div class="w-full" v-if="articles.length > 0">
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center gap-3">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white">
          {{ isLoggedIn ? '为你推荐' : '热门推荐' }}
        </h2>
        <span v-if="!isLoggedIn" class="text-sm text-gray-400 dark:text-gray-500">
          登录获取个性化推荐
        </span>
      </div>
      <button
        v-if="isLoggedIn"
        @click="refresh"
        class="text-sm text-primary hover:text-primary/80 transition-colors"
        :disabled="loading"
      >
        换一批
      </button>
    </div>

    <div class="relative group">
      <!-- Left arrow -->
      <button
        v-show="canScrollLeft"
        @click="scrollLeft"
        class="absolute left-0 top-1/2 -translate-y-1/2 z-10 w-8 h-8 bg-white/90 dark:bg-gray-800/90 rounded-full shadow-md flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
      >
        <span class="text-gray-600 dark:text-gray-300">&lt;</span>
      </button>

      <!-- Scrollable container -->
      <div
        ref="scrollContainer"
        class="flex gap-4 overflow-x-auto scrollbar-hide scroll-smooth pb-2"
        @scroll="updateScrollState"
      >
        <RecommendCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
          @click="goToArticle(article)"
        />
      </div>

      <!-- Right arrow -->
      <button
        v-show="canScrollRight"
        @click="scrollRight"
        class="absolute right-0 top-1/2 -translate-y-1/2 z-10 w-8 h-8 bg-white/90 dark:bg-gray-800/90 rounded-full shadow-md flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
      >
        <span class="text-gray-600 dark:text-gray-300">&gt;</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getRecommendedArticles } from '@/api/recommend'
import RecommendCard from './RecommendCard.vue'

const router = useRouter()
const userStore = useUserStore()
const articles = ref([])
const loading = ref(false)
const scrollContainer = ref(null)
const canScrollLeft = ref(false)
const canScrollRight = ref(false)

const isLoggedIn = ref(!!userStore.token)

const fetchRecommendations = async () => {
  loading.value = true
  try {
    const res = await getRecommendedArticles({ size: 10 })
    if (res && res.data) {
      articles.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (error) {
    articles.value = []
  } finally {
    loading.value = false
    await nextTick()
    updateScrollState()
  }
}

const refresh = () => {
  fetchRecommendations()
}

const goToArticle = (article) => {
  router.push({ name: 'ArticleDetail', params: { id: article.id } })
}

const scrollLeft = () => {
  if (scrollContainer.value) {
    scrollContainer.value.scrollBy({ left: -300, behavior: 'smooth' })
  }
}

const scrollRight = () => {
  if (scrollContainer.value) {
    scrollContainer.value.scrollBy({ left: 300, behavior: 'smooth' })
  }
}

const updateScrollState = () => {
  if (!scrollContainer.value) return
  const el = scrollContainer.value
  canScrollLeft.value = el.scrollLeft > 0
  canScrollRight.value = el.scrollLeft + el.clientWidth < el.scrollWidth - 1
}

onMounted(() => {
  fetchRecommendations()
})
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>