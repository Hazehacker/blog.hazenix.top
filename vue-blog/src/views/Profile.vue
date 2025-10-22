<template>
  <div class="profile">
    <div class="container mx-auto px-4 py-8">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">
          个人中心
        </h1>

        <div v-if="loading" class="flex justify-center py-8">
          <LoadingSpinner />
        </div>

        <div v-else class="space-y-8">
          <!-- 用户信息 -->
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              个人信息
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    用户名
                  </label>
                  <input
                    :value="userInfo.username"
                    disabled
                    class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400"
                  />
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    邮箱
                  </label>
                  <input
                    :value="userInfo.email"
                    disabled
                    class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400"
                  />
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    注册时间
                  </label>
                  <input
                    :value="formatDate(userInfo.createdAt)"
                    disabled
                    class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400"
                  />
                </div>
              </div>

              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    昵称
                  </label>
                  <input
                    :value="userInfo.nickname || '未设置'"
                    disabled
                    class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400"
                  />
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    个人简介
                  </label>
                  <textarea
                    :value="userInfo.bio || '未设置'"
                    disabled
                    rows="3"
                    class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400"
                  ></textarea>
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    用户角色
                  </label>
                  <input
                    :value="userInfo.isAdmin ? '管理员' : '普通用户'"
                    disabled
                    class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 统计信息 -->
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              统计信息
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
                <div class="flex items-center">
                  <div class="p-2 bg-blue-100 dark:bg-blue-800 rounded-lg">
                    <i class="fas fa-file-alt text-blue-600 dark:text-blue-400"></i>
                  </div>
                  <div class="ml-4">
                    <p class="text-sm font-medium text-blue-600 dark:text-blue-400">我的文章</p>
                    <p class="text-2xl font-bold text-blue-900 dark:text-blue-100">
                      {{ userStats.articlesCount || 0 }}
                    </p>
                  </div>
                </div>
              </div>

              <div class="bg-green-50 dark:bg-green-900/20 rounded-lg p-4">
                <div class="flex items-center">
                  <div class="p-2 bg-green-100 dark:bg-green-800 rounded-lg">
                    <i class="fas fa-comments text-green-600 dark:text-green-400"></i>
                  </div>
                  <div class="ml-4">
                    <p class="text-sm font-medium text-green-600 dark:text-green-400">我的评论</p>
                    <p class="text-2xl font-bold text-green-900 dark:text-green-100">
                      {{ userStats.commentsCount || 0 }}
                    </p>
                  </div>
                </div>
              </div>

              <div class="bg-purple-50 dark:bg-purple-900/20 rounded-lg p-4">
                <div class="flex items-center">
                  <div class="p-2 bg-purple-100 dark:bg-purple-800 rounded-lg">
                    <i class="fas fa-eye text-purple-600 dark:text-purple-400"></i>
                  </div>
                  <div class="ml-4">
                    <p class="text-sm font-medium text-purple-600 dark:text-purple-400">总浏览量</p>
                    <p class="text-2xl font-bold text-purple-900 dark:text-purple-100">
                      {{ userStats.totalViews || 0 }}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex justify-end space-x-4">
            <button
              @click="$router.push('/profile/edit')"
              class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
            >
              编辑资料
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const userStore = useUserStore()

// 响应式数据
const loading = ref(true)
const userInfo = ref({})
const userStats = ref({
  articlesCount: 0,
  commentsCount: 0,
  totalViews: 0
})

// 方法
const fetchUserInfo = async () => {
  try {
    await userStore.getUserInfo()
    userInfo.value = userStore.userInfo || {}
    
    // 获取用户统计信息
    // const statsResponse = await getUserStats()
    // userStats.value = statsResponse.data || {}
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile {
  min-height: 100vh;
  background-color: #f8fafc;
}

.dark .profile {
  background-color: #1a202c;
}
</style>
