<template>
  <div class="markdown-content" ref="markdownContainerRef">
    <div 
      class="prose prose-lg dark:prose-invert max-w-none" 
      v-html="renderedContent"
    ></div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, nextTick } from 'vue'
import md, { resetUsedIds } from '@/utils/markdown'

const props = defineProps({
  content: {
    type: String,
    required: true
  }
})

const markdownContainerRef = ref(null)

// 处理普通锚点链接点击（文章内容中的链接）
const handleAnchorLinkClick = async (event) => {
  const link = event.target.closest('a')
  if (!link) {
    return false
  }

  const href = link.getAttribute('href')
  
  // 只处理锚点链接（以 # 开头）
  if (!href || !href.startsWith('#')) {
    return false
  }

  // 排除外部链接和 TOC 链接
  if (link.classList.contains('toc-link')) {
    return false
  }

  event.preventDefault()
  event.stopPropagation()

  const targetId = href.substring(1) // 移除 # 号
  const linkText = link.textContent.trim() // 保存链接文本，用于文本匹配

  // 等待 DOM 更新
  await nextTick()

  // 尝试查找目标元素并滚动
  const findAndScroll = (retries = 5) => {
    const container = markdownContainerRef.value
    const searchScope = container || document
    
    let targetElement = null
    
    // 方法1: 直接通过 ID 查找（支持特殊字符）
    try {
      if (CSS && CSS.escape) {
        targetElement = searchScope.querySelector(`#${CSS.escape(targetId)}`)
      } else {
        targetElement = document.getElementById(targetId)
      }
    } catch (e) {
      targetElement = document.getElementById(targetId)
    }
    
    // 方法2: 如果找不到，尝试在所有标题中查找匹配的 ID
    if (!targetElement) {
      const allHeadings = searchScope.querySelectorAll('h1, h2, h3, h4, h5, h6')
      for (const heading of allHeadings) {
        if (heading.id === targetId) {
          targetElement = heading
          break
        }
      }
    }
    
    // 方法3: 尝试解码 URL 编码的 ID
    if (!targetElement) {
      try {
        const decodedId = decodeURIComponent(targetId)
        if (CSS && CSS.escape) {
          targetElement = searchScope.querySelector(`#${CSS.escape(decodedId)}`) || document.getElementById(decodedId)
        } else {
          targetElement = document.getElementById(decodedId)
        }
      } catch (e) {
        // 忽略错误
      }
    }
    
    // 方法4: 如果还是找不到，尝试通过标题文本进行模糊匹配
    if (!targetElement) {
      const allHeadings = searchScope.querySelectorAll('h1, h2, h3, h4, h5, h6')
      for (const heading of allHeadings) {
        if (!heading.id) continue
        
        // 获取标题文本，移除可能的 HTML 标签和高亮标记
        let headingText = heading.textContent.trim()
        // 移除高亮标记 ==文本== 中的 == 符号（虽然 textContent 通常不包含，但为了保险）
        headingText = headingText.replace(/==([^=]+)==/g, '$1').trim()
        
        // 清理链接文本，移除高亮标记
        let cleanLinkText = linkText.replace(/==([^=]+)==/g, '$1').trim()
        
        // 尝试多种匹配方式：
        // 1. 精确匹配链接文本和标题文本
        // 2. 标题文本包含链接文本
        // 3. 链接文本包含标题文本
        // 4. 忽略大小写的匹配
        const linkTextLower = cleanLinkText.toLowerCase()
        const headingTextLower = headingText.toLowerCase()
        
        if (headingText === cleanLinkText || 
            headingTextLower === linkTextLower ||
            headingText.includes(cleanLinkText) || 
            cleanLinkText.includes(headingText) ||
            headingTextLower.includes(linkTextLower) ||
            linkTextLower.includes(headingTextLower)) {
          targetElement = heading
          break
        }
      }
    }
    
    // 方法5: 如果还是找不到，尝试通过 ID 的部分匹配（处理大小写问题）
    if (!targetElement) {
      const allHeadings = searchScope.querySelectorAll('h1, h2, h3, h4, h5, h6')
      const targetIdLower = targetId.toLowerCase()
      for (const heading of allHeadings) {
        if (!heading.id) continue
        if (heading.id.toLowerCase() === targetIdLower) {
          targetElement = heading
          break
        }
      }
    }
    
    if (targetElement) {
      // 计算偏移量，考虑固定头部
      const offset = 100
      const elementPosition = targetElement.getBoundingClientRect().top + window.pageYOffset
      const offsetPosition = Math.max(0, elementPosition - offset)
      
      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      })
      
      // 更新 URL hash（不触发滚动）
      history.pushState(null, '', `#${targetElement.id}`)
      return true
    } else if (retries > 0) {
      // 如果找不到，等待一下再重试（可能是 DOM 还没完全渲染）
      setTimeout(() => findAndScroll(retries - 1), 100)
      return true // 表示正在处理中
    } else {
      // 如果所有方法都失败，不进行任何操作，避免回到顶部
      return false
    }
  }
  
  return findAndScroll()
}

