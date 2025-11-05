<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 页面头部 -->
    <div class="bg-white dark:bg-gray-800 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="text-center">
          <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-4">
            <el-icon class="mr-3 text-primary"><Clock /></el-icon>
            本站历史
          </h1>
          <p class="text-lg text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
            记录本站的每一次更新与改进
          </p>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-8">
        <div class="min-h-[400px]">
          <div v-if="updates.length === 0" class="text-center py-12">
            <p class="text-gray-500 dark:text-gray-400">暂无更新记录</p>
          </div>
          
          <div v-else class="prose dark:prose-invert max-w-none">
            <!-- 时间线样式的更新记录 -->
            <div class="space-y-8">
              <div
                v-for="(update, index) in updates"
                :key="update.id"
                class="relative pl-8 pb-8 border-l-2 border-gray-200 dark:border-gray-700 last:border-l-0 last:pb-0"
              >
                <!-- 时间点 -->
                <div class="absolute -left-2 top-0 w-4 h-4 rounded-full bg-primary border-2 border-white dark:border-gray-800"></div>
                
                <!-- 更新内容 -->
                <div class="space-y-2">
                  <div class="flex items-center gap-3 flex-wrap">
                    <el-tag type="primary" size="small">{{ update.version }}</el-tag>
                    <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100 m-0">
                      {{ update.title }}
                    </h3>
                    <el-tag v-if="update.isImportant" type="danger" size="small">重要</el-tag>
                    <el-tag :type="getTypeColor(update.type)" size="small">
                      {{ getTypeText(update.type) }}
                    </el-tag>
                  </div>
                  
                  <p class="text-sm text-gray-500 dark:text-gray-400 m-0">
                    {{ formatDate(update.releaseDate) }}
                  </p>
                  
                  <p class="text-gray-700 dark:text-gray-300 whitespace-pre-wrap m-0">
                    {{ update.content }}
                  </p>
                  
                  <div v-if="update.link" class="mt-2">
                    <a
                      :href="update.link"
                      target="_blank"
                      class="text-blue-600 dark:text-blue-400 hover:underline text-sm"
                    >
                      相关链接 →
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Clock } from '@element-plus/icons-vue'

// 更新记录数据（前端静态数据）
const updates = ref([])

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 获取类型颜色
const getTypeColor = (type) => {
  const colorMap = {
    feature: 'success',
    bugfix: 'warning',
    optimization: 'info',
    security: 'danger',
    other: ''
  }
  return colorMap[type] || ''
}

// 获取类型文本
const getTypeText = (type) => {
  const textMap = {
    feature: '功能更新',
    bugfix: '问题修复',
    optimization: '性能优化',
    security: '安全更新',
    other: '其他'
  }
  return textMap[type] || '未知'
}
</script>

<style scoped>
.text-primary {
  color: var(--el-color-primary);
}

/* 时间线样式 */
.relative.pl-8::before {
  content: '';
  position: absolute;
  left: -2px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, 
    var(--el-color-primary) 0%, 
    transparent 100%);
}
</style>
