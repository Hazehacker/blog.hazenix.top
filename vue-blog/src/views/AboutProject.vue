<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 页面头部 -->
    <div class="bg-white dark:bg-gray-800 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="text-center">
          <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-4">
            <el-icon class="mr-3 text-primary"><Document /></el-icon>
            关于此项目
          </h1>
          <p class="text-lg text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
            了解这个项目的详细信息
          </p>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-8">
        <div class="prose dark:prose-invert max-w-none">
          <!-- 项目简介 -->
          <section>
            <h2 class="text-2xl font-semibold text-gray-900 dark:text-gray-100 mb-3">项目简介</h2>
            <p class="text-gray-700 dark:text-gray-300">
              Hazenix Blog 是一个专注于内容创作与知识沉淀的个人博客平台，提供文章创作、阅读、检索与互动的完整体验，兼顾性能、可维护性与美观的阅读界面。
            </p>
          </section>

          <!-- 技术架构 -->
          <section class="mt-10">
            <h2 class="text-2xl font-semibold text-gray-900 dark:text-gray-100 mb-3">技术架构</h2>
            <div class="grid md:grid-cols-2 gap-6">
              <div class="rounded-lg border border-gray-200 dark:border-gray-700 p-4">
                <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">前端</h3>
                <ul class="list-disc pl-6 text-gray-700 dark:text-gray-300">
                  <li>Vue 3 + Vite</li>
                  <li>Element Plus 组件库</li>
                  <li>Tailwind CSS 样式体系</li>
                  <li>Toast UI Editor 富文本/Markdown 编辑</li>
                </ul>
              </div>
              <div class="rounded-lg border border-gray-200 dark:border-gray-700 p-4">
                <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">后端与服务</h3>
                <ul class="list-disc pl-6 text-gray-700 dark:text-gray-300">
                  <li>Spring Boot</li>
                  <li>Redis</li>
                  <li>Redis 缓存（热门文章、会话）</li>
                </ul>
              </div>
              <div class="rounded-lg border border-gray-200 dark:border-gray-700 p-4">
                <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">数据与存储</h3>
                <ul class="list-disc pl-6 text-gray-700 dark:text-gray-300">
                  <li>MySQL</li>
                  <li>对象存储OSS</li>
                </ul>
              </div>
              <div class="rounded-lg border border-gray-200 dark:border-gray-700 p-4">
                <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">部署与运维</h3>
                <ul class="list-disc pl-6 text-gray-700 dark:text-gray-300">
                  <li>Docker ，Nginx </li>
                  <li>Jenkins </li>
                  <li>CDN与WAF</li>
                </ul>
              </div>
            </div>
          </section>

          <!-- 核心功能或特色 -->
          <section class="mt-10">
            <h2 class="text-2xl font-semibold text-gray-900 dark:text-gray-100 mb-3">核心功能或特色</h2>
            <ul class="space-y-3 text-gray-700 dark:text-gray-300">
              <li>• 文章创作与管理：Markdown/所见即所得编辑、草稿、发布与置顶。</li>
              <li>• 阅读体验优化：目录生成、代码高亮、暗色模式与响应式布局。</li>
              <li>• 评论与互动：评论列表、回复、表情与通知提醒。</li>
              <li>• 搜索与发现：关键词检索、热门文章、标签/分类聚合。</li>
              <li>• 友情链接：友链展示与申请审核流程。</li>
              <li>• 后台管理：内容、用户与友链等模块的可视化管理。</li>
            </ul>
          </section>

          <!-- 项目背景 -->
          <section class="mt-10">
            <h2 class="text-2xl font-semibold text-gray-900 dark:text-gray-100 mb-3">项目背景</h2>
            <p class="text-gray-700 dark:text-gray-300">
              从想法的发端到正式上线，以及长期维护，请看
              <router-link to="/history" class="text-primary hover:underline font-medium">本站历史</router-link>
              。
            </p>
          </section>

          <!-- 致谢 -->
          <section class="mt-10">
            <h2 class="text-2xl font-semibold text-gray-900 dark:text-gray-100 mb-3">致谢</h2>
            <div class="text-gray-700 dark:text-gray-300 space-y-3">
              <p>
                <strong class="text-gray-900 dark:text-gray-100">前端：</strong>
                Vue 3、Vite、Element Plus、Tailwind CSS、Toast UI Editor、highlight.js
              </p>
              <p>
                <strong class="text-gray-900 dark:text-gray-100">后端：</strong>
                Spring Boot、MyBatis、MySQL、Redis、Druid、Lombok、Knife4j、JWT、Aliyun OSS SDK
              </p>
              <p class="mt-4 text-gray-600 dark:text-gray-400">
                感谢以上优秀的开源项目，为项目开发提供了强大的技术支撑。<br>
                感谢社区文章与文档资料，为项目提供灵感与指引。
              </p>
            </div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Document } from '@element-plus/icons-vue'
</script>

<style scoped>
.text-primary {
  color: var(--el-color-primary);
}
</style>

