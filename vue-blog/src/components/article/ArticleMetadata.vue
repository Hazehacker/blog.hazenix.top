<template>
  <div class="article-metadata">
    <!-- 作者信息 -->
    <div class="metadata-item">
      <el-icon class="metadata-icon"><User /></el-icon>
      <span class="metadata-label">作者：</span>
      <span class="metadata-value">{{ authorName || '未知' }}</span>
    </div>

    <!-- 发布时间 -->
    <div class="metadata-item">
      <el-icon class="metadata-icon"><Calendar /></el-icon>
      <span class="metadata-label">发布时间：</span>
      <span class="metadata-value">{{ formatDate(createdAt) }}</span>
    </div>

    <!-- 更新时间 -->
    <div v-if="updatedAt && updatedAt !== createdAt" class="metadata-item">
      <el-icon class="metadata-icon"><Edit /></el-icon>
      <span class="metadata-label">更新时间：</span>
      <span class="metadata-value">{{ formatDate(updatedAt) }}</span>
    </div>

    <!-- 浏览量 -->
    <div class="metadata-item">
      <el-icon class="metadata-icon"><View /></el-icon>
      <span class="metadata-label">浏览量：</span>
      <span class="metadata-value">{{ views || 0 }}</span>
    </div>

    <!-- 点赞数 -->
    <div class="metadata-item">
      <el-icon class="metadata-icon"><Star /></el-icon>
      <span class="metadata-label">点赞：</span>
      <span class="metadata-value">{{ likes || 0 }}</span>
    </div>

    <!-- 评论数 -->
    <div class="metadata-item">
      <el-icon class="metadata-icon"><ChatDotRound /></el-icon>
      <span class="metadata-label">评论：</span>
      <span class="metadata-value">{{ comments || 0 }}</span>
    </div>

    <!-- 分类 -->
    <div v-if="categoryName" class="metadata-item">
      <el-icon class="metadata-icon"><Folder /></el-icon>
      <span class="metadata-label">分类：</span>
      <el-tag type="primary" size="small" class="metadata-tag">
        {{ categoryName }}
      </el-tag>
    </div>

    <!-- 标签 -->
    <div v-if="tags && tags.length > 0" class="metadata-tags">
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
  PriceTag 
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
  }
})

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.article-metadata {
  @apply space-y-3 text-sm;
}

.metadata-item {
  @apply flex items-center text-gray-600 dark:text-gray-400;
}

.metadata-icon {
  @apply mr-2 text-gray-500 dark:text-gray-500;
}

.metadata-label {
  @apply font-medium mr-2;
}

.metadata-value {
  @apply text-gray-900 dark:text-gray-100;
}

.metadata-tag {
  @apply ml-1;
}

.metadata-tags {
  @apply flex items-start;
}

.tags-container {
  @apply flex flex-wrap gap-1 ml-1;
}

.tag-item {
  @apply cursor-pointer transition-colors hover:bg-blue-100 dark:hover:bg-blue-900/30;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .article-metadata {
    @apply space-y-2;
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