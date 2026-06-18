# 移动端响应式优化 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 对博客进行四方面移动端优化：全屏覆盖式导航、Hero 垂直堆叠、文章详情页功能补全、页脚卡片布局。

**Architecture:** 纯前端改动，只修改现有 Vue 组件（AppHeader、Home、ArticleDetail、AppFooter、TableOfContents），不创建新文件。所有改动在 `@media (max-width: 768px)` 或 Tailwind `md:` 断点内生效，桌面端不受影响。

**Tech Stack:** Vue 3 + Tailwind CSS + Element Plus

---

### Task 1: AppHeader — 移动端全屏覆盖式汉堡菜单

**Files:**
- Modify: `frontend/src/components/layout/AppHeader.vue`

- [ ] **Step 1: 添加移动端导航汉堡按钮和覆盖菜单**

在 `<template>` 中的 `<header>` 内，修改移动端显示结构。当前导航 `hidden md:flex` 保持不变，在其同级新增移动端专用元素。

修改 header 内左侧部分（替换现有 `margin-left: 30px` 的 div）：

```html
<header class="fixed top-0 left-0 right-0 z-50 flex justify-between items-center py-6 bg-white dark:bg-gray-900/90 backdrop-blur-sm shadow-md" style="height: 70px;">
  <!-- 左侧：桌面 logo + 移动端头像汉堡 -->
  <div class="flex items-center space-x-2" style="margin-left: 30px;">
    <!-- 桌面端 logo -->
    <img :src="avatarUrl" 
         alt="User avatar" 
         class="w-10 h-10 rounded-full hidden md:block"
         @error="onAvatarError" />
    
    <!-- 移动端：头像作为汉堡菜单触发器 -->
    <div class="flex items-center space-x-2 md:hidden cursor-pointer" @click="openMobileMenu">
      <img :src="avatarUrl" 
           alt="Menu" 
           class="w-9 h-9 rounded-full"
           @error="onAvatarError" />
      <span class="text-base font-semibold text-gray-800 dark:text-gray-200">Hazenix的后端札记</span>
    </div>
  </div>
  
  <!-- 桌面导航（不变） -->
  <nav class="hidden md:flex items-center space-x-8 text-gray-600 dark:text-gray-400">
    <!-- ... existing nav content unchanged ... -->
  </nav>
  
  <!-- 右侧操作区保持不变 -->
  <div class="flex items-center space-x-4" style="margin-right: 20px;">
    <!-- ... existing right side buttons unchanged ... -->
  </div>
</header>
```

- [ ] **Step 2: 添加全屏覆盖菜单的 DOM 结构**

在 `</header>` 之后、template 结束前添加覆盖层：

