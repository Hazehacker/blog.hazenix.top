import request from '@/utils/request'

// 用户相关API
export const userApi = {
    // 获取用户收藏的文章
    getFavoriteArticles(params = {}) {
        return request({
            url: '/api/user/user/favorite',
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
            url: '/api/user/user/stats',
            method: 'get'
        })
    },
    // 上传图片（用户端）
    uploadImage(file) {
        const formData = new FormData()
        formData.append('file', file)
        return request({
            url: '/api/user/common/upload',
            method: 'post',
            data: formData
            // 不设置Content-Type，让axios自动处理FormData，会自动添加multipart/form-data和boundary
        })
    },
    // 更新用户资料
    updateProfile(data) {
        return request({
            url: '/api/user/user/profile',
            method: 'put',
            data
        })
    },
    // 修改密码
    updatePassword(data) {
        return request({
            url: '/api/user/user/password',
            method: 'put',
            data
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

export function uploadImage(file) {
    return userApi.uploadImage(file)
}

export function updateProfile(data) {
    return userApi.updateProfile(data)
}

export function updatePassword(data) {
    return userApi.updatePassword(data)
}
