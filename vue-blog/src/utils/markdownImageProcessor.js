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
            const imageType = getImageType(url)
            imageUrls.push({
                url,
                alt: match[1] || '',
                type: 'markdown',
                fullMatch: match[0],
                imageType: imageType,
                fileName: extractFileName(url) // 提取文件名用于匹配用户选择的文件
            })
        }
    }

    // 匹配HTML格式的图片
    while ((match = htmlImageRegex.exec(markdownContent)) !== null) {
        const url = match[1].trim()
        if (url && !isServerImage(url)) {
            const imageType = getImageType(url)
            imageUrls.push({
                url,
                alt: '',
                type: 'html',
                fullMatch: match[0],
                imageType: imageType,
                fileName: extractFileName(url)
            })
        }
    }

    return imageUrls
}

/**
 * 从路径中提取文件名
 * @param {string} path - 文件路径
 * @returns {string} 文件名
 */
function extractFileName(path) {
    // 移除 file:// 协议
    let cleanPath = path.replace(/^file:\/\//, '')
    
    // Windows 路径: C:\path\to\file.jpg -> file.jpg
    // Unix 路径: /path/to/file.jpg -> file.jpg
    const parts = cleanPath.split(/[\\/]/)
    return parts[parts.length - 1] || path
}

/**
 * 判断图片类型
 * @param {string} url - 图片URL
 * @returns {string} 'base64' | 'http' | 'relative' | 'local' | 'local-absolute'
 */
function getImageType(url) {
    if (!url) return 'unknown'
    
    // Base64 图片
    if (url.startsWith('data:image/')) {
        return 'base64'
    }
    
    // HTTP/HTTPS 图片
    if (url.startsWith('http://') || url.startsWith('https://')) {
        return 'http'
    }
    
    // 本地文件路径 (file://)
    if (url.startsWith('file://')) {
        return 'local'
    }
    
    // Windows 绝对路径 (C:\, D:\, E:\ 等)
    if (/^[A-Za-z]:[\\/]/.test(url)) {
        return 'local-absolute'
    }
    
    // Unix/Mac 绝对路径 (/Users/, /home/, /var/ 等，但不包括网络路径)
    if (url.startsWith('/') && !url.startsWith('//')) {
        // 排除相对路径标记
        if (!url.startsWith('./') && !url.startsWith('../')) {
            // 检查是否是常见的系统路径
            const unixAbsolutePaths = ['/Users/', '/home/', '/var/', '/tmp/', '/usr/', '/opt/', '/etc/']
            if (unixAbsolutePaths.some(path => url.startsWith(path))) {
                return 'local-absolute'
            }
            // 如果没有协议，且以 / 开头，也可能是绝对路径
            if (!url.includes('://')) {
                return 'local-absolute'
            }
        }
    }
    
    // 相对路径 (./ 或 ../ 开头，或者不以 / 开头的路径)
    if (url.startsWith('./') || url.startsWith('../') || (!url.startsWith('/') && !url.includes('://'))) {
        return 'relative'
    }
    
    return 'unknown'
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
 * @param {File} file - 可选的本地文件对象（用于本地绝对路径）
 * @returns {Promise<Blob>}
 */
async function downloadImageAsBlob(imageUrl, file = null) {
    // 如果是本地文件对象，直接转换
    if (file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader()
            reader.onload = (e) => {
                const blob = new Blob([e.target.result], { type: file.type || 'image/png' })
                resolve(blob)
            }
            reader.onerror = () => reject(new Error('读取文件失败'))
            reader.readAsArrayBuffer(file)
        })
    }
    
    // 处理本地绝对路径 - 浏览器无法直接访问，需要用户选择文件
    const imageType = getImageType(imageUrl)
    if (imageType === 'local-absolute' || imageType === 'local') {
        throw new Error('本地文件路径，需要用户手动选择文件')
    }
    
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
        console.error('下载图片失败:', error)
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
        console.error('上传图片失败:', error)
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
 * @param {Function} options.onLocalImages - 本地图片回调函数，返回Promise<Map<url, File>>
 * @returns {Promise<string>} 处理后的Markdown内容
 */
export async function processMarkdownImages(markdownContent, options = {}) {
    const { showProgress = true, onProgress, onLocalImages } = options

    if (!markdownContent || typeof markdownContent !== 'string') {
        return markdownContent
    }

    // 提取所有图片链接
    const imageUrls = extractImageUrls(markdownContent)

    if (imageUrls.length === 0) {
        return markdownContent
    }

    // 分离本地绝对路径图片和网络图片
    const localImages = imageUrls.filter(img => img.imageType === 'local-absolute' || img.imageType === 'local')
    const networkImages = imageUrls.filter(img => img.imageType !== 'local-absolute' && img.imageType !== 'local')

    // 如果有本地图片，需要用户选择文件
    let localFileMap = new Map()
    if (localImages.length > 0 && onLocalImages) {
        try {
            localFileMap = await onLocalImages(localImages)
        } catch (error) {
            console.error('用户取消选择本地图片:', error)
            if (showProgress) {
                ElMessage.warning('本地图片未选择，将跳过这些图片')
            }
        }
    }

    if (showProgress) {
        const totalImages = imageUrls.length
        const localCount = localImages.length
        const networkCount = networkImages.length
        ElMessage.info(`发现 ${totalImages} 张图片（${networkCount} 张网络图片，${localCount} 张本地图片），开始处理...`)
    }

    const replacements = []
    let processedCount = 0

    // 处理网络图片
    for (const imageInfo of networkImages) {
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

            if (showProgress && processedCount % 5 === 0) {
                ElMessage.success(`图片 ${processedCount}/${imageUrls.length} 处理完成`)
            }

        } catch (error) {
            console.error(`处理图片失败 ${imageInfo.url}:`, error)

            if (showProgress) {
                ElMessage.warning(`图片 ${imageInfo.url} 处理失败: ${error.message}`)
            }

            // 继续处理下一张图片
            processedCount++
        }
    }

    // 处理本地图片
    for (const imageInfo of localImages) {
        try {
            if (onProgress) {
                onProgress(processedCount, imageUrls.length, imageInfo.url)
            }

            // 获取用户选择的文件
            const file = localFileMap.get(imageInfo.url)
            if (!file) {
                console.warn(`未找到本地图片文件: ${imageInfo.url}`)
                processedCount++
                continue
            }

            // 转换为Blob并上传
            const blob = await downloadImageAsBlob(imageInfo.url, file)
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
                ElMessage.success(`本地图片 ${processedCount}/${imageUrls.length} 处理完成`)
            }

        } catch (error) {
            console.error(`处理本地图片失败 ${imageInfo.url}:`, error)

            if (showProgress) {
                ElMessage.warning(`本地图片 ${imageInfo.url} 处理失败: ${error.message}`)
            }

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
 * @param {Function} onLocalImages - 本地图片选择回调
 * @returns {Promise<string>} 处理后的Markdown内容
 */
export async function batchProcessMarkdownImages(markdownContent, onLocalImages = null) {
    return processMarkdownImages(markdownContent, {
        showProgress: true,
        onProgress: (current, total, url) => {
            console.log(`处理进度: ${current}/${total} - ${url}`)
        },
        onLocalImages: onLocalImages
    })
}
