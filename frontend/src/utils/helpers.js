import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { getFileURL } from '@/utils/apiConfig'

// 设置dayjs中文语言
dayjs.locale('zh-cn')

/**
 * 格式化日期
 * @param {string|Date} date - 日期
 * @param {string} format - 格式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
    if (!date) return ''
    return dayjs(date).format(format)
}

/**
 * 格式化相对时间
 * @param {string|Date} date - 日期
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(date) {
    if (!date) return ''

    const now = dayjs()
    const target = dayjs(date)
    const diff = now.diff(target, 'second')

    if (diff < 60) return '刚刚'
    if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
    if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
    if (diff < 604800) return `${Math.floor(diff / 86400)}天前`

    return target.format('YYYY-MM-DD')
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的文件大小
 */
export function formatFileSize(bytes) {
    if (bytes === 0) return '0 B'

    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化数字
 * @param {number} num - 数字
 * @returns {string} 格式化后的数字
 */
export function formatNumber(num) {
    if (num < 1000) return num.toString()
    if (num < 1000000) return (num / 1000).toFixed(1) + 'K'
    if (num < 1000000000) return (num / 1000000).toFixed(1) + 'M'
    return (num / 1000000000).toFixed(1) + 'B'
}

/**
 * 生成随机字符串
 * @param {number} length - 长度
 * @returns {string} 随机字符串
 */
export function generateRandomString(length = 8) {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    let result = ''
    for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    return result
}

/**
 * 防抖函数
 * @param {Function} func - 要防抖的函数
 * @param {number} wait - 等待时间
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait) {
    let timeout
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout)
            func(...args)
        }
        clearTimeout(timeout)
        timeout = setTimeout(later, wait)
    }
}

/**
 * 节流函数
 * @param {Function} func - 要节流的函数
 * @param {number} limit - 时间间隔
 * @returns {Function} 节流后的函数
 */
export function throttle(func, limit) {
    let inThrottle
    return function (...args) {
        if (!inThrottle) {
            func.apply(this, args)
            inThrottle = true
            setTimeout(() => inThrottle = false, limit)
        }
    }
}

/**
 * 深拷贝对象
 * @param {any} obj - 要拷贝的对象
 * @returns {any} 拷贝后的对象
 */
export function deepClone(obj) {
    if (obj === null || typeof obj !== 'object') return obj
    if (obj instanceof Date) return new Date(obj.getTime())
    if (obj instanceof Array) return obj.map(item => deepClone(item))
    if (typeof obj === 'object') {
        const clonedObj = {}
        for (const key in obj) {
            if (obj.hasOwnProperty(key)) {
                clonedObj[key] = deepClone(obj[key])
            }
        }
        return clonedObj
    }
}

/**
 * 获取URL参数
 * @param {string} name - 参数名
 * @param {string} url - URL，默认为当前页面URL
 * @returns {string|null} 参数值
 */
export function getUrlParam(name, url = window.location.href) {
    const urlObj = new URL(url)
    return urlObj.searchParams.get(name)
}

/**
 * 设置URL参数
 * @param {string} name - 参数名
 * @param {string} value - 参数值
 * @param {string} url - URL，默认为当前页面URL
 * @returns {string} 新的URL
 */
export function setUrlParam(name, value, url = window.location.href) {
    const urlObj = new URL(url)
    urlObj.searchParams.set(name, value)
    return urlObj.toString()
}

/**
 * 删除URL参数
 * @param {string} name - 参数名
 * @param {string} url - URL，默认为当前页面URL
 * @returns {string} 新的URL
 */
export function removeUrlParam(name, url = window.location.href) {
    const urlObj = new URL(url)
    urlObj.searchParams.delete(name)
    return urlObj.toString()
}

/**
 * 验证邮箱格式
 * @param {string} email - 邮箱地址
 * @returns {boolean} 是否为有效邮箱
 */
export function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(email)
}

