<template>
  <div class="max-w-6xl mx-auto">
    <h1 class="text-3xl font-bold mb-8">文章分类</h1>
    
    <div v-loading="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="category in categories" :key="category.id" 
           class="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6 hover:shadow-xl transition-shadow duration-300 cursor-pointer"
           @click="goToCategory(category)">
        <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-2">
          {{ category.name }}
        </h3>
<!--        <p class="text-gray-600 dark:text-gray-400 mb-4">-->
<!--          {{ category.description }}-->
<!--        </p>-->
        <div class="text-sm text-gray-500">
          {{ category.articleCount || 0 }} 篇文章
        </div>
      </div>
      
      <div v-if="categories.length === 0 && !loading" class="col-span-full text-center py-12 text-gray-500">
        暂无分类
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategoryList } from '@/api/category'

const router = useRouter()
const categories = ref([])
const loading = ref(false)

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
    // Mock数据作为fallback
    categories.value = [
      { id: 1, name: '前端开发', description: '前端技术相关文章', articleCount: 5 },
      { id: 2, name: '后端开发', description: '后端技术相关文章', articleCount: 3 },
      { id: 3, name: '数据库', description: '数据库技术相关文章', articleCount: 2 }
    ]
  } finally {
    loading.value = false
  }
}

const goToCategory = (category) => {
  router.push(`/articles?category=${category.id}`)
}

onMounted(() => {
  loadCategories()
})
</script>
