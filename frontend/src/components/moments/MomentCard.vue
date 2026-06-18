<!-- frontend/src/components/moments/MomentCard.vue -->
<template>
  <div class="bg-white dark:bg-gray-800 rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow">
    <!-- 图片区域 -->
    <MomentImageGrid v-if="moment.images?.length" :images="moment.images" />

    <!-- 内容区域 -->
    <div class="p-5">
      <!-- 日期 -->
      <div class="text-xs text-indigo-500 font-semibold tracking-wider mb-2">
        {{ formatDate(moment.createTime) }}
      </div>

      <!-- 标题 -->
      <h3 v-if="moment.title" class="text-base font-bold text-gray-900 dark:text-gray-100 mb-2 leading-snug">
        {{ moment.title }}
      </h3>

      <!-- 正文 -->
      <p class="text-sm text-gray-600 dark:text-gray-400 leading-relaxed mb-3 whitespace-pre-wrap">
        {{ moment.content }}
      </p>

      <!-- 标签 -->
      <div v-if="moment.tags?.length" class="flex flex-wrap gap-1.5 mb-3">
        <span
          v-for="tag in moment.tags"
          :key="tag"
          class="px-2.5 py-0.5 rounded-full text-xs bg-indigo-50 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-300 cursor-pointer"
          @click.stop="$emit('tag-click', tag)"
        >
          #{{ tag }}
        </span>
      </div>

      <!-- 底栏 -->
      <div class="flex items-center gap-4 pt-3 border-t border-gray-100 dark:border-gray-700 text-xs text-gray-400">
        <span class="flex items-center gap-1">
          <el-icon><View /></el-icon>
          {{ moment.viewCount }}
        </span>
        <span class="flex items-center gap-1">
          <el-icon><ChatDotRound /></el-icon>
          {{ moment.commentCount || 0 }}
        </span>
        <button
          :class="['flex items-center gap-1 transition-colors', moment.liked ? 'text-red-500' : 'hover:text-red-400']"
          @click.stop="handleLike"
        >
          <el-icon><component :is="moment.liked ? 'StarFilled' : 'Star'" /></el-icon>
          {{ moment.likeCount }}
        </button>
        <span class="ml-auto text-indigo-400 cursor-pointer hover:text-indigo-600" @click="$emit('detail-click', moment.id)">
          查看详情 →
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { View, ChatDotRound, Star, StarFilled } from '@element-plus/icons-vue'
import MomentImageGrid from './MomentImageGrid.vue'
import { momentApi } from '@/api/moment'
import { ElMessage } from 'element-plus'

const props = defineProps({
  moment: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['tag-click', 'detail-click', 'liked'])

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}

async function handleLike() {
  if (props.moment.liked) {
    ElMessage.info('您已经点过赞了')
    return
  }
  try {
    await momentApi.like(props.moment.id)
    emit('liked', props.moment.id)
  } catch (e) {
    ElMessage.warning(e?.response?.data?.msg || '点赞失败')
  }
}
</script>
