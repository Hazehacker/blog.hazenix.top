<template>
  <div class="space-y-6">
    <!-- 页面标题和操作按钮 -->
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">文章管理</h1>
      <div class="flex space-x-3">
        <el-button @click="handleBatchDelete" type="danger" :disabled="selectedArticles.length === 0">
          批量删除
        </el-button>
        <el-button @click="handleCreate" type="primary">
          <el-icon class="mr-1"><Plus /></el-icon>
          新建文章
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索文章标题或内容"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="searchForm.categoryId" placeholder="选择分类" clearable>
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>

        <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
          <el-option label="发布" value=0 />
          <el-option label="草稿" value=2 />
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

    <!-- 文章列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
      <el-table
        :data="articles"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center">
              <span class="font-medium text-gray-900 dark:text-gray-100">{{ row.title }}</span>
              <el-tag v-if="row.isTop" type="warning" size="small" class="ml-2">置顶</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.category" type="primary" size="small">
              {{ row.category.name }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="tags" label="标签" width="150">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <el-tag
                v-for="tag in row.tags?.slice(0, 2)"
                :key="tag.id"
                size="small"
                type="info"
              >
                {{ tag.name }}
              </el-tag>
              <span v-if="row.tags?.length > 2" class="text-xs text-gray-500">
                +{{ row.tags.length - 2 }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'warning'" size="small">
              {{ row.status === 0 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="likeCount" label="点赞量" width="100" />
        <el-table-column prop="favoriteCount" label="收藏量" width="100" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="235" fixed="right">
          <template #default="{ row }">
            <div class="flex space-x-2 " >
              <el-button @click="handleView(row)" size="small" type="primary" plain>
                查看
              </el-button>
              <el-button @click="handleEdit(row)" size="small">
                编辑
              </el-button>
              <el-button @click="handleToggleStatus(row)" size="small" 
                         :type="row.status === 0 ? 'warning' : 'success'">
                {{ row.status === 0 ? '下架' : '发布' }}
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

    <!-- 文章编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="90%"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <ToastUIEditor
        v-if="dialogVisible"
        :article="currentArticle"
        :categories="categories"
        :tags="tags"
        @save="handleSave"
        @cancel="handleDialogClose"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import ToastUIEditor from '@/components/admin/ToastUIEditor.vue'



// 响应式数据
const loading = ref(false)
const articles = ref([])
const categories = ref([])
const tags = ref([])
const selectedArticles = ref([])
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: '',
  status: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20
})

// 对话框
const dialogVisible = ref(false)
const currentArticle = ref(null)
const dialogTitle = computed(() => currentArticle.value ? '编辑文章' : '新建文章')

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

// 加载文章列表
const loadArticles = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const response = await adminApi.getArticles(params)
    articles.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    console.error('加载文章列表失败:', error)
    ElMessage.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

// 加载分类和标签
const loadCategoriesAndTags = async () => {
  try {
    const [categoriesRes, tagsRes] = await Promise.all([
      adminApi.getCategories(),
      adminApi.getTags()
    ])
    categories.value = categoriesRes.data
    tags.value = tagsRes.data
  } catch (error) {
    console.error('加载分类和标签失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadArticles()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    categoryId: '',
    status: ''
  })
  pagination.page = 1
  loadArticles()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadArticles()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadArticles()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedArticles.value = selection
}

// 新建文章
const handleCreate = () => {
  currentArticle.value = null
  dialogVisible.value = true
}

// 编辑文章
const handleEdit = (article) => {
  currentArticle.value = { ...article }
  dialogVisible.value = true
}

// 查看文章
const handleView = (article) => {
  window.open(`/article/${article.id}`, '_blank')
}

// 切换文章状态
const handleToggleStatus = async (article) => {
  try {
    const newStatus = article.status === 0 ? 2 : 0
    await adminApi.toggleArticleStatus(article.id, newStatus)
    ElMessage.success(`${newStatus === 0 ? '发布' : '下架'}成功`)
    loadArticles()
  } catch (error) {
    console.error('切换文章状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除文章
const handleDelete = async (article) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文章"${article.title}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await adminApi.deleteArticle(article.id)
    ElMessage.success('删除成功')
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文章失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedArticles.value.length} 篇文章吗？此操作不可恢复。`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedArticles.value.map(item => item.id)
    await adminApi.batchDeleteArticles(ids)
    ElMessage.success('批量删除成功')
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 保存文章
const handleSave = async (articleData) => {
  try {
    if (currentArticle.value) {
      // 更新文章
      await adminApi.updateArticle(currentArticle.value.id, articleData)
      ElMessage.success('更新成功')
    } else {
      // 创建文章
      await adminApi.createArticle(articleData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadArticles()
  } catch (error) {
    console.error('保存文章失败:', error)
    ElMessage.error('保存失败')
  }
}

// 关闭对话框
const handleDialogClose = () => {
  dialogVisible.value = false
  currentArticle.value = null
}

// 初始化
onMounted(() => {
  loadCategoriesAndTags()
  loadArticles()
})
</script>
