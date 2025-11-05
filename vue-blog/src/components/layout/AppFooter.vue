<template>
  <footer class="relative bg-gray-100 dark:bg-gray-900/50 text-gray-600 dark:text-gray-400" style="background-color:rgb(242, 248, 255);">
    <div class="container mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <!-- 主要内容区域 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <!-- 想要了解我 -->
        <div>
          <h3 class="font-bold text-base text-gray-900 dark:text-white mb-4">
            想要了解我&gt;
          </h3>
          <ul class="space-y-2 text-sm">
            <li>
              <router-link to="/about" class="hover:text-primary transition-colors">
                关于我
              </router-link>
            </li>
            <li>
              <a href="#" class="hover:text-primary transition-colors" @click.prevent="handleSiteHistory">
                本站历史
              </a>
            </li>
            <li>
              <router-link to="/about-project" class="hover:text-primary transition-colors">
                关于此项目
              </router-link>
            </li>
          </ul>
        </div>

        <!-- 你也许在找 -->
        <div>
          <h3 class="font-bold text-base text-gray-900 dark:text-white mb-4">
            你也许在找&gt;
          </h3>
          <ul class="space-y-2 text-sm">
            <li>
              <router-link to="/friend-links" class="hover:text-primary transition-colors">
                友链
              </router-link>
            </li>
            <!-- <li>
              <a href="/rss.xml" class="hover:text-primary transition-colors" target="_blank">
                RSS
              </a>
            </li> -->
          </ul>
        </div>

        <!-- 联系我 -->
        <div>
          <h3 class="font-bold text-base text-gray-900 dark:text-white mb-4">
            联系我叭&gt;
          </h3>
          <ul class="space-y-2 text-sm">
            <li>
              <a href="#" class="hover:text-primary transition-colors" @click.prevent="handleLeaveMessage">
                写留言
              </a>
            </li>
            <li>
              <a :href="`mailto:${email}`" class="hover:text-primary transition-colors">
                发邮件
              </a>
            </li>
            <li>
              <a :href="githubUrl" target="_blank" rel="noopener noreferrer" class="hover:text-primary transition-colors">
                GitHub
              </a>
            </li>
          </ul>
        </div>
      </div>

      <!-- 底部信息 -->
      <div class="pt-4 border-t border-gray-200 dark:border-gray-700">
        <div class="flex flex-col md:flex-row justify-between items-center gap-4 text-xs text-gray-500 dark:text-gray-400">
          <!-- 左侧：备案号和版权 -->
          <div class="flex flex-col md:flex-row items-center gap-2 md:gap-4">
            <p>湘ICP备2025144995号</p>
            <p class="hidden md:inline">|</p>
            <p>Copyright © 2025-2026 Hazenix. 保留所有权利。</p>
          </div>

          <!-- 右侧：技术支持信息 -->
          <div class="flex items-center gap-2">
            <p>Powered by</p>
            <a href="https://github.com/HazeHacker" target="_blank" rel="noopener noreferrer" 
               class="text-primary hover:underline">
              Hazenix Blog
            </a>
          </div>
        </div>
      </div>
    </div>

    <!-- 返回顶部按钮 -->
    <button
      v-show="showBackToTop"
      @click="scrollToTop"
      class="fixed bottom-8 right-8 bg-primary text-white p-3 rounded-full shadow-lg hover:bg-blue-600 transition-all duration-300 z-50 flex items-center justify-center"
      style="width: 48px; height: 48px;"
      aria-label="返回顶部"
    >
      <el-icon :size="20">
        <ArrowUp />
      </el-icon>
    </button>
  </footer>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowUp } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 联系信息
const email = 'L3542495583@outlook.com'
const githubUrl = 'https://github.com/HazeHacker'

// 返回顶部按钮显示状态
const showBackToTop = ref(false)

// 滚动处理
const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  showBackToTop.value = scrollTop > 300
}

// 返回顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

// 写留言处理（可以跳转到树洞或评论区域）
const handleLeaveMessage = () => {
  router.push('/tree-hole')
  ElMessage.info('欢迎在树洞留言~')
}

// 本站历史处理
const handleSiteHistory = () => {
  router.push('/history')
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  handleScroll() // 初始化检查
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* 确保页脚高度在合理范围内 (200px-300px) */
footer {
  min-height: 200px;
  /* 通过调整padding和间距来控制高度，而不是限制max-height */
}

/* 响应式调整 */
@media (max-width: 768px) {
  footer {
    min-height: auto;
  }
}

/* 链接悬停效果 */
a:hover {
  color: var(--el-color-primary);
}

/* 确保router-link悬停效果 */
.router-link-active {
  color: var(--el-color-primary);
}

/* 确保文本颜色正确 */
.text-primary {
  color: var(--el-color-primary);
}
</style>
