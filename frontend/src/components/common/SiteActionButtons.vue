<template>
  <div class="site-action-buttons">
    <div class="flex gap-4 w-full mt-8">
      <!-- 喜欢本站 -->
      <button
        class="flex-1 h-16 rounded-full flex items-center justify-center gap-2 transition-all focus:outline-none"
        :class="liked
          ? 'bg-gray-200 dark:bg-gray-700 cursor-not-allowed opacity-60'
          : 'bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700'"
        :disabled="liked"
        @click="handleLike"
      >
        <span class="text-lg">❤️</span>
        <div class="flex flex-col items-start">
          <span class="text-sm font-bold" :class="liked ? 'text-gray-500' : 'text-gray-700 dark:text-gray-200'">
            {{ liked ? '已喜欢' : '喜欢本站' }}
          </span>
          <span class="text-xs text-gray-400">{{ likeCount }} 人</span>
        </div>
      </button>

      <!-- 订阅文章 -->
      <button
        class="flex-1 h-16 rounded-full bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 flex items-center justify-center gap-2 transition-all focus:outline-none"
        @click="showSubscribeDialog = true"
      >
        <span class="text-lg">✉️</span>
        <div class="flex flex-col items-start">
          <span class="text-sm font-bold text-gray-700 dark:text-gray-200">订阅文章</span>
          <span class="text-xs text-gray-400">实时推送</span>
        </div>
      </button>

      <!-- 催更 -->
      <button
        class="flex-1 h-16 rounded-full bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 flex items-center justify-center gap-2 transition-all focus:outline-none"
        @click="handleUrge"
      >
        <span class="text-lg">⚡</span>
        <div class="flex flex-col items-start">
          <span class="text-sm font-bold text-gray-700 dark:text-gray-200">催更</span>
          <span class="text-xs text-gray-400">本月 {{ urgeCount }} 人催更</span>
        </div>
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