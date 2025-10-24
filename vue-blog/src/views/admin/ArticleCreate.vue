<template>
  <div class="article-create">
    <ToastUIEditor
      @save="handleSave"
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'
import { ElMessage } from 'element-plus'
import ToastUIEditor from '@/components/admin/ToastUIEditor.vue'

const router = useRouter()

// 响应式数据
const categories = ref([])
const tags = ref([])

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
const handleSave = async (articleData, resolve, reject) => {
  try {
    await adminApi.createArticle(articleData)
    ElMessage.success('文章创建成功')
    router.push('/admin/articles')
    if (resolve) resolve()
  } catch (error) {
    console.error('创建文章失败:', error)
    ElMessage.error('创建文章失败，请重试')
    if (reject) reject(error)
  }
}

// 取消编辑
const handleCancel = () => {
  router.push('/admin/articles')
}

// 生命周期
onMounted(() => {
  loadCategoriesAndTags()
})
</script>

<style scoped>
.article-create {
  min-height: 100vh;
  background-color: #f8fafc;
}

.dark .article-create {
  background-color: #1a202c;
}
</style>