```html
<!-- 移动端全屏覆盖菜单 -->
<teleport to="body">
  <transition name="mobile-menu-fade">
    <div v-if="mobileMenuVisible" class="mobile-menu-overlay">
      <div class="mobile-menu-panel">
        <!-- 顶部关闭栏 -->
        <div class="flex justify-between items-center p-4">
          <span class="text-lg font-semibold text-gray-800 dark:text-gray-200">导航</span>
          <el-button link @click="closeMobileMenu" class="text-2xl">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        
        <!-- 导航列表 -->
        <div class="mobile-menu-items">
          <router-link to="/home" class="mobile-menu-item" @click="closeMobileMenu">
            <span class="text-2xl">🏠</span>
            <span>首页</span>
          </router-link>
          
          <div class="mobile-menu-item" @click="toggleMobileCategories">
            <span class="text-2xl">📝</span>
            <span>文章</span>
            <el-icon class="ml-auto"><ArrowDown :class="{ 'rotate-180': mobileCategoriesExpanded }" /></el-icon>
          </div>
          <div v-show="mobileCategoriesExpanded" class="mobile-sub-items">
            <router-link 
              v-for="cat in categories" :key="cat.id"
              :to="`/articles?categoryId=${cat.id}`"
              class="mobile-sub-item"
              @click="closeMobileMenu"
            >
              {{ cat.name }} <span class="text-gray-400">({{ cat.articleCount }})</span>
            </router-link>
          </div>
          
          <router-link to="/tags" class="mobile-menu-item" @click="closeMobileMenu">
            <span class="text-2xl">🏷️</span>
            <span>标签</span>
          </router-link>
          <router-link to="/tree-hole" class="mobile-menu-item" @click="closeMobileMenu">
            <span class="text-2xl">🌳</span>
            <span>树洞</span>
          </router-link>
          <router-link to="/friend-links" class="mobile-menu-item" @click="closeMobileMenu">
            <span class="text-2xl">🔗</span>
            <span>友链</span>
          </router-link>
          <router-link to="/about" class="mobile-menu-item" @click="closeMobileMenu">
            <span class="text-2xl">👤</span>
            <span>关于我</span>
          </router-link>
          <a href="https://monitor.hazenix.top/share/UOi3M8l7j4ZSjDn3" target="_blank" class="mobile-menu-item" @click="closeMobileMenu">
            <span class="text-2xl">📊</span>
            <span>监控</span>
          </a>
          <a href="https://www.hazenix.top" target="_blank" class="mobile-menu-item" @click="closeMobileMenu">
            <span class="text-2xl">💼</span>
            <span>作品集</span>
          </a>
        </div>
        
        <!-- 底部分割+登录 -->
        <div class="mobile-menu-footer">
          <div v-if="userStore.token && userStore.userInfo" class="mobile-menu-item" @click="goToProfile">
            <img :src="avatarUrl" class="w-6 h-6 rounded-full" @error="onAvatarError" />
            <span>个人中心</span>
          </div>
          <div v-else class="mobile-menu-item" @click="openLoginFromMobile">
            <span class="text-2xl">👤</span>
            <span>登录</span>
          </div>
        </div>
      </div>
    </div>
  </transition>
</teleport>
```

- [ ] **Step 3: 添加 script 中的响应式状态和方法**

在 `<script setup>` 中添加：

```js
import { Close } from '@element-plus/icons-vue'

// 移动端菜单状态
const mobileMenuVisible = ref(false)
const mobileCategoriesExpanded = ref(false)

const openMobileMenu = () => {
  mobileMenuVisible.value = true
  document.body.style.overflow = 'hidden'
}

const closeMobileMenu = () => {
  mobileMenuVisible.value = false
  mobileCategoriesExpanded.value = false
  document.body.style.overflow = ''
}

const toggleMobileCategories = () => {
  mobileCategoriesExpanded.value = !mobileCategoriesExpanded.value
}

const openLoginFromMobile = () => {
  closeMobileMenu()
  window.dispatchEvent(new CustomEvent('open-login-dialog'))
}

const goToProfile = () => {
  closeMobileMenu()
  router.push('/profile')
}
```

- [ ] **Step 4: 添加移动端菜单样式**

在 `<style scoped>` 末尾添加（在文件最后的 `</style>` 前）：

```css
/* 移动端全屏覆盖菜单 */
.mobile-menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(255, 255, 255, 0.98);
}

.dark .mobile-menu-overlay {
  background: rgba(17, 24, 39, 0.98);
}

.mobile-menu-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: env(safe-area-inset-top) 0 env(safe-area-inset-bottom);
}

.mobile-menu-items {
  flex: 1;
  overflow-y: auto;
  padding: 0 24px;
}

.mobile-menu-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  text-decoration: none;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: color 0.2s;
}

.dark .mobile-menu-item {
  color: #e5e7eb;
  border-bottom-color: #1f2937;
}

.mobile-menu-item:active {
  color: var(--el-color-primary);
}

.mobile-sub-items {
  padding-left: 40px;
  padding-bottom: 8px;
}

.mobile-sub-item {
  display: block;
  padding: 10px 0;
  font-size: 15px;
  color: #6b7280;
  text-decoration: none;
  border-bottom: 1px solid #f9fafb;
}

.dark .mobile-sub-item {
  color: #9ca3af;
  border-bottom-color: #1f2937;
}

.mobile-menu-footer {
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
}

.dark .mobile-menu-footer {
  border-top-color: #374151;
}

/* 过渡动画 */
.mobile-menu-fade-enter-active {
  transition: opacity 0.25s ease;
}
.mobile-menu-fade-enter-active .mobile-menu-panel {
  transition: transform 0.25s ease;
}
.mobile-menu-fade-leave-active {
  transition: opacity 0.2s ease;
}
.mobile-menu-fade-leave-active .mobile-menu-panel {
  transition: transform 0.2s ease;
}
.mobile-menu-fade-enter-from {
  opacity: 0;
}
.mobile-menu-fade-enter-from .mobile-menu-panel {
  transform: translateY(10px);
}
.mobile-menu-fade-leave-to {
  opacity: 0;
}
```

