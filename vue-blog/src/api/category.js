import request from '@/utils/request'

export function getCategoryList() {
    return request({
        url: '/user/categories',
        method: 'get'
    })
}

export function getCategoryArticles(id) {
    return request({
        url: `/user/categories/${id}/articles`,
        method: 'get'
    })
}
