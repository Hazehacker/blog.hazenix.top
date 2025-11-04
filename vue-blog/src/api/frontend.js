import request from '@/utils/request'

export const frontendApi = {
    // ========== 友链相关 ==========
    // 获取友链列表（前台）
    getLinks(params = {}) {
        return request.get('/api/user/links', { params })
    },

    // 申请友链（用户端）
    applyLink(data) {
        return request.post('/api/user/links/apply', data)
    }
}