- [ ] **Step 5: 验证导航功能**

启动开发服务器，在浏览器 DevTools 切换到移动端视口（375px/414px），检查：
- 桌面端导航正常显示（`md:` 断点以上）
- 移动端显示头像+"Hazenix的后端札记"
- 点击头像打开全屏菜单
- 所有导航链接可点击并跳转
- 文章分类可展开/收起
- 关闭按钮和路由跳转后菜单关闭
- 暗色模式下样式正确

---

### Task 2: Home.vue — 移动端 Hero 垂直堆叠

**Files:**
- Modify: `frontend/src/views/Home.vue`

- [ ] **Step 1: 修改 Hero 区域布局**

将第 1-21 行的 Hero 区域改为响应式布局：

```html
<div class="py-8 md:py-24">
  <!-- 主题切换引导 -->
  <ThemeGuide v-model="showThemeGuide" />
  
  <!-- 移动端 Hero：垂直居中堆叠 -->
  <div class="flex flex-col items-center text-center md:hidden mb-10">
    <img src="@/assets/img/avatar.jpg" 
         alt="Hazenix" 
         class="w-20 h-20 rounded-full mb-5 shadow-lg" />
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-3" style="font-family: 'Playfair Display', 'Times New Roman', serif;">
      Hazenix's Blog
    </h1>
    <p class="text-base text-primary font-semibold mb-2 px-4">
      Only those who keep walking light up the long road of time
    </p>
    <p class="text-sm text-gray-500 dark:text-gray-400">
      我会一直走，走到灯火通明
    </p>
    <!-- 移动端统计数据 -->
    <div class="flex justify-center space-x-6 mt-6">
      <div class="text-center">
        <div class="text-lg font-bold text-primary">{{ latestArticles.length }}</div>
        <div class="text-xs text-gray-400">文章</div>
      </div>
      <div class="text-center">
        <div class="text-lg font-bold text-primary">{{ totalViews }}</div>
        <div class="text-xs text-gray-400">浏览</div>
      </div>
    </div>
  </div>
  
  <!-- 桌面端 Hero：保持当前横排布局 -->
  <div class="hidden md:flex flex-col md:flex-row justify-between items-start gap-12">
    <!-- ... 现有 Hero 桌面端代码保持不变 ... -->
  </div>
```

- [ ] **Step 2: 调整最新文章标题在移动端的字号**

将第 27 行的 `<h2>` 改为响应式：

```html
<h2 class="text-xl md:text-3xl font-bold mb-8">最新文章</h2>
```

- [ ] **Step 3: 添加计算属性 totalViews（可选统计数据）**

在 `<script setup>` 中添加：

```js
const totalViews = computed(() => {
  return latestArticles.value.reduce((sum, a) => sum + (a.viewCount || 0), 0)
})
```

- [ ] **Step 4: 验证 Hero 显示**

在移动端视口检查：
- 头像居中显示
- 标题 `text-2xl`（约 1.5rem）合适
- 英文副标题和中文副标题层级清楚
- 桌面端布局不变

---

### Task 3: TableOfContents — 支持移动端可折叠模式

**Files:**
- Modify: `frontend/src/components/article/TableOfContents.vue`

- [ ] **Step 1: 添加移动端折叠模式**

在 `<template>` 中，将现有目录内容包裹在可折叠容器中（仅在 `isMobile` 时启用折叠）：

