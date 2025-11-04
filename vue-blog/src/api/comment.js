import request from '@/utils/request'

// 评论相关API
export const commentApi = {
    // 获取评论列表
    getCommentList(params = {}) {
        return request({
            url: '/api/user/comments/list',
            method: 'get',
            isPublicResource: true, // 标记为公开资源，未登录用户也可以查看评论
            params  // { articleId }
        })
    },

    // 获取评论详情
    getCommentDetail(id) {
        return request({
            url: `/api/admin/comments/${id}`,
            method: 'get'
        })
    },

    // 创建评论
    createComment(data) {
        return request({
            url: '/api/user/comments',
            method: 'post',
            data
        })
    },

    // 更新评论
    updateComment(id, data) {
        return request({
            url: `/api/admin/comments/${id}`,
            method: 'put',
            data
        })
    },

    // 删除评论
    deleteComment(id) {
        return request({
            url: `/api/admin/comments/${id}`,
            method: 'delete'
        })
    },

    // 批量删除评论
    batchDeleteComments(ids) {
        return request({
            url: '/api/admin/comments/batch',
            method: 'delete',
            data: { ids }
        })
    },

    // 点赞评论
    likeComment(id) {
        return request({
            url: `/api/admin/comments/${id}/like`,
            method: 'post'
        })
    },

    // 审核评论
    approveComment(id) {
        return request({
            url: `/api/admin/comments/${id}/approve`,
            method: 'patch'
        })
    },

    // 拒绝评论
    rejectComment(id, reason = '') {
        return request({
            url: `/api/admin/comments/${id}/reject`,
            method: 'patch',
            data: { reason }
        })
    },

    // 批量审核评论
    batchApproveComments(ids) {
        return request({
            url: '/api/admin/comments/batch/approve',
            method: 'patch',
            data: { ids }
        })
    },

    // 批量拒绝评论
    batchRejectComments(ids, reason = '') {
        return request({
            url: '/api/admin/comments/batch/reject',
            method: 'patch',
            data: { ids, reason }
        })
    },

    // 获取评论统计
    getCommentStats(articleId) {
        return request({
            url: `/api/admin/comments/stats`,
            method: 'get',
            params: { articleId }
        })
    },

    // 获取最新评论
    getLatestComments(params = {}) {
        return request({
            url: '/api/admin/comments/latest',
            method: 'get',
            params
        })
    },

    // 获取热门评论
    getPopularComments(params = {}) {
        return request({
            url: '/api/admin/comments/popular',
            method: 'get',
            params
        })
    }
}

// 新的API函数
export function getComments(params = {}) {
    return commentApi.getCommentList(params)
}

export function createComment(data) {
    return commentApi.createComment(data)
}

export function deleteComment(id) {
    return commentApi.deleteComment(id)
}

export function likeComment(id) {
    return commentApi.likeComment(id)
}
