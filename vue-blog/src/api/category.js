import request from '@/utils/request'

export function getCategoryList() {
    return request({
        url: '/user/categories',
        method: 'get',
        isPublicResource: true // 标记为公开资源
    })
}

export function getCategoryArticles(id) {
    return request({
        url: `/user/categories/${id}/articles`,
        method: 'get',
        isPublicResource: true // 标记为公开资源
    })
}
