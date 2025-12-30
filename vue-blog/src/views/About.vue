<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 页面头部 -->
    <div class="bg-white dark:bg-gray-800 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="text-center">
          <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-4">
            <el-icon class="mr-3 text-primary"><InfoFilled /></el-icon>
            关于我
          </h1>
          <p class="text-lg text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
            欢迎来到我的博客，这里分享技术心得和生活感悟
          </p>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 長文樣式內容 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-8">
        <div class="prose dark:prose-invert max-w-none leading-8">
          <h2>技术栈</h2>
          <p>主攻后端开发，会使用以下技术：</p>
          <!-- <h3>后端与服务</h3> -->
          <ul>
            <li>SpringMVC、SpringBoot、SpringCloud</li>
            <li>MyBatis、MyBatis-Plus</li>
            <li>knife4j 、Nginx、Docker、Jenkins</li>
         
          <!-- <h3>数据库与缓存</h3> -->
          
            <li>MySQL、PostgreSQL、MongoDB</li>
            <li>Redis</li>
          <!-- 中间件-->
            <li>RabbitMQ、Elasticsearch</li>
            <li>Nacos、Seata、Sentinel、SkyWalking</li>
          </ul>
          桌面与跨平台我会这些
          <ul>
            <li>Qt</li>
            <li>uni-app</li>
          </ul>

          <h2>兴趣爱好</h2>
          <ul>
            <li>喜欢运动：乒乓球、网球、羽毛球、健身</li>
            <li>喜欢摄影  (*❦ω❦)</li> 
          </ul>

          <h2>联系方式</h2>
          <p>如果您有任何问题或建议，欢迎通过以下方式联系我：</p>
          <ul>
            <li>邮箱：L3542495583@outlook.com</li>
            <li>GitHub：<a href="https://github.com/HazeHacker" target="_blank">Hazenix</a></li>
            <li>
              微信：
              <span 
                class="relative inline-block text-blue-600 dark:text-blue-400 cursor-pointer hover:underline"
                @mouseenter="showWeChatQR = true"
                @mouseleave="showWeChatQR = false"
              >
                扫码添加
                <!-- 微信二维码悬停显示 -->
                <transition name="fade">
                  <div 
                    v-if="showWeChatQR"
                    class="absolute left-1/2 bottom-full mb-2 z-50 bg-white dark:bg-gray-800 rounded-lg shadow-xl p-4 border border-gray-200 dark:border-gray-700"
                    style="min-width: 220px; transform: translateX(-50%);"
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
              </span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import weChatQRCodeImage from '@/assets/img/Hazenix.png'

// 响应式数据
const showWeChatQR = ref(false)

// 微信二维码图片路径
const weChatQRCode = weChatQRCodeImage

// 处理二维码图片加载错误
const handleQRCodeError = (e) => {
  console.warn('微信二维码图片加载失败，请检查图片路径')
  // 可以设置一个默认的占位图
  e.target.style.display = 'none'
}
</script>

<style scoped>
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
  transform: translateX(-50%) translateY(10px);
}

.fade-enter-to,
.fade-leave-from {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}
</style>
