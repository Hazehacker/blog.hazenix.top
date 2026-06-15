<template>
  <!-- 桌面端：保持不变 -->
  <div v-if="!isMobile" class="toc-container mt-[50px]">
    <div class="scroll-progress" @click="scrollToTop">
      <div class="progress-text">
        {{ scrollProgress }}% ↑ 返回顶部
      </div>
    </div>

    <div class="toc-header">
      <h3 class="toc-title">
        <el-icon class="mr-2"><Document /></el-icon>
        目录
      </h3>
    </div>

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

    <div v-else class="toc-empty">
      <p class="text-sm text-gray-500 dark:text-gray-400">暂无目录</p>
    </div>

    <div v-if="article" class="article-stats flex pl-10">
      <div class="stats-item flex w-1/2 mb-2">
        <el-icon class="stats-icon"><ChatDotRound /></el-icon>
        <span class="stats-text"> {{ article.commentCount || article.comments || 0 }}</span>
      </div>
    </div>
  </div>

  <!-- 移动端：悬浮目录按钮 -->
  <div v-else class="toc-float-wrapper">
    <teleport to="body">
      <transition name="toc-backdrop-fade">
        <div v-show="mobileTocExpanded" class="toc-backdrop" @click="closeMobileToc"></div>
      </transition>
      <div class="toc-float" :class="{ 'toc-float--open': mobileTocExpanded }">
        <button
          class="toc-float-btn"
          @click="toggleMobileToc"
          :aria-expanded="mobileTocExpanded"
          aria-controls="toc-float-panel"
        >
          <el-icon :size="18"><Document /></el-icon>
          <span>目录</span>
          <el-icon :size="16" class="toc-float-arrow">
            <ArrowLeft v-if="!mobileTocExpanded" />
            <ArrowDown v-else />
          </el-icon>
        </button>

        <div id="toc-float-panel" v-show="mobileTocExpanded" class="toc-float-panel">
          <div v-if="tocItems.length > 0" class="toc-float-content">
            <nav>
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
                    @click.prevent="handleTocClick(item.id); closeMobileToc()"
                  >
                    {{ item.text }}
                  </a>
                </li>
              </ul>
            </nav>
          </div>
          <div v-else class="toc-empty">
            <p class="text-sm text-gray-500 dark:text-gray-400">暂无目录</p>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Document, Star, Collection, Share, View, ChatDotRound, ArrowDown, ArrowLeft } from '@element-plus/icons-vue'
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
const mobileTocExpanded = ref(false)

const toggleMobileToc = () => {
  mobileTocExpanded.value = !mobileTocExpanded.value
}

const closeMobileToc = () => {
  mobileTocExpanded.value = false
}

const usedIds = new Map()

