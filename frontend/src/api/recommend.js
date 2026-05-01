import request from '@/utils/request'

export function getRecommendedArticles(params = {}) {
    return request({
        url: '/user/articles/recommended',
        method: 'get',
        isPublicResource: true,
        params
    })
}

export function getUserInterests() {
    return request({
        url: '/user/interests',
        method: 'get'
    })
}

export function setUserInterests(tagIds) {
    return request({
        url: '/user/interests',
        method: 'post',
        data: { tagIds }
    })
}

export function updateUserInterests(tagIds) {
    return request({
        url: '/user/interests',
        method: 'put',
        data: { tagIds }
    })
}

export function reportBehavior(articleId, duration) {
    return request({
        url: '/user/behavior',
        method: 'post',
        data: { articleId, duration }
    })
}