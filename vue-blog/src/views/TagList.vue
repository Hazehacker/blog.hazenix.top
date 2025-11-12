<template>
  <div class="max-w-6xl mx-auto">
    <h1 class="text-3xl font-bold mb-8">文章标签</h1>
    
    <div v-loading="loading" class="flex flex-wrap gap-4">
      <el-tag 
        v-for="tag in tags" 
        :key="tag.id" 
        size="large" 
        class="cursor-pointer hover:opacity-80 transition-opacity"
        @click="goToTag(tag)"
      >
        {{ tag.name }}
        <span class="ml-2 text-xs opacity-70">({{ tag.articleCount || 0 }})</span>
      </el-tag>
      
      <div v-if="tags.length === 0 && !loading" class="w-full text-center py-12 text-gray-500">
        暂无标签
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTagList } from '@/api/tag'
import { articleApi } from '@/api/article'

const router = useRouter()
const tags = ref([])
const loading = ref(false)

// 重新计算标签的文章数量（只统计已发布的文章，排除id=1）
const recalculateTagCounts = async (tagList) => {
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
      
      // 统计每个标签的文章数量
      const tagCountMap = new Map()
      publishedArticles.forEach(article => {
        if (article.tags && Array.isArray(article.tags)) {
          article.tags.forEach(tag => {
            const tagId = typeof tag === 'object' ? tag.id : tag
            if (tagId) {
              tagCountMap.set(tagId, (tagCountMap.get(tagId) || 0) + 1)
            }
          })
        }
      })
      
      // 更新标签列表中的文章数量
      return tagList.map(tag => ({
        ...tag,
        articleCount: tagCountMap.get(tag.id) || 0
      })).filter(tag => tag.articleCount > 0) // 只显示有文章数量的标签
    }
    
    return tagList
  } catch (error) {
    // console.error('重新计算标签数量失败:', error)
    return tagList
  }
}

const loadTags = async () => {
  loading.value = true
  try {
    const res = await getTagList()
    let tagList = res.data || []
    
    // 重新计算标签的文章数量
    tagList = await recalculateTagCounts(tagList)
    
    tags.value = tagList
  } catch (error) {
    // console.error('Failed to load tags:', error)
    tags.value = []
  } finally {
    loading.value = false
  }
}

const goToTag = (tag) => {
  router.push(`/tag/${tag.id}`)
}

onMounted(() => {
  loadTags()
})
</script>
