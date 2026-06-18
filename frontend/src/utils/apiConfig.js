/**
 * API 配置工具
 * 統一管理 API 基礎 URL 配置，確保生產環境正確工作
 */

/**
 * 獲取 API 基礎 URL
 * @returns {string} API 基礎 URL
 * 
 * 配置說明：
 * - 開發環境：使用 http://localhost:9090（直接連接到後端服務）
 * - 生產環境：使用 https://blog.hazenix.top/api（通過 Nginx 代理）
 * 
 * 環境變量配置：
 * - 開發環境：可選，默認為 http://localhost:9090
 * - 生產環境：必須設置 VITE_API_BASE_URL=https://blog.hazenix.top/api
 */
export function getApiBaseURL() {
    // 優先使用環境變量
    if (import.meta.env.VITE_API_BASE_URL !== undefined) {
        const baseURL = import.meta.env.VITE_API_BASE_URL
        // 如果是空字符串，根據環境決定
        if (baseURL === '') {
            // 生產環境不應該使用空字符串，應該明確配置
            if (import.meta.env.PROD) {
                console.warn('生產環境 VITE_API_BASE_URL 為空，建議設置為 https://blog.hazenix.top/api')
                return '' // 使用相對路徑作為 fallback
            }
            return '' // 開發環境可以使用空字符串（相對路徑）
        }
        // 移除末尾的斜杠
        return baseURL.replace(/\/$/, '')
    }
    
    // 生產環境：如果沒有設置環境變量，使用默認的生產環境 API 地址
    if (import.meta.env.PROD) {
        // 生產環境默認使用 /api 前綴（通過 Nginx 代理）
        // 注意：這會使用相對路徑，實際請求會是當前域名 + /api
        // 如果前端部署在 https://blog.hazenix.top，則會請求 https://blog.hazenix.top/api
        return '/api'
    }
    
    // 開發環境默認使用 localhost
    return "http://localhost:9090"
}

/**
 * 構建完整的 API URL
 * @param {string} path API 路徑
 * @returns {string} 完整的 API URL
 */
export function buildApiURL(path) {
    const baseURL = getApiBaseURL()
    if (!baseURL) {
        // 如果 baseURL 為空，直接返回路徑（相對路徑）
        return path
    }
    // 確保路徑以 / 開頭
    const cleanPath = path.startsWith('/') ? path : `/${path}`
    // 移除 baseURL 末尾的斜杠，確保不會有雙斜杠
    const cleanBaseURL = baseURL.replace(/\/$/, '')
    return `${cleanBaseURL}${cleanPath}`
}

/**
 * 獲取上傳文件的完整 URL
 * @param {string} path 文件路徑
 * @returns {string} 完整的文件 URL
 */
export function getFileURL(path) {
    if (!path) {
        return ''
    }
    
    // 如果已經是完整的 URL（以 http:// 或 https:// 開頭），直接返回
    if (path.match(/^https?:\/\//i)) {
        return path
    }
    
    // 如果是 data: 開頭的 base64 圖片，直接返回
    if (path.startsWith('data:')) {
        return path
    }
    
    // 如果路徑以 / 開頭，使用相對路徑或拼接 API 基礎 URL
    if (path.startsWith('/')) {
        const baseURL = getApiBaseURL()
        if (!baseURL) {
            // 如果 baseURL 為空，使用相對路徑
            return path
        }
        return `${baseURL}${path}`
    }
    
    // 其他情況，嘗試拼接 API 基礎 URL
    const baseURL = getApiBaseURL()
    if (!baseURL) {
        // 如果 baseURL 為空，添加前導斜杠作為相對路徑
        return `/${path}`
    }
    return `${baseURL}/${path}`
}

/**
 * 獲取 OSS 縮略圖 URL（帶圖片處理參數）
 * 將 1.4MB 原圖轉為 ~25KB WebP 縮略圖，大幅提升列表頁加載速度
 * @param {string} url 原始圖片 URL
 * @param {number} width 縮略圖寬度，默認 400px
 * @returns {string} 帶 OSS 處理參數的 URL
 */
export function getThumbnailUrl(url, width = 400) {
    if (!url) return ''
    if (url.includes('oss-cn-heyuan.aliyuncs.com')) {
        const base = url.split('?')[0]
        return `${base}?x-oss-process=image/resize,w_${width}/format,webp`
    }
    return url
}

// 在開發環境輸出配置信息
if (import.meta.env.DEV) {
    console.log('API 配置:', {
        baseURL: getApiBaseURL(),
        mode: import.meta.env.MODE,
        prod: import.meta.env.PROD,
        dev: import.meta.env.DEV
    })
}

