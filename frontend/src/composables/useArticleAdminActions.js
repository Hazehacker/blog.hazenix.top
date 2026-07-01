import { ElMessage, ElMessageBox } from 'element-plus'
import { articleApi } from '@/api/article'
import { adminApi } from '@/api/admin'

/**
 * 文章/手记管理行操作 composable
 * @param {Object} options
 * @param {Function} [options.reload]      - 刷新列表的回调（如 loadArticles）
 * @param {string}  [options.entityLabel]  - 实体名称，默认 '文章'（手记页传 '手记'）
 */
export function useArticleAdminActions({ reload, entityLabel = '文章' } = {}) {
  // ── 推荐度工具 ──────────────────────────────────────────────
  const levelLabel = (lv) => {
    const map = { 0: '屏蔽', 1: '弱', 2: '较弱', 3: '默认', 4: '推荐', 5: '精华' }
    return map[lv ?? 3]
  }

  const levelTagType = (lv) => {
    if (lv === 0) return 'info'
    if (lv === 5) return 'danger'
    if (lv === 4) return 'warning'
    return ''
  }

  // ── 修改推荐度 ───────────────────────────────────────────────
  const changeLevel = async (row, lv) => {
    try {
      await articleApi.updateRecommendLevel(row.id, lv)
      row.recommendLevel = lv
      ElMessage.success('推荐度已更新')
    } catch (error) {
      console.error('更新推荐度失败:', error)
      ElMessage.error('更新推荐度失败')
    }
  }

  // ── 切换发布状态 ─────────────────────────────────────────────
  const toggleStatus = async (row) => {
    try {
      const newStatus = row.status === 0 ? 2 : 0
      await adminApi.toggleArticleStatus(row.id, newStatus)
      ElMessage.success(`${newStatus === 0 ? '发布' : '下架'}成功`)
      reload?.()
    } catch (error) {
      console.error('切换状态失败:', error)
      ElMessage.error('操作失败')
    }
  }

  // ── 删除单条 ─────────────────────────────────────────────────
  const remove = async (row) => {
    try {
      await ElMessageBox.confirm(
        `确定要删除${entityLabel}"${row.title}"吗？此操作不可恢复。`,
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      await adminApi.deleteArticle(row.id)
      ElMessage.success('删除成功')
      reload?.()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
      }
    }
  }

  // ── 批量删除 ─────────────────────────────────────────────────
  const batchRemove = async (rows) => {
    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${rows.length} 条${entityLabel}吗？此操作不可恢复。`,
        '确认批量删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      const ids = rows.map((item) => item.id)
      await adminApi.batchDeleteArticles(ids)
      ElMessage.success('批量删除成功')
      reload?.()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除失败:', error)
        ElMessage.error('批量删除失败')
      }
    }
  }

  // ── 切换置顶 ─────────────────────────────────────────────────
  const toggleTop = async (row) => {
    try {
      const newVal = row.isTop === 1 ? 0 : 1
      await adminApi.updateArticle(row.id, { isTop: newVal })
      row.isTop = newVal
      ElMessage.success(newVal === 1 ? '已置顶' : '已取消置顶')
    } catch (error) {
      console.error('置顶操作失败:', error)
      ElMessage.error('操作失败')
    }
  }

  return { levelLabel, levelTagType, changeLevel, toggleStatus, toggleTop, remove, batchRemove }
}
