/**
 * Markdown图片处理器
 * 用于处理Markdown文档中的图片，自动上传到服务器并替换链接
 */

import { adminApi } from '@/api/admin'
import { ElMessage } from 'element-plus'

/**
 * 从Markdown内容中提取所有图片链接
 * @param {string} markdownContent - Markdown内容
 * @returns {Array} 图片链接数组
 */
export function extractImageUrls(markdownContent) {
    if (!markdownContent || typeof markdownContent !== 'string') {
        return []
    }

    const imageUrls = []

    // 匹配Markdown图片语法: ![alt](url) 和 <img src="url" alt="alt">
    const markdownImageRegex = /!\[([^\]]*)\]\(([^)]+)\)/g
    const htmlImageRegex = /<img[^>]+src\s*=\s*["']([^"']+)["'][^>]*>/gi

    let match

    // 匹配Markdown格式的图片
    while ((match = markdownImageRegex.exec(markdownContent)) !== null) {
        const url = match[2].trim()
        if (url && !isServerImage(url)) {
            imageUrls.push({
                url,
                alt: match[1] || '',
                type: 'markdown',
                fullMatch: match[0]
            })
        }
    }

    // 匹配HTML格式的图片
    while ((match = htmlImageRegex.exec(markdownContent)) !== null) {
        const url = match[1].trim()
        if (url && !isServerImage(url)) {
            imageUrls.push({
                url,
                alt: '',
                type: 'html',
                fullMatch: match[0]
            })
        }
    }

    return imageUrls
}

/**
 * 检查URL是否是服务器图片（避免重复上传）
 * @param {string} url - 图片URL
 * @returns {boolean}
 */
function isServerImage(url) {
    if (!url) return true

    // 检查是否是相对路径（服务器图片）
    if (url.startsWith('/') && !url.startsWith('//')) {
        return true
    }

    // 检查是否是当前服务器的图片
    const currentHost = window.location.host
    try {
        const urlObj = new URL(url, window.location.origin)
        return urlObj.hostname === currentHost || urlObj.hostname === 'localhost'
    } catch {
        return false
    }
}

/**
 * 下载图片并转换为Blob
 * @param {string} imageUrl - 图片URL
 * @returns {Promise<Blob>}
 */
async function downloadImageAsBlob(imageUrl) {
    try {
        const response = await fetch(imageUrl, {
            mode: 'cors',
            credentials: 'omit'
        })

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`)
        }

        const blob = await response.blob()

        // 验证是否为图片
        if (!blob.type.startsWith('image/')) {
            throw new Error('下载的文件不是图片格式')
        }

        return blob
    } catch (error) {
        // console.error('下载图片失败:', error)
        throw new Error(`下载图片失败: ${error.message}`)
    }
}

/**
 * 上传图片到服务器
 * @param {Blob} blob - 图片Blob对象
 * @param {string} originalUrl - 原始图片URL
 * @returns {Promise<string>} 服务器返回的图片URL
 */
async function uploadImageToServer(blob, originalUrl) {
    try {
        // 生成文件名
        const timestamp = Date.now()
        const extension = getImageExtension(blob.type) || 'png'
        const fileName = `imported_${timestamp}.${extension}`

        // 创建FormData
        const formData = new FormData()
        formData.append('file', blob, fileName)

        // 上传到服务器
        const response = await adminApi.uploadImage(formData)

        if (response.code === 200) {
            return response.data
        } else {
            throw new Error(response.message || '上传失败')
        }
    } catch (error) {
        // console.error('上传图片失败:', error)
        throw new Error(`上传图片失败: ${error.message}`)
    }
}

/**
 * 根据MIME类型获取图片扩展名
 * @param {string} mimeType - MIME类型
 * @returns {string} 扩展名
 */
function getImageExtension(mimeType) {
    const mimeToExt = {
        'image/jpeg': 'jpg',
        'image/jpg': 'jpg',
        'image/png': 'png',
        'image/gif': 'gif',
        'image/webp': 'webp',
        'image/svg+xml': 'svg',
        'image/bmp': 'bmp'
    }
    return mimeToExt[mimeType] || 'png'
}

/**
 * 替换Markdown内容中的图片链接
 * @param {string} markdownContent - 原始Markdown内容
 * @param {Array} replacements - 替换映射数组 [{oldUrl, newUrl, type}]
 * @returns {string} 替换后的Markdown内容
 */
export function replaceImageUrls(markdownContent, replacements) {
    let result = markdownContent

    replacements.forEach(({ oldUrl, newUrl, type, fullMatch }) => {
        if (type === 'markdown') {
            // 替换Markdown格式: ![alt](oldUrl) -> ![alt](newUrl)
            const regex = new RegExp(`!\\[([^\\]]*)\\]\\(${escapeRegExp(oldUrl)}\\)`, 'g')
            result = result.replace(regex, `![$1](${newUrl})`)
        } else if (type === 'html') {
            // 替换HTML格式: <img src="oldUrl" ...> -> <img src="newUrl" ...>
            const regex = new RegExp(`(<img[^>]+src\\s*=\\s*["'])${escapeRegExp(oldUrl)}(["'][^>]*>)`, 'gi')
            result = result.replace(regex, `$1${newUrl}$2`)
        }
    })

    return result
}

