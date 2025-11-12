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
      <!-- 長文樣式內容 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-8">
        <div class="prose dark:prose-invert max-w-none leading-8">
          <h2>技术栈</h2>
          <p>主攻后端开发，会使用以下技术：</p>
          <!-- <h3>后端与服务</h3> -->
          <ul>
            <li>SpringMVC、SpringBoot</li>
            <li>MyBatis、MyBatis-Plus</li>
            <li>knife4j 接口文档、Nginx、Docker</li>
         
          <!-- <h3>数据库与缓存</h3> -->
          
            <li>MySQL、PostgreSQL、MongoDB</li>
            <li>Redis</li>
          </ul>
          桌面与跨平台我会这些
          <ul>
            <li>Qt</li>
            <li>uni-app</li>
          </ul>

          <h2>兴趣爱好</h2>
          <ul>
            <li>喜欢运动：乒乓球、网球、羽毛球、健身</li>
            <li>喜欢摄影  (*❦ω❦)</li> 
          </ul>

          <h2>联系方式</h2>
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