const parseToc = (content) => {
  if (!content) return []

  usedIds.clear()

  const headings = []

  const md = new MarkdownIt()

  try {
    const tokens = md.parse(content, {})

    tokens.forEach((token, index) => {
      if (token.type === 'heading_open') {
        const nextToken = tokens[index + 1]
        if (nextToken && nextToken.type === 'inline') {
          let text = ''
          if (nextToken.children) {
            nextToken.children.forEach(child => {
              if (child.type === 'text') {
                text += child.content
              } else if (child.type === 'highlight') {
                text += child.content
              } else if (child.content) {
                text += child.content
              }
            })
          } else {
            text = nextToken.content || ''
          }

          text = text.replace(/==([^=]+)==/g, '$1').trim()

          const level = parseInt(token.tag.substring(1))
          const id = generateId(text, true)

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
    return parseTocFallback(content)
  }

  return headings
}

const parseTocFallback = (content) => {
  if (!content) return []

  usedIds.clear()

  const headings = []
  const lines = content.split('\n')
  let inCodeBlock = false
  let codeBlockFence = ''

  lines.forEach((line, index) => {
    const trimmedLine = line.trim()

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

    if (inCodeBlock) {
      return
    }

    const match = line.match(/^(#{1,6})\s+(.+)$/)
    if (match) {
      const level = match[1].length
      let text = match[2].trim()

      text = text.replace(/==([^=]+)==/g, '$1').trim()

      const id = generateId(text, true)

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

const generateId = (text, isUnique = false) => {
  if (!text) return ''

  let cleanText = text.replace(/<[^>]*>/g, '')

  cleanText = cleanText.replace(/==([^=]+)==/g, '$1')

  let id = cleanText.toLowerCase()

  id = id.replace(/[\s　 ]+/g, '-')
         .replace(/[^\w一-龥-]/g, '')
         .replace(/-+/g, '-')
         .replace(/^-|-$/g, '')

  if (!id) {
    id = `heading-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
  }

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

const updateToc = () => {
  tocItems.value = parseToc(props.content)

  setTimeout(() => {
    syncTocIdsFromDOM()
  }, 300)
}

const syncTocIdsFromDOM = () => {
  const articleContent = document.querySelector('.markdown-content, .article-body, .prose')
  const searchContainer = articleContent || document

  const domHeadings = Array.from(searchContainer.querySelectorAll('h1, h2, h3, h4, h5, h6'))
    .filter(h => h.id)

  if (domHeadings.length === 0) {
    setTimeout(() => syncTocIdsFromDOM(), 200)
    return
  }

  const updatedItems = []
  let domIndex = 0

  for (let i = 0; i < tocItems.value.length; i++) {
    const tocItem = tocItems.value[i]
    let matched = false

    for (let j = domIndex; j < domHeadings.length; j++) {
      const heading = domHeadings[j]
      let headingText = heading.textContent.trim()
      headingText = headingText.replace(/==([^=]+)==/g, '$1').trim()

      let cleanTocText = tocItem.text.replace(/==([^=]+)==/g, '$1').trim()

      if (headingText === cleanTocText ||
          headingText === tocItem.text ||
          headingText.includes(cleanTocText) ||
          cleanTocText.includes(headingText) ||
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

    if (!matched) {
      updatedItems.push(tocItem)
    }
  }

  tocItems.value = updatedItems

  nextTick(() => {
    updateActiveHeading()
  })
}

const handleTocClick = (id) => {
  const tocItem = tocItems.value.find(item => item.id === id)
  const originalTocText = tocItem ? tocItem.text : ''
  let cleanTocText = originalTocText.replace(/==([^=]+)==/g, '$1').trim()
  let highlightedTocText = `==${cleanTocText}==`

  activeId.value = id
  emit('toc-click', id)

  const scrollToElement = (retries = 5) => {
    const articleContent = document.querySelector('.markdown-content, .article-body, .prose')
    const searchContainer = articleContent || document

    let element = null

    try {
      if (CSS && CSS.escape) {
        element = searchContainer.querySelector(`#${CSS.escape(id)}`) || document.getElementById(id)
      } else {
        element = document.getElementById(id)
      }
    } catch (e) {
      element = document.getElementById(id)
    }

    if (!element) {
      const allHeadings = searchContainer.querySelectorAll('h1, h2, h3, h4, h5, h6')
      for (const heading of allHeadings) {
        if (heading.id === id) {
          element = heading
          break
        }
      }
    }

    if (!element) {
      try {
        const decodedId = decodeURIComponent(id)
        if (CSS && CSS.escape) {
          element = searchContainer.querySelector(`#${CSS.escape(decodedId)}`) || document.getElementById(decodedId)
        } else {
          element = document.getElementById(decodedId)
        }
      } catch (e) {}
    }

    if (!element) {
      const allHeadings = searchContainer.querySelectorAll('h1, h2, h3, h4, h5, h6')
      const idLower = id.toLowerCase()
      for (const heading of allHeadings) {
        if (!heading.id) continue
        if (heading.id.toLowerCase() === idLower) {
          element = heading
          break
        }
      }
    }

    if (!element && cleanTocText) {
      const allHeadings = searchContainer.querySelectorAll('h1, h2, h3, h4, h5, h6')
      for (const heading of allHeadings) {
        if (!heading.id) continue

        let headingText = heading.textContent.trim()
        headingText = headingText.replace(/==([^=]+)==/g, '$1').trim()

        const headingHTML = heading.innerHTML || ''
        const hasHighlight = headingHTML.includes('<mark') || headingHTML.includes('markdown-highlight')

        const tocTextLower = cleanTocText.toLowerCase()
        const headingTextLower = headingText.toLowerCase()

        if (headingText === cleanTocText ||
            headingTextLower === tocTextLower ||
            headingText.includes(cleanTocText) ||
            cleanTocText.includes(headingText) ||
            headingTextLower.includes(tocTextLower) ||
            tocTextLower.includes(headingTextLower)) {
          element = heading
          break
        }

        if (hasHighlight) {
          if (headingHTML.includes(cleanTocText) ||
              headingHTML.toLowerCase().includes(tocTextLower)) {
            element = heading
            break
          }
        }
      }
    }

    if (!element && cleanTocText) {
      const allHeadings = searchContainer.querySelectorAll('h1, h2, h3, h4, h5, h6')

      for (const heading of allHeadings) {
        if (!heading.id) continue

        let headingText = heading.textContent.trim()
        headingText = headingText.replace(/==([^=]+)==/g, '$1').trim()

        const headingHTML = heading.innerHTML || ''

        const tocTextLower = cleanTocText.toLowerCase()
        const headingTextLower = headingText.toLowerCase()

        const hasHighlightInHTML = headingHTML.includes('<mark') || headingHTML.includes('markdown-highlight')

        if (headingText === cleanTocText ||
            headingTextLower === tocTextLower ||
            headingText.includes(cleanTocText) ||
            cleanTocText.includes(headingText) ||
            headingTextLower.includes(tocTextLower) ||
            tocTextLower.includes(headingTextLower) ||
            (hasHighlightInHTML && (
              headingHTML.includes(cleanTocText) ||
              headingHTML.toLowerCase().includes(tocTextLower)
            ))) {
          element = heading
          break
        }
      }
    }

    if (!element) {
      const allHeadings = searchContainer.querySelectorAll('h1, h2, h3, h4, h5, h6')
      const idBase = id.replace(/-\d+$/, '')
      for (const heading of allHeadings) {
        if (!heading.id) continue
        const headingIdBase = heading.id.replace(/-\d+$/, '')
        if (headingIdBase === idBase || heading.id.startsWith(idBase + '-')) {
          element = heading
          break
        }
      }
    }

    if (element) {
      const offset = 100
      const elementPosition = element.getBoundingClientRect().top + window.pageYOffset
      const offsetPosition = Math.max(0, elementPosition - offset)

      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      })

      history.pushState(null, '', `#${element.id}`)

      activeId.value = element.id

      setTimeout(() => {
        updateActiveHeading()
      }, 500)
      return true
    } else if (retries > 0) {
      setTimeout(() => {
        scrollToElement(retries - 1)
      }, 100)
      return true
    }

    return false
  }

  scrollToElement()
}

const updateScrollProgress = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const docHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight
  const progress = Math.round((scrollTop / docHeight) * 100)
  scrollProgress.value = Math.min(100, Math.max(0, progress))
}

const updateActiveHeading = () => {
  const headings = document.querySelectorAll('h1, h2, h3, h4, h5, h6')
  if (headings.length === 0) return

  let current = ''
  let currentTop = Infinity

  headings.forEach(heading => {
    if (!heading.id) return

    const rect = heading.getBoundingClientRect()
    const top = rect.top

    if (top <= 150) {
      if (top > currentTop || currentTop === Infinity) {
        current = heading.id
        currentTop = top
      }
    }
  })

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

  if (!current && headings.length > 0 && headings[0].id) {
    current = headings[0].id
  }

  activeId.value = current
}

const handleScroll = () => {
  updateScrollProgress()
  updateActiveHeading()
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  updateToc()
  window.addEventListener('scroll', handleScroll)
  setTimeout(() => {
    updateActiveHeading()
  }, 200)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

watch(() => props.content, () => {
  updateToc()
  setTimeout(() => {
    updateActiveHeading()
  }, 100)
}, { immediate: true })
</script>

<style scoped>
/* 桌面端样式 */
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

.toc-level-1 { @apply pl-0; }
.toc-level-2 { @apply pl-4; }
.toc-level-3 { @apply pl-8; }
.toc-level-4 { @apply pl-12; }
.toc-level-5 { @apply pl-16; }
.toc-level-6 { @apply pl-20; }

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

/* ========== 移动端悬浮目录 ========== */

.toc-float-wrapper {
  display: contents;
}

.toc-backdrop {
  position: fixed;
  inset: 0;
  z-index: 150;
  background: rgba(0, 0, 0, 0.3);
}

.dark .toc-backdrop {
  background: rgba(0, 0, 0, 0.5);
}

.toc-backdrop-fade-enter-active,
.toc-backdrop-fade-leave-active {
  transition: opacity 0.2s ease;
}
.toc-backdrop-fade-enter-from,
.toc-backdrop-fade-leave-to {
  opacity: 0;
}

.toc-float {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  top: 64px;
  z-index: 151;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.toc-float-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 24px;
  width: 200px;
  background: rgba(243, 244, 246, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(209, 213, 219, 0.6);
  border-radius: 20px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  white-space: nowrap;
  transition: all 0.2s;
}

.dark .toc-float-btn {
  background: rgba(31, 41, 55, 0.85);
  backdrop-filter: blur(8px);
  border-color: rgba(55, 65, 81, 0.6);
  color: #9ca3af;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.2);
}

.toc-float-btn:active {
  transform: scale(0.95);
}

.toc-float-arrow {
  transition: transform 0.2s;
  flex-shrink: 0;
}

.toc-float-panel {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  top: calc(100% + 8px);
  width: 280px;
  max-height: 50vh;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  padding: 8px 0;
}

.dark .toc-float-panel {
  background: #1f2937;
  border-color: #374151;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
}

.toc-float-content {
  padding: 4px 8px;
}

.toc-float-content .toc-link {
  font-size: 13px;
  padding: 8px 12px;
  border-radius: 8px;
}
</style>
