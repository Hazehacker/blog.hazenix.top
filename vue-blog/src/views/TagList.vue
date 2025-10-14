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

const router = useRouter()
const tags = ref([])
const loading = ref(false)

const loadTags = async () => {
  loading.value = true
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    console.error('Failed to load tags:', error)
    // Mock数据作为fallback
    tags.value = [
      { id: 1, name: 'Vue3', articleCount: 3 },
      { id: 2, name: 'JavaScript', articleCount: 5 },
      { id: 3, name: '前端', articleCount: 8 },
      { id: 4, name: '后端', articleCount: 4 },
      { id: 5, name: '数据库', articleCount: 2 },
      { id: 6, name: '最佳实践', articleCount: 3 }
    ]
  } finally {
    loading.value = false
  }
}

const goToTag = (tag) => {
  router.push(`/articles?tag=${tag.id}`)
}

onMounted(() => {
  loadTags()
})
</script>
