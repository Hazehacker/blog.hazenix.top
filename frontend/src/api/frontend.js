import request from '@/utils/request'

// 前台API - 友链相关
export const frontendApi = {
    // ========== 友链相关 ==========
    // 获取友链列表（前台）
    getLinks(params = {}) {
        return request.get('/user/links', { params })
    },

    // 申请友链（用户端）
    applyLink(data) {
        return request.post('/user/links/apply', data)
    },

    // ========== 更新记录相关 ==========
    // 获取更新记录列表（公开，无需登录）
    getUpdates(params = {}) {
        return request.get('/user/updates', { params })
    },

    // ========== 订阅与互动相关 ==========
    // 订阅文章
    subscribeArticle(data) {
        return request.post('/user/subscription/subscribe', data)
    },
    // 获取本月催更数
    getUrgeCount() {
        return request.get('/user/urge/count')
    },
    // 催更
    urgeArticle() {
        return request.post('/user/urge')
    },
    // 获取本站喜欢总数
    getSitelikeCount() {
        return request.get('/user/site-like/count')
    },
    // 喜欢本站
    likeSite() {
        return request.post('/user/site-like')
    }
}
