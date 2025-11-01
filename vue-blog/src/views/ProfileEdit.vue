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
            <!-- 第一行：昵称（左）、邮箱（右） -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  昵称
                </label>
                <input
                  v-model="form.username"
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
                  readonly
                  disabled
                  class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-gray-100 dark:bg-gray-600 cursor-not-allowed text-gray-500 dark:text-gray-400"
                  placeholder="请输入邮箱"
                />
              </div>
            </div>

            <!-- 第二行：性别（左）、头像（右） -->
            <div class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">性别</label>
                <select
                  v-model="form.gender"
                  class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                >
                  <option :value="0">保密</option>
                  <option :value="1">男</option>
                  <option :value="2">女</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">头像</label>
                <div class="flex items-center space-x-4">
                  <!-- 头像预览 -->
                  <div class="flex-shrink-0">
                    <div class="w-20 h-20 rounded-full overflow-hidden border-2 border-gray-300 dark:border-gray-600 bg-gray-100 dark:bg-gray-700">
                      <img
                        v-if="avatarPreview || form.avatar"
                        :src="avatarPreview || form.avatar"
                        alt=""
                        class="w-full h-full object-cover"
                      />
                      <div
                        v-else
                        class="w-full h-full flex items-center justify-center text-gray-400 dark:text-gray-500"
                      >
                        <svg class="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 上传按钮 -->
                  <div class="flex-1">
                    <input
                      ref="fileInputRef"
                      type="file"
                      accept="image/*"
                      @change="handleFileChange"
                      class="hidden"
                    />
                    <button
                      type="button"
                      @click="triggerFileUpload"
                      :disabled="uploading"
                      class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      {{ uploading ? '上传中...' : '选择头像' }}
                    </button>
                    <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">
                      支持 JPG、PNG 格式，建议尺寸 200x200 像素
                    </p>
                  </div>
                </div>
              </div>
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
import { uploadImage, updateProfile, updatePassword } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(true)
const submitLoading = ref(false)
const uploading = ref(false)
const avatarPreview = ref('')
const fileInputRef = ref(null)

// 表单数据
const form = reactive({
  username: '',
  email: '',
  gender: 0,
  avatar: '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 方法
const fetchUserInfo = async () => {
  try {
    await userStore.getUserInfo()
    const userInfo = userStore.userInfo || {}
    
    form.username = userInfo.username || ''
    form.email = userInfo.email || ''
    form.gender = userInfo.gender ?? 0
    form.avatar = userInfo.avatar || ''
    avatarPreview.value = ''
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 触发文件选择
const triggerFileUpload = () => {
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }

  // 验证文件大小（限制为 5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  // 显示预览
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target.result
  }
  reader.readAsDataURL(file)

  // 上传文件
  uploading.value = true
  try {
    const response = await uploadImage(file)
    if (response.code === 200) {
      form.avatar = response.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(response.msg || '头像上传失败')
      avatarPreview.value = ''
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请重试')
    avatarPreview.value = ''
  } finally {
    uploading.value = false
    // 清空input，以便可以再次选择同一文件
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

const validateForm = () => {
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
    // 更新基本信息（不包含邮箱，邮箱不能修改）
    const updateData = {
      username: form.username,
      gender: form.gender,
      avatar: form.avatar
    }

    await updateProfile(updateData)

    // 如果修改了密码，单独调用密码修改接口
    if (form.currentPassword && form.newPassword) {
      await updatePassword({
        currentPassword: form.currentPassword,
        newPassword: form.newPassword
      })
    }

    // 更新用户store中的信息
    await userStore.getUserInfo()

    ElMessage.success('资料更新成功')
    router.push('/profile')
  } catch (error) {
    console.error('更新资料失败:', error)
    ElMessage.error(error.response?.data?.msg || error.message || '更新资料失败，请重试')
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
