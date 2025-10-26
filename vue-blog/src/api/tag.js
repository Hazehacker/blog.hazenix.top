import request from '@/utils/request'

export function getTagList() {
    return request({
        url: '/user/tags',
        method: 'get'
    })
}

export function getTagArticles(id, params = {}) {
    return request({
        url: `/user/tags/${id}/articles`,
        method: 'get',
        params
    })
}
