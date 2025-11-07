<template>
  <el-dialog v-model="visible" title="搜索文章" width="600px" :z-index="999999" :modal="true" :append-to-body="true">
    <el-input
      v-model="searchKeyword"
      placeholder="输入关键词搜索..."
      @keyup.enter="handleSearch"
      class="mb-4"
    >
      <template #append>
        <el-button @click="handleSearch">搜索</el-button>
      </template>
    </el-input>
    
    <!-- 快速搜索选项 -->
    <div class="mb-4">
      <el-button 
        type="primary" 
        plain 
        @click="goToSearchPage"
        class="w-full"
      >
        进入搜索页面查看更多结果
      </el-button>
    </div>
    
    <div v-loading="loading" class="max-h-96 overflow-y-auto">
      <div v-for="article in searchResults" :key="article.id" 
           class="p-3 border-b hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer"
           @click="goToArticle(article)">
        <h4 class="font-semibold text-gray-900 dark:text-white">{{ article.title }}</h4>
        <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">{{ article.summary || '暂无摘要' }}</p>
        <div class="flex items-center gap-2 mt-2 text-xs text-gray-500">
          <span>{{ article.author || '匿名' }}</span>
          <span>{{ formatDate(article.createTime) }}</span>
        </div>
      </div>
      
      <div v-if="searchResults.length === 0 && !loading" class="text-center py-8 text-gray-500">
        暂无搜索结果
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList } from '@/api/article'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const visible = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const loading = ref(false)

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  loading.value = true
  try {
    const res = await getArticleList({ 
      keyword: searchKeyword.value, 
      status: '0' // 只搜索正常状态的文章
    })
    searchResults.value = res.data || []
  } catch (error) {
    console.error('Search failed:', error)
    ElMessage.error('搜索失败')
    searchResults.value = []
  } finally {
    loading.value = false
  }
}

// 跳转到文章详情（优先使用slug）
const goToArticle = (article) => {
  // 优先使用slug，如果没有slug则使用id
  const identifier = article.slug || article.id
  router.push(`/article/${identifier}`)
  visible.value = false
}

const goToSearchPage = () => {
  if (searchKeyword.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(searchKeyword.value)}`)
  } else {
    router.push('/search')
  }
  visible.value = false
}

// 暴露方法给父组件调用
defineExpose({
  open: () => {
    console.log('SearchDialog open method called, visible:', visible.value)
    visible.value = true
    console.log('SearchDialog visible set to:', visible.value)
  }
})
</script>
