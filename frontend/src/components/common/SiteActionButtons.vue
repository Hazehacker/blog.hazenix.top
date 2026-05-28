<template>
  <div class="action-section">
    <!-- Decorative header -->
    <div class="action-header" aria-hidden="true">
      <span class="header-line"></span>
      <span class="header-symbol">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2l2.4 7.2h7.6l-6 4.8 2.4 7.2-6.4-4.8-6.4 4.8 2.4-7.2-6-4.8h7.6z"/>
        </svg>
      </span>
      <span class="header-line"></span>
    </div>

    <div class="action-cards">
      <!-- Like Card -->
      <button
        class="action-card like-card"
        :class="{ 'is-active': liked }"
        :disabled="liked"
        @click="handleLike"
      >
        <span class="card-icon-bg">
          <svg class="card-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
        </span>
        <span class="card-body">
          <span class="card-label">{{ liked ? '已喜欢' : '喜欢本站' }}</span>
          <span class="card-sub">{{ likeCount }} 人</span>
        </span>
      </button>

      <!-- Subscribe Card -->
      <button
        class="action-card subscribe-card"
        @click="showSubscribeDialog = true"
      >
        <span class="card-icon-bg">
          <svg class="card-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="4" width="20" height="16" rx="2"/>
            <path d="M22 6L13.2 12.8a2 2 0 0 1-2.4 0L2 6"/>
          </svg>
        </span>
        <span class="card-body">
          <span class="card-label">订阅文章</span>
          <span class="card-sub">实时推送</span>
        </span>
      </button>

      <!-- Urge Card -->
      <button
        class="action-card urge-card"
        @click="handleUrge"
      >
        <span class="card-icon-bg">
          <svg class="card-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
          </svg>
        </span>
        <span class="card-body">
          <span class="card-label">催更</span>
          <span class="card-sub">本月 {{ urgeCount }} 人催更</span>
        </span>
      </button>
    </div>

    <!-- 订阅弹窗 -->
    <el-dialog v-model="showSubscribeDialog" title="订阅文章更新" width="400px" :close-on-click-modal="true">
      <div class="space-y-4">
        <p class="text-sm text-gray-500 dark:text-gray-400">留下邮箱，新文章发布第一时间通知你</p>
        <el-input v-model="subscribeEmail" placeholder="your@email.com" size="large" />
        <el-button type="primary" class="w-full" @click="handleSubscribe" :loading="subscribing">
          订阅
        </el-button>
        <div class="flex items-center gap-2 text-sm text-gray-400 py-2">
          <div class="flex-1 border-t border-gray-200 dark:border-gray-600"></div>
          <span>或</span>
          <div class="flex-1 border-t border-gray-200 dark:border-gray-600"></div>
        </div>
        <div class="flex items-center gap-2">
          <el-input :modelValue="feedUrl" readonly size="small" />
          <el-button size="small" @click="copyFeed">复制</el-button>
        </div>
        <p class="text-xs text-gray-400">
          <a :href="feedUrl" target="_blank" class="text-blue-500 hover:underline">打开 /feed</a>
        </p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { frontendApi } from '@/api/frontend'

const liked = ref(localStorage.getItem('site_liked') === '1')
const likeCount = ref(0)
const urgeCount = ref(0)
const showSubscribeDialog = ref(false)
const subscribeEmail = ref('')
const subscribing = ref(false)
const feedUrl = ref('https://blog.hazenix.top/feed')

onMounted(() => {
  // 初始化数字显示（可从后端拉取，这里暂时写0）
  likeCount.value = 0
  urgeCount.value = 0
})

const handleLike = async () => {
  if (liked.value) {
    ElMessage.warning('你已喜欢过啦！')
    return
  }
  try {
    const res = await frontendApi.likeSite()
    liked.value = true
    localStorage.setItem('site_liked', '1')
    likeCount.value = res.data?.totalCount || likeCount.value + 1
    ElMessage.success('感谢你的支持！')
  } catch (e) {
    if (e.response?.data?.code === '409') {
      ElMessage.warning('你已喜欢过啦！')
    } else {
      ElMessage.error('操作失败')
    }
  }
}

const handleSubscribe = async () => {
  if (!subscribeEmail.value) {
    ElMessage.warning('请输入邮箱')
    return
  }
  subscribing.value = true
  try {
    await frontendApi.subscribeArticle({ email: subscribeEmail.value })
    ElMessage.success('订阅成功！')
    showSubscribeDialog.value = false
    subscribeEmail.value = ''
  } catch (e) {
    if (e.response?.data?.code === '400') {
      ElMessage.error('邮箱格式错误')
    } else if (e.response?.data?.code === '409') {
      ElMessage.warning('该邮箱已订阅')
    } else {
      ElMessage.error('订阅失败')
    }
  } finally {
    subscribing.value = false
  }
}

const handleUrge = async () => {
  try {
    const res = await frontendApi.urgeArticle()
    urgeCount.value = res.data?.currentCount || urgeCount.value + 1
    ElMessage({
      message: `本月已有 ${urgeCount.value} 人催更，快马加鞭更新中！`,
      duration: 3000
    })
  } catch (e) {
    ElMessage.error('催更失败')
  }
}

const copyFeed = () => {
  navigator.clipboard.writeText(feedUrl.value).then(() => {
    ElMessage.success('已复制')
  })
}
</script>

