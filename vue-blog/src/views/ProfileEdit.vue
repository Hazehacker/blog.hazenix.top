<template>
  <div class="profile-edit">
    <div class="container mx-auto px-4 py-8">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">
          编辑资料
        </h1>

        <div v-if="loading" class="flex justify-center py-8">
          <LoadingSpinner />
        </div>

        <form v-else @submit.prevent="handleSubmit" class="space-y-6">
          <!-- 基本信息 -->
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              基本信息
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  昵称
                </label>
                <input
                  v-model="form.nickname"
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                  placeholder="请输入昵称"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  邮箱
                </label>
                <input
                  v-model="form.email"
                  type="email"
                  class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                  placeholder="请输入邮箱"
                />
              </div>
            </div>

            <div class="mt-6">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                个人简介
              </label>
              <textarea
                v-model="form.bio"
                rows="4"
                class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                placeholder="请输入个人简介"
              ></textarea>
            </div>
          </div>

          <!-- 密码修改 -->
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              密码修改
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  当前密码
                </label>
                <input
                  v-model="form.currentPassword"
                  type="password"
                  class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                  placeholder="请输入当前密码"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  新密码
                </label>
                <input
                  v-model="form.newPassword"
                  type="password"
                  class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                  placeholder="请输入新密码"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  确认新密码
                </label>
                <input
                  v-model="form.confirmPassword"
                  type="password"
                  class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                  placeholder="请确认新密码"
                />
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex justify-end space-x-4">
            <button
              type="button"
              @click="$router.go(-1)"
              class="px-6 py-2 border border-gray-300 dark:border-gray-600 rounded-md text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="submitLoading"
              class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {{ submitLoading ? '保存中...' : '保存修改' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(true)
const submitLoading = ref(false)

// 表单数据
const form = reactive({
  nickname: '',
  email: '',
  bio: '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 方法
const fetchUserInfo = async () => {
  try {
    await userStore.getUserInfo()
    const userInfo = userStore.userInfo || {}
    
    form.nickname = userInfo.nickname || ''
    form.email = userInfo.email || ''
    form.bio = userInfo.bio || ''
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const validateForm = () => {
  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (form.email && !emailRegex.test(form.email)) {
    ElMessage.error('请输入有效的邮箱地址')
    return false
  }

  // 如果要修改密码，验证密码
  if (form.currentPassword || form.newPassword || form.confirmPassword) {
    if (!form.currentPassword) {
      ElMessage.error('请输入当前密码')
      return false
    }
    if (!form.newPassword) {
      ElMessage.error('请输入新密码')
      return false
    }
    if (form.newPassword.length < 6) {
      ElMessage.error('新密码长度不能少于6位')
      return false
    }
    if (form.newPassword !== form.confirmPassword) {
      ElMessage.error('两次输入的密码不一致')
      return false
    }
  }

  return true
}

const handleSubmit = async () => {
  if (!validateForm()) return

  submitLoading.value = true
  try {
    const updateData = {
      nickname: form.nickname,
      email: form.email,
      bio: form.bio
    }

    // 如果修改了密码，添加密码字段
    if (form.currentPassword && form.newPassword) {
      updateData.currentPassword = form.currentPassword
      updateData.newPassword = form.newPassword
    }

    // await updateUserProfile(updateData)
    ElMessage.success('资料更新成功')
    router.push('/profile')
  } catch (error) {
    console.error('更新资料失败:', error)
    ElMessage.error('更新资料失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile-edit {
  min-height: 100vh;
  background-color: #f8fafc;
}

.dark .profile-edit {
  background-color: #1a202c;
}
</style>
