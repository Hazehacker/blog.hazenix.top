<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 页面头部 -->
    <div class="bg-white dark:bg-gray-800 shadow-sm"style="margin-left: 190px;margin-right: 190px;">
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
      <!-- 独立描述卡片（与下方模块同宽） -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-8 mb-8">
        <div class="text-lg text-gray-700 dark:text-gray-300 space-y-3 leading-8">
          <p>大一下受到学校大佬的启发，种下了做自己博客的小小种子。</p>
          <p>九月份的时候又见到了很多大佬的博客、了解了博客的价值，恰逢一段技术学习的结束，于是决定做一个自己的网站，应用所学技术的同时、为将来求职做好铺垫。</p>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-8">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-6 flex items-center">
          <span class="inline-block w-2 h-2 rounded-full bg-primary mr-3"></span>
          时间线
        </h2>
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
                <div class="absolute -left-2 top-0 w-4 h-4 rounded-full bg-primary border-2 border-white dark:border-gray-800 shadow"></div>
                
                <!-- 更新内容 -->
                <div class="space-y-2">
                  <div class="flex items-center gap-3 flex-wrap">
                    <el-tag type="primary" size="small" effect="dark">{{ update.version }}</el-tag>
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
      
      <!-- 未完待续 -->
      <div class="mt-8 bg-gradient-to-b from-white to-gray-50 dark:from-gray-800 dark:to-gray-900 rounded-lg shadow p-8" style="margin-top: 16px;">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">未完待续</h2>
        <p class="text-gray-700 dark:text-gray-300 mb-2">
          故事没有终点，只有不断展开的章节。<br>
        </p>
        <p class="text-gray-700 dark:text-gray-300 mb-2">
          
          只要心中微光未熄，我便执笔不止，步履不停——<br>
          
        </p>

        <p class="text-gray-700 dark:text-gray-300 mb-2">

          愿我们永远行于路上，不负热爱，不惧远方。
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Clock } from '@element-plus/icons-vue'

// 更新记录数据（前端静态数据）
const updates = ref([
  {
    id: '2025-10-13',
    version: 'v0.1',
    title: '项目立项',
    isImportant: false,
    type: 'other',
    releaseDate: '2025-10-13',
    content: '受到学校大佬的启发，埋下打造个人博客的种子，确定方向与目标。'
  },
  {
    id: '2025-11-05',
    version: 'v1.0',
    title: '首版上线',
    isImportant: true,
    type: 'feature',
    releaseDate: '2025-11-05',
    content: '完成核心功能与基础页面，部署上线，开启公开访问。'
  },
  {
    id: '2025-11-07',
    version: 'v1.0.1',
    title: '微调与正式发布',
    isImportant: false,
    type: 'optimization',
    releaseDate: '2025-11-07',
    content: '根据反馈进行样式与交互微调，完成测试与材料编写，正式发布。'
  }
])

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
