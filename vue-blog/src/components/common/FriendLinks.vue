<template>
  <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
        <el-icon class="mr-2"><Link /></el-icon>
        友情链接
      </h3>
      <el-button 
        v-if="isAdmin" 
        size="small" 
        type="primary" 
        plain
        @click="$router.push('/admin/links')"
      >
        管理友链
      </el-button>
    </div>

    <div v-if="loading" class="flex justify-center py-8">
      <el-icon class="is-loading text-2xl"><Loading /></el-icon>
    </div>

    <div v-else-if="links.length === 0" class="text-center text-gray-500 dark:text-gray-400 py-8">
      暂无友情链接
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <a
        v-for="link in links"
        :key="link.id"
        :href="link.url"
        target="_blank"
        rel="noopener noreferrer"
        class="group flex items-center p-4 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors"
      >
        <img
          v-if="link.avatar"
          :src="link.avatar"
          :alt="link.name"
          class="w-12 h-12 rounded-full object-cover mr-4 flex-shrink-0"
          @error="handleImageError"
        />
        <div class="flex-1 min-w-0">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 group-hover:text-primary truncate">
            {{ link.name }}
          </h4>
          <p v-if="link.description" class="text-sm text-gray-500 dark:text-gray-400 line-clamp-2 mt-1">
            {{ link.description }}
          </p>
        </div>
      </a>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Link, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { frontendApi } from '@/api/frontend'

// 响应式数据
const loading = ref(false)
const links = ref([])
const userStore = useUserStore()

// 计算属性
const isAdmin = computed(() => {
  return userStore.userInfo?.isAdmin || false
})

// 加载友链列表
const loadLinks = async () => {
  loading.value = true
  try {
    const response = await frontendApi.getLinks({ status: 0 })
    links.value = response.data || []
  } catch (error) {
    console.error('加载友链列表失败:', error)
    // 如果API调用失败，使用模拟数据
    const mockLinks = [
      {
        id: 1,
        name: 'Vue.js',
        description: '渐进式JavaScript框架',
        url: 'https://vuejs.org',
        avatar: 'https://vuejs.org/images/logo.png',
        status: 0
      },
      {
        id: 2,
        name: 'Element Plus',
        description: '基于Vue 3的桌面端组件库',
        url: 'https://element-plus.org',
        avatar: 'https://element-plus.org/images/element-plus-logo-small.svg',
        status: 0
      }
    ]
    links.value = mockLinks
  } finally {
    loading.value = false
  }
}

// 图片加载失败处理
const handleImageError = (event) => {
  event.target.src = '/default-avatar.png'
}

// 初始化
onMounted(() => {
  loadLinks()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.group:hover .group-hover\:text-primary {
  color: var(--el-color-primary);
}
</style>
