import request from '@/utils/request'

// 管理端API基础路径
const ADMIN_BASE_URL = '/admin'

export const adminApi = {
  // 获取仪表盘统计数据
  getStats() {
    return request.get(`${ADMIN_BASE_URL}/stats`)
  },

  // 获取最新文章
  getRecentArticles(params = {}) {
    return request.get(`${ADMIN_BASE_URL}/articles/recent`, { params })
  },

  // 获取最新评论
  getRecentComments(params = {}) {
    return request.get(`${ADMIN_BASE_URL}/comments/recent`, { params })
  },

  // ========== 文章管理 ==========
  // 获取文章列表
  getArticles(params = {}) {
    return request.get(`${ADMIN_BASE_URL}/articles`, { params })
  },

  // 获取文章详情
  getArticle(id) {
    return request.get(`${ADMIN_BASE_URL}/articles/${id}`)
  },

  // 创建文章
  createArticle(data) {
    return request.post(`${ADMIN_BASE_URL}/articles`, data)
  },

  // 更新文章
  updateArticle(id, data) {
    return request.put(`${ADMIN_BASE_URL}/articles/${id}`, data)
  },

  // 删除文章
  deleteArticle(id) {
    return request.delete(`${ADMIN_BASE_URL}/articles/${id}`)
  },

  // 批量删除文章
  batchDeleteArticles(ids) {
    return request.delete(`${ADMIN_BASE_URL}/articles/batch`, { data: { ids } })
  },

  // 发布/取消发布文章
  toggleArticleStatus(id, status) {
    return request.patch(`${ADMIN_BASE_URL}/articles/${id}/status`, { status })
  },

  // ========== 分类管理 ==========
  // 获取分类列表
  getCategories(params = {}) {
    return request.get(`${ADMIN_BASE_URL}/categories`, { params })
  },

  // 获取分类详情
  getCategory(id) {
    return request.get(`${ADMIN_BASE_URL}/categories/${id}`)
  },

  // 创建分类
  createCategory(data) {
    return request.post(`${ADMIN_BASE_URL}/categories`, data)
  },

  // 更新分类
  updateCategory(id, data) {
    return request.put(`${ADMIN_BASE_URL}/categories/${id}`, data)
  },

  // 删除分类
  deleteCategory(id) {
    return request.delete(`${ADMIN_BASE_URL}/categories/${id}`)
  },

  // 批量删除分类
  batchDeleteCategories(ids) {
    return request.delete(`${ADMIN_BASE_URL}/categories/batch`, { data: { ids } })
  },

  // ========== 标签管理 ==========
  // 获取标签列表
  getTags(params = {}) {
    return request.get(`${ADMIN_BASE_URL}/tags`, { params })
  },

  // 获取标签详情
  getTag(id) {
    return request.get(`${ADMIN_BASE_URL}/tags/${id}`)
  },

  // 创建标签
  createTag(data) {
    return request.post(`${ADMIN_BASE_URL}/tags`, data)
  },

  // 更新标签
  updateTag(id, data) {
    return request.put(`${ADMIN_BASE_URL}/tags/${id}`, data)
  },

  // 删除标签
  deleteTag(id) {
    return request.delete(`${ADMIN_BASE_URL}/tags/${id}`)
  },

  // 批量删除标签
  batchDeleteTags(ids) {
    return request.delete(`${ADMIN_BASE_URL}/tags/batch`, { data: { ids } })
  },

  // ========== 评论管理 ==========
  // 获取评论列表
  getComments(params = {}) {
    return request.get(`${ADMIN_BASE_URL}/comments`, { params })
  },

  // 获取评论详情
  getComment(id) {
    return request.get(`${ADMIN_BASE_URL}/comments/${id}`)
  },

  // 更新评论状态
  updateCommentStatus(id, status) {
    return request.patch(`${ADMIN_BASE_URL}/comments/${id}/status`, { status })
  },

  // 删除评论
  deleteComment(id) {
    return request.delete(`${ADMIN_BASE_URL}/comments/${id}`)
  },

  // 批量删除评论
  batchDeleteComments(ids) {
    return request.delete(`${ADMIN_BASE_URL}/comments/batch`, { data: { ids } })
  },

  // 批量审核评论
  batchApproveComments(ids) {
    return request.patch(`${ADMIN_BASE_URL}/comments/batch/approve`, { ids })
  },

  // 批量拒绝评论
  batchRejectComments(ids) {
    return request.patch(`${ADMIN_BASE_URL}/comments/batch/reject`, { ids })
  },

  // ========== 更新记录管理 ==========
  // 获取更新记录列表
  getUpdates(params = {}) {
    return request.get(`${ADMIN_BASE_URL}/updates`, { params })
  },

  // 获取更新记录详情
  getUpdate(id) {
    return request.get(`${ADMIN_BASE_URL}/updates/${id}`)
  },

  // 创建更新记录
  createUpdate(data) {
    return request.post(`${ADMIN_BASE_URL}/updates`, data)
  },

  // 更新记录
  updateUpdate(id, data) {
    return request.put(`${ADMIN_BASE_URL}/updates/${id}`, data)
  },

  // 删除更新记录
  deleteUpdate(id) {
    return request.delete(`${ADMIN_BASE_URL}/updates/${id}`)
  },

  // 批量删除更新记录
  batchDeleteUpdates(ids) {
    return request.delete(`${ADMIN_BASE_URL}/updates/batch`, { data: { ids } })
  },

  // ========== 文件上传 ==========
  // 上传图片
  uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post(`${ADMIN_BASE_URL}/upload/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 上传文件
  uploadFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post(`${ADMIN_BASE_URL}/upload/file`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