// 统一的事件处理函数
const handleMarkdownClick = async (event) => {
  // 先处理 TOC 链接点击
  const tocHandled = await handleTocLinkClick(event)
  if (tocHandled) {
    return // TOC 链接已处理，不再处理其他
  }
  
  // 处理普通锚点链接点击
  const anchorHandled = await handleAnchorLinkClick(event)
  if (anchorHandled) {
    return // 锚点链接已处理，不再处理其他
  }
  
  // 最后处理复制按钮点击
  handleCopyClick(event)
}

const renderedContent = computed(() => {
  if (!props.content) return ''
  // 在每次渲染前重置已使用的ID，確保每個標題都有唯一的ID
  resetUsedIds()
  
  // 预处理：将 [spacer] 替换为 HTML 空行元素（在渲染前处理，因为 Markdown 支持 HTML）
  // 有几个 [spacer] 就添加几个空行
  let processedContent = props.content.replace(/\[spacer\]/gi, () => {
    // 每个 [spacer] 替换为一个空行 div（高度为 1.5em，相当于一行半的高度，形成明显的空行效果）
    return '<div class="markdown-spacer" style="height: 1.5em; margin: 0; padding: 0;"></div>'
  })
  
  // 渲染 Markdown（HTML 会被保留）
  const rendered = md.render(processedContent)
  
  // 处理 TOC：为每个 toc-item 添加级别类名
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = rendered
  
  // 查找 TOC 容器
  const tocContainer = tempDiv.querySelector('.toc')
  if (tocContainer) {
    // 递归函数：为 TOC 项添加级别类名
    const addLevelClasses = (list, level = 1) => {
      if (!list) return
      
      const items = list.querySelectorAll(':scope > .toc-item')
      items.forEach(item => {
        // 添加级别类名
        item.classList.add(`toc-level-${level}`)
        
        // 递归处理嵌套的列表
        const nestedList = item.querySelector(':scope > .toc-list')
        if (nestedList) {
          addLevelClasses(nestedList, level + 1)
        }
      })
    }
    
    const rootList = tocContainer.querySelector(':scope > .toc-list')
    if (rootList) {
      addLevelClasses(rootList, 1)
    }
  }
  

  // 同步 TOC 链接的 href 和实际标题的 ID，并处理高亮显示
  if (tocContainer && headings.length > 0) {
    const tocLinks = tocContainer.querySelectorAll('.toc-link')
    const headingArray = Array.from(headings)
    
    tocLinks.forEach((link) => {
      // 获取链接文本（移除HTML标签和高亮标记用于匹配）
      const linkText = link.textContent.trim()
      const currentHref = link.getAttribute('href')
      
      // 尝试在标题中查找匹配的标题
      for (const heading of headingArray) {
        const headingText = heading.textContent.trim()
        const headingId = heading.id
        const headingHTML = heading.innerHTML || ''
        
        // 检查标题是否包含高亮标记（通过检查HTML中是否有<mark>标签）
        const hasHighlight = headingHTML.includes('<mark') || headingHTML.includes('markdown-highlight')
        
        // 如果文本匹配（允许一些容差），更新链接的 href
        if (headingId && (linkText === headingText || 
            linkText.includes(headingText) || 
            headingText.includes(linkText))) {
          const newHref = `#${headingId}`
          if (currentHref !== newHref) {
            link.setAttribute('href', newHref)
          }
          
          // 如果标题包含高亮标记，在目录链接中也添加高亮效果
          if (hasHighlight) {
            // 从标题HTML中提取高亮部分
            // 如果标题HTML是 <mark class="markdown-highlight">数据类型差异</mark>
            // 我们需要在目录链接中也显示高亮
            const highlightMatch = headingHTML.match(/<mark[^>]*>([^<]+)<\/mark>/)
            if (highlightMatch) {
              const highlightedText = highlightMatch[1]
              // 检查链接文本是否包含这个高亮文本
              if (linkText.includes(highlightedText)) {
                // 将链接文本中的高亮部分替换为高亮HTML
                const highlightedHTML = linkText.replace(
                  new RegExp(highlightedText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'),
                  `<mark class="markdown-highlight">${highlightedText}</mark>`
                )
                link.innerHTML = highlightedHTML
              } else {
                // 如果链接文本不包含，直接使用标题的高亮HTML部分
                link.innerHTML = `<mark class="markdown-highlight">${highlightedText}</mark>`
              }
            } else {
              // 如果标题包含高亮但格式不同，尝试从原始文本中提取
              // 检查标题的原始文本是否包含 ==文本== 格式
              const originalHeadingText = heading.textContent || heading.innerText || ''
              if (originalHeadingText && linkText === originalHeadingText) {
                // 如果链接文本和标题文本完全匹配，使用标题的HTML
                link.innerHTML = headingHTML
              }
            }
          }
          
          break // 找到匹配的标题后跳出循环
        }
      }
    })
  }
  
  return tempDiv.innerHTML
})

// 处理 TOC 链接点击事件
const handleTocLinkClick = async (event) => {
  const tocLink = event.target.closest('.toc-link')
  if (!tocLink) {
    return false
  }
  
  const href = tocLink.getAttribute('href')
  
  if (!href || !href.startsWith('#')) {
    return false
  }
  
  event.preventDefault()
  event.stopPropagation()
  
  const targetId = href.substring(1) // 移除 # 号
  
  // 保存链接文本，用于降级匹配
  const linkText = tocLink.textContent.trim()
  
  // 等待 DOM 更新
  await nextTick()
  
  // 尝试查找目标元素
  const findAndScroll = (retries = 5) => {
    // 在 markdown 容器内查找，避免找到其他地方的标题
    const container = markdownContainerRef.value
    const searchScope = container || document
    
    // 使用多种方式查找元素
    let targetElement = null
    
    // 方法1: 直接通过 ID 查找（支持特殊字符）
    try {
      if (CSS && CSS.escape) {
        targetElement = searchScope.querySelector(`#${CSS.escape(targetId)}`)
      } else {
        targetElement = document.getElementById(targetId)
      }
    } catch (e) {
      // 如果 CSS.escape 不支持，使用普通方式
      targetElement = document.getElementById(targetId)
    }
    
    // 方法2: 如果找不到，尝试在所有标题中查找匹配的
    if (!targetElement) {
      const allHeadings = searchScope.querySelectorAll('h1, h2, h3, h4, h5, h6')
      for (const heading of allHeadings) {
        if (heading.id === targetId) {
          targetElement = heading
          break
        }
      }
    }
    
    // 方法3: 尝试解码 URL 编码的 ID
    if (!targetElement) {
      try {
        const decodedId = decodeURIComponent(targetId)
        if (CSS && CSS.escape) {
          targetElement = searchScope.querySelector(`#${CSS.escape(decodedId)}`) || document.getElementById(decodedId)
        } else {
          targetElement = document.getElementById(decodedId)
        }
      } catch (e) {
        // 忽略错误
      }
    }
    
    if (targetElement) {
      // 计算偏移量，考虑固定头部
      const offset = 100
      const elementPosition = targetElement.getBoundingClientRect().top + window.pageYOffset
      const offsetPosition = Math.max(0, elementPosition - offset)
      
      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      })
      
      // 更新 URL hash（不触发滚动）
      history.pushState(null, '', href)
      return true // 表示已成功处理
    } else if (retries > 0) {
      // 如果找不到，等待一下再重试（可能是 DOM 还没完全渲染）
      setTimeout(() => findAndScroll(retries - 1), 100)
    } else {
      // 如果还是找不到，尝试降级方案
      
      // 尝试使用原生方式，但先阻止默认行为
      // 不直接设置 window.location.hash，因为这可能导致页面跳转
      // 而是尝试通过 URL 解码等方式查找
      try {
        // 尝试查找所有可能的匹配
        const allHeadings = searchScope.querySelectorAll('h1, h2, h3, h4, h5, h6')
        let foundHeading = null
        
        // 尝试模糊匹配：查找包含目标文本的标题
        for (const heading of allHeadings) {
          const headingText = heading.textContent.trim()
          // 如果 TOC 链接的文本与标题文本匹配，就使用这个标题
          if (headingText === linkText || headingText.includes(linkText) || linkText.includes(headingText)) {
            foundHeading = heading
            break
          }
        }
        
        if (foundHeading) {
          const offset = 100
          const elementPosition = foundHeading.getBoundingClientRect().top + window.pageYOffset
          const offsetPosition = Math.max(0, elementPosition - offset)
          window.scrollTo({
            top: offsetPosition,
            behavior: 'smooth'
          })
          history.pushState(null, '', `#${foundHeading.id}`)
          return true
        }
      } catch (e) {
        // 降级方案失败，静默处理
      }
      
      // 如果所有方法都失败，不进行任何操作，避免回到顶部
      return false
    }
  }
  
  findAndScroll()
  return true // 表示已处理（无论成功与否）
}