```html
<template>
  <div class="toc-container" :class="{ 'toc-mobile': isMobile }" style="margin-top: 50px;">
    <!-- 移动端折叠头部 -->
    <div v-if="isMobile" class="toc-collapse-header" @click="toggleMobileToc">
      <div class="flex items-center gap-2">
        <el-icon><Document /></el-icon>
        <span class="font-semibold text-sm">目录</span>
      </div>
      <el-icon :class="{ 'rotate-180': mobileTocExpanded }">
        <ArrowDown />
      </el-icon>
    </div>
    
    <!-- 目录内容（移动端可折叠） -->
    <div v-show="!isMobile || mobileTocExpanded">
      <!-- 桌面端滚动进度（不变） -->
      <div v-if="!isMobile" class="scroll-progress" @click="scrollToTop">
        <div class="progress-text">{{ scrollProgress }}% ↑ 返回顶部</div>
      </div>

      <!-- 目录标题（桌面端） -->
      <div v-if="!isMobile" class="toc-header">
        <h3 class="toc-title">
          <el-icon class="mr-2"><Document /></el-icon>
          目录
        </h3>
      </div>

      <!-- 目录内容（不变） -->
      <div v-if="tocItems.length > 0" class="toc-content">
        <!-- ... existing toc content unchanged ... -->
      </div>

      <!-- 空状态 -->
      <div v-else class="toc-empty">
        <p class="text-sm text-gray-500 dark:text-gray-400">暂无目录</p>
      </div>

      <!-- 文章统计信息（桌面端不变） -->
      <div v-if="!isMobile && article" class="article-stats" style="display: flex;padding-left: 40px;">
        <!-- ... existing stats unchanged ... -->
      </div>
    </div>
  </div>
</template>
```

- [ ] **Step 2: 添加 script 逻辑**

在 `<script setup>` 中添加：

```js
import { ArrowDown } from '@element-plus/icons-vue'

const mobileTocExpanded = ref(false)

const toggleMobileToc = () => {
  mobileTocExpanded.value = !mobileTocExpanded.value
}
```

- [ ] **Step 3: 添加移动端折叠样式**

在 `<style scoped>` 的 `@media (max-width: 768px)` 块中替换/扩展：

```css
@media (max-width: 768px) {
  .toc-container {
    @apply rounded-lg border mx-0;
    margin-top: 0 !important;
  }
  
  .toc-mobile {
    background: transparent;
    border: none;
    box-shadow: none;
  }
  
  .toc-collapse-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #f0f7ff;
    border-radius: 10px;
    cursor: pointer;
    user-select: none;
    transition: background 0.2s;
  }
  
  .dark .toc-collapse-header {
    background: rgba(59, 130, 246, 0.1);
  }
  
  .toc-collapse-header:active {
    background: #e0efff;
  }
  
  .dark .toc-collapse-header:active {
    background: rgba(59, 130, 246, 0.2);
  }
  
  .toc-content {
    padding: 8px 0;
  }
  
  .article-stats,
  .toc-actions {
    @apply hidden;
  }
}
```

---

### Task 4: ArticleDetail.vue — 移动端标签+按钮移至底部

**Files:**
- Modify: `frontend/src/views/ArticleDetail.vue`

- [ ] **Step 1: 移动端标签云移入正文底部**

在 template 中，找到 `<aside class="article-sidebar">` 中的标签云部分（约 107-122 行），在所有 `v-if="!isMobile"` 的 sidebar 内容之后、`</aside>` 之前不修改。而是在 `<div class="article-body">` 之后、`</main>` 之前添加移动端专属标签区域：

