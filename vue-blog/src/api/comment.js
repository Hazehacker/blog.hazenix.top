import request from '@/utils/request'

export function getCommentList(articleId) {
    return request({
        url: `/api/articles/${articleId}/comments`,
        method: 'get'
    })
}

export function addComment(articleId, data) {
    return request({
        url: `/api/articles/${articleId}/comments`,
        method: 'post',
        data  // { content, parentId }
    })
}