// 处理复制按钮点击事件
const handleCopyClick = async (event) => {
  const copyBtn = event.target.closest('.copy-btn')
  if (!copyBtn) return
  
  const encodedContent = copyBtn.getAttribute('data-code')
  if (!encodedContent) return
  
  try {
    // 解码 base64 编码的代码内容
    const decodedContent = decodeURIComponent(escape(atob(encodedContent)))
    await navigator.clipboard.writeText(decodedContent)
    
    // 显示复制成功提示
    const originalText = copyBtn.textContent
    copyBtn.textContent = '已复制'
    copyBtn.style.opacity = '0.7'
    
    setTimeout(() => {
      copyBtn.textContent = originalText
      copyBtn.style.opacity = '1'
    }, 2000)
  } catch (error) {
    // 降级方案：使用 execCommand
    try {
      const textarea = document.createElement('textarea')
      const decodedContent = decodeURIComponent(escape(atob(encodedContent)))
      textarea.value = decodedContent
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      
      const originalText = copyBtn.textContent
      copyBtn.textContent = '已复制'
      copyBtn.style.opacity = '0.7'
      setTimeout(() => {
        copyBtn.textContent = originalText
        copyBtn.style.opacity = '1'
      }, 2000)
    } catch (fallbackError) {
      // 降级方案也失败，静默处理
    }
  }
}

