import request from '@/utils/request'

// 前台API基础路径
const FRONTEND_BASE_URL = '/api'

export const frontendApi = {
    // ========== 友链相关 ==========
    // 获取友链列表（前台）
    getLinks(params = {}) {
        return request.get('/user/links', { params })
    },

    // 申请友链（用户端）
    applyLink(data) {
        return request.post('/user/links/apply', data)
    },

    // ========== 文章相关 ==========
    // 获取文章列表
    getArticles(params = {}) {
        return request.get(`${FRONTEND_BASE_URL}/articles`, { params })
    },

    // 获取文章详情
    getArticle(id) {
        return request.get(`${FRONTEND_BASE_URL}/articles/${id}`)
    },

    // ========== 分类相关 ==========
    // 获取分类列表
    getCategories(params = {}) {
        return request.get(`${FRONTEND_BASE_URL}/categories`, { params })
    },

    // 获取分类详情
    getCategory(id) {
        return request.get(`${FRONTEND_BASE_URL}/categories/${id}`)
    },

    // ========== 标签相关 ==========
    // 获取标签列表
    getTags(params = {}) {
        return request.get(`${FRONTEND_BASE_URL}/tags`, { params })
    },

    // 获取标签详情
    getTag(id) {
        return request.get(`${FRONTEND_BASE_URL}/tags/${id}`)
    },

    // ========== 评论相关 ==========
    // 获取评论列表
    getComments(params = {}) {
        return request.get(`${FRONTEND_BASE_URL}/comments`, { params })
    },

    // 创建评论
    createComment(data) {
        return request.post(`${FRONTEND_BASE_URL}/comments`, data)
    },

    // ========== 搜索相关 ==========
    // 搜索
    search(params = {}) {
        return request.get(`${FRONTEND_BASE_URL}/search`, { params })
    }
}