```html
<!-- 文章正文 -->
<div class="article-body">
  <MarkdownRenderer :content="article.content" />
</div>

<!-- 移动端：文章底部标签和互动 -->
<div v-if="isMobile && article" class="mobile-article-footer md:hidden">
  <!-- 标签 -->
  <div v-if="article.tags && article.tags.length > 0" class="mobile-tags">
    <span class="mobile-section-label">标签</span>
    <div class="flex flex-wrap gap-2">
      <el-tag
        v-for="tag in article.tags"
        :key="tag.id || tag"
        size="small"
        class="tag-item"
        @click="searchByTag(getTagName(tag))"
      >
        {{ getTagName(tag) }}
      </el-tag>
    </div>
  </div>
  
  <!-- 互动按钮 -->
  <div class="mobile-actions">
    <div class="mobile-action-btn" @click="likeArticle" :class="{ 'liked': article?.isLiked }">
      <el-icon :class="{ 'text-red-500': article?.isLiked }"><Pointer /></el-icon>
      <span>{{ article?.likeCount || article?.likes || 0 }}</span>
    </div>
    <div class="mobile-action-btn" @click="collectArticle" :class="{ 'collected': article?.isCollected }">
      <el-icon :class="{ 'text-green-500': article?.isCollected }"><Collection /></el-icon>
      <span>{{ article?.isCollected ? '已收藏' : '收藏' }}</span>
    </div>
    <div class="mobile-action-btn" @click="shareArticle">
      <el-icon><Share /></el-icon>
      <span>分享</span>
    </div>
  </div>
  
  <!-- 相关文章 -->
  <div v-if="relatedArticles.length > 0" class="mobile-related">
    <span class="mobile-section-label">相关文章</span>
    <div
      v-for="related in relatedArticles"
      :key="related.id"
      class="mobile-related-item"
      @click="goToArticle(related)"
    >
      <h4>{{ related.title }}</h4>
      <p>{{ formatDate(related.createTime) }}</p>
    </div>
  </div>
</div>
```

- [ ] **Step 2: 添加移动端样式**

在 `<style scoped>` 的 `@media (max-width: 768px)` 块中添加：

```css
.mobile-article-footer {
  padding: 20px 16px;
  margin-top: 16px;
}

.mobile-section-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #9ca3af;
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.mobile-tags {
  margin-bottom: 24px;
}

.mobile-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
  margin-bottom: 24px;
}

.mobile-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 24px;
  border-radius: 12px;
  background: #f3f4f6;
  cursor: pointer;
  font-size: 14px;
  color: #6b7280;
  transition: all 0.2s;
  min-width: 70px;
}

.dark .mobile-action-btn {
  background: #1f2937;
  color: #9ca3af;
}

.mobile-action-btn:active {
  transform: scale(0.95);
}

.mobile-action-btn.liked {
  background: #fef2f2;
  color: #ef4444;
}

.mobile-action-btn.collected {
  background: #f0fdf4;
  color: #10b981;
}

.mobile-related {
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.dark .mobile-related {
  border-top-color: #1f2937;
}

.mobile-related-item {
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
}

.dark .mobile-related-item {
  border-bottom-color: #1f2937;
}

.mobile-related-item h4 {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.dark .mobile-related-item h4 {
  color: #e5e7eb;
}

.mobile-related-item p {
  font-size: 12px;
  color: #9ca3af;
}
```

- [ ] **Step 3: 调整移动端 TOC 位置**

在 template 中修改移动端 sidebar，将 TOC 移到主内容区域上方（当前 `article-sidebar` 在 `@media (max-width: 1024px)` 已经是 `order-first`，确认移动端 TOC 在正文上方即可）。不需要额外修改——现有的 `@media (max-width: 1024px)` 规则已将 sidebar 设为 `order-first`。

- [ ] **Step 4: 验证文章详情页**

在移动端视口检查：
- TOC 在文章标题下方、正文上方，可折叠
- 正文下方显示标签 chips
- 互动按钮（点赞/收藏/分享）居中排列
- 相关文章在底部
- 评论区正常显示
- 桌面端侧边栏和固定互动按钮不变

---

### Task 5: AppFooter — 移动端卡片式布局

**Files:**
- Modify: `frontend/src/components/layout/AppFooter.vue`

- [ ] **Step 1: 修改页脚移动端布局**

将页脚三列 grid 在移动端改为三张独立卡片。修改 template 中 `<div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">` 部分：

```html
<!-- 移动端卡片式布局 -->
<div class="grid grid-cols-1 md:grid-cols-3 gap-3 md:gap-6 mb-6">
  <!-- 想要了解我 -->
  <div class="footer-card">
    <h3 class="footer-card-title">👤 想要了解我</h3>
    <div class="footer-card-links">
      <router-link to="/about" class="footer-link">关于我</router-link>
      <a class="footer-link" @click.prevent="handleSiteHistory">本站历史</a>
      <router-link to="/about-project" class="footer-link">关于此项目</router-link>
    </div>
  </div>

  <!-- 你也许在找 -->
  <div class="footer-card">
    <h3 class="footer-card-title">🔍 你也许在找</h3>
    <div class="footer-card-links">
      <router-link to="/friend-links" class="footer-link">友链</router-link>
      <a href="https://www.hazenix.top" target="_blank" class="footer-link">作品集</a>
    </div>
  </div>

  <!-- 联系我 -->
  <div class="footer-card">
    <h3 class="footer-card-title">📬 联系我</h3>
    <div class="footer-card-links">
      <a class="footer-link" @click.prevent="handleLeaveMessage">写留言</a>
      <a :href="`mailto:${email}`" class="footer-link">发邮件</a>
      <a :href="githubUrl" target="_blank" rel="noopener noreferrer" class="footer-link">GitHub</a>
    </div>
  </div>
</div>
```