<style scoped>
/* ===== Layout ===== */
.action-section {
  margin-top: 3.5rem;
}

.action-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.header-line {
  width: 2.5rem;
  height: 1px;
  background: linear-gradient(to right, transparent, #d1d5db, transparent);
}

.header-symbol {
  width: 0.625rem;
  height: 0.625rem;
  color: #9ca3af;
  flex-shrink: 0;
}

.action-cards {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
}

/* ===== Card Base ===== */
.action-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1.25rem;
  border-radius: 0.75rem;
  border: none;
  background: #f6f7f9;
  cursor: pointer;
  transition:
    transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
    box-shadow 0.35s ease,
    background 0.35s ease;
  outline: none;
  -webkit-tap-highlight-color: transparent;
  overflow: hidden;
}

.action-card::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
  z-index: 0;
}

.action-card:hover {
  transform: translateY(-3px);
}

/* ===== Like Card ===== */
.like-card::after {
  background: radial-gradient(ellipse at 30% 50%, rgba(244, 63, 94, 0.08) 0%, transparent 60%);
}
.like-card:hover {
  box-shadow:
    0 8px 28px rgba(244, 63, 94, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.04);
  background: #fef2f3;
}
.like-card:hover::after {
  opacity: 1;
}

.like-card.is-active {
  opacity: 0.55;
  cursor: default;
  filter: grayscale(0.3);
}
.like-card.is-active:hover {
  transform: none;
  box-shadow: none;
  background: #f6f7f9;
}

/* ===== Subscribe Card ===== */
.subscribe-card::after {
  background: radial-gradient(ellipse at 30% 50%, rgba(99, 102, 241, 0.08) 0%, transparent 60%);
}
.subscribe-card:hover {
  box-shadow:
    0 8px 28px rgba(99, 102, 241, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.04);
  background: #f5f4ff;
}
.subscribe-card:hover::after {
  opacity: 1;
}

/* ===== Urge Card ===== */
.urge-card::after {
  background: radial-gradient(ellipse at 30% 50%, rgba(245, 158, 11, 0.08) 0%, transparent 60%);
}
.urge-card:hover {
  box-shadow:
    0 8px 28px rgba(245, 158, 11, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.04);
  background: #fffdf5;
}
.urge-card:hover::after {
  opacity: 1;
}

/* ===== Icon Background ===== */
.card-icon-bg {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 0.625rem;
  flex-shrink: 0;
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.like-card .card-icon-bg {
  background: rgba(244, 63, 94, 0.1);
  color: #e11d48;
}
.subscribe-card .card-icon-bg {
  background: rgba(99, 102, 241, 0.1);
  color: #4f46e5;
}
.urge-card .card-icon-bg {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.like-card.is-active .card-icon-bg {
  background: rgba(156, 163, 175, 0.15);
  color: #9ca3af;
}

.action-card:hover .card-icon-bg {
  transform: scale(1.1);
}
.like-card.is-active:hover .card-icon-bg {
  transform: none;
}

/* ===== Icon SVG ===== */
.card-icon {
  width: 1rem;
  height: 1rem;
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.action-card:hover .card-icon {
  transform: scale(1.05);
}
.like-card.is-active:hover .card-icon {
  transform: none;
}

/* ===== Card Text ===== */
.card-body {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 0;
}

.card-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2937;
  letter-spacing: 0.015em;
  white-space: nowrap;
  line-height: 1.3;
}

.card-sub {
  font-size: 0.75rem;
  font-weight: 400;
  color: #9ca3af;
  margin-top: 0.125rem;
  white-space: nowrap;
  line-height: 1.3;
}

.like-card.is-active .card-sub {
  color: #c4c4c4;
}

/* ===== Dark Mode ===== */
:global(.dark) .action-card {
  background: rgba(30, 41, 59, 0.5);
}

:global(.dark) .header-line {
  background: linear-gradient(to right, transparent, #475569, transparent);
}
:global(.dark) .header-symbol {
  color: #64748b;
}

:global(.dark) .card-label {
  color: #e2e8f0;
}
:global(.dark) .card-sub {
  color: #64748b;
}

:global(.dark) .like-card:hover {
  box-shadow:
    0 8px 32px rgba(244, 63, 94, 0.18),
    0 2px 8px rgba(0, 0, 0, 0.2);
  background: rgba(244, 63, 94, 0.08);
}
:global(.dark) .like-card.is-active:hover {
  box-shadow: none;
  background: rgba(30, 41, 59, 0.5);
}

:global(.dark) .subscribe-card:hover {
  box-shadow:
    0 8px 32px rgba(99, 102, 241, 0.18),
    0 2px 8px rgba(0, 0, 0, 0.2);
  background: rgba(99, 102, 241, 0.08);
}

:global(.dark) .urge-card:hover {
  box-shadow:
    0 8px 32px rgba(245, 158, 11, 0.18),
    0 2px 8px rgba(0, 0, 0, 0.2);
  background: rgba(245, 158, 11, 0.08);
}

:global(.dark) .like-card.is-active .card-icon-bg {
  background: rgba(100, 116, 139, 0.15);
  color: #64748b;
}
:global(.dark) .like-card.is-active .card-sub {
  color: #475569;
}

/* ===== Responsive ===== */
@media (max-width: 640px) {
  .action-cards {
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
  }

  .action-card {
    padding: 0.875rem 1rem;
  }

  .action-header {
    margin-bottom: 1rem;
  }
}
</style>