// 处理高亮标题，确保它们出现在目录中
const processHighlightedHeadings = () => {
  if (!markdownContainerRef.value) return
  
  const container = markdownContainerRef.value
  const tocContainer = container.querySelector('.toc')
  const headings = container.querySelectorAll('h1, h2, h3, h4, h5, h6')
  
  if (!tocContainer || headings.length === 0) return
  
  const tocLinks = tocContainer.querySelectorAll('.toc-link')
  const tocLinkTexts = Array.from(tocLinks).map(link => link.textContent.trim())
  
  // 找出所有包含高亮标记的标题
  const highlightedHeadings = Array.from(headings).filter(heading => {
    const html = heading.innerHTML || ''
    return html.includes('<mark') || html.includes('markdown-highlight')
  })
  
  // 检查每个高亮标题是否在目录中，如果不在则手动添加
  highlightedHeadings.forEach(heading => {
    const headingText = heading.textContent.trim()
    const headingId = heading.id
    const headingHTML = heading.innerHTML || ''
    
    // 检查是否已经在目录中
    const isInToc = tocLinkTexts.some(linkText => {
      const cleanLinkText = linkText.replace(/==([^=]+)==/g, '$1').trim()
      return cleanLinkText === headingText || linkText === headingText
    })
    
    if (!isInToc && headingId) {
      const tocList = tocContainer.querySelector('.toc-list')
      if (tocList) {
        // 创建新的目录项
        const tocItem = document.createElement('li')
        tocItem.className = 'toc-item'
        
        // 确定级别
        const level = parseInt(heading.tagName.substring(1))
        tocItem.classList.add(`toc-level-${level}`)
        
        // 创建链接
        const link = document.createElement('a')
        link.className = 'toc-link'
        link.href = `#${headingId}`
        link.innerHTML = headingHTML
        
        tocItem.appendChild(link)
        
        // 找到合适的位置插入
        const allHeadings = Array.from(headings)
        const currentHeadingIndex = allHeadings.indexOf(heading)
        
        if (currentHeadingIndex >= 0) {
          const nextHeading = currentHeadingIndex < allHeadings.length - 1 ? allHeadings[currentHeadingIndex + 1] : null
          const existingTocItems = Array.from(tocList.querySelectorAll('.toc-item'))
          let inserted = false
          
          // 尝试找到下一个标题的位置
          if (nextHeading && nextHeading.id) {
            for (const item of existingTocItems) {
              const itemLink = item.querySelector('.toc-link')
              if (itemLink) {
                const itemHref = itemLink.getAttribute('href')
                if (itemHref === `#${nextHeading.id}` || itemHref === nextHeading.id) {
                  tocList.insertBefore(tocItem, item)
                  inserted = true
                  break
                }
              }
            }
          }
          
          // 如果没找到，添加到末尾
          if (!inserted) {
            tocList.appendChild(tocItem)
          }
        } else {
          tocList.appendChild(tocItem)
        }
      }
    }
  })
}

