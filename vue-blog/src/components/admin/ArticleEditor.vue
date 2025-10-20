<template>
  <div class="space-y-6">
    <!-- 基本信息 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧：编辑器 -->
      <div class="lg:col-span-2 space-y-4">
        <!-- 标题 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            文章标题 *
          </label>
          <el-input
            v-model="form.title"
            placeholder="请输入文章标题"
            size="large"
            maxlength="100"
            show-word-limit
          />
        </div>

        <!-- 摘要 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            文章摘要
          </label>
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入文章摘要"
            maxlength="200"
            show-word-limit
          />
        </div>

        <!-- 内容编辑器 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            文章内容 *
          </label>
          <div class="border border-gray-300 dark:border-gray-600 rounded-lg">
            <div class="flex border-b border-gray-300 dark:border-gray-600">
              <button
                @click="editorMode = 'markdown'"
                :class="[
                  'px-4 py-2 text-sm font-medium',
                  editorMode === 'markdown'
                    ? 'bg-primary text-white'
                    : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                ]"
              >
                Markdown
              </button>
              <button
                @click="editorMode = 'preview'"
                :class="[
                  'px-4 py-2 text-sm font-medium',
                  editorMode === 'preview'
                    ? 'bg-primary text-white'
                    : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                ]"
              >
                预览
              </button>
            </div>
            
            <div v-if="editorMode === 'markdown'" class="h-96">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="20"
                placeholder="请输入Markdown内容..."
                class="border-0"
              />
            </div>
            
            <div v-else class="h-96 p-4 overflow-auto bg-gray-50 dark:bg-gray-800">
              <MarkdownRenderer :content="form.content" />
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：设置 -->
      <div class="space-y-4">
        <!-- 发布设置 -->
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-4">发布设置</h3>
          
          <div class="space-y-4">
            <!-- 状态 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                发布状态
              </label>
              <el-select v-model="form.status" class="w-full">
                <el-option label="草稿" value=2 />
                <el-option label="已发布" value=0 />
              </el-select>
            </div>

            <!-- 分类 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                分类
              </label>
              <el-select v-model="form.categoryId" placeholder="选择分类" class="w-full" clearable>
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </div>

            <!-- 标签 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                标签
              </label>
              <el-select
                v-model="form.tagIds"
                multiple
                placeholder="选择标签"
                class="w-full"
                clearable
              >
                <el-option
                  v-for="tag in tags"
                  :key="tag.id"
                  :label="tag.name"
                  :value="tag.id"
                />
              </el-select>
            </div>

            <!-- 封面图片 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                封面图片
              </label>
              <div class="space-y-2">
                <el-input
                  v-model="form.coverImage"
                  placeholder="输入图片URL"
                  class="w-full"
                />
                <el-upload
                  class="w-full"
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleImageUpload"
                  :before-upload="beforeImageUpload"
                >
                  <el-button type="primary" plain class="w-full">
                    <el-icon class="mr-1"><Upload /></el-icon>
                    上传图片
                  </el-button>
                </el-upload>
                <div v-if="form.coverImage" class="mt-2">
                  <img :src="form.coverImage" alt="封面" class="w-full h-32 object-cover rounded" />
                </div>
              </div>
            </div>

            <!-- 其他选项 -->
            <div class="space-y-2">
              <el-checkbox v-model="form.isTop">置顶文章</el-checkbox>
         
            </div>
          </div>
        </div>

        <!-- SEO设置 -->
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-4">SEO设置</h3>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                自定义URL
              </label>
              <el-input
                v-model="form.slug"
                placeholder="留空自动生成"
                maxlength="100"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Meta描述
              </label>
              <el-input
                v-model="form.metaDescription"
                type="textarea"
                :rows="3"
                placeholder="用于SEO的页面描述"
                maxlength="160"
                show-word-limit
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                关键词
              </label>
              <el-input
                v-model="form.keywords"
                placeholder="用逗号分隔关键词"
                maxlength="200"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="flex justify-end space-x-3 pt-6 border-t border-gray-200 dark:border-gray-700">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
      <el-button @click="handlePublish" type="primary" :loading="saving">发布文章</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import { getToken } from '@/utils/auth'
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue'

// Props
const props = defineProps({
  article: {
    type: Object,
    default: null
  },
  categories: {
    type: Array,
    default: () => []
  },
  tags: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['save', 'cancel'])

// 响应式数据
const editorMode = ref('markdown')
const saving = ref(false)

// 表单数据
const form = reactive({
  title: '',
  summary: '',
  content: '',
  status: 2,
  categoryId: '',
  tagIds: [],
  coverImage: '',
  isTop: false,

  slug: '',
  metaDescription: '',
  keywords: ''
})

// 上传配置
const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL}/common/upload`)
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${getToken()}`
}))

// 初始化表单
const initForm = () => {
  if (props.article) {
    Object.assign(form, {
      title: props.article.title || '',
      summary: props.article.summary || '',
      content: props.article.content || '',
      status: props.article.status || 2,
      categoryId: props.article.categoryId || '',
      tagIds: props.article.tagIds || [],
      coverImage: props.article.coverImage || '',
      isTop: props.article.isTop || false,

      slug: props.article.slug || '',
      metaDescription: props.article.metaDescription || '',
      keywords: props.article.keywords || ''
    })
  } else {
    // 重置表单
    Object.assign(form, {
      title: '',
      summary: '',
      content: '',
      status: 2,
      categoryId: '',
      tagIds: [],
      coverImage: '',
      isTop: false,
      slug: '',
      metaDescription: '',
      keywords: ''
    })
  }
}

// 图片上传前验证
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 图片上传成功
const handleImageUpload = (response) => {
  if (response.code === 200) {
    form.coverImage = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 保存草稿
const handleSaveDraft = async () => {
  await saveArticle(2)
}

// 发布文章
const handlePublish = async () => {
  await saveArticle('published')
}

// 保存文章
const saveArticle = async (status) => {
  if (!form.title.trim()) {
    ElMessage.error('请输入文章标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.error('请输入文章内容')
    return
  }

  saving.value = true
  try {
    const articleData = {
      ...form,
      status
    }
    emit('save', articleData)
  } catch (error) {
    console.error('保存文章失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 初始化
onMounted(() => {
  initForm()
})
</script>
