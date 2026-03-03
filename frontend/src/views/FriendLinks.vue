<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 页面头部 -->
    <div class="bg-white dark:bg-gray-800 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="text-center">
          <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-4">
            <el-icon class="mr-3 text-primary"><Link /></el-icon>
            友情链接
          </h1>
          <p class="text-lg text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
            这里是我的友链，希望能为您提供更多有价值的内容
          </p>
        </div>
      </div>
    </div>

    <!-- 友链内容 -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 友链列表 -->
        <div class="lg:col-span-2">
          <FriendLinks />
        </div>

        <!-- 侧边栏 -->
        <div class="space-y-6">
          <!-- 申请友链 -->
          <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
              <el-icon class="mr-2"><Plus /></el-icon>
              申请友链
            </h3>
            <p class="text-gray-600 dark:text-gray-400 mb-4">
              如果您希望与本站建立友情链接，请填写以下信息：
            </p>
            
            <!-- 申请按钮 -->
            <el-button 
              type="primary" 
              size="large" 
              class="w-full mb-4"
              @click="showApplyDialog = true"
            >
              <el-icon class="mr-2"><Plus /></el-icon>
              立即申请友链
            </el-button>
            
            <div class="mt-4 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
              <h4 class="font-medium text-blue-900 dark:text-blue-100 mb-2">申请要求：</h4>
              <ul class="text-sm text-blue-800 dark:text-blue-200 space-y-1">
                <li>• 网站内容健康向上</li>
                <li>• 网站正常运行</li>
                <li>• 愿意互加友链</li>
                <li>• 提供网站名称、描述和头像</li>
              </ul>
            </div>
            
            <!-- 联系方式 -->
            <div class="mt-4 p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <h4 class="font-medium text-gray-900 dark:text-gray-100 mb-2">其他联系方式：</h4>
              <div class="space-y-1 text-sm text-gray-600 dark:text-gray-400">
                <p><strong>邮箱：</strong> L3542495583@outlook.com</p>
                <p class="relative inline-block">
                  <strong 
                  >
                    微信：
                  </strong>
                  <span 
                    class="text-blue-600 dark:text-blue-400 cursor-pointer hover:underline"
                    @mouseenter="showWeChatQR = true"
                    @mouseleave="showWeChatQR = false"
                  >
                    扫码添加
                  </span>
                  <!-- 微信二维码悬停显示 -->
                  <transition name="fade">
                    <div 
                      v-if="showWeChatQR"
                      class="absolute left-0 top-full mt-2 z-50 bg-white dark:bg-gray-800 rounded-lg shadow-xl p-4 border border-gray-200 dark:border-gray-700"
                      style="min-width: 220px; transform: translateX(-50%); left: 50%;"
                      @mouseenter="showWeChatQR = true"
                      @mouseleave="showWeChatQR = false"
                    >
                      <div class="text-center">
                        <img 
                          :src="weChatQRCode" 
                          alt="微信二维码" 
                          class="w-48 h-48 mx-auto mb-2 rounded object-contain"
                          @error="handleQRCodeError"
                        />
                        <p class="text-xs text-gray-600 dark:text-gray-400 mb-1">
                          在本站扫码加我的，请加上
                        </p>
                        <p class="text-xs font-semibold text-blue-600 dark:text-blue-400 mb-1">
                          "[blog]"
                        </p>
                        <p class="text-xs text-gray-600 dark:text-gray-400">
                          前缀，感谢配合
                        </p>
                      </div>
                    </div>
                  </transition>
                </p>
              </div>
            </div>
          </div>

          <!-- 统计信息 -->
          <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
              <el-icon class="mr-2"><DataAnalysis /></el-icon>
              友链统计
            </h3>
            <div class="grid grid-cols-2 gap-4">
              <div class="text-center">
                <div class="text-2xl font-bold text-primary">{{ totalLinks }}</div>
                <div class="text-sm text-gray-600 dark:text-gray-400">友链总数</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-green-500">{{ activeLinks }}</div>
                <div class="text-sm text-gray-600 dark:text-gray-400">活跃友链</div>
              </div>
            </div>
          </div>

          <!-- 快速导航 -->
          <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
              <el-icon class="mr-2"><Guide /></el-icon>
              快速导航
            </h3>
            <div class="space-y-2">
              <router-link 
                to="/home" 
                class="flex items-center p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
                <el-icon class="mr-2 text-primary"><House /></el-icon>
                <span class="text-gray-700 dark:text-gray-300">返回首页</span>
              </router-link>
              <router-link 
                to="/articles" 
                class="flex items-center p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
                <el-icon class="mr-2 text-primary"><Document /></el-icon>
                <span class="text-gray-700 dark:text-gray-300">浏览文章</span>
              </router-link>
              <router-link 
                to="/about" 
                class="flex items-center p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
                <el-icon class="mr-2 text-primary"><InfoFilled /></el-icon>
                <span class="text-gray-700 dark:text-gray-300">关于我</span>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 申请友链弹窗 -->
    <LinkApplyDialog 
      v-model="showApplyDialog" 
      @success="handleApplySuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Link, Plus, DataAnalysis, Guide, House, Document, InfoFilled } from '@element-plus/icons-vue'
import FriendLinks from '@/components/common/FriendLinks.vue'
import LinkApplyDialog from '@/components/common/LinkApplyDialog.vue'
import { frontendApi } from '@/api/frontend'
import weChatQRCodeImage from '@/assets/img/Hazenix.png'

// 响应式数据
const totalLinks = ref(0)
const activeLinks = ref(0)
const showApplyDialog = ref(false)
const showWeChatQR = ref(false)

// 微信二维码图片路径
const weChatQRCode = weChatQRCodeImage

// 处理二维码图片加载错误
const handleQRCodeError = (e) => {
  console.warn('微信二维码图片加载失败，请检查图片路径')
  // 可以设置一个默认的占位图
  e.target.style.display = 'none'
}

// 加载友链统计
const loadStats = async () => {
  try {
    const response = await frontendApi.getLinks({ status: 0 })
    activeLinks.value = response.data?.length || 0
    
    // 获取所有友链（包括审核中的）
    const allResponse = await frontendApi.getLinks()
    totalLinks.value = allResponse.data?.length || 0
  } catch (error) {
    // console.error('加载友链统计失败:', error)
    // 使用模拟数据
    totalLinks.value = 12
    activeLinks.value = 10
  }
}

// 申请成功处理
const handleApplySuccess = () => {
  // 重新加载统计信息
  loadStats()
}

// 初始化
onMounted(() => {
  loadStats()
})
</script>

<style scoped>
/* 确保页面标题样式 */
.text-primary {
  color: var(--el-color-primary);
}

/* 微信二维码悬停动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-10px);
}

.fade-enter-to,
.fade-leave-from {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}
</style>
