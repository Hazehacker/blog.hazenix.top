import request from '@/utils/request'

export function getTagList() {
    return request({
        url: '/user/tags',
        method: 'get',
        isPublicResource: true // 标记为公开资源
    })
}

export function getTagArticles(id, params = {}) {
    return request({
        url: `/user/tags/${id}/articles`,
        method: 'get',
        isPublicResource: true, // 标记为公开资源
        params
    })
}
