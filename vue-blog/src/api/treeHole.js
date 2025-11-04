import request from '@/utils/request'

// 树洞相关API
export const treeHoleApi = {
    // 获取树洞弹幕列表（不需要认证）
    getTreeHoleList() {
        return request({
            url: '/user/tree/list',
            method: 'get',
            isPublicResource: true  // 标记为公开资源，不需要认证
        })
    },

    // 发送弹幕（需要认证）
    addTreeHole(data) {
        return request({
            url: '/user/tree',
            method: 'post',
            data  // { userId, username, content }
        })
    }
}

// 为了保持向后兼容，导出原有的函数
export function getTreeHoleList() {
    return treeHoleApi.getTreeHoleList()
}

export function addTreeHole(data) {
    return treeHoleApi.addTreeHole(data)
}

