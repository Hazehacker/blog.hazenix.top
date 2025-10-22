import request from '@/utils/request'

// 文章相关API
export const articleApi = {
    // 获取文章列表
    getArticleList(params = {}) {
        return request({
            url: '/api/articles',
            method: 'get',
            params  // { page, pageSize, categoryId, tagId, keyword, status, sortBy, sortOrder }
        })
    },

    // 获取文章详情
    getArticleDetail(id) {
        return request({
            url: `/api/articles/${id}`,
            method: 'get'
        })
    },

    // 根据slug获取文章
    getArticleBySlug(slug) {
        return request({
            url: `/api/articles/slug/${slug}`,
            method: 'get'
        })
    },

    // 搜索文章
    searchArticles(keyword, params = {}) {
        return request({
            url: '/api/articles/search',
            method: 'get',
            params: { keyword, ...params }
        })
    },

    // 创建文章
    createArticle(data) {
        return request({
            url: '/api/articles',
            method: 'post',
            data
        })
    },

    // 更新文章
    updateArticle(id, data) {
        return request({
            url: `/api/articles/${id}`,
            method: 'put',
            data
        })
    },

    // 删除文章
    deleteArticle(id) {
        return request({
            url: `/api/articles/${id}`,
            method: 'delete'
        })
    },

    // 批量删除文章
    batchDeleteArticles(ids) {
        return request({
            url: '/api/articles/batch',
            method: 'delete',
            data: { ids }
        })
    },

    // 发布/取消发布文章
    toggleArticleStatus(id, status) {
        return request({
            url: `/api/articles/${id}/status`,
            method: 'patch',
            data: { status }
        })
    },

    // 点赞文章
    likeArticle(id) {
        return request({
            url: `/api/articles/${id}/like`,
            method: 'post'
        })
    },

    // 收藏文章
    collectArticle(id) {
        return request({
            url: `/api/articles/${id}/collect`,
            method: 'post'
        })
    },

    // 获取相关文章
    getRelatedArticles(id, params = {}) {
        return request({
            url: `/api/articles/${id}/related`,
            method: 'get',
            params
        })
    },

    // 获取热门文章
    getPopularArticles(params = {}) {
        return request({
            url: '/api/articles/popular',
            method: 'get',
            params
        })
    },

    // 获取最新文章
    getLatestArticles(params = {}) {
        return request({
            url: '/api/articles/latest',
            method: 'get',
            params
        })
    },

    // 获取推荐文章
    getRecommendedArticles(params = {}) {
        return request({
            url: '/api/articles/recommended',
            method: 'get',
            params
        })
    },

    // 获取文章统计
    getArticleStats(id) {
        return request({
            url: `/api/articles/${id}/stats`,
            method: 'get'
        })
    },

    // 增加文章浏览量
    incrementViewCount(id) {
        return request({
            url: `/api/articles/${id}/view`,
            method: 'post'
        })
    },

    // 获取所有已发布文章的slug
    getAllSlugs() {
        return request({
            url: '/api/articles/slugs',
            method: 'get'
        })
    }
}

// 为了保持向后兼容，导出原有的函数
export function getArticleList(params) {
    return articleApi.getArticleList(params)
}

export function getArticleDetail(id) {
    return articleApi.getArticleDetail(id)
}

export function searchArticles(keyword, params = {}) {
    return articleApi.searchArticles(keyword, params)
}

export function createArticle(data) {
    return articleApi.createArticle(data)
}

export function updateArticle(id, data) {
    return articleApi.updateArticle(id, data)
}

export function deleteArticle(id) {
    return articleApi.deleteArticle(id)
}

export function toggleArticleStatus(id, status) {
    return articleApi.toggleArticleStatus(id, status)
}

export function likeArticle(id) {
    return articleApi.likeArticle(id)
}

export function collectArticle(id) {
    return articleApi.collectArticle(id)
}

export function getRelatedArticles(id, params = {}) {
    return articleApi.getRelatedArticles(id, params)
}
