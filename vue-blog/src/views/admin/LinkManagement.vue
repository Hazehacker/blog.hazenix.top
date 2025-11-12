<template>
  <div class="space-y-6">
    <!-- 页面标题和操作按钮 -->
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">友链管理</h1>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon class="mr-2"><Plus /></el-icon>
        添加友链
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索网站名称或描述"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="searchForm.status" placeholder="选择状态" clearable @change="handleSearch">
          <el-option label="正常" :value="0" />
          <el-option label="审核中" :value="1" />
        </el-select>

        <el-button type="primary" @click="handleSearch">
          <el-icon class="mr-2"><Search /></el-icon>
          搜索
        </el-button>

        <el-button @click="handleReset">
          <el-icon class="mr-2"><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <!-- 批量操作 -->
    <div v-if="selectedLinks.length > 0" class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
      <div class="flex items-center justify-between">
        <span class="text-blue-700 dark:text-blue-300">
          已选择 {{ selectedLinks.length }} 个友链
        </span>
        <div class="space-x-2">
          <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
        </div>
      </div>
    </div>

    <!-- 友链列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
      <el-table
        v-loading="loading"
        :data="links"
        @selection-change="handleSelectionChange"
        row-key="id"
        stripe
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="name" label="网站名称" min-width="120">
          <template #default="{ row }">
            <div class="flex items-center space-x-3">
              <img
                :src="getLinkAvatar(row.avatar)"
                :alt="row.name"
                class="w-8 h-8 rounded-full object-cover border border-gray-200 dark:border-gray-600"
                @error="handleImageError"
              />
              <div>
                <div class="font-medium text-gray-900 dark:text-gray-100">{{ row.name }}</div>
                <div class="text-sm text-gray-500 dark:text-gray-400">{{ row.url }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />

        <el-table-column prop="sort" label="排序" width="80" align="center" />

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'warning'" size="small">
              {{ row.status === 0 ? '正常' : '审核中' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="space-x-2">
              <el-button size="small" @click="editLink(row)">编辑</el-button>
              <el-button 
                size="small" 
                :type="row.status === 0 ? 'warning' : 'success'"
                @click="toggleStatus(row)"
              >
                {{ row.status === 0 ? '审核' : '通过' }}
              </el-button>
              <el-button size="small" type="danger" @click="deleteLink(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="flex justify-center p-4">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 创建/编辑友链对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingLink ? '编辑友链' : '添加友链'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="网站名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入网站名称" />
        </el-form-item>

        <el-form-item label="网站描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入网站描述"
          />
        </el-form-item>

        <el-form-item label="网站地址" prop="url">
          <el-input v-model="form.url" placeholder="请输入网站地址" />
        </el-form-item>

        <el-form-item label="头像" prop="avatar">
          <div class="flex items-center space-x-4">
            <img
              :src="getLinkAvatar(form.avatar)"
              alt="头像预览"
              class="w-16 h-16 rounded-full object-cover border border-gray-200 dark:border-gray-600"
              @error="handleImageError"
            />
            <el-upload
              :action="uploadAction"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              accept="image/*"
            >
              <el-button type="primary" plain>上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">审核中</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end space-x-2">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ editingLink ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import { getToken } from '@/utils/auth'
import { getAvatarUrl } from '@/utils/helpers'
import { buildApiURL } from '@/utils/apiConfig'
import avatarFallback from '@/assets/img/avatar.jpg'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const editingLink = ref(null)
const selectedLinks = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: null
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 友链列表
const links = ref([])

// 表单数据
const form = reactive({
  name: '',
  description: '',
  url: '',
  avatar: '',
  sort: 0,
  status: 0
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入网站名称', trigger: 'blur' },
    { max: 48, message: '网站名称不能超过48个字符', trigger: 'blur' }
  ],
  description: [
    { max: 255, message: '描述不能超过255个字符', trigger: 'blur' }
  ],
  url: [
    { required: true, message: '请输入网站地址', trigger: 'blur' },
    { type: 'url', message: '请输入正确的URL格式', trigger: 'blur' }
  ],
  sort: [
    { type: 'number', message: '排序必须为数字', trigger: 'blur' }
  ]
}

// 上传配置
const uploadAction = computed(() => buildApiURL('/common/upload'))
const uploadHeaders = {
  Authorization: `Bearer ${getToken()}`
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

// 加载友链列表
const loadLinks = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    
    const response = await adminApi.getLinks(params)
    links.value = response.data.records || response.data
    pagination.total = response.data.total || 0
  } catch (error) {
    // console.error('加载友链列表失败:', error)
    ElMessage.error('加载友链列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadLinks()
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.page = 1
  loadLinks()
}

// 分页处理
const handlePageChange = (page) => {
  pagination.page = page
  loadLinks()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadLinks()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedLinks.value = selection
}

// 编辑友链
const editLink = (link) => {
  editingLink.value = link
  Object.assign(form, {
    name: link.name,
    description: link.description,
    url: link.url,
    avatar: link.avatar,
    sort: link.sort,
    status: link.status
  })
  showCreateDialog.value = true
}

// 切换状态
const toggleStatus = async (link) => {
  try {
    const newStatus = link.status === 0 ? 1 : 0
    await adminApi.updateLinkStatus(link.id, newStatus)
    ElMessage.success('状态更新成功')
    loadLinks()
  } catch (error) {
    // console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

// 删除友链
const deleteLink = async (link) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除友链"${link.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await adminApi.deleteLink(link.id)
    ElMessage.success('删除成功')
    loadLinks()
  } catch (error) {
    if (error !== 'cancel') {
      // console.error('删除友链失败:', error)
      ElMessage.error('删除失败')
    }
  }
}


// 批量删除
const batchDelete = async () => {
  if (selectedLinks.value.length === 0) {
    ElMessage.warning('请先选择要删除的友链')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedLinks.value.length} 个友链吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedLinks.value.map(link => link.id)
    await adminApi.batchDeleteLinks(ids)
    ElMessage.success('批量删除成功')
    selectedLinks.value = []
    loadLinks()
  } catch (error) {
    if (error !== 'cancel') {
      // console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  editingLink.value = null
  Object.assign(form, {
    name: '',
    description: '',
    url: '',
    avatar: '',
    sort: 0,
    status: 0
  })
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    if (editingLink.value) {
      await adminApi.updateLink(editingLink.value.id, form)
      ElMessage.success('更新成功')
    } else {
      await adminApi.createLink(form)
      ElMessage.success('创建成功')
    }

    showCreateDialog.value = false
    loadLinks()
  } catch (error) {
    // console.error('提交失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 上传成功处理
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    form.avatar = response.data.url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败处理
const handleUploadError = (error) => {
  // console.error('上传失败:', error)
  ElMessage.error('上传失败')
}

// 获取友链头像URL
const getLinkAvatar = (avatar) => {
  // 使用getAvatarUrl辅助函数处理头像URL
  // 如果是相对路径，会自动拼接API基础URL
  // 如果为空或无效，返回默认头像
  return getAvatarUrl(avatar, avatarFallback)
}

// 图片加载失败处理
const handleImageError = (event) => {
  // console.warn('友链头像加载失败，使用默认头像:', event.target.src)
  // 直接使用默认头像，与用户头像处理方式一致
  event.target.src = avatarFallback
}

// 表单引用
const formRef = ref()

// 初始化
onMounted(() => {
  loadLinks()
})
</script>

<style scoped>
.el-table {
  --el-table-border-color: var(--el-border-color-lighter);
}

.el-table .el-table__row:hover {
  background-color: var(--el-fill-color-light);
}
</style>
