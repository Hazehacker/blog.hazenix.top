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
import { articleApi } from '@/api/article'

const router = useRouter()
const categories = ref([])
const loading = ref(false)

// 重新计算分类的文章数量（只统计已发布的文章，排除id=1）
const recalculateCategoryCounts = async (categoryList) => {
  try {
    // 获取所有文章列表
    const response = await articleApi.getArticleList()
    
    if (response && response.code === 200 && response.data) {
      // 处理不同的响应格式
      let articles = []
      if (Array.isArray(response.data)) {
        articles = response.data
      } else if (response.data.list && Array.isArray(response.data.list)) {
        articles = response.data.list
      } else if (Array.isArray(response.data.records)) {
        articles = response.data.records
      } else {
        articles = []
      }
      
      // 过滤文章：只保留已发布的文章（status=0），排除留言板文章（id=1）
      const publishedArticles = articles.filter(article => {
        // 排除留言板文章
        if (article.id === 1 || article.id === '1') {
          return false
        }
        // 只显示已发布的文章（status=0）
        if (article.status !== 0 && article.status !== '0') {
          return false
        }
        return true
      })
      
      // 统计每个分类的文章数量
      const categoryCountMap = new Map()
      publishedArticles.forEach(article => {
        const categoryId = article.categoryId || article.category?.id
        if (categoryId) {
          categoryCountMap.set(categoryId, (categoryCountMap.get(categoryId) || 0) + 1)
        }
      })
      
      // 更新分类列表中的文章数量
      return categoryList.map(category => ({
        ...category,
        articleCount: categoryCountMap.get(category.id) || 0
      })).filter(category => category.articleCount > 0) // 只显示有文章数量的分类
    }
    
    return categoryList
  } catch (error) {
    console.error('重新计算分类数量失败:', error)
    return categoryList
  }
}

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await getCategoryList()
    let categoryList = res.data || []
    
    // 重新计算分类的文章数量
    categoryList = await recalculateCategoryCounts(categoryList)
    
    categories.value = categoryList
  } catch (error) {
    console.error('Failed to load categories:', error)
    categories.value = []
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