/**
 * 验证手机号格式
 * @param {string} phone - 手机号
 * @returns {boolean} 是否为有效手机号
 */
export function isValidPhone(phone) {
    const phoneRegex = /^1[3-9]\d{9}$/
    return phoneRegex.test(phone)
}

/**
 * 验证URL格式
 * @param {string} url - URL地址
 * @returns {boolean} 是否为有效URL
 */
export function isValidUrl(url) {
    try {
        new URL(url)
        return true
    } catch {
        return false
    }
}

/**
 * 截取字符串
 * @param {string} str - 原字符串
 * @param {number} length - 截取长度
 * @param {string} suffix - 后缀，默认为 '...'
 * @returns {string} 截取后的字符串
 */
export function truncateString(str, length, suffix = '...') {
    if (!str || str.length <= length) return str
    return str.substring(0, length) + suffix
}

/**
 * 高亮搜索关键词
 * @param {string} text - 原文本
 * @param {string} keyword - 关键词
 * @param {string} className - 高亮样式类名
 * @returns {string} 高亮后的HTML
 */
export function highlightKeyword(text, keyword, className = 'highlight') {
    if (!keyword) return text
    const regex = new RegExp(`(${keyword})`, 'gi')
    return text.replace(regex, `<span class="${className}">$1</span>`)
}

/**
 * 生成颜色
 * @param {string} str - 字符串
 * @returns {string} 颜色值
 */
export function generateColor(str) {
    let hash = 0
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash)
    }
    const color = Math.abs(hash).toString(16).substring(0, 6)
    return `#${color.padEnd(6, '0')}`
}

/**
 * 获取文件扩展名
 * @param {string} filename - 文件名
 * @returns {string} 扩展名
 */
export function getFileExtension(filename) {
    return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2)
}

/**
 * 检查是否为图片文件
 * @param {string} filename - 文件名
 * @returns {boolean} 是否为图片
 */
export function isImageFile(filename) {
    const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']
    const ext = getFileExtension(filename).toLowerCase()
    return imageExtensions.includes(ext)
}

/**
 * 检查是否为视频文件
 * @param {string} filename - 文件名
 * @returns {boolean} 是否为视频
 */
export function isVideoFile(filename) {
    const videoExtensions = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm', 'mkv']
    const ext = getFileExtension(filename).toLowerCase()
    return videoExtensions.includes(ext)
}

/**
 * 检查是否为音频文件
 * @param {string} filename - 文件名
 * @returns {boolean} 是否为音频
 */
export function isAudioFile(filename) {
    const audioExtensions = ['mp3', 'wav', 'flac', 'aac', 'ogg', 'm4a']
    const ext = getFileExtension(filename).toLowerCase()
    return audioExtensions.includes(ext)
}

/**
 * 计算阅读时间
 * @param {string} content - 内容
 * @param {number} wordsPerMinute - 每分钟阅读字数，默认为200
 * @returns {number} 阅读时间（分钟）
 */
export function calculateReadingTime(content, wordsPerMinute = 200) {
    if (!content) return 0
    const wordCount = content.length
    return Math.ceil(wordCount / wordsPerMinute)
}

/**
 * 生成文章摘要
 * @param {string} content - 内容
 * @param {number} length - 摘要长度，默认为150
 * @returns {string} 摘要
 */
export function generateSummary(content, length = 150) {
    if (!content) return ''

    // 移除Markdown标记
    const plainText = content
        .replace(/#{1,6}\s+/g, '') // 移除标题标记
        .replace(/\*\*(.*?)\*\*/g, '$1') // 移除粗体标记
        .replace(/\*(.*?)\*/g, '$1') // 移除斜体标记
        .replace(/`(.*?)`/g, '$1') // 移除行内代码标记
        .replace(/```[\s\S]*?```/g, '') // 移除代码块
        .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1') // 移除链接，保留文本
        .replace(/!\[([^\]]*)\]\([^)]+\)/g, '') // 移除图片
        .replace(/\n+/g, ' ') // 替换换行为空格
        .trim()

    return truncateString(plainText, length)
}

