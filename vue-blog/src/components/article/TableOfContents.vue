<template>
  <div class="toc-container" style="margin-top: 50px;">
    <!-- 滚动进度 -->
    <div v-if="!isMobile" class="scroll-progress" @click="scrollToTop">
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
              @click.prevent="handleTocClick(item.id)"
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

    <!-- 文章统计信息 -->
    <div v-if="!isMobile && article" class="article-stats" style="display: flex;padding-left: 40px;">
      <div class="stats-item" style="display: flex;width: 50%;">
        <el-icon class="stats-icon"><View /></el-icon>
        <span class="stats-text">  {{ article.viewCount || article.views || 0 }}</span>
      </div>
      <div class="stats-item" style="display: flex;width: 50%;margin-bottom: 8px;">
        <el-icon class="stats-icon"><ChatDotRound /></el-icon>
        <span class="stats-text"> {{ article.commentCount || article.comments || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { Document, Star, Collection, Share, View, ChatDotRound } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  isMobile: {
    type: Boolean,
    default: false
  },
  article: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['toc-click', 'like', 'collect', 'share'])

const tocItems = ref([])
const activeId = ref('')
const scrollProgress = ref(0)

// 使用 markdown-it 解析内容，只提取非代码块中的标题
// markdown-it 的 fence token 是自包含的，所以任何 heading_open token 都不可能在代码块中
const parseToc = (content) => {
  if (!content) return []
  
  const headings = []
  
  // 创建 markdown-it 实例（只用于解析，不渲染）
  const md = new MarkdownIt()
  
  try {
    // 解析 markdown 内容为 tokens
    const tokens = md.parse(content, {})
    
    // 遍历 tokens，提取标题
    // markdown-it 会将代码块内容作为单独的 fence token，不会混在其他 token 中
    // 所以如果遇到 heading_open token，它肯定不在代码块中
    tokens.forEach((token, index) => {
      if (token.type === 'heading_open') {
        // 获取标题内容（下一个 token 应该是 inline 类型，包含标题文本）
        const nextToken = tokens[index + 1]
        if (nextToken && nextToken.type === 'inline') {
          const text = nextToken.content
          const level = parseInt(token.tag.substring(1)) // h1 -> 1, h2 -> 2, etc.
          const id = generateId(text)
          
          headings.push({
            id,
            level,
            text,
            line: token.map ? token.map[0] : 0
          })
        }
      }
    })
  } catch (error) {
    // console.error('Error parsing TOC with markdown-it:', error)
    // 如果解析失败，回退到简单的行解析（但不解析代码块中的内容）
    return parseTocFallback(content)
  }
  
  // console.log('Generated TOC items:', headings)
  return headings
}

// 回退方案：简单的行解析，但排除代码块
const parseTocFallback = (content) => {
  if (!content) return []
  
  const headings = []
  const lines = content.split('\n')
  let inCodeBlock = false
  let codeBlockFence = ''
  
  lines.forEach((line, index) => {
    const trimmedLine = line.trim()
    
    // 检查是否是代码块标记
    const codeFenceMatch = trimmedLine.match(/^(```+|~~~+)/)
    
    if (codeFenceMatch) {
      const fence = codeFenceMatch[1]
      const fenceType = fence.substring(0, 3)
      
      if (!inCodeBlock) {
        inCodeBlock = true
        codeBlockFence = fenceType
      } else {
        if (fenceType === codeBlockFence) {
          inCodeBlock = false
          codeBlockFence = ''
        }
      }
      return
    }
    
    // 如果在代码块中，跳过
    if (inCodeBlock) {
      return
    }
    
    // 检查是否是标题
    const match = line.match(/^(#{1,6})\s+(.+)$/)
    if (match) {
      const level = match[1].length
      const text = match[2].trim()
      const id = generateId(text)
      
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

// 生成ID的函数，支持中文字符
const generateId = (text) => {
  if (!text) return ''
  
  // 移除HTML标签
  const cleanText = text.replace(/<[^>]*>/g, '')
  
  // 转换为小写
  let id = cleanText.toLowerCase()
  
  // 替换空格和特殊字符为连字符，但保留中文字符
  id = id.replace(/[\s\u3000\u00a0]+/g, '-') // 替换各种空格
         .replace(/[^\w\u4e00-\u9fa5-]/g, '') // 保留字母、数字、中文、连字符
         .replace(/-+/g, '-') // 合并多个连字符
         .replace(/^-|-$/g, '') // 移除首尾连字符
  
  // 如果ID为空，使用索引作为fallback
  if (!id) {
    id = `heading-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
  }
  
  return id
}

// 更新目录
const updateToc = () => {
  tocItems.value = parseToc(props.content)
}

// 处理目录点击
const handleTocClick = (id) => {
  // console.log('TOC clicked:', id)
  activeId.value = id
  emit('toc-click', id)
  
  // 滚动到对应位置
  const element = document.getElementById(id)
  // console.log('Found element:', element)
  
  if (element) {
    element.scrollIntoView({ 
      behavior: 'smooth',
      block: 'start'
    })
  } else {
    // console.warn('Element not found for ID:', id)
    // 尝试查找所有标题元素进行调试
    const allHeadings = document.querySelectorAll('h1, h2, h3, h4, h5, h6')
    // console.log('All headings:', Array.from(allHeadings).map(h => ({ id: h.id, text: h.textContent })))
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

// 处理点赞
const handleLike = () => {
  emit('like')
}

// 处理收藏
const handleCollect = () => {
  emit('collect')
}

// 处理分享
const handleShare = () => {
  emit('share')
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
  position: relative;
  z-index: 10;
}

.scroll-progress {
  @apply p-4 border-b border-gray-200 dark:border-gray-700 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20;
}

.progress-text {
  @apply text-sm text-gray-600 dark:text-gray-400 cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 transition-colors font-medium;
}

.toc-header {
  @apply p-4 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700/50;
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

.article-stats {
  @apply p-4 border-t border-gray-200 dark:border-gray-700 space-y-2;
}

.stats-item {
  @apply flex items-center text-sm text-gray-600 dark:text-gray-400;
}

.stats-icon {
  @apply mr-2 text-gray-500 dark:text-gray-500;
}

.stats-text {
  @apply font-medium;
}

.toc-actions {
  @apply p-4 border-t border-gray-200 dark:border-gray-700 flex justify-between;
  gap: 0.5rem;
}

.action-item {
  @apply flex flex-col items-center cursor-pointer hover:text-blue-600 dark:hover:text-blue-400 transition-colors p-1.5 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700;
  flex: 1;
}

.action-item.liked {
  @apply text-yellow-500 hover:text-yellow-600;
}

.action-item.collected {
  @apply text-green-500 hover:text-green-600;
}

.action-icon {
  @apply text-lg mb-0.5;
}

.action-text {
  @apply text-xs font-medium;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .toc-container {
    @apply rounded-none border-x-0;
  }
  
  .article-stats,
  .toc-actions {
    @apply hidden;
  }
}
</style>