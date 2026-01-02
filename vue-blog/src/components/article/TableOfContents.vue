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
      <!-- <div class="stats-item" style="display: flex;width: 50%;">
        <el-icon class="stats-icon"><View /></el-icon>
        <span class="stats-text">  {{ article.viewCount || article.views || 0 }}</span>
      </div> -->
      <div class="stats-item" style="display: flex;width: 50%;margin-bottom: 8px;">
        <el-icon class="stats-icon"><ChatDotRound /></el-icon>
        <span class="stats-text"> {{ article.commentCount || article.comments || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
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

// 注意：TableOfContents 现在直接从 DOM 读取 ID，不再自己生成
// 保留 usedIds 用于回退方案
const usedIds = new Map()

// 使用 markdown-it 解析内容，只提取非代码块中的标题
// markdown-it 的 fence token 是自包含的，所以任何 heading_open token 都不可能在代码块中
const parseToc = (content) => {
  if (!content) return []
  
  // 重置已使用的ID
  usedIds.clear()
  
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
          const id = generateId(text, true) // 使用唯一ID生成函数
          
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
  
  // 重置已使用的ID
  usedIds.clear()
  
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
      const id = generateId(text, true) // 使用唯一ID生成函数
      
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

// 生成ID的函数，支持中文字符，并为重复的标题添加序号后缀（与 markdown.js 中的逻辑一致）
const generateId = (text, isUnique = false) => {
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
  
  // 如果需要唯一ID，检查是否已存在，如果存在则添加序号后缀
  if (isUnique) {
    const baseId = id
    let counter = 1
    while (usedIds.has(id)) {
      id = `${baseId}-${counter}`
      counter++
    }
    usedIds.set(id, true)
  }
  
  return id
}

// 更新目录
const updateToc = () => {
  tocItems.value = parseToc(props.content)
  
  // 等待 DOM 更新后，从实际渲染的标题中同步 ID
  setTimeout(() => {
    syncTocIdsFromDOM()
  }, 300)
}

// 从 DOM 中同步标题 ID，确保目录 ID 与实际渲染的标题 ID 一致
const syncTocIdsFromDOM = () => {
  // 在文章内容区域查找标题（避免找到其他地方的标题）
  const articleContent = document.querySelector('.markdown-content, .article-body, .prose')
  const searchContainer = articleContent || document
  
  const domHeadings = Array.from(searchContainer.querySelectorAll('h1, h2, h3, h4, h5, h6'))
    .filter(h => h.id) // 只获取有 ID 的标题
  
  if (domHeadings.length === 0) {
    // 如果 DOM 中还没有标题，稍后重试
    setTimeout(() => syncTocIdsFromDOM(), 200)
    return
  }
  
  // 按顺序匹配目录项和 DOM 标题
  const updatedItems = []
  let domIndex = 0
  
  for (let i = 0; i < tocItems.value.length; i++) {
    const tocItem = tocItems.value[i]
    let matched = false
    
    // 从当前位置开始查找匹配的标题
    for (let j = domIndex; j < domHeadings.length; j++) {
      const heading = domHeadings[j]
      const headingText = heading.textContent.trim()
      
      // 如果文本匹配（允许一些容差）
      if (headingText === tocItem.text || 
          headingText.includes(tocItem.text) || 
          tocItem.text.includes(headingText)) {
        updatedItems.push({
          ...tocItem,
          id: heading.id
        })
        domIndex = j + 1
        matched = true
        break
      }
    }
    
    // 如果没有找到匹配，保持原来的 ID
    if (!matched) {
      updatedItems.push(tocItem)
    }
  }
  
  tocItems.value = updatedItems
  
  // 同步完成后，更新活动标题
  nextTick(() => {
    updateActiveHeading()
  })
}

// 处理目录点击
const handleTocClick = (id) => {
  activeId.value = id
  emit('toc-click', id)
  
  // 滚动到对应位置，添加重试机制
  const scrollToElement = (retries = 3) => {
    const element = document.getElementById(id)
    
    if (element) {
      // 计算偏移量，考虑固定头部
      const offset = 100
      const elementPosition = element.getBoundingClientRect().top + window.pageYOffset
      const offsetPosition = elementPosition - offset
      
      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      })
      
      // 滚动完成后更新活动标题
      setTimeout(() => {
        updateActiveHeading()
      }, 500)
    } else if (retries > 0) {
      // 如果元素还没渲染，等待一下再重试
      setTimeout(() => {
        scrollToElement(retries - 1)
      }, 100)
    }
  }
  
  scrollToElement()
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
  if (headings.length === 0) return
  
  let current = ''
  let currentTop = Infinity
  
  // 找到当前最接近顶部但仍在视口上方的标题
  headings.forEach(heading => {
    if (!heading.id) return // 跳过没有ID的标题
    
    const rect = heading.getBoundingClientRect()
    const top = rect.top
    
    // 如果标题在视口上方或刚刚进入视口（距离顶部150px以内）
    if (top <= 150) {
      // 选择最接近顶部但仍在视口上方的标题
      if (top > currentTop || currentTop === Infinity) {
        current = heading.id
        currentTop = top
      }
    }
  })
  
  // 如果没有找到在视口上方的标题，选择第一个在视口内的标题
  if (!current) {
    for (let i = 0; i < headings.length; i++) {
      const heading = headings[i]
      if (!heading.id) continue
      
      const rect = heading.getBoundingClientRect()
      if (rect.top >= 0 && rect.top <= 300) {
        current = heading.id
        break
      }
    }
  }
  
  // 如果还是没有找到，选择第一个标题
  if (!current && headings.length > 0 && headings[0].id) {
    current = headings[0].id
  }
  
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
  // 等待 DOM 渲染完成后初始化活动标题
  setTimeout(() => {
    updateActiveHeading()
  }, 200)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 监听内容变化
watch(() => props.content, () => {
  updateToc()
  // 内容更新后，等待 DOM 更新再初始化活动标题
  setTimeout(() => {
    updateActiveHeading()
  }, 100)
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