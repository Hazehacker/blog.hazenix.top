<template>
  <div class="markdown-content">
    <div 
      class="prose prose-lg dark:prose-invert max-w-none" 
      v-html="renderedContent"
    ></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import md, { resetUsedIds } from '@/utils/markdown'

const props = defineProps({
  content: {
    type: String,
    required: true
  }
})

const renderedContent = computed(() => {
  if (!props.content) return ''
  // 在每次渲染前重置已使用的ID，確保每個標題都有唯一的ID
  resetUsedIds()
  const rendered = md.render(props.content)
  // console.log('Rendered markdown:', rendered)
  
  // 检查是否有标题元素
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = rendered
  const headings = tempDiv.querySelectorAll('h1, h2, h3, h4, h5, h6')
  // console.log('Found headings in rendered content:', Array.from(headings).map(h => ({ id: h.id, text: h.textContent })))
  
  return rendered
})
</script>

<style scoped>
.markdown-content {
  @apply w-full;
}

/* 代码块样式 */
:deep(.code-block-wrapper) {
  @apply relative my-4 rounded-lg overflow-hidden border border-gray-200 dark:border-gray-700;
}

:deep(.code-block-header) {
  @apply flex justify-between items-center px-4 py-2 bg-gray-100 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700;
}

:deep(.code-block-lang) {
  @apply text-sm font-medium text-gray-600 dark:text-gray-400;
}

:deep(.copy-btn) {
  @apply px-2 py-1 text-xs bg-blue-500 hover:bg-blue-600 text-white rounded transition-colors;
}

:deep(.hljs) {
  @apply p-4 m-0 overflow-x-auto;
  background-color: #0f172a !important;
  color: #f8fafc !important;
}

/* 表格样式 */
:deep(.table-wrapper) {
  @apply overflow-x-auto my-4;
}

:deep(.table-auto) {
  @apply min-w-full;
}

/* 引用块样式 */
:deep(blockquote) {
  @apply border-l-4 border-blue-500 pl-4 text-gray-600 dark:text-gray-400 my-4 bg-gray-50 dark:bg-gray-800 py-2 rounded-r;
  font-style: normal !important; /* 覆盖 Tailwind prose 的默认斜体样式 */
}

/* 引用块内的文字不应该是斜体 */
:deep(blockquote p),
:deep(blockquote *:not(em):not(i)) {
  font-style: normal !important;
}

/* 链接样式 */
:deep(a) {
  @apply text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 underline transition-colors;
}

/* 外部链接样式 - 添加小图标提示 */
:deep(a[target="_blank"]) {
  @apply relative;
}

:deep(a[target="_blank"]::after) {
  content: '↗';
  @apply ml-1 text-xs opacity-70;
}

/* 标题样式 */
:deep(h1) {
  @apply text-3xl font-bold text-gray-900 dark:text-gray-100 mt-8 mb-4 pb-2 border-b border-gray-200 dark:border-gray-700;
}

:deep(h2) {
  @apply text-2xl font-semibold text-gray-900 dark:text-gray-100 mt-6 mb-3;
}

:deep(h3) {
  @apply text-xl font-semibold text-gray-900 dark:text-gray-100 mt-5 mb-2;
}

:deep(h4) {
  @apply text-lg font-semibold text-gray-900 dark:text-gray-100 mt-4 mb-2;
}

:deep(h5) {
  @apply text-base font-semibold text-gray-900 dark:text-gray-100 mt-3 mb-2;
}

:deep(h6) {
  @apply text-sm font-semibold text-gray-900 dark:text-gray-100 mt-3 mb-2;
}

/* 列表样式 */
:deep(ul),
:deep(ol) {
  @apply my-4;
  padding-left: 1.5rem;
  margin-top: 1rem;
  margin-bottom: 1rem;
}

:deep(ul) {
  list-style-type: disc;
  list-style-position: outside;
}

:deep(ol) {
  list-style-type: decimal;
  list-style-position: outside;
}

:deep(li) {
  @apply text-gray-700 dark:text-gray-300;
  margin: 0.25rem 0;
  padding-left: 0.5rem;
  line-height: 1.75;
  display: list-item;
}

/* 确保所有列表项之间的间距完全一致 */
:deep(ul > li + li),
:deep(ol > li + li) {
  margin-top: 0.25rem;
}

/* 第一个列表项没有上边距 */
:deep(ul > li:first-child),
:deep(ol > li:first-child) {
  margin-top: 0;
}

/* 最后一个列表项没有下边距 */
:deep(ul > li:last-child),
:deep(ol > li:last-child) {
  margin-bottom: 0;
}

/* 段落样式 */
:deep(p) {
  @apply text-gray-700 dark:text-gray-300 leading-relaxed my-4;
}

/* 图片样式 */
:deep(img) {
  @apply max-w-full h-auto rounded-lg shadow-sm my-4;
}

/* 水平线样式 */
:deep(hr) {
  @apply border-gray-300 dark:border-gray-600 my-8;
}

/* 内联代码样式 */
:deep(code:not(.hljs code)) {
  @apply bg-gray-100 dark:bg-gray-800 px-1 py-0.5 rounded text-sm font-mono text-pink-600 dark:text-pink-400;
}

/* 强调样式 */
:deep(strong) {
  @apply font-semibold text-gray-900 dark:text-gray-100;
}

:deep(em) {
  @apply italic;
}

/* 删除线样式 */
:deep(del) {
  @apply line-through text-gray-500 dark:text-gray-500;
}

/* 目录样式 */
:deep(.toc) {
  @apply bg-gray-50 dark:bg-gray-800 p-4 rounded-lg my-6 border border-gray-200 dark:border-gray-700;
}

:deep(.toc-list) {
  @apply list-none p-0 m-0;
}

:deep(.toc-item) {
  @apply py-1;
}

:deep(.toc-link) {
  @apply text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 no-underline transition-colors;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.code-block-header) {
    @apply px-2 py-1;
  }
  
  :deep(.hljs) {
    @apply p-2 text-sm;
  }
  
  :deep(h1) {
    @apply text-2xl;
  }
  
  :deep(h2) {
    @apply text-xl;
  }
  
  :deep(h3) {
    @apply text-lg;
  }
}
</style>
