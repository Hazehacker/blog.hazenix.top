<template>
  <div class="max-w-7xl mx-auto">
    <!-- 页面标题 -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
        {{ props.article ? '编辑文章' : '创建新文章' }}
      </h1>
      <p class="text-gray-600 dark:text-gray-400 mt-2">
        {{ props.article ? '修改您的文章内容' : '创建一篇新的博客文章' }}
      </p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- 左侧：编辑器 -->
      <div class="lg:col-span-2 space-y-6">
        <!-- 标题 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
            <i class="fas fa-heading mr-2"></i>
            文章标题 *
          </label>
          <el-input
            v-model="form.title"
            placeholder="请输入文章标题"
            size="large"
            maxlength="100"
            show-word-limit
            class="text-lg"
          />
        </div>

        <!-- 摘要 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
            <i class="fas fa-align-left mr-2"></i>
            文章摘要
          </label>
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入文章摘要，这将显示在文章列表中..."
            maxlength="200"
            show-word-limit
          />
        </div>

        <!-- 内容编辑器 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
            <i class="fas fa-edit mr-2"></i>
            文章内容 *
          </label>
          <div class="border border-gray-300 dark:border-gray-600 rounded-lg overflow-hidden">
            <!-- 工具栏 -->
            <Toolbar
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              :mode="mode"
              class="border-b border-gray-200 dark:border-gray-600"
            />
            <!-- 编辑器 -->
            <Editor
              v-model="form.content"
              :defaultConfig="editorConfig"
              :mode="mode"
              style="height: 600px; overflow-y: auto;"
              @onCreated="handleCreated"
            />
          </div>
        </div>
      </div>

      <!-- 右侧：设置 -->
      <div class="space-y-6">
        <!-- 发布设置 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-4 flex items-center">
            <i class="fas fa-cog mr-2"></i>
            发布设置
          </h3>
          
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
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-4 flex items-center">
            <i class="fas fa-search mr-2"></i>
            SEO设置
          </h3>
          
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
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6 mt-8">
      <div class="flex justify-between items-center">
        <div class="text-sm text-gray-500 dark:text-gray-400">
          <i class="fas fa-info-circle mr-1"></i>
          保存草稿会自动保存您的编辑内容
        </div>
        <div class="flex space-x-3">
          <el-button @click="$emit('cancel')" size="large">
            <i class="fas fa-times mr-2"></i>
            取消
          </el-button>
          <el-button @click="handleSaveDraft" :loading="saving" size="large">
            <i class="fas fa-save mr-2"></i>
            保存草稿
          </el-button>
          <el-button @click="handlePublish" type="primary" :loading="saving" size="large">
            <i class="fas fa-paper-plane mr-2"></i>
            发布文章
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import { getToken } from '@/utils/auth'
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue'
import '@wangeditor/editor/dist/css/style.css' //引入css
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

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
const editorRef = ref()
const mode = 'default'
const saving = ref(false)

// 工具栏配置
const toolbarConfig = {}

// 编辑器配置
const editorConfig = {
  placeholder: '请输入内容...',
  MENU_CONF: {}
}

// 配置图片上传
editorConfig.MENU_CONF['uploadImage'] = {
  server: `${import.meta.env.VITE_API_BASE_URL}/common/upload`,
  headers: {
    Authorization: `Bearer ${getToken()}`
  },
  fieldName: 'file',
  maxFileSize: 2 * 1024 * 1024, // 2MB
  onBeforeUpload(file) {
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
  },
  onSuccess(file, res) {
    ElMessage.success('图片上传成功')
  }
}

// 表单数据
const form = reactive({
  title: '',
  summary: '',
  content: '',
  status: '2',
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
      status: props.article.status || '2',
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
      status: '2',
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
    form.coverImage = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 编辑器创建回调
const handleCreated = (editor) => {
  editorRef.value = editor
}

// 组件销毁前清理编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

// 保存草稿
const handleSaveDraft = async () => {
  await saveArticle('2')
}

// 发布文章
const handlePublish = async () => {
  await saveArticle('0')
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