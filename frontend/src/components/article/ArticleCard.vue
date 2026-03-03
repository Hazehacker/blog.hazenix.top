<template>
  <div class="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6 hover:shadow-xl transition-shadow duration-300">
    <div class="flex items-start justify-between mb-4">
      <div class="flex-1">
        <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-2 hover:text-primary cursor-pointer"
            @click="$emit('click', article)">
          {{ article.title }}
        </h3>
        <p class="text-gray-600 dark:text-gray-400 text-sm mb-3">
          {{ article.summary }}
        </p>
      </div>
    </div>
    
    <div class="flex items-center justify-between text-sm text-gray-500 dark:text-gray-400">
      <div class="flex items-center space-x-4">
        <span>{{ article.author }}</span>
        <span>{{ formatDate(article.createTime) }}</span>
        <!-- <span>浏览 {{ article.viewCount }}</span> -->
      </div>
      
      <div class="flex items-center space-x-2">
        <el-tag v-for="tag in article.tags" :key="tag.id || tag" size="small">
          {{ tag.name || tag }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import dayjs from 'dayjs'

defineProps({
  article: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}
</script>
