import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import anchor from 'markdown-it-anchor'
import toc from 'markdown-it-toc-done-right'

// 导入代码高亮样式
import 'highlight.js/styles/github.css'
import 'highlight.js/styles/github-dark.css'

// 配置代码高亮
hljs.configure({
    languages: ['javascript', 'typescript', 'vue', 'html', 'css', 'scss', 'json', 'bash', 'python', 'java', 'cpp', 'c', 'sql', 'markdown', 'yaml', 'xml']
})

const md = new MarkdownIt({
    html: true,
    linkify: true,
    typographer: true,
    breaks: true,
    highlight: function (str, lang) {
        if (lang && hljs.getLanguage(lang)) {
            try {
                return '<pre class="hljs"><code>' +
                    hljs.highlight(str, { language: lang }).value +
                    '</code></pre>'
            } catch (__) { }
        }

        // 如果没有指定语言，尝试自动检测
        try {
            const detected = hljs.highlightAuto(str)
            if (detected.language) {
                return '<pre class="hljs"><code>' + detected.value + '</code></pre>'
            }
        } catch (__) { }

        // 如果都不行，返回转义的代码
        return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
    }
})

// 用于追踪已使用的ID，确保每个标题都有唯一的ID
const usedIds = new Map()

// 生成ID的函数，支持中文字符，并为重复的标题添加序号后缀
const generateId = (text, isUnique = false) => {
    if (!text) {
        return ''
    }

    // 移除HTML标签
    let cleanText = text.replace(/<[^>]*>/g, '')
    
    // 移除高亮标记 ==文本== 中的 == 符号
    // 匹配 ==文本== 格式，提取中间的文本
    cleanText = cleanText.replace(/==([^=]+)==/g, '$1')

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

// 重置已使用的ID（在每次渲染新内容时调用）
const resetUsedIds = () => {
    usedIds.clear()
}

// 添加锚点插件
md.use(anchor, {
    permalink: false, // 不显示锚点链接，只添加ID
    level: [1, 2, 3, 4, 5, 6],
    slugify: (text) => {
        // anchor 插件传入的 text 可能包含原始格式，需要处理
        return generateId(text, true)
    }, // 使用唯一ID生成函数，确保与 TOC 一致
    // 确保插件正确工作
    tabIndex: false,
    uniqueSlugStartIndex: 1 // 确保重复标题有唯一ID
})

// 保存原始的 heading_open 渲染规则
const defaultHeadingOpen = md.renderer.rules.heading_open || function(tokens, idx, options, env, renderer) {
    return renderer.renderToken(tokens, idx, options)
}

// 自定义 heading_open 渲染规则，确保所有标题都有ID
md.renderer.rules.heading_open = function(tokens, idx, options, env, renderer) {
    const token = tokens[idx]
    
    // 检查是否已有ID
    if (!token.attrGet('id')) {
        // 查找对应的 inline token（包含标题文本）
        const inlineToken = tokens[idx + 1]
        if (inlineToken && inlineToken.type === 'inline') {
            // 提取纯文本（处理 highlight token）
            let text = ''
            if (inlineToken.children) {
                inlineToken.children.forEach(child => {
                    if (child.type === 'text') {
                        text += child.content
                    } else if (child.type === 'highlight') {
                        text += child.content
                    } else if (child.content) {
                        text += child.content
                    }
                })
            } else {
                text = inlineToken.content || ''
            }
            
            // 移除高亮标记
            text = text.replace(/==([^=]+)==/g, '$1').trim()
            
            // 如果文本不为空，生成ID并添加
            if (text) {
                const id = generateId(text, true)
                if (id) {
                    token.attrSet('id', id)
                }
            }
        }
    }
    
    // 调用原始渲染规则
    return defaultHeadingOpen(tokens, idx, options, env, renderer)
}

// 添加自定义 core ruler，确保包含高亮标记的标题能被正确提取
// 这个 ruler 在渲染之前运行，确保所有标题（包括高亮标题）都被处理
md.core.ruler.push('ensure-highlight-headings', function(state) {
    const tokens = state.tokens
    
    // 遍历所有 tokens，查找包含高亮标记的标题
    for (let i = 0; i < tokens.length; i++) {
        const token = tokens[i]
        
        if (token.type === 'heading_open') {
            // 查找对应的 inline token（包含标题文本）
            const inlineToken = tokens[i + 1]
            if (inlineToken && inlineToken.type === 'inline') {
                // 检查 inline token 的子 tokens 中是否有 highlight token
                let hasHighlight = false
                if (inlineToken.children) {
                    for (const child of inlineToken.children) {
                        if (child.type === 'highlight') {
                            hasHighlight = true
                            break
                        }
                    }
                }
                
                // 如果包含高亮标记，确保标题能被正确识别
                // markdown-it-toc-done-right 应该能处理，但我们可以添加标记
                if (hasHighlight) {
                    // 在 token 上添加标记，表示这是一个高亮标题
                    token.meta = token.meta || {}
                    token.meta.hasHighlight = true
                }
            }
        }
    }
    
    return true
})

// 保存标题信息的映射，用于后续处理
const headingInfoMap = new Map()

// 添加目录插件
md.use(toc, {
    containerClass: 'toc',
    containerId: 'toc',
    listClass: 'toc-list',
    itemClass: 'toc-item',
    linkClass: 'toc-link',
    level: [1, 2, 3, 4, 5, 6],
    listType: 'ul',
    slugify: (text) => {
        // 对于目录，也需要移除高亮标记来生成ID，但保留原始文本用于显示
        const cleanText = text.replace(/==([^=]+)==/g, '$1').trim()
        return generateId(cleanText, true)
    }, // 使用与 anchor 相同的 slugify 函数，确保 ID 匹配
    format: function (heading, opts) {
        // 保留原始标题文本（包括高亮标记），用于在目录中显示
        return heading
    }
})

// 注意：不再需要自定义 heading_open 规则
// anchor 插件会自动为标题添加 ID，使用相同的 slugify 函数确保 ID 匹配

// 添加自定义渲染规则
md.renderer.rules.table_open = function () {
    return '<div class="table-wrapper"><table class="table-auto w-full border-collapse border border-gray-300 dark:border-gray-600">'
}

md.renderer.rules.table_close = function () {
    return '</table></div>'
}

md.renderer.rules.thead_open = function () {
    return '<thead class="bg-gray-50 dark:bg-gray-800">'
}

md.renderer.rules.th_open = function () {
    return '<th class="border border-gray-300 dark:border-gray-600 px-4 py-2 text-left font-semibold text-gray-900 dark:text-gray-100">'
}

md.renderer.rules.td_open = function () {
    return '<td class="border border-gray-300 dark:border-gray-600 px-4 py-2 text-gray-700 dark:text-gray-300">'
}

md.renderer.rules.blockquote_open = function () {
    return '<blockquote class="border-l-4 border-blue-500 pl-4 text-gray-600 dark:text-gray-400 my-4" style="font-style: normal !important;">'
}

md.renderer.rules.blockquote_close = function () {
    return '</blockquote>'
}

md.renderer.rules.code_inline = function (tokens, idx, options, env, renderer) {
    const token = tokens[idx]
    return '<code class="bg-gray-100 dark:bg-gray-800 px-1 py-0.5 rounded text-sm font-mono text-pink-600 dark:text-pink-400">' +
        md.utils.escapeHtml(token.content) + '</code>'
}

md.renderer.rules.fence = function (tokens, idx, options, env, renderer) {
    const token = tokens[idx]
    const info = token.info ? md.utils.unescapeAll(token.info).trim() : ''
    const langName = info ? info.split(/\s+/g)[0] : ''

    let highlighted
    if (langName && hljs.getLanguage(langName)) {
        try {
            highlighted = hljs.highlight(token.content, { language: langName }).value
        } catch (__) {
            highlighted = md.utils.escapeHtml(token.content)
        }
    } else {
        try {
            highlighted = hljs.highlightAuto(token.content).value
        } catch (__) {
            highlighted = md.utils.escapeHtml(token.content)
        }
    }

    // 使用 base64 编码存储代码内容，避免特殊字符问题
    // 先使用 encodeURIComponent 处理 Unicode 字符，再 base64 编码
    const encodedContent = btoa(unescape(encodeURIComponent(token.content)))
    
    return `<div class="code-block-wrapper">
    <div class="code-block-header">
      <span class="code-block-lang">${langName || 'text'}</span>
      <button class="copy-btn" data-code="${encodedContent}">复制</button>
    </div>
    <pre class="hljs"><code>${highlighted}</code></pre>
  </div>`
}

// 添加自定义链接渲染规则，让所有链接在新标签页中打开
md.renderer.rules.link_open = function (tokens, idx, options, env, renderer) {
    const token = tokens[idx]
    const hrefIndex = token.attrIndex('href')
    const href = hrefIndex >= 0 ? token.attrs[hrefIndex][1] : ''

    // 检查是否是外部链接
    const isExternal = href.startsWith('http://') || href.startsWith('https://') || href.startsWith('//')

    if (isExternal) {
        // 外部链接在新标签页中打开
        token.attrSet('target', '_blank')
        token.attrSet('rel', 'noopener noreferrer')
    } else if (href && href.startsWith('#')) {
        // 处理锚点链接：如果链接目标格式是 ### 标题 或 ##### 标题，需要转换为正确的 ID
        // 例如：### ssh 克隆 -> #ssh-克隆
        let targetId = href.substring(1) // 移除开头的 #
        
        // 检查是否包含多个 # 符号（表示是标题格式）
        if (targetId.startsWith('#')) {
            // 提取标题文本（移除所有开头的 # 和空格）
            const titleText = targetId.replace(/^#+\s*/, '').trim()
            // 使用 generateId 函数生成正确的 ID
            // 注意：使用 isUnique = false，因为链接转换不应该影响标题ID的唯一性检查
            // 如果标题有重复，标题会自己处理唯一性；链接会通过文本匹配找到正确的标题
            targetId = generateId(titleText, false)
            // 更新 href 属性
            token.attrSet('href', `#${targetId}`)
        }
    }

    return renderer.renderToken(tokens, idx, options)
}

// 添加高亮语法支持：==文字== 会被高亮显示
// 使用 markdown-it 的自定义 inline 规则来实现
md.inline.ruler.before('emphasis', 'highlight', function(state, silent) {
    const start = state.pos
    const max = state.posMax
    
    // 检查是否以 == 开头
    if (state.src.charCodeAt(start) !== 0x3D/* = */ || 
        state.src.charCodeAt(start + 1) !== 0x3D/* = */) {
        return false
    }
    
    // 查找结束的 ==
    let pos = start + 2
    let found = false
    
    while (pos < max - 1) {
        if (state.src.charCodeAt(pos) === 0x3D/* = */ && 
            state.src.charCodeAt(pos + 1) === 0x3D/* = */) {
            found = true
            break
        }
        pos++
    }
    
    if (!found || pos === start + 2) {
        return false
    }
    
    const content = state.src.slice(start + 2, pos)
    
    if (!silent) {
        const token = state.push('highlight', 'mark', 0)
        token.content = content
        token.markup = '=='
    }
    
    state.pos = pos + 2
    return true
})

// 添加高亮渲染规则
md.renderer.rules.highlight = function(tokens, idx, options, env, renderer) {
    const token = tokens[idx]
    return `<mark class="markdown-highlight">${md.utils.escapeHtml(token.content)}</mark>`
}

// 導出重置函數和 ID 生成函數，用於在每次渲染新內容時重置已使用的ID
export { resetUsedIds, generateId }

export default md
