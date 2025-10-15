<template>
  <div class="space-y-6">
    <!-- 页面标题和操作按钮 -->
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">分类管理</h1>
      <div class="flex space-x-3">
        <el-button @click="handleBatchDelete" type="danger" :disabled="selectedCategories.length === 0">
          批量删除
        </el-button>
        <el-button @click="handleCreate" type="primary">
          <el-icon class="mr-1"><Plus /></el-icon>
          新建分类
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索分类名称或描述"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
          <el-option label="启用" value="active" />
          <el-option label="禁用" value="inactive" />
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

    <!-- 分类列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
      <el-table
        :data="categories"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="name" label="分类名称" min-width="150">
          <template #default="{ row }">
            <div class="flex items-center">
              <div class="w-4 h-4 rounded mr-2" :style="{ backgroundColor: row.color }"></div>
              <span class="font-medium text-gray-900 dark:text-gray-100">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">
            <span class="text-gray-600 dark:text-gray-400">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="articleCount" label="文章数量" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.articleCount || 0 }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序" width="100" />

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="flex space-x-2">
              <el-button @click="handleEdit(row)" size="small">
                编辑
              </el-button>
              <el-button @click="handleToggleStatus(row)" size="small" 
                         :type="row.status === 'active' ? 'warning' : 'success'">
                {{ row.status === 'active' ? '禁用' : '启用' }}
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

    <!-- 分类编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        @submit.prevent
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入分类名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类颜色" prop="color">
          <div class="flex items-center space-x-3">
            <el-color-picker v-model="form.color" />
            <el-input
              v-model="form.color"
              placeholder="#000000"
              maxlength="7"
              style="width: 120px"
            />
          </div>
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="form.sort"
            :min="0"
            :max="9999"
            controls-position="right"
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end space-x-3">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button @click="handleSave" type="primary" :loading="saving">
            {{ currentCategory ? '更新' : '创建' }}
          </el-button>
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
const categories = ref([])
const selectedCategories = ref([])
const total = ref(0)
const formRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20
})

// 对话框
const dialogVisible = ref(false)
const currentCategory = ref(null)
const dialogTitle = computed(() => currentCategory.value ? '编辑分类' : '新建分类')

// 表单数据
const form = reactive({
  name: '',
  description: '',
  color: '#3B82F6',
  sort: 0,
  status: 'active'
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  color: [
    { required: true, message: '请选择分类颜色', trigger: 'change' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

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

// 加载分类列表
const loadCategories = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const response = await adminApi.getCategories(params)
    categories.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error('加载分类列表失败:', error)
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadCategories()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: ''
  })
  pagination.page = 1
  loadCategories()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadCategories()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadCategories()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedCategories.value = selection
}

// 新建分类
const handleCreate = () => {
  currentCategory.value = null
  resetForm()
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (category) => {
  currentCategory.value = { ...category }
  Object.assign(form, {
    name: category.name,
    description: category.description || '',
    color: category.color || '#3B82F6',
    sort: category.sort || 0,
    status: category.status || 'active'
  })
  dialogVisible.value = true
}

// 切换分类状态
const handleToggleStatus = async (category) => {
  try {
    const newStatus = category.status === 'active' ? 'inactive' : 'active'
    await adminApi.updateCategory(category.id, { status: newStatus })
    ElMessage.success(`${newStatus === 'active' ? '启用' : '禁用'}成功`)
    loadCategories()
  } catch (error) {
    console.error('切换分类状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除分类
const handleDelete = async (category) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${category.name}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await adminApi.deleteCategory(category.id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedCategories.value.length} 个分类吗？此操作不可恢复。`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedCategories.value.map(item => item.id)
    await adminApi.batchDeleteCategories(ids)
    ElMessage.success('批量删除成功')
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 保存分类
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    saving.value = true
    
    if (currentCategory.value) {
      // 更新分类
      await adminApi.updateCategory(currentCategory.value.id, form)
      ElMessage.success('更新成功')
    } else {
      // 创建分类
      await adminApi.createCategory(form)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadCategories()
  } catch (error) {
    if (error !== false) {
      console.error('保存分类失败:', error)
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    name: '',
    description: '',
    color: '#3B82F6',
    sort: 0,
    status: 'active'
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 关闭对话框
const handleDialogClose = () => {
  dialogVisible.value = false
  currentCategory.value = null
  resetForm()
}

// 初始化
onMounted(() => {
  loadCategories()
})
</script>
