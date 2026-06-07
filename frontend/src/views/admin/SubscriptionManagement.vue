<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">邮件订阅管理</h1>
      <el-button @click="handleExport" :loading="exporting">
        <el-icon class="mr-1"><Download /></el-icon>
        导出邮箱列表
      </el-button>
    </div>

    <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
      <el-table :data="list" v-loading="loading" row-key="id">
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已订阅' : '已退订' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subscribeAt" label="订阅时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.subscribeAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              size="small"
              type="danger"
              link
              @click="handleUnsubscribe(row)"
            >退订</el-button>
            <span v-else class="text-gray-400 text-sm">—</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end p-4">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const exporting = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await adminApi.getSubscriptions({ page: page.value, size: pageSize.value })
    list.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

const handleUnsubscribe = async (row) => {
  await ElMessageBox.confirm(`确认退订 ${row.email} 吗？`, '提示', { type: 'warning' })
  await adminApi.deleteSubscription(row.id)
  ElMessage.success('已退订')
  fetchList()
}

const handleExport = async () => {
  exporting.value = true
  try {
    const res = await adminApi.exportSubscriptions()
    const emails = res.data ?? []
    if (!emails.length) { ElMessage.info('暂无订阅邮箱'); return }
    const blob = new Blob([emails.join('\n')], { type: 'text/plain' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = `subscriptions_${new Date().toISOString().slice(0, 10)}.txt`
    a.click()
    URL.revokeObjectURL(a.href)
  } finally {
    exporting.value = false
  }
}

const formatTime = (t) => t ? t.replace('T', ' ').slice(0, 16) : '—'

onMounted(fetchList)
</script>