onMounted(() => {
  // 使用 nextTick 确保 DOM 已经渲染完成
  nextTick(() => {
    // 使用事件委托处理复制按钮和 TOC 链接点击，只在 markdown 容器内监听
    if (markdownContainerRef.value) {
      markdownContainerRef.value.addEventListener('click', handleMarkdownClick)
    }
    
    // 处理高亮标题
    setTimeout(() => {
      processHighlightedHeadings()
    }, 100)
  })
  
  // 也监听全局的 hashchange 事件，处理直接点击链接的情况
  window.addEventListener('hashchange', handleHashChange)
})

// 监听内容变化，重新处理高亮标题
watch(() => props.content, () => {
  nextTick(() => {
    setTimeout(() => {
      processHighlightedHeadings()
    }, 100)
  })
})

// 处理 hashchange 事件（当用户直接点击链接时）
const handleHashChange = () => {
  const hash = window.location.hash
  if (!hash) return
  
  const targetId = hash.substring(1)
  nextTick(() => {
    const targetElement = document.getElementById(targetId)
    if (targetElement) {
      const offset = 100
      const elementPosition = targetElement.getBoundingClientRect().top + window.pageYOffset
      const offsetPosition = Math.max(0, elementPosition - offset)
      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      })
    }
  })
}

onUnmounted(() => {
  if (markdownContainerRef.value) {
    markdownContainerRef.value.removeEventListener('click', handleMarkdownClick)
  }
  window.removeEventListener('hashchange', handleHashChange)
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
  @apply font-bold text-gray-900 dark:text-gray-100 mt-8 mb-4 pb-2 border-b border-gray-200 dark:border-gray-700;
  font-size: 2.25rem; /* 36px - 最大标题 */
  line-height: 1.2;
}

:deep(h2) {
  @apply font-semibold text-gray-900 dark:text-gray-100 mt-6 mb-3;
  font-size: 1.875rem; /* 30px - 二级标题 */
  line-height: 1.3;
}

:deep(h3) {
  @apply font-semibold text-gray-900 dark:text-gray-100 mt-5 mb-2;
  font-size: 1.65rem; /* 24px - 三级标题 */
  line-height: 1.4;
}

:deep(h4) {
  @apply font-semibold text-gray-900 dark:text-gray-100 mt-4 mb-2;
  font-size: 1.35rem; /* 20px - 四级标题，明显大于加粗字体(16px) */
  line-height: 1.5;
}

:deep(h5) {
  @apply font-semibold text-gray-900 dark:text-gray-100 mt-3 mb-2;
  font-size: 1.125rem; /* 18px */
  line-height: 1.5;
}

:deep(h6) {
  @apply font-semibold text-gray-900 dark:text-gray-100 mt-3 mb-2;
  font-size: 1rem; /* 16px */
  line-height: 1.5;
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

/* 高亮样式 */
:deep(mark.markdown-highlight) {
  @apply bg-[#ffff00] text-gray-900 dark:text-gray-100 px-1 rounded;
  font-weight: 500;
}

/* 删除线样式 */
:deep(del) {
  @apply line-through text-gray-500 dark:text-gray-500;
}

/* 目录样式 - 参考 Typora 样式，增强不同级别的区分度 */
:deep(.toc) {
  @apply bg-gray-50 dark:bg-gray-800 p-4 rounded-lg my-6 border border-gray-200 dark:border-gray-700;
}

:deep(.toc-list) {
  @apply list-none p-0 m-0;
}

:deep(.toc-item) {
  @apply py-1;
  line-height: 1.6;
}

/* 不同级别的标题样式 - 大幅增加缩进和字体区分度 */
:deep(.toc-item.toc-level-1) {
  padding-left: 10;
  font-size: 1.125rem; /* 18px - 更大的一级标题 */
  font-weight: 700; /* 更粗 */
}

:deep(.toc-item.toc-level-2) {
  padding-left: 2.5rem; /* 40px - 大幅增加 */
  font-size: 1rem; /* 16px */
  font-weight: 600;
}

:deep(.toc-item.toc-level-3) {
  padding-left: 2rem; /* 80px - 大幅增加 */
  font-size: 1rem; /* 16px */
  font-weight: 500;
}

:deep(.toc-item.toc-level-4) {
  padding-left: 2rem; /* 120px - 大幅增加 */
  font-size:1rem; /* 16px */
  font-weight: 500;
}

:deep(.toc-item.toc-level-5) {
  padding-left: 2rem; /* 160px - 大幅增加 */
  font-size: 0.875rem; /* 14px */
  font-weight: 400;
}

:deep(.toc-item.toc-level-6) {
  padding-left: 12.5rem; /* 200px - 大幅增加 */
  font-size: 0.8125rem; /* 13px */
  font-weight: 400;
}

:deep(.toc-link) {
  @apply text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 no-underline transition-colors cursor-pointer;
  display: inline-block;
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
    font-size: 1.875rem; /* 30px */
  }
  
  :deep(h2) {
    font-size: 1.5rem; /* 24px */
  }
  
  :deep(h3) {
    font-size: 1.25rem; /* 20px */
  }
  
  :deep(h4) {
    font-size: 1.125rem; /* 18px */
  }
}
</style>