/**
 * 生成文章目录
 * @param {string} content - Markdown内容
 * @returns {Array} 目录数组
 */
export function generateTableOfContents(content) {
    if (!content) return []

    const headings = []
    const lines = content.split('\n')

    lines.forEach((line, index) => {
        const match = line.match(/^(#{1,6})\s+(.+)$/)
        if (match) {
            const level = match[1].length
            const text = match[2].trim()
            const id = `heading-${index}`

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

/**
 * 滚动到指定元素
 * @param {string|HTMLElement} element - 元素选择器或元素
 * @param {number} offset - 偏移量
 * @param {number} duration - 动画持续时间
 */
export function scrollToElement(element, offset = 0, duration = 500) {
    const target = typeof element === 'string' ? document.querySelector(element) : element
    if (!target) return

    const targetPosition = target.offsetTop - offset
    const startPosition = window.pageYOffset
    const distance = targetPosition - startPosition
    let startTime = null

    function animation(currentTime) {
        if (startTime === null) startTime = currentTime
        const timeElapsed = currentTime - startTime
        const run = ease(timeElapsed, startPosition, distance, duration)
        window.scrollTo(0, run)
        if (timeElapsed < duration) requestAnimationFrame(animation)
    }

    function ease(t, b, c, d) {
        t /= d / 2
        if (t < 1) return c / 2 * t * t + b
        t--
        return -c / 2 * (t * (t - 2) - 1) + b
    }

    requestAnimationFrame(animation)
}

/**
 * 复制文本到剪贴板
 * @param {string} text - 要复制的文本
 * @returns {Promise<boolean>} 是否复制成功
 */
export async function copyToClipboard(text) {
    try {
        if (navigator.clipboard && window.isSecureContext) {
            await navigator.clipboard.writeText(text)
            return true
        } else {
            // 降级方案
            const textArea = document.createElement('textarea')
            textArea.value = text
            textArea.style.position = 'fixed'
            textArea.style.left = '-999999px'
            textArea.style.top = '-999999px'
            document.body.appendChild(textArea)
            textArea.focus()
            textArea.select()
            const result = document.execCommand('copy')
            document.body.removeChild(textArea)
            return result
        }
    } catch (error) {
        // console.error('复制失败:', error)
        return false
    }
}

/**
 * 下载文件
 * @param {string} url - 文件URL
 * @param {string} filename - 文件名
 */
export function downloadFile(url, filename) {
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
}

/**
 * 获取设备类型
 * @returns {string} 设备类型
 */
export function getDeviceType() {
    const userAgent = navigator.userAgent.toLowerCase()

    if (/mobile|android|iphone|ipad|phone/i.test(userAgent)) {
        return 'mobile'
    } else if (/tablet|ipad/i.test(userAgent)) {
        return 'tablet'
    } else {
        return 'desktop'
    }
}

/**
 * 检查是否为移动设备
 * @returns {boolean} 是否为移动设备
 */
export function isMobile() {
    return getDeviceType() === 'mobile'
}

/**
 * 检查是否为平板设备
 * @returns {boolean} 是否为平板设备
 */
export function isTablet() {
    return getDeviceType() === 'tablet'
}

/**
 * 检查是否为桌面设备
 * @returns {boolean} 是否为桌面设备
 */
export function isDesktop() {
    return getDeviceType() === 'desktop'
}

/**
 * 获取浏览器信息
 * @returns {object} 浏览器信息
 */
export function getBrowserInfo() {
    const userAgent = navigator.userAgent
    const browsers = {
        chrome: /chrome/i.test(userAgent),
        firefox: /firefox/i.test(userAgent),
        safari: /safari/i.test(userAgent) && !/chrome/i.test(userAgent),
        edge: /edge/i.test(userAgent),
        ie: /msie|trident/i.test(userAgent)
    }

    const browser = Object.keys(browsers).find(key => browsers[key]) || 'unknown'

    return {
        browser,
        userAgent,
        isChrome: browsers.chrome,
        isFirefox: browsers.firefox,
        isSafari: browsers.safari,
        isEdge: browsers.edge,
        isIE: browsers.ie
    }
}

/**
 * 检查是否支持WebP格式
 * @returns {Promise<boolean>} 是否支持WebP
 */
export function supportsWebP() {
    return new Promise((resolve) => {
        const webP = new Image()
        webP.onload = webP.onerror = () => {
            resolve(webP.height === 2)
        }
        webP.src = 'data:image/webp;base64,UklGRjoAAABXRUJQVlA4IC4AAACyAgCdASoCAAIALmk0mk0iIiIiIgBoSygABc6WWgAA/veff/0PP8bA//LwYAAA'
    })
}

/**
 * 检查是否支持WebP格式（同步版本）
 * @returns {boolean} 是否支持WebP
 */
export function supportsWebPSync() {
    const canvas = document.createElement('canvas')
    canvas.width = 1
    canvas.height = 1
    return canvas.toDataURL('image/webp').indexOf('data:image/webp') === 0
}

/**
 * 获取头像URL
 * @param {string} avatar - 头像路径或URL
 * @param {string} defaultAvatar - 默认头像路径，默认为null
 * @returns {string} 处理后的头像URL
 */
export function getAvatarUrl(avatar, defaultAvatar = null) {
    // 如果avatar为空、null或undefined，返回默认值
    if (!avatar || avatar === 'null' || avatar === 'undefined') {
        return defaultAvatar
    }
    
    // 转换为字符串并去除首尾空格
    let avatarStr = String(avatar).trim()
    
    // 如果去除空格后为空，返回默认值
    if (avatarStr === '') {
        return defaultAvatar
    }
    
    // 如果已经是完整的URL（以http://或https://开头），直接返回
    // 注意：这里要严格匹配，避免误判
    if (avatarStr.match(/^https?:\/\//i)) {
        return avatarStr
    }
    
    // 如果是data:开头的base64图片，直接返回
    if (avatarStr.startsWith('data:')) {
        return avatarStr
    }
    
    // 如果是相对路径（以/开头），使用統一的文件 URL 構建函數
    if (avatarStr.startsWith('/')) {
        return getFileURL(avatarStr)
    }
    
    // 如果包含文件扩展名（.jpg, .png, .gif等），认为是文件路径，使用統一的文件 URL 構建函數
    const imageExtensions = /\.(jpg|jpeg|png|gif|bmp|webp|svg)(\?.*)?$/i
    if (imageExtensions.test(avatarStr)) {
        const cleanAvatar = avatarStr.startsWith('/') ? avatarStr : `/${avatarStr}`
        return getFileURL(cleanAvatar)
    }
    
    // 如果包含常见路径模式（如 uploads/、avatar/ 等），使用統一的文件 URL 構建函數
    if (avatarStr.includes('/') || avatarStr.includes('upload') || avatarStr.includes('avatar')) {
        const cleanAvatar = avatarStr.startsWith('/') ? avatarStr : `/${avatarStr}`
        return getFileURL(cleanAvatar)
    }
    
    // 如果是纯文件名或标识符（不包含路径分隔符和扩展名）
    // 这种情况通常表示后端返回的是文件名，需要拼接上传目录路径
    if (avatarStr.length > 0 && !avatarStr.includes('.') && !avatarStr.includes('/')) {
        const uploadPath = `/uploads/${avatarStr}`
        return getFileURL(uploadPath)
    }
    
    // 其他情况（可能是无效的标识符或无法识别的格式），返回默认值
    return defaultAvatar
}