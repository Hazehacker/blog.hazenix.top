<template>
  <div class="article-edit">
    <div v-if="loading" class="flex justify-center py-8">
      <LoadingSpinner />
    </div>
    <ToastUIEditor
      v-else
      :article="article"
      :categories="categories"
      :tags="tags"
      @save="handleSave"
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ToastUIEditor from '@/components/admin/ToastUIEditor.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const article = ref(null)
const categories = ref([])
const tags = ref([])

// 加载文章详情
const fetchArticle = async () => {
  try {
    const response = await adminApi.getArticleById(route.params.id)
    article.value = response.data
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('获取文章详情失败')
    router.push('/admin/articles')
  }
}

// 加载分类和标签
const loadCategoriesAndTags = async () => {
  try {
    const [categoriesRes, tagsRes] = await Promise.all([
      adminApi.getCategories(),
      adminApi.getTags()
    ])
    categories.value = categoriesRes.data
    tags.value = tagsRes.data
  } catch (error) {
    console.error('加载分类和标签失败:', error)
  }
}

// 保存文章
const handleSave = async (articleData) => {
  try {
    await adminApi.updateArticle(route.params.id, articleData)
    ElMessage.success('文章更新成功')
    router.push('/admin/articles')
  } catch (error) {
    console.error('更新文章失败:', error)
    ElMessage.error('更新文章失败，请重试')
  }
}

// 取消编辑
const handleCancel = () => {
  router.push('/admin/articles')
}

// 生命周期
onMounted(async () => {
  await Promise.all([
    fetchArticle(),
    loadCategoriesAndTags()
  ])
  loading.value = false
})
</script>

<style scoped>
.article-edit {
  min-height: 100vh;
  background-color: #f8fafc;
}

.dark .article-edit {
  background-color: #1a202c;
}
</style>
