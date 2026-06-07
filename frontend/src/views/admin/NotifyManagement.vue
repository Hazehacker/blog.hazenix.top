<template>
  <div class="space-y-6">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">通知配置</h1>

    <!-- SMTP 配置 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-1">SMTP 邮件配置</h2>
      <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">
        同时用于：文章订阅通知（发给订阅者）、每日摘要通知（发给管理员）
      </p>

      <el-form :model="form" label-width="120px" class="max-w-xl" v-loading="configLoading">
        <el-form-item label="SMTP 服务器">
          <el-input v-model="form.smtpHost" placeholder="smtp.example.com" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input-number v-model="form.smtpPort" :min="1" :max="65535" style="width:140px" />
          <el-checkbox v-model="sslEnabled" class="ml-4">SSL</el-checkbox>
        </el-form-item>
        <el-form-item label="发件人邮箱">
          <el-input v-model="form.smtpUsername" placeholder="your@example.com" />
        </el-form-item>
        <el-form-item label="SMTP 密码">
          <el-input v-model="form.smtpPassword" type="password" show-password
                    placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item label="管理员收件箱">
          <el-input v-model="form.recipient" placeholder="admin@example.com" />
          <div class="text-xs text-gray-400 mt-1">每日摘要（评论/树洞/友链申请）发送到此邮箱</div>
        </el-form-item>
        <el-form-item label="每日发送时间">
          <el-time-picker v-model="sendTimeParsed" format="HH:mm" value-format="HH:mm"
                          placeholder="08:00" style="width:140px" />
        </el-form-item>
        <el-form-item label="每日通知">
          <el-switch v-model="dailyEnabled" active-text="开启" inactive-text="关闭" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存配置</el-button>
          <el-button @click="handleTest" :loading="testing" class="ml-2">发送测试邮件</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 通知日志 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-100">每日通知发送日志</h2>
        <el-button size="small" @click="fetchLogs">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>

      <el-table :data="logs" v-loading="logsLoading" size="small">
        <el-table-column prop="statDate" label="统计日期" width="120" />
        <el-table-column prop="sendTime" label="发送时间" width="160">
          <template #default="{ row }">{{ formatTime(row.sendTime) }}</template>
        </el-table-column>
        <el-table-column prop="recipient" label="收件人" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试次数" width="90" />
      </el-table>

      <div class="flex justify-end mt-3">
        <el-pagination
          v-model:current-page="logPage"
          :page-size="10"
          :total="logTotal"
          layout="total, prev, pager, next"
          @current-change="fetchLogs"
          small
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

const configLoading = ref(false)
const saving = ref(false)
const testing = ref(false)
const logsLoading = ref(false)
const logs = ref([])
const logPage = ref(1)
const logTotal = ref(0)

const form = ref({
  smtpHost: '',
  smtpPort: 465,
  smtpUsername: '',
  smtpPassword: '',
  recipient: '',
  sendTime: '08:00',
  smtpSsl: 1,
  enabled: 0
})

const sslEnabled = computed({
  get: () => form.value.smtpSsl === 1,
  set: (v) => { form.value.smtpSsl = v ? 1 : 0 }
})

const dailyEnabled = computed({
  get: () => form.value.enabled === 1,
  set: (v) => { form.value.enabled = v ? 1 : 0 }
})

const sendTimeParsed = computed({
  get: () => form.value.sendTime || '08:00',
  set: (v) => { form.value.sendTime = v }
})

const fetchConfig = async () => {
  configLoading.value = true
  try {
    const res = await adminApi.getNotifyConfig()
    if (res.data) {
      Object.assign(form.value, res.data)
      form.value.smtpPassword = '' // 后端返回掩码，清空让用户按需填写
    }
  } finally {
    configLoading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await adminApi.updateNotifyConfig(form.value)
    ElMessage.success('配置已保存，调度已更新')
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.msg || e.message))
  } finally {
    saving.value = false
  }
}

const handleTest = async () => {
  testing.value = true
  try {
    await adminApi.testNotify()
    ElMessage.success('测试邮件已发送，请检查收件箱')
  } catch (e) {
    ElMessage.error('发送失败：' + (e.response?.data?.msg || e.message))
  } finally {
    testing.value = false
  }
}

const fetchLogs = async () => {
  logsLoading.value = true
  try {
    const res = await adminApi.getNotifyLogs({ page: logPage.value, size: 10 })
    logs.value = res.data?.records ?? []
    logTotal.value = res.data?.total ?? 0
  } finally {
    logsLoading.value = false
  }
}

const formatTime = (t) => t ? t.replace('T', ' ').slice(0, 16) : '—'

onMounted(() => { fetchConfig(); fetchLogs() })
</script>
