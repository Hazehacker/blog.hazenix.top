<!-- frontend/src/views/Moments.vue -->
<template>
  <div class="max-w-2xl mx-auto px-4 py-8">
    <!-- 页头 -->
    <div class="mb-8">
      <h1 class="text-3xl font-extrabold text-gray-900 dark:text-gray-100 mb-1">手记</h1>
      <p class="text-sm text-gray-500 dark:text-gray-400">记录生活里那些细碎的光</p>

      <!-- 标签筛选 -->
      <div class="flex flex-wrap gap-2 mt-4">
        <button
          :class="['px-4 py-1 rounded-full text-sm transition-colors', !activeTag ? 'bg-indigo-500 text-white' : 'bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-600 text-gray-600 dark:text-gray-300']"
          @click="filterByTag(null)"
        >
          全部
        </button>
        <button
          v-for="tag in tags"
          :key="tag"
          :class="['px-4 py-1 rounded-full text-sm transition-colors', activeTag === tag ? 'bg-indigo-500 text-white' : 'bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-600 text-gray-600 dark:text-gray-300']"
          @click="filterByTag(tag)"
        >
          #{{ tag }}
        </button>
      </div>
    </div>

    <!-- 时间轴 -->
    <div class="relative">
      <!-- 竖线 -->
      <div class="absolute left-5 top-0 bottom-0 w-0.5 bg-gradient-to-b from-indigo-400 via-indigo-200 to-transparent"></div>

      <div v-if="loading && moments.length === 0" class="flex justify-center py-16">
        <el-icon class="text-2xl animate-spin text-indigo-400"><Loading /></el-icon>
      </div>

      <div v-else-if="moments.length === 0" class="text-center py-16 text-gray-400">
        暂无手记
      </div>

      <div v-else class="space-y-8">
        <div
          v-for="moment in moments"
          :key="moment.id"
          class="flex gap-5"
        >
          <!-- 时间轴节点 -->
          <div class="flex-shrink-0 w-10 flex flex-col items-center pt-4">
            <div class="w-3 h-3 rounded-full bg-indigo-500 border-2 border-white dark:border-gray-900 shadow-[0_0_0_3px_#e0e7ff] dark:shadow-[0_0_0_3px_#3730a3] z-10"></div>
          </div>

          <!-- 卡片 -->
          <div class="flex-1 min-w-0">
            <MomentCard
              :moment="moment"
              @tag-click="filterByTag"
              @detail-click="openDetail"
              @liked="handleLiked"
            />
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div class="mt-8 text-center">
        <el-button v-if="hasMore" :loading="loading" @click="loadMore" round>
          加载更多
        </el-button>
        <p v-else-if="moments.length > 0" class="text-sm text-gray-400">已加载全部手记</p>
      </div>
    </div>

    <!-- 详情 Drawer -->
    <el-drawer
      v-model="drawerVisible"
      direction="rtl"
      size="480px"
      :title="activeMoment?.title || '手记详情'"
    >
      <div v-if="activeMoment" class="p-2">
        <MomentCard :moment="activeMoment" @liked="handleLiked" />
        <div class="mt-6">
          <!-- CommentList uses articleId prop; pass moment id as articleId -->
          <CommentList :article-id="activeMoment.id" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import MomentCard from '@/components/moments/MomentCard.vue'
import CommentList from '@/components/article/CommentList.vue'
import { momentApi } from '@/api/moment'
import { ElMessage } from 'element-plus'

const moments = ref([])
const tags = ref([])
const activeTag = ref(null)
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const drawerVisible = ref(false)
const activeMoment = ref(null)

async function loadMoments(reset = false) {
  if (loading.value) return
  loading.value = true
  try {
    if (reset) {
      page.value = 1
      moments.value = []
      hasMore.value = true
    }
    const res = await momentApi.getPage({ page: page.value, pageSize: 10, tagName: activeTag.value || undefined })
    const records = res.data?.records || []
    moments.value = reset ? records : [...moments.value, ...records]
    hasMore.value = moments.value.length < (res.data?.total || 0)
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  try {
    const res = await momentApi.getTags()
    tags.value = res.data || []
  } catch {}
}

function filterByTag(tag) {
  activeTag.value = tag
  loadMoments(true)
}

function loadMore() {
  page.value++
  loadMoments()
}

async function openDetail(id) {
  try {
    const res = await momentApi.getDetail(id)
    activeMoment.value = res.data
    drawerVisible.value = true
  } catch {
    ElMessage.error('加载详情失败')
  }
}

function handleLiked(id) {
  const m = moments.value.find(m => m.id === id)
  if (m) {
    m.liked = true
    m.likeCount++
  }
  if (activeMoment.value?.id === id) {
    activeMoment.value.liked = true
    activeMoment.value.likeCount++
  }
}

onMounted(() => {
  loadMoments()
  loadTags()
})
</script>