/**
 * 转义正则表达式特殊字符
 * @param {string} string - 要转义的字符串
 * @returns {string} 转义后的字符串
 */
function escapeRegExp(string) {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

/**
 * 处理Markdown内容中的所有图片
 * @param {string} markdownContent - Markdown内容
 * @param {Object} options - 配置选项
 * @param {boolean} options.showProgress - 是否显示进度提示
 * @param {Function} options.onProgress - 进度回调函数
 * @returns {Promise<string>} 处理后的Markdown内容
 */
export async function processMarkdownImages(markdownContent, options = {}) {
    const { showProgress = true, onProgress } = options

    if (!markdownContent || typeof markdownContent !== 'string') {
        return markdownContent
    }

    // 提取所有图片链接
    const imageUrls = extractImageUrls(markdownContent)

    if (imageUrls.length === 0) {
        return markdownContent
    }

    if (showProgress) {
        ElMessage.info(`发现 ${imageUrls.length} 张图片，开始处理...`)
    }

    const replacements = []
    let processedCount = 0

    // 处理每张图片
    for (const imageInfo of imageUrls) {
        try {
            if (onProgress) {
                onProgress(processedCount, imageUrls.length, imageInfo.url)
            }

            // 下载图片
            const blob = await downloadImageAsBlob(imageInfo.url)

            // 上传到服务器
            const serverUrl = await uploadImageToServer(blob, imageInfo.url)

            // 记录替换信息
            replacements.push({
                oldUrl: imageInfo.url,
                newUrl: serverUrl,
                type: imageInfo.type,
                fullMatch: imageInfo.fullMatch
            })

            processedCount++

            if (showProgress) {
                ElMessage.success(`图片 ${processedCount}/${imageUrls.length} 处理完成`)
            }

        } catch (error) {
            // console.error(`处理图片失败 ${imageInfo.url}:`, error)

            if (showProgress) {
                ElMessage.warning(`图片 ${imageInfo.url} 处理失败: ${error.message}`)
            }

            // 继续处理下一张图片
            processedCount++
        }
    }

    // 替换Markdown内容中的图片链接
    const processedContent = replaceImageUrls(markdownContent, replacements)

    if (showProgress) {
        ElMessage.success(`图片处理完成！成功处理 ${replacements.length}/${imageUrls.length} 张图片`)
    }

    return processedContent
}

/**
 * 批量处理图片（用于文件导入时）
 * @param {string} markdownContent - Markdown内容
 * @returns {Promise<string>} 处理后的Markdown内容
 */
export async function batchProcessMarkdownImages(markdownContent) {
    return processMarkdownImages(markdownContent, {
        showProgress: true,
        onProgress: (current, total, url) => {
            // console.log(`处理进度: ${current}/${total} - ${url}`)
        }
    })
}
