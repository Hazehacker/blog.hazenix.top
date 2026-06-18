<!-- frontend/src/views/admin/MomentManagement.vue -->
<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">手记管理</h1>
      <el-button type="primary" @click="editorRef.open()">
        <el-icon class="mr-2"><Plus /></el-icon>
        新建手记
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4">
      <div class="flex gap-4">
        <el-input
          v-model="keyword"
          placeholder="搜索标题..."
          clearable
          style="max-width: 280px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 批量操作 -->
    <div v-if="selected.length > 0" class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-3 flex items-center justify-between">
      <span class="text-blue-700 dark:text-blue-300 text-sm">已选 {{ selected.length }} 条</span>
      <el-button size="small" type="danger" @click="handleBatchDelete">批量删除</el-button>
    </div>

    <!-- 列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
      <el-table v-loading="loading" :data="list" @selection-change="selected = $event" stripe row-key="id">
        <el-table-column type="selection" width="55" />
        <el-table-column label="内容" min-width="240">
          <template #default="{ row }">
            <div class="font-medium text-gray-900 dark:text-gray-100 truncate">
              {{ row.title || row.content.slice(0, 30) }}
            </div>
            <div class="text-xs text-gray-400 mt-0.5">{{ formatDate(row.createTime) }} · {{ row.images?.length || 0 }} 张图</div>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="180">
          <template #default="{ row }">
            <el-tag v-for="tag in (row.tags || [])" :key="tag" size="small" class="mr-1 mb-1">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="70" align="center" />
        <el-table-column prop="likeCount" label="点赞" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">
              {{ row.status === 0 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="p-4 flex justify-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </div>

    <MomentEditor ref="editorRef" @saved="loadData" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MomentEditor from '@/components/admin/MomentEditor.vue'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const selected = ref([])
const editorRef = ref()

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.getMoments({ page: page.value, pageSize: pageSize.value, keyword: keyword.value || undefined })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadData()
}

function handleReset() {
  keyword.value = ''
  page.value = 1
  loadData()
}

function handleEdit(row) {
  editorRef.value.open(row)
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除这条手记吗？', '提示', { type: 'warning' })
  await adminApi.deleteMoments([row.id])
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selected.value.length} 条手记吗？`, '提示', { type: 'warning' })
  await adminApi.deleteMoments(selected.value.map(r => r.id))
  ElMessage.success('删除成功')
  selected.value = []
  loadData()
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(loadData)
</script>
