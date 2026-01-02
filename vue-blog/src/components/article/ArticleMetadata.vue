<template>
  <div class="article-metadata">
    <!-- 元数据信息水平排列 -->
    <div class="metadata-row">
      <!-- 作者信息 -->
      <div class="metadata-item">
        <el-icon class="metadata-icon"><User /></el-icon>
        <span class="metadata-value">{{ authorName || '未知' }}</span>
      </div>

      <!-- 发布时间 -->
      <div class="metadata-item">
        <el-icon class="metadata-icon"><Calendar /></el-icon>
        <span class="metadata-value">{{ formatDate(createdAt) }}</span>
      </div>



      <!-- 阅读量 -->
      <!-- <div class="metadata-item">
        <el-icon class="metadata-icon"><View /></el-icon>
        <span class="metadata-value">{{ views || 0 }} views</span>
      </div> -->

      <!-- 预计阅读时长 -->
      <div class="metadata-item">
        <el-icon class="metadata-icon"><Clock /></el-icon>
        <span class="metadata-value">预计阅读时长 {{ estimatedReadingTime }} 分钟</span>
      </div>
    </div>

    <!-- 分类和标签 -->
    <div class="metadata-tags-row">
      <!-- 分类 -->
      <div v-if="categoryName" class="metadata-item">
        <el-icon class="metadata-icon"><Folder /></el-icon>
        <span class="metadata-label">分类：</span>
        <el-tag type="primary" size="small" class="metadata-tag">
          {{ categoryName }}
        </el-tag>
      </div>

      <!-- 标签 -->
      <div v-if="tags && tags.length > 0" class="metadata-item">
        <el-icon class="metadata-icon"><PriceTag /></el-icon>
        <span class="metadata-label">标签：</span>
        <div class="tags-container">
          <el-tag
            v-for="tag in tags"
            :key="tag.id || tag"
            size="small"
            type="info"
            class="tag-item"
          >
            {{ tag.name || tag }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { 
  User, 
  Calendar, 
  Edit, 
  View, 
  Star, 
  ChatDotRound, 
  Folder, 
  PriceTag,
  Clock
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const props = defineProps({
  authorName: {
    type: String,
    default: ''
  },
  createdAt: {
    type: [String, Date],
    default: ''
  },
  updatedAt: {
    type: [String, Date],
    default: ''
  },
  views: {
    type: Number,
    default: 0
  },
  likes: {
    type: Number,
    default: 0
  },
  comments: {
    type: Number,
    default: 0
  },
  categoryName: {
    type: String,
    default: ''
  },
  tags: {
    type: Array,
    default: () => []
  },
  content: {
    type: String,
    default: ''
  }
})

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 计算预计阅读时长（按中文阅读速度每分钟300字计算）
const estimatedReadingTime = computed(() => {
  if (!props.content) return 0
  
  // 移除Markdown标记，只计算纯文本长度
  const textContent = props.content
    .replace(/#{1,6}\s+/g, '') // 移除标题标记
    .replace(/\*\*(.*?)\*\*/g, '$1') // 移除粗体标记
    .replace(/\*(.*?)\*/g, '$1') // 移除斜体标记
    .replace(/`(.*?)`/g, '$1') // 移除代码标记
    .replace(/\[(.*?)\]\(.*?\)/g, '$1') // 移除链接标记
    .replace(/!\[.*?\]\(.*?\)/g, '') // 移除图片标记
    .replace(/\n+/g, ' ') // 将换行符替换为空格
    .trim()
  
  const wordCount = textContent.length
  const readingTime = Math.ceil(wordCount / 300) // 每分钟300字
  return Math.max(1, readingTime) // 最少1分钟
})
</script>

<style scoped>
.article-metadata {
  @apply space-y-4 text-sm;
}

.metadata-row {
  @apply flex flex-wrap items-center gap-4 text-gray-600 dark:text-gray-400;
}

.metadata-tags-row {
  @apply flex flex-wrap items-center gap-4;
}

.metadata-item {
  @apply flex items-center text-gray-600 dark:text-gray-400;
}

.metadata-icon {
  @apply mr-1 text-gray-500 dark:text-gray-500;
}

.metadata-label {
  @apply font-medium mr-2;
}

.metadata-value {
  @apply text-gray-900 dark:text-gray-100 font-medium;
}

.metadata-text {
  @apply text-gray-500 dark:text-gray-400;
}

.metadata-tag {
  @apply ml-1;
}

.tags-container {
  @apply flex flex-wrap gap-1 ml-1;
}

.tag-item {
  @apply cursor-pointer transition-colors hover:bg-blue-100 dark:hover:bg-blue-900/30;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .metadata-row {
    @apply flex-col items-start gap-2;
  }
  
  .metadata-tags-row {
    @apply flex-col items-start gap-2;
  }
  
  .metadata-item {
    @apply flex-wrap;
  }
  
  .metadata-label {
    @apply min-w-0 flex-shrink-0;
  }
  
  .tags-container {
    @apply mt-1;
  }
}
</style>