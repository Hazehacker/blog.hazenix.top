<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 页面头部 -->
    <div class="bg-white dark:bg-gray-800 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="text-center">
          <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-4">
            <el-icon class="mr-3 text-primary"><InfoFilled /></el-icon>
            关于我
          </h1>
          <p class="text-lg text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
            欢迎来到我的博客，这里分享技术心得和生活感悟
          </p>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-8">
        <div class="prose dark:prose-invert max-w-none">
          <h2>关于这个博客</h2>
          <p>这是一个基于Vue 3构建的现代化博客系统，集成了文章管理、分类标签、评论系统、友链管理等功能。</p>
          
          <h3>技术栈</h3>
          <ul>
            <li>前端：Vue 3 + Vite + Element Plus</li>
            <li>样式：Tailwind CSS</li>
            <li>状态管理：Pinia</li>
            <li>路由：Vue Router</li>
            <li>编辑器：Toast UI Editor</li>
          </ul>

          <h3>功能特性</h3>
          <ul>
            <li>响应式设计，支持移动端</li>
            <li>暗色主题切换</li>
            <li>文章分类和标签管理</li>
            <li>评论系统</li>
            <li>友链管理</li>
            <li>用户认证和个人中心</li>
            <li>搜索功能</li>
          </ul>

          <h3>联系方式</h3>
          <p>如果您有任何问题或建议，欢迎通过以下方式联系我：</p>
          <ul>
            <li>邮箱：L3542495583@outlook.com</li>
            <li>GitHub：<a href="https://github.com/HazeHacker" target="_blank">Hazenix</a></li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { InfoFilled } from '@element-plus/icons-vue'
</script>

<style scoped>
.text-primary {
  color: var(--el-color-primary);
}
</style>
