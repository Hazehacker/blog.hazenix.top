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
  },

  adminPage(params) {
    return request({ url: '/admin/moment/page', method: 'get', params })
  },

  create(data) {
    return request({ url: '/admin/moment', method: 'post', data })
  },

  update(id, data) {
    return request({ url: `/admin/moment/${id}`, method: 'put', data })
  },

  batchDelete(ids) {
    return request({ url: '/admin/moment/batch', method: 'delete', data: { ids } })
  }
}
