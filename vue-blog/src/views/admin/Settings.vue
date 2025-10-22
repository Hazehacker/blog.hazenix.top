<template>
  <div class="settings">
    <div class="container mx-auto px-4 py-8">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">
          系统设置
        </h1>

        <div class="space-y-8">
          <!-- 基本设置 -->
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              基本设置
            </h2>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  网站名称
                </label>
                <input
                  v-model="settings.siteName"
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                  placeholder="请输入网站名称"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  网站描述
                </label>
                <textarea
                  v-model="settings.siteDescription"
                  rows="3"
                  class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                  placeholder="请输入网站描述"
                ></textarea>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  网站关键词
                </label>
                <input
                  v-model="settings.siteKeywords"
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                  placeholder="请输入网站关键词，用逗号分隔"
                />
              </div>
            </div>
          </div>

          <!-- 文章设置 -->
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              文章设置
            </h2>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  每页文章数量
                </label>
                <input
                  v-model.number="settings.articlesPerPage"
                  type="number"
                  min="1"
                  max="50"
                  class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                />
              </div>

              <div class="flex items-center">
                <input
                  v-model="settings.allowComments"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label class="ml-2 block text-sm text-gray-700 dark:text-gray-300">
                  允许评论
                </label>
              </div>

              <div class="flex items-center">
                <input
                  v-model="settings.autoApproveComments"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label class="ml-2 block text-sm text-gray-700 dark:text-gray-300">
                  自动审核评论
                </label>
              </div>
            </div>
          </div>

          <!-- 用户设置 -->
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              用户设置
            </h2>
            <div class="space-y-4">
              <div class="flex items-center">
                <input
                  v-model="settings.allowRegistration"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label class="ml-2 block text-sm text-gray-700 dark:text-gray-300">
                  允许用户注册
                </label>
              </div>

              <div class="flex items-center">
                <input
                  v-model="settings.emailVerification"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label class="ml-2 block text-sm text-gray-700 dark:text-gray-300">
                  需要邮箱验证
                </label>
              </div>
            </div>
          </div>

          <!-- 保存按钮 -->
          <div class="flex justify-end">
            <button
              @click="saveSettings"
              :disabled="loading"
              class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {{ loading ? '保存中...' : '保存设置' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const loading = ref(false)

// 设置数据
const settings = reactive({
  siteName: '',
  siteDescription: '',
  siteKeywords: '',
  articlesPerPage: 10,
  allowComments: true,
  autoApproveComments: false,
  allowRegistration: true,
  emailVerification: false
})

// 方法
const fetchSettings = async () => {
  try {
    // 这里应该调用API获取设置
    // const response = await getSettings()
    // Object.assign(settings, response.data)
    
    // 临时设置默认值
    settings.siteName = 'Vue Blog'
    settings.siteDescription = '一个基于Vue.js的博客系统'
    settings.siteKeywords = 'vue,blog,前端,技术'
    settings.articlesPerPage = 10
    settings.allowComments = true
    settings.autoApproveComments = false
    settings.allowRegistration = true
    settings.emailVerification = false
  } catch (error) {
    console.error('获取设置失败:', error)
    ElMessage.error('获取设置失败')
  }
}

const saveSettings = async () => {
  loading.value = true
  try {
    // 这里应该调用API保存设置
    // await saveSettings(settings)
    
    ElMessage.success('设置保存成功')
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  fetchSettings()
})
</script>

<style scoped>
.settings {
  min-height: 100vh;
  background-color: #f8fafc;
}

.dark .settings {
  background-color: #1a202c;
}
</style>
