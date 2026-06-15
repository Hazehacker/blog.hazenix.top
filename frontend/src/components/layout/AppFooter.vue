<template>
  <footer class="relative text-gray-600 dark:text-gray-400" style="background-color:rgb(242, 248, 255);">
    <div class="container mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <!-- 主要内容区域 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-3 md:gap-6 mb-6">
        <!-- 想要了解我 -->
        <div class="footer-card">
          <h3 class="footer-card-title">👤 想要了解我</h3>
          <div class="footer-card-links">
            <router-link to="/about" class="footer-link">关于我</router-link>
            <a class="footer-link" @click.prevent="handleSiteHistory">本站历史</a>
            <router-link to="/about-project" class="footer-link">关于此项目</router-link>
          </div>
        </div>

        <!-- 你也许在找 -->
        <div class="footer-card">
          <h3 class="footer-card-title">🔍 你也许在找</h3>
          <div class="footer-card-links">
            <router-link to="/friend-links" class="footer-link">友链</router-link>
            <a href="https://www.hazenix.top" target="_blank" class="footer-link">作品集</a>
          </div>
        </div>

        <!-- 联系我 -->
        <div class="footer-card">
          <h3 class="footer-card-title">📬 联系我</h3>
          <div class="footer-card-links">
            <a class="footer-link" @click.prevent="handleLeaveMessage">写留言</a>
            <a :href="`mailto:${email}`" class="footer-link">发邮件</a>
            <a :href="githubUrl" target="_blank" rel="noopener noreferrer" class="footer-link">GitHub</a>
          </div>
        </div>
      </div>

      <!-- 底部信息 -->
      <div class="pt-4 border-t border-gray-200 dark:border-gray-700">
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-3 text-xs text-gray-500 dark:text-gray-400">
          <!-- Left: ICP and copyright - left aligned on mobile -->
          <div class="flex flex-col gap-1">
            <p>湘ICP备2025144995号</p>
            <p>Copyright © 2025-2026 Hazenix. 保留所有权利。</p>
          </div>

          <!-- Right: Powered by -->
          <div class="flex items-center gap-1">
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


// 写留言处理（跳转到留言板）
const handleLeaveMessage = () => {
  router.push('/messageboard')
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
    padding: 24px 0 16px;
  }

  .footer-card {
    background: #f8fafc;
    border-radius: 10px;
    padding: 14px;
  }

  .dark .footer-card {
    background: rgba(30, 41, 59, 0.6);
  }

  .footer-card-title {
    font-size: 14px;
    font-weight: 700;
    color: #1f2937;
    margin: 0 0 10px;
  }

  .dark .footer-card-title {
    color: #e5e7eb;
  }

  .footer-card-links {
    display: flex;
    gap: 14px;
    flex-wrap: wrap;
  }

  .footer-link {
    font-size: 13px;
    color: var(--el-color-primary);
    cursor: pointer;
    text-decoration: none;
  }

  .footer-link:hover {
    text-decoration: underline;
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
