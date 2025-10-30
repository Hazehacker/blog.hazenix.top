import request from '@/utils/request'

// 用户相关API
export const userApi = {
    // 获取用户收藏的文章
    getFavoriteArticles(params = {}) {
        return request({
            url: '/user/user/favorite',
            method: 'get',
            params: {
                sortBy: 'createTime', // 默认按创建时间排序
                sortOrder: 'desc',    // 默认降序（最新的在前）
                ...params  // { page, pageSize }
            }
        })
    },
    // 获取当前登录用户的统计信息（收藏数、点赞数、评论数）
    getUserStats() {
        return request({
            url: '/user/user/stats',
            method: 'get'
        })
    }
}

// 为了保持向后兼容，导出原有的函数
export function getFavoriteArticles(params = {}) {
    return userApi.getFavoriteArticles(params)
}

export function getUserStats() {
    return userApi.getUserStats()
}
