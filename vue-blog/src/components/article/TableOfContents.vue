<template>
  <div class="toc-container">
    <!-- 滚动进度 -->
    <div v-if="!isMobile" class="scroll-progress">
      <div class="progress-text">
        {{ scrollProgress }}% ↑ 返回顶部
      </div>
    </div>

    <!-- 目录标题 -->
    <div class="toc-header">
      <h3 class="toc-title">
        <el-icon class="mr-2"><Document /></el-icon>
        目录
      </h3>
    </div>

    <!-- 目录内容 -->
    <div v-if="tocItems.length > 0" class="toc-content">
      <nav class="toc-nav">
        <ul class="toc-list">
          <li
            v-for="item in tocItems"
            :key="item.id"
            :class="[
              'toc-item',
              `toc-level-${item.level}`,
              { 'toc-active': item.id === activeId }
            ]"
          >
            <a
              :href="`#${item.id}`"
              class="toc-link"
              @click="handleTocClick(item.id)"
            >
              {{ item.text }}
            </a>
          </li>
        </ul>
      </nav>
    </div>

    <!-- 空状态 -->
    <div v-else class="toc-empty">
      <p class="text-sm text-gray-500 dark:text-gray-400">
        暂无目录
      </p>
    </div>

    <!-- 互动按钮 -->
    <div v-if="!isMobile" class="toc-actions">
      <div class="action-item">
        <el-icon class="action-icon"><Star /></el-icon>
        <span class="action-text">0</span>
      </div>
      <div class="action-item">
        <el-icon class="action-icon"><ChatDotRound /></el-icon>
        <span class="action-text">0</span>
      </div>
      <div class="action-item">
        <el-icon class="action-icon"><Share /></el-icon>
        <span class="action-text">0</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { Document, Star, ChatDotRound, Share } from '@element-plus/icons-vue'

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  isMobile: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toc-click'])

const tocItems = ref([])
const activeId = ref('')
const scrollProgress = ref(0)

// 解析内容生成目录
const parseToc = (content) => {
  if (!content) return []
  
  const headings = []
  const lines = content.split('\n')
  
  lines.forEach((line, index) => {
    const match = line.match(/^(#{1,6})\s+(.+)$/)
    if (match) {
      const level = match[1].length
      const text = match[2].trim()
      const id = `heading-${index}-${text.toLowerCase().replace(/[^\w\u4e00-\u9fa5]/g, '-')}`
      
      headings.push({
        id,
        level,
        text,
        line: index
      })
    }
  })
  
  return headings
}

// 更新目录
const updateToc = () => {
  tocItems.value = parseToc(props.content)
}

// 处理目录点击
const handleTocClick = (id) => {
  activeId.value = id
  emit('toc-click', id)
  
  // 滚动到对应位置
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ 
      behavior: 'smooth',
      block: 'start'
    })
  }
}

// 更新滚动进度
const updateScrollProgress = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const docHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight
  const progress = Math.round((scrollTop / docHeight) * 100)
  scrollProgress.value = Math.min(100, Math.max(0, progress))
}

// 更新活动标题
const updateActiveHeading = () => {
  const headings = document.querySelectorAll('h1, h2, h3, h4, h5, h6')
  let current = ''
  
  headings.forEach(heading => {
    const rect = heading.getBoundingClientRect()
    if (rect.top <= 100) {
      current = heading.id
    }
  })
  
  activeId.value = current
}

// 滚动事件处理
const handleScroll = () => {
  updateScrollProgress()
  updateActiveHeading()
}

// 返回顶部
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  updateToc()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 监听内容变化
watch(() => props.content, () => {
  updateToc()
}, { immediate: true })
</script>

<style scoped>
.toc-container {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700;
}

.scroll-progress {
  @apply p-4 border-b border-gray-200 dark:border-gray-700;
}

.progress-text {
  @apply text-sm text-gray-600 dark:text-gray-400 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 transition-colors;
}

.toc-header {
  @apply p-4 border-b border-gray-200 dark:border-gray-700;
}

.toc-title {
  @apply text-lg font-semibold text-gray-900 dark:text-gray-100 m-0 flex items-center;
}

.toc-content {
  @apply p-4;
}

.toc-nav {
  @apply space-y-1;
}

.toc-list {
  @apply list-none m-0 p-0 space-y-1;
}

.toc-item {
  @apply relative;
}

.toc-level-1 {
  @apply pl-0;
}

.toc-level-2 {
  @apply pl-4;
}

.toc-level-3 {
  @apply pl-8;
}

.toc-level-4 {
  @apply pl-12;
}

.toc-level-5 {
  @apply pl-16;
}

.toc-level-6 {
  @apply pl-20;
}

.toc-link {
  @apply block py-2 px-3 text-sm text-gray-700 dark:text-gray-300 rounded-md transition-colors hover:bg-gray-100 dark:hover:bg-gray-700 hover:text-blue-600 dark:hover:text-blue-400;
  text-decoration: none;
}

.toc-active .toc-link {
  @apply bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 font-medium;
}

.toc-empty {
  @apply p-4 text-center;
}

.toc-actions {
  @apply p-4 border-t border-gray-200 dark:border-gray-700 flex justify-around;
}

.action-item {
  @apply flex flex-col items-center cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 transition-colors;
}

.action-icon {
  @apply text-lg mb-1;
}

.action-text {
  @apply text-xs;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .toc-container {
    @apply rounded-none border-x-0;
  }
  
  .toc-actions {
    @apply hidden;
  }
}
</style>