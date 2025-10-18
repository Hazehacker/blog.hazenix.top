<template>
  <div class="max-w-6xl mx-auto">
    <div class="mb-8">
      <h1 class="text-3xl font-bold mb-4">文章列表</h1>
      
      <!-- 搜索和筛选 -->
      <div class="flex flex-col md:flex-row gap-4 mb-6">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文章..."
          @keyup.enter="handleSearch"
          class="flex-1"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        
        <el-select v-model="selectedCategory" placeholder="选择分类" @change="handleFilter" clearable>
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
        
        <el-select v-model="selectedTag" placeholder="选择标签" @change="handleFilter" clearable>
          <el-option
            v-for="tag in tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>
      </div>
    </div>
    
    <!-- 文章列表 -->
    <div v-loading="loading">
      <ArticleList :articles="articles" />
      
      <!-- 分页 -->
      <div class="mt-8 flex justify-center">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getArticleList } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import ArticleList from '@/components/article/ArticleList.vue'

const articles = ref([])
const categories = ref([])
const tags = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const selectedCategory = ref('')
const selectedTag = ref('')

const loadArticles = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value,
      categoryId: selectedCategory.value,
      tagId: selectedTag.value
    }
    const res = await getArticleList(params)
    articles.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('Failed to load articles:', error)
    // Mock数据作为fallback
    articles.value = [
      {
        id: 1,
        title: 'Vue3博客系统开发指南',
        summary: '本文介绍了如何使用Vue3、Element Plus和Tailwind CSS构建一个现代化的博客系统...',
        author: 'Hazenix',
        createTime: '2025-01-01',
        viewCount: 100,
        tags: [{ id: 1, name: 'Vue3' }, { id: 2, name: '前端' }]
      }
    ]
    total.value = 1
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const loadTags = async () => {
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    console.error('Failed to load tags:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadArticles()
}

const handleFilter = () => {
  currentPage.value = 1
  loadArticles()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadArticles()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadArticles()
}

onMounted(() => {
  loadArticles()
  loadCategories()
  loadTags()
})
</script>