- [ ] **Step 2: 修改底部信息对齐方式**

将底部备案信息改为移动端左对齐：

```html
<div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-3 text-xs text-gray-500 dark:text-gray-400">
  <!-- 左侧：备案号和版权 -->
  <div class="flex flex-col gap-1">
    <p>湘ICP备2025144995号</p>
    <p>Copyright © 2025-2026 Hazenix. 保留所有权利。</p>
  </div>

  <!-- 右侧：技术支持信息 -->
  <div class="flex items-center gap-1">
    <p>Powered by</p>
    <a href="https://github.com/HazeHacker" target="_blank" rel="noopener noreferrer" 
       class="text-primary hover:underline">
      Hazenix Blog
    </a>
  </div>
</div>
```

- [ ] **Step 3: 添加移动端卡片样式**

在 `<style scoped>` 的 `@media (max-width: 768px)` 块中替换为：

```css
@media (max-width: 768px) {
  footer {
    min-height: auto;
    padding: 24px 0 16px;
  }
  
  .footer-card {
    background: #f8fafc;
    border-radius: 10px;
    padding: 14px;
  }
  
  .dark .footer-card {
    background: rgba(30, 41, 59, 0.6);
  }
  
  .footer-card-title {
    font-size: 14px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 8px;
  }
  
  .dark .footer-card-title {
    color: #e5e7eb;
  }
  
  .footer-card-links {
    display: flex;
    gap: 14px;
    flex-wrap: wrap;
  }
  
  .footer-link {
    font-size: 13px;
    color: var(--el-color-primary);
    cursor: pointer;
    text-decoration: none;
  }
  
  .footer-link:hover {
    text-decoration: underline;
  }
}
```

- [ ] **Step 4: 验证页脚**

在移动端视口检查：
- 三张独立卡片，每张有浅灰背景和圆角
- 每组内链接横向排列
- 备案信息左对齐、不溢出
- 桌面端保持三列 grid 不变

---

### Task 6: 全局验证与微调

**Files:**
- 无新建文件

- [ ] **Step 1: 完整移动端走查**

启动开发服务器，在 Chrome DevTools 中模拟以下设备：
- iPhone SE (375×667)
- iPhone 12/13 (390×844)
- iPad Mini (768×1024，检查桌面端布局是否触发)

检查每个页面：
- `/home` — Hero 垂直居中、导航可用
- `/articles` — 文章列表正常
- `/articles/:id` — TOC 折叠、标签/按钮在底部
- `/friend-links` — 内容不溢出
- `/tags` — 标签页正常

- [ ] **Step 2: 暗色模式检查**

切换到暗色模式，逐页检查所有移动端新增元素：
- 全屏菜单背景和文字颜色
- Hero 标题和副标题
- TOC 折叠头部
- 互动按钮
- 页脚卡片

- [ ] **Step 3: 安全区域适配**

确认所有固定/全屏元素考虑了 iPhone 刘海/底部指示条：
- 全屏菜单：`padding: env(safe-area-inset-top) 0 env(safe-area-inset-bottom)`
- 返回顶部按钮位置不会被底部指示条遮挡

- [ ] **Step 4: 提交**

```bash
git add frontend/src/components/layout/AppHeader.vue frontend/src/views/Home.vue frontend/src/components/article/TableOfContents.vue frontend/src/views/ArticleDetail.vue frontend/src/components/layout/AppFooter.vue
git commit -m "feat(frontend): 移动端响应式优化 — 全屏导航、Hero堆叠、TOC折叠、卡片页脚"
```
