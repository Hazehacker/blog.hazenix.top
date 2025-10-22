<template>
  <div class="test-editor">
    <div class="container mx-auto px-4 py-8">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">
        Toast UI Editor 测试页面
      </h1>
      
      <ToastUIEditor
        :categories="categories"
        :tags="tags"
        @save="handleSave"
        @cancel="handleCancel"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ToastUIEditor from '@/components/admin/ToastUIEditor.vue'

// 测试数据
const categories = ref([
  { id: 1, name: '学习分享' },
  { id: 2, name: '技术文章' },
  { id: 3, name: '生活随笔' }
])

const tags = ref([
  { id: 1, name: 'Vue' },
  { id: 2, name: 'JavaScript' },
  { id: 3, name: '前端' },
  { id: 4, name: '教程' }
])

const handleSave = (articleData) => {
  console.log('保存文章数据:', articleData)
  alert('文章保存成功！')
}

const handleCancel = () => {
  console.log('取消编辑')
  alert('取消编辑')
}
</script>

<style scoped>
.test-editor {
  min-height: 100vh;
  background-color: #f8fafc;
}

.dark .test-editor {
  background-color: #1a202c;
}
</style>
