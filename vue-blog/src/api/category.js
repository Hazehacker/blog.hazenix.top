import request from '@/utils/request'

export function getCategoryList() {
    return request({
        url: '/categories',
        method: 'get'
    })
}
