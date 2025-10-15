<template>
  <div class="space-y-6">
    <!-- 页面标题和操作按钮 -->
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">评论管理</h1>
      <div class="flex space-x-3">
        <el-button @click="handleBatchApprove" type="success" :disabled="selectedComments.length === 0">
          批量通过
        </el-button>
        <el-button @click="handleBatchReject" type="warning" :disabled="selectedComments.length === 0">
          批量拒绝
        </el-button>
        <el-button @click="handleBatchDelete" type="danger" :disabled="selectedComments.length === 0">
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索评论内容或作者"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>

        <el-select v-model="searchForm.articleId" placeholder="选择文章" clearable>
          <el-option
            v-for="article in articles"
            :key="article.id"
            :label="article.title"
            :value="article.id"
          />
        </el-select>

        <div class="flex space-x-2">
          <el-button @click="handleSearch" type="primary">
            <el-icon class="mr-1"><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon class="mr-1"><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
      <el-table
        :data="comments"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="author" label="评论者" width="120">
          <template #default="{ row }">
            <div class="flex items-center">
              <img
                :src="row.avatar || defaultAvatar"
                alt="头像"
                class="w-8 h-8 rounded-full mr-2"
              />
              <span class="font-medium text-gray-900 dark:text-gray-100">{{ row.author }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="content" label="评论内容" min-width="300">
          <template #default="{ row }">
            <div class="max-w-xs">
              <p class="text-gray-900 dark:text-gray-100 line-clamp-2">{{ row.content }}</p>
              <div v-if="row.replyTo" class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                回复: {{ row.replyTo.author }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="article" label="所属文章" width="200">
          <template #default="{ row }">
            <div class="max-w-xs">
              <p class="text-sm text-gray-900 dark:text-gray-100 line-clamp-1">
                {{ row.article?.title || '-' }}
              </p>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="ip" label="IP地址" width="120">
          <template #default="{ row }">
            <span class="text-sm text-gray-500 dark:text-gray-400">{{ row.ip || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="评论时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="flex space-x-2">
              <el-button @click="handleView(row)" size="small" type="primary" plain>
                查看
              </el-button>
              <el-button
                v-if="row.status === 'pending'"
                @click="handleApprove(row)"
                size="small"
                type="success"
              >
                通过
              </el-button>
              <el-button
                v-if="row.status === 'pending'"
                @click="handleReject(row)"
                size="small"
                type="warning"
              >
                拒绝
              </el-button>
              <el-button @click="handleDelete(row)" size="small" type="danger" plain>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="flex justify-between items-center p-4 border-t border-gray-200 dark:border-gray-700">
        <div class="text-sm text-gray-500 dark:text-gray-400">
          共 {{ total }} 条记录
        </div>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 评论详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="评论详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentComment" class="space-y-4">
        <!-- 评论者信息 -->
        <div class="flex items-center space-x-3 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
          <img
            :src="currentComment.avatar || defaultAvatar"
            alt="头像"
            class="w-12 h-12 rounded-full"
          />
          <div>
            <h3 class="font-medium text-gray-900 dark:text-gray-100">{{ currentComment.author }}</h3>
            <p class="text-sm text-gray-500 dark:text-gray-400">
              {{ formatDate(currentComment.createTime) }}
            </p>
          </div>
        </div>

        <!-- 评论内容 -->
        <div class="p-4 bg-white dark:bg-gray-800 rounded-lg border">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-2">评论内容</h4>
          <p class="text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{{ currentComment.content }}</p>
        </div>

        <!-- 所属文章 -->
        <div v-if="currentComment.article" class="p-4 bg-white dark:bg-gray-800 rounded-lg border">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-2">所属文章</h4>
          <p class="text-gray-700 dark:text-gray-300">{{ currentComment.article.title }}</p>
        </div>

        <!-- 回复信息 -->
        <div v-if="currentComment.replyTo" class="p-4 bg-white dark:bg-gray-800 rounded-lg border">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-2">回复对象</h4>
          <div class="flex items-center space-x-2">
            <img
              :src="currentComment.replyTo.avatar || defaultAvatar"
              alt="头像"
              class="w-8 h-8 rounded-full"
            />
            <span class="text-gray-700 dark:text-gray-300">{{ currentComment.replyTo.author }}</span>
          </div>
        </div>

        <!-- 其他信息 -->
        <div class="p-4 bg-white dark:bg-gray-800 rounded-lg border">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-2">其他信息</h4>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="text-gray-500 dark:text-gray-400">IP地址:</span>
              <span class="ml-2 text-gray-700 dark:text-gray-300">{{ currentComment.ip || '-' }}</span>
            </div>
            <div>
              <span class="text-gray-500 dark:text-gray-400">状态:</span>
              <el-tag
                :type="getStatusType(currentComment.status)"
                size="small"
                class="ml-2"
              >
                {{ getStatusText(currentComment.status) }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end space-x-3">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="currentComment?.status === 'pending'"
            @click="handleApprove(currentComment)"
            type="success"
          >
            通过
          </el-button>
          <el-button
            v-if="currentComment?.status === 'pending'"
            @click="handleReject(currentComment)"
            type="warning"
          >
            拒绝
          </el-button>
          <el-button @click="handleDelete(currentComment)" type="danger">
            删除
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import avatarFallback from '@/assets/img/avatar.jpg'

// 响应式数据
const loading = ref(false)
const comments = ref([])
const articles = ref([])
const selectedComments = ref([])
const total = ref(0)
const defaultAvatar = avatarFallback

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  articleId: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentComment = ref(null)

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return statusMap[status] || '未知'
}

// 加载评论列表
const loadComments = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const response = await adminApi.getComments(params)
    comments.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error('加载评论列表失败:', error)
    ElMessage.error('加载评论列表失败')
  } finally {
    loading.value = false
  }
}

// 加载文章列表（用于筛选）
const loadArticles = async () => {
  try {
    const response = await adminApi.getArticles({ pageSize: 100 })
    articles.value = response.data.list
  } catch (error) {
    console.error('加载文章列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadComments()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    articleId: ''
  })
  pagination.page = 1
  loadComments()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadComments()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadComments()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedComments.value = selection
}

// 查看评论详情
const handleView = (comment) => {
  currentComment.value = comment
  detailDialogVisible.value = true
}

// 通过评论
const handleApprove = async (comment) => {
  try {
    await adminApi.updateCommentStatus(comment.id, 'approved')
    ElMessage.success('评论已通过')
    loadComments()
    if (detailDialogVisible.value) {
      detailDialogVisible.value = false
    }
  } catch (error) {
    console.error('通过评论失败:', error)
    ElMessage.error('操作失败')
  }
}

// 拒绝评论
const handleReject = async (comment) => {
  try {
    await adminApi.updateCommentStatus(comment.id, 'rejected')
    ElMessage.success('评论已拒绝')
    loadComments()
    if (detailDialogVisible.value) {
      detailDialogVisible.value = false
    }
  } catch (error) {
    console.error('拒绝评论失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除评论
const handleDelete = async (comment) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await adminApi.deleteComment(comment.id)
    ElMessage.success('删除成功')
    loadComments()
    if (detailDialogVisible.value) {
      detailDialogVisible.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量通过
const handleBatchApprove = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要通过选中的 ${selectedComments.value.length} 条评论吗？`,
      '确认批量通过',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedComments.value.map(item => item.id)
    await adminApi.batchApproveComments(ids)
    ElMessage.success('批量通过成功')
    loadComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量通过失败:', error)
      ElMessage.error('批量通过失败')
    }
  }
}

// 批量拒绝
const handleBatchReject = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要拒绝选中的 ${selectedComments.value.length} 条评论吗？`,
      '确认批量拒绝',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedComments.value.map(item => item.id)
    await adminApi.batchRejectComments(ids)
    ElMessage.success('批量拒绝成功')
    loadComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败:', error)
      ElMessage.error('批量拒绝失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedComments.value.length} 条评论吗？此操作不可恢复。`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedComments.value.map(item => item.id)
    await adminApi.batchDeleteComments(ids)
    ElMessage.success('批量删除成功')
    loadComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadArticles()
  loadComments()
})
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
