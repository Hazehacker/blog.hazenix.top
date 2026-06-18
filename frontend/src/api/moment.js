import request from '@/utils/request'

export const momentApi = {
  getPage(params = {}) {
    return request({
      url: '/user/moment/page',
      method: 'get',
      isPublicResource: true,
      params
    })
  },

  getDetail(id) {
    return request({
      url: `/user/moment/${id}`,
      method: 'get',
      isPublicResource: true
    })
  },

  like(id) {
    return request({
      url: `/user/moment/${id}/like`,
      method: 'post'
    })
  },

  getTags() {
    return request({
      url: '/user/moment/tags',
      method: 'get',
      isPublicResource: true
    })
  }
}
