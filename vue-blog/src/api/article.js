import request from '@/utils/request'

export function getArticleList(params) {
    return request({
        url: '/api/articles',
        method: 'get',
        params  // { page, pageSize, categoryId, tagId, keyword }
    })
}

export function getArticleDetail(id) {
    return request({
        url: `/api/articles/${id}`,
        method: 'get'
    })
}

export function searchArticles(keyword) {
    return request({
        url: '/api/articles/search',
        method: 'get',
        params: { keyword }
    })
}
