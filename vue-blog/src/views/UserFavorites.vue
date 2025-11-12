<template>
  <div class="user-favorites">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">我的收藏</h1>
        <p class="page-description">您收藏的文章列表</p>
      </div>
    </div>

    <!-- 收藏文章列表 -->
    <div class="favorites-container">
      <div v-loading="loading" class="favorites-list">
        <!-- 文章卡片 -->
        <div
          v-for="article in favoriteArticles"
          :key="article.id"
          class="favorite-item"
          @click="goToArticle(article)"
        >
          <div class="article-card">
            <!-- 文章封面 -->
            <div v-if="article.coverImage" class="article-cover">
              <img :src="article.coverImage" :alt="article.title" />
            </div>
            
            <!-- 文章内容 -->
            <div class="article-content">
              <h3 class="article-title">{{ article.title }}</h3>
              
              <div class="article-meta">
                <span class="meta-item">
                  <el-icon><View /></el-icon>
                  {{ article.viewCount || 0 }} 阅读
                </span>
                <span class="meta-item">
                  <el-icon><Star /></el-icon>
                  {{ article.likeCount || 0 }} 点赞
                </span>
                <span class="meta-item">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ article.commentCount || 0 }} 评论
                </span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ formatDate(article.createTime) }}
                </span>
              </div>
              
              <!-- 分类和标签 -->
              <div class="article-tags">
                <el-tag v-if="article.categoryName" size="small" type="primary">
                  {{ article.categoryName }}
                </el-tag>
                <el-tag
                  v-for="tag in article.tags"
                  :key="tag.id || tag"
                  size="small"
                  class="tag-item"
                  @click.stop="searchByTag(getTagName(tag), tag)"
                >
                  {{ getTagName(tag) }}
                </el-tag>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="article-actions">
              <el-button
                type="danger"
                size="small"
                :icon="Delete"
                @click.stop="removeFavorite(article.id)"
              >
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && favoriteArticles.length === 0" class="empty-state">
        <el-empty description="还没有收藏任何文章">
          <el-button type="primary" @click="$router.push('/articles')">
            去浏览文章
          </el-button>
        </el-empty>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Star, ChatDotRound, Clock, Delete } from '@element-plus/icons-vue'
import { getFavoriteArticles } from '@/api/user'
import { collectArticle } from '@/api/article'
import dayjs from 'dayjs'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const favoriteArticles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

// 获取标签名称
const getTagName = (tag) => {
  if (typeof tag === 'object' && tag !== null) {
    return tag.name
  }
  return tag
}

// 按标签搜索
const searchByTag = (tagName, tag) => {
  // 如果tag是对象，直接使用其ID
  if (tag && typeof tag === 'object' && tag.id) {
    router.push(`/tag/${tag.id}`)
  } else {
    // 如果没找到，跳转到文章列表页进行搜索
    router.push(`/articles?tag=${encodeURIComponent(tagName)}`)
  }
}

// 加载收藏文章
const loadFavoriteArticles = async () => {
  loading.value = true
  try {
    const response = await getFavoriteArticles({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    
    favoriteArticles.value = response.data.records || response.data || []
    total.value = response.data.total || favoriteArticles.value.length
  } catch (error) {
    // console.error('加载收藏文章失败:', error)
    ElMessage.error('加载收藏文章失败')
  } finally {
    loading.value = false
  }
}

// 跳转到文章详情（优先使用slug）
const goToArticle = (article) => {
  // 如果传入的是对象，优先使用slug
  if (typeof article === 'object' && article !== null) {
    const identifier = article.slug || article.id
    router.push(`/article/${identifier}`)
  } else {
    // 如果传入的是ID，直接使用
    router.push(`/article/${article}`)
  }
}

// 取消收藏
const removeFavorite = async (articleId) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await collectArticle(articleId)
    ElMessage.success('已取消收藏')
    loadFavoriteArticles()
  } catch (error) {
    if (error !== 'cancel') {
      // console.error('取消收藏失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadFavoriteArticles()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadFavoriteArticles()
}

// 组件挂载时加载数据
onMounted(() => {
  loadFavoriteArticles()
})
</script>

<style scoped>
.user-favorites {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-title {
  font-size: 2rem;
  font-weight: bold;
  color: var(--el-text-color-primary);
  margin-bottom: 10px;
}

.page-description {
  color: var(--el-text-color-regular);
  font-size: 1rem;
}

.favorites-container {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.favorite-item {
  cursor: pointer;
  transition: transform 0.2s ease;
}

.favorite-item:hover {
  transform: translateY(-2px);
}

.article-card {
  display: flex;
  background: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.article-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.article-cover {
  width: 120px;
  height: 80px;
  margin-right: 20px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.article-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  gap: 15px;
  color: var(--el-text-color-regular);
  font-size: 0.9rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  margin: 0;
}

.article-actions {
  display: flex;
  align-items: center;
  margin-left: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .article-card {
    flex-direction: column;
  }
  
  .article-cover {
    width: 100%;
    height: 200px;
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .article-actions {
    margin-left: 0;
    margin-top: 15px;
    justify-content: center;
  }
  
  .article-meta {
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>
