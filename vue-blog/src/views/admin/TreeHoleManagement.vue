<template>
  <div class="space-y-6">
    <!-- 页面标题和操作按钮 -->
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">樹洞管理</h1>
      <div class="flex space-x-3">
        <el-button @click="handleBatchDelete" type="danger" :disabled="selectedTreeHoles.length === 0">
          批量刪除
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索樹洞內容"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-input
          v-model="searchForm.username"
          placeholder="搜索用戶名"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
        </el-input>

        <el-select v-model="searchForm.status" placeholder="選擇狀態" clearable>
          <el-option label="全部" :value="undefined" />
          <el-option label="正常" :value="0" />
          <el-option label="已刪除" :value="1" />
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

    <!-- 樹洞列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
      <el-table
        :data="treeHoles"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column prop="userId" label="用戶ID" width="100" />

        <el-table-column prop="username" label="用戶名" width="120">
          <template #default="{ row }">
            <span class="font-medium text-gray-900 dark:text-gray-100">{{ row.username || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="content" label="樹洞內容" min-width="300">
          <template #default="{ row }">
            <div class="max-w-xs">
              <p class="text-gray-900 dark:text-gray-100 line-clamp-2">{{ row.content }}</p>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="狀態" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="創建時間" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="flex space-x-2">
              <el-button @click="handleView(row)" size="small" type="primary" plain>
                查看
              </el-button>
              <el-button @click="handleDelete(row)" size="small" type="danger" plain>
                刪除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="flex justify-between items-center p-4 border-t border-gray-200 dark:border-gray-700">
        <div class="text-sm text-gray-500 dark:text-gray-400">
          共 {{ total }} 條記錄
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

    <!-- 樹洞詳情對話框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="樹洞詳情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentTreeHole" class="space-y-4">
        <!-- 用戶信息 -->
        <div class="flex items-center space-x-3 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
          <div>
            <h3 class="font-medium text-gray-900 dark:text-gray-100">用戶ID: {{ currentTreeHole.userId }}</h3>
            <p class="text-sm text-gray-500 dark:text-gray-400">
              用戶名: {{ currentTreeHole.username || '-' }}
            </p>
          </div>
        </div>

        <!-- 樹洞內容 -->
        <div class="p-4 bg-white dark:bg-gray-800 rounded-lg border">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-2">樹洞內容</h4>
          <p class="text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{{ currentTreeHole.content }}</p>
        </div>

        <!-- 其他信息 -->
        <div class="p-4 bg-white dark:bg-gray-800 rounded-lg border">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-2">其他信息</h4>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="text-gray-500 dark:text-gray-400">ID:</span>
              <span class="ml-2 text-gray-700 dark:text-gray-300">{{ currentTreeHole.id }}</span>
            </div>
            <div>
              <span class="text-gray-500 dark:text-gray-400">狀態:</span>
              <el-tag
                :type="getStatusType(currentTreeHole.status)"
                size="small"
                class="ml-2"
              >
                {{ getStatusText(currentTreeHole.status) }}
              </el-tag>
            </div>
            <div class="col-span-2">
              <span class="text-gray-500 dark:text-gray-400">創建時間:</span>
              <span class="ml-2 text-gray-700 dark:text-gray-300">{{ formatDate(currentTreeHole.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end space-x-3">
          <el-button @click="detailDialogVisible = false">關閉</el-button>
          <el-button @click="handleDelete(currentTreeHole)" type="danger">
            刪除
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, User } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

// 響應式數據
const loading = ref(false)
const treeHoles = ref([])
const selectedTreeHoles = ref([])
const total = ref(0)

// 搜索表單
const searchForm = reactive({
  keyword: '',
  username: '',
  status: undefined
})

// 分頁
const pagination = reactive({
  page: 1,
  pageSize: 20
})

// 詳情對話框
const detailDialogVisible = ref(false)
const currentTreeHole = ref(null)

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 獲取狀態類型
const getStatusType = (status) => {
  const statusMap = {
    0: 'success',
    1: 'danger'
  }
  return statusMap[status] || 'info'
}

// 獲取狀態文本
const getStatusText = (status) => {
  const statusMap = {
    0: '正常',
    1: '已刪除'
  }
  return statusMap[status] || '未知'
}

// 加載樹洞列表
const loadTreeHoles = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    
    // 只添加有值的參數
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    if (searchForm.username) {
      params.username = searchForm.username
    }
    if (searchForm.status !== undefined && searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    
    const response = await adminApi.getTreeHoles(params)
    
    // 根據接口文檔，響應數據結構為 { code, message, data: { records, total } }
    if (response.data && response.data.records) {
      treeHoles.value = response.data.records
      total.value = response.data.total || 0
    } else {
      // 兼容其他可能的數據結構
      treeHoles.value = response.data?.list || response.data || []
      total.value = response.data?.total || 0
    }
  } catch (error) {
    console.error('加載樹洞列表失敗:', error)
    ElMessage.error('加載樹洞列表失敗')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadTreeHoles()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    username: '',
    status: undefined
  })
  pagination.page = 1
  loadTreeHoles()
}

// 分頁處理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadTreeHoles()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadTreeHoles()
}

// 選擇處理
const handleSelectionChange = (selection) => {
  selectedTreeHoles.value = selection
}

// 查看樹洞詳情
const handleView = (treeHole) => {
  currentTreeHole.value = treeHole
  detailDialogVisible.value = true
}

// 刪除樹洞
const handleDelete = async (treeHole) => {
  try {
    await ElMessageBox.confirm(
      '確定要刪除這條樹洞嗎？此操作不可恢復。',
      '確認刪除',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await adminApi.deleteTreeHole(treeHole.id)
    ElMessage.success('刪除成功')
    loadTreeHoles()
    if (detailDialogVisible.value) {
      detailDialogVisible.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('刪除樹洞失敗:', error)
      ElMessage.error('刪除失敗')
    }
  }
}

// 批量刪除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `確定要刪除選中的 ${selectedTreeHoles.value.length} 條樹洞嗎？此操作不可恢復。`,
      '確認批量刪除',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedTreeHoles.value.map(item => item.id)
    await adminApi.batchDeleteTreeHoles(ids)
    ElMessage.success('批量刪除成功')
    loadTreeHoles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量刪除失敗:', error)
      ElMessage.error('批量刪除失敗')
    }
  }
}

// 初始化
onMounted(() => {
  loadTreeHoles()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

