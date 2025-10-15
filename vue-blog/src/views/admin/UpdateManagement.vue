<template>
  <div class="space-y-6">
    <!-- 页面标题和操作按钮 -->
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">更新记录管理</h1>
      <div class="flex space-x-3">
        <el-button @click="handleBatchDelete" type="danger" :disabled="selectedUpdates.length === 0">
          批量删除
        </el-button>
        <el-button @click="handleCreate" type="primary">
          <el-icon class="mr-1"><Plus /></el-icon>
          新建记录
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索版本号或更新内容"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="searchForm.type" placeholder="选择类型" clearable>
          <el-option label="功能更新" value="feature" />
          <el-option label="问题修复" value="bugfix" />
          <el-option label="性能优化" value="optimization" />
          <el-option label="安全更新" value="security" />
          <el-option label="其他" value="other" />
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

    <!-- 更新记录列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
      <el-table
        :data="updates"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="version" label="版本号" width="120">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.version }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="更新标题" min-width="200">
          <template #default="{ row }">
            <span class="font-medium text-gray-900 dark:text-gray-100">{{ row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="content" label="更新内容" min-width="300">
          <template #default="{ row }">
            <div class="max-w-xs">
              <p class="text-gray-900 dark:text-gray-100 line-clamp-2">{{ row.content }}</p>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="releaseDate" label="发布日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.releaseDate) }}
          </template>
        </el-table-column>

        <el-table-column prop="isImportant" label="重要更新" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isImportant" type="danger" size="small">重要</el-tag>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="180">
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
              <el-button @click="handleEdit(row)" size="small">
                编辑
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

    <!-- 更新记录编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        @submit.prevent
      >
        <el-form-item label="版本号" prop="version">
          <el-input
            v-model="form.version"
            placeholder="例如: v1.0.0"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="更新标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入更新标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="更新类型" prop="type">
          <el-select v-model="form.type" placeholder="选择更新类型" class="w-full">
            <el-option label="功能更新" value="feature" />
            <el-option label="问题修复" value="bugfix" />
            <el-option label="性能优化" value="optimization" />
            <el-option label="安全更新" value="security" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="更新内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="详细描述本次更新的内容..."
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="发布日期" prop="releaseDate">
          <el-date-picker
            v-model="form.releaseDate"
            type="date"
            placeholder="选择发布日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="重要更新">
          <el-switch v-model="form.isImportant" />
          <span class="ml-2 text-sm text-gray-500">标记为重要更新</span>
        </el-form-item>

        <el-form-item label="更新链接">
          <el-input
            v-model="form.link"
            placeholder="相关链接（可选）"
            maxlength="200"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end space-x-3">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button @click="handleSave" type="primary" :loading="saving">
            {{ currentUpdate ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 更新记录详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="更新记录详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentUpdate" class="space-y-4">
        <!-- 版本信息 -->
        <div class="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
          <div class="flex items-center space-x-3">
            <el-tag type="primary" size="large">{{ currentUpdate.version }}</el-tag>
            <div>
              <h3 class="font-medium text-gray-900 dark:text-gray-100">{{ currentUpdate.title }}</h3>
              <p class="text-sm text-gray-500 dark:text-gray-400">
                {{ formatDate(currentUpdate.releaseDate) }}
              </p>
            </div>
          </div>
          <div class="flex items-center space-x-2">
            <el-tag :type="getTypeColor(currentUpdate.type)" size="small">
              {{ getTypeText(currentUpdate.type) }}
            </el-tag>
            <el-tag v-if="currentUpdate.isImportant" type="danger" size="small">重要</el-tag>
          </div>
        </div>

        <!-- 更新内容 -->
        <div class="p-4 bg-white dark:bg-gray-800 rounded-lg border">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-2">更新内容</h4>
          <p class="text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{{ currentUpdate.content }}</p>
        </div>

        <!-- 相关链接 -->
        <div v-if="currentUpdate.link" class="p-4 bg-white dark:bg-gray-800 rounded-lg border">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-2">相关链接</h4>
          <a
            :href="currentUpdate.link"
            target="_blank"
            class="text-blue-600 dark:text-blue-400 hover:underline"
          >
            {{ currentUpdate.link }}
          </a>
        </div>

        <!-- 其他信息 -->
        <div class="p-4 bg-white dark:bg-gray-800 rounded-lg border">
          <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-2">其他信息</h4>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="text-gray-500 dark:text-gray-400">创建时间:</span>
              <span class="ml-2 text-gray-700 dark:text-gray-300">{{ formatDate(currentUpdate.createTime) }}</span>
            </div>
            <div>
              <span class="text-gray-500 dark:text-gray-400">更新时间:</span>
              <span class="ml-2 text-gray-700 dark:text-gray-300">{{ formatDate(currentUpdate.updatedAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end space-x-3">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button @click="handleEdit(currentUpdate)">编辑</el-button>
          <el-button @click="handleDelete(currentUpdate)" type="danger">删除</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const updates = ref([])
const selectedUpdates = ref([])
const total = ref(0)
const formRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  type: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20
})

// 对话框
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentUpdate = ref(null)
const dialogTitle = computed(() => currentUpdate.value ? '编辑更新记录' : '新建更新记录')

// 表单数据
const form = reactive({
  version: '',
  title: '',
  type: '',
  content: '',
  releaseDate: '',
  isImportant: false,
  link: ''
})

// 表单验证规则
const rules = {
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' },
    { min: 1, max: 20, message: '版本号长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入更新标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择更新类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入更新内容', trigger: 'blur' },
    { min: 1, max: 1000, message: '内容长度在 1 到 1000 个字符', trigger: 'blur' }
  ],
  releaseDate: [
    { required: true, message: '请选择发布日期', trigger: 'change' }
  ]
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

// 获取类型颜色
const getTypeColor = (type) => {
  const colorMap = {
    feature: 'success',
    bugfix: 'warning',
    optimization: 'info',
    security: 'danger',
    other: ''
  }
  return colorMap[type] || ''
}

// 获取类型文本
const getTypeText = (type) => {
  const textMap = {
    feature: '功能更新',
    bugfix: '问题修复',
    optimization: '性能优化',
    security: '安全更新',
    other: '其他'
  }
  return textMap[type] || '未知'
}

// 加载更新记录列表
const loadUpdates = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const response = await adminApi.getUpdates(params)
    updates.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error('加载更新记录列表失败:', error)
    ElMessage.error('加载更新记录列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadUpdates()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    type: ''
  })
  pagination.page = 1
  loadUpdates()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadUpdates()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadUpdates()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedUpdates.value = selection
}

// 新建更新记录
const handleCreate = () => {
  currentUpdate.value = null
  resetForm()
  dialogVisible.value = true
}

// 编辑更新记录
const handleEdit = (update) => {
  currentUpdate.value = { ...update }
  Object.assign(form, {
    version: update.version,
    title: update.title,
    type: update.type,
    content: update.content,
    releaseDate: update.releaseDate,
    isImportant: update.isImportant || false,
    link: update.link || ''
  })
  dialogVisible.value = true
}

// 查看更新记录详情
const handleView = (update) => {
  currentUpdate.value = update
  detailDialogVisible.value = true
}

// 删除更新记录
const handleDelete = async (update) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除更新记录"${update.title}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await adminApi.deleteUpdate(update.id)
    ElMessage.success('删除成功')
    loadUpdates()
    if (detailDialogVisible.value) {
      detailDialogVisible.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除更新记录失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedUpdates.value.length} 条更新记录吗？此操作不可恢复。`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedUpdates.value.map(item => item.id)
    await adminApi.batchDeleteUpdates(ids)
    ElMessage.success('批量删除成功')
    loadUpdates()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 保存更新记录
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    saving.value = true
    
    if (currentUpdate.value) {
      // 更新记录
      await adminApi.updateUpdate(currentUpdate.value.id, form)
      ElMessage.success('更新成功')
    } else {
      // 创建记录
      await adminApi.createUpdate(form)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadUpdates()
  } catch (error) {
    if (error !== false) {
      console.error('保存更新记录失败:', error)
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    version: '',
    title: '',
    type: '',
    content: '',
    releaseDate: '',
    isImportant: false,
    link: ''
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 关闭对话框
const handleDialogClose = () => {
  dialogVisible.value = false
  currentUpdate.value = null
  resetForm()
}

// 初始化
onMounted(() => {
  loadUpdates()
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
