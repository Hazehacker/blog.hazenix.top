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
    }
}
