<template>
  <div class="toast-ui-editor-container">
    <!-- 面包屑导航 -->
    <div class="mb-4">
      <nav class="flex" aria-label="Breadcrumb">
        <ol class="inline-flex items-center space-x-1 md:space-x-3">
          <li class="inline-flex items-center">
            <router-link to="/admin/articles" class="text-gray-700 hover:text-blue-600 dark:text-gray-300 dark:hover:text-blue-400">
              文章
            </router-link>
          </li>
          <li>
            <div class="flex items-center">
              <svg class="w-6 h-6 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"></path>
              </svg>
              <span class="ml-1 text-gray-500 md:ml-2 dark:text-gray-400">
                {{ isEdit ? '编辑文章' : '新建文章' }}
              </span>
            </div>
          </li>
        </ol>
      </nav>
    </div>

    <!-- 页面标题 -->
    <div class="mb-6">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white">
        {{ isEdit ? '编辑文章' : '新建文章' }}
      </h1>
      <p class="text-gray-600 dark:text-gray-400 mt-2">
        {{ isEdit ? '修改您的文章内容' : '每一次的雕琢都是成就完美的作品哇' }}
      </p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- 左侧：编辑器 -->
      <div class="lg:col-span-2 space-y-6">
        <!-- 分类选择 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
            <i class="fas fa-folder mr-2"></i>
            *选择分类:
          </label>
          <div class="flex items-center space-x-3">
            <el-select 
              v-model="form.categoryId" 
              placeholder="选择分类" 
              class="flex-1"
              size="large"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
            <el-button type="text" class="text-blue-600 hover:text-blue-800">
              没有合适的分类?新建一个叭
            </el-button>
          </div>
        </div>

        <!-- 标题输入 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
            <i class="fas fa-heading mr-2"></i>
            *标题:
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

        <!-- Toast UI Editor -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
            <i class="fas fa-edit mr-2"></i>
            *内容:
          </label>
          <div class="border border-gray-300 dark:border-gray-600 rounded-lg overflow-hidden">
            <div ref="editorRef" class="toast-ui-editor"></div>
          </div>
          
          <!-- 导入选项 -->
          <div class="mt-4 text-sm text-gray-600 dark:text-gray-400">
            当然,你也可以选择从本地 markdown 文件导入
            <el-button 
              type="text" 
              class="text-blue-600 hover:text-blue-800 ml-2"
              @click="triggerFileUpload"
            >
              点击上传
            </el-button>
            <!-- 隐藏的文件输入 -->
            <input
              ref="fileInputRef"
              type="file"
              accept=".md,.markdown,.txt"
              style="display: none"
              @change="handleFileImport"
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
                <el-option label="草稿" value="2" />
                <el-option label="已发布" value="0" />
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
                当前封面:
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
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import Editor from '@toast-ui/editor'
import '@toast-ui/editor/dist/toastui-editor.css'

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
const fileInputRef = ref()
const editor = ref(null)
const saving = ref(false)

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

// 计算属性
const isEdit = computed(() => !!props.article)

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

// 初始化编辑器
const initEditor = async () => {
  await nextTick()
  
  if (editorRef.value) {
    editor.value = new Editor({
      el: editorRef.value,
      height: '600px',
      initialEditType: 'markdown',
      previewStyle: 'vertical',
      initialValue: form.content,
      usageStatistics: false,
      toolbarItems: [
        ['heading', 'bold', 'italic', 'strike'],
        ['hr', 'quote'],
        ['ul', 'ol', 'task', 'indent', 'outdent'],
        ['table', 'image', 'link'],
        ['code', 'codeblock'],
        ['scrollSync']
      ],
      hooks: {
        addImageBlobHook: (blob, callback) => {
          // 处理编辑器中的图片上传
          handleEditorImageUpload(blob, callback)
        }
      }
    })

    // 监听内容变化
    editor.value.on('change', () => {
      form.content = editor.value.getMarkdown()
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

// 处理编辑器中的图片上传
const handleEditorImageUpload = async (blob, callback) => {
  try {
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', blob, 'image.png')
    
    // 使用fetch上传图片到服务器
    const response = await fetch(uploadUrl.value, {
      method: 'POST',
      headers: uploadHeaders.value,
      body: formData
    })
    
    const result = await response.json()
    
    if (result.code === 200) {
      // 上传成功，返回图片URL给编辑器
      callback(result.data.url, '图片上传成功')
      ElMessage.success('图片上传成功')
    } else {
      // 上传失败
      callback('', '图片上传失败')
      ElMessage.error(result.message || '图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    callback('', '图片上传失败')
    ElMessage.error('网络错误，图片上传失败')
  }
}

// 触发文件上传
const triggerFileUpload = () => {
  fileInputRef.value?.click()
}

// 处理文件导入
const handleFileImport = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 文件类型验证
  const allowedTypes = ['.md', '.markdown', '.txt']
  const fileExtension = '.' + file.name.split('.').pop().toLowerCase()
  
  if (!allowedTypes.includes(fileExtension)) {
    ElMessage.error('只支持 .md、.markdown、.txt 格式的文件')
    return
  }

  // 文件大小验证 (限制为5MB)
  const maxSize = 5 * 1024 * 1024 // 5MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 5MB')
    return
  }

  // 读取文件内容
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const content = e.target.result
      
      // 设置编辑器内容
      if (editor.value) {
        editor.value.setMarkdown(content)
        form.content = content
        ElMessage.success('文件导入成功')
      } else {
        // 如果编辑器还没初始化，先保存内容
        form.content = content
        ElMessage.success('文件已读取，编辑器初始化后将自动加载')
      }
    } catch (error) {
      console.error('文件读取失败:', error)
      ElMessage.error('文件读取失败')
    }
  }
  
  reader.onerror = () => {
    ElMessage.error('文件读取失败')
  }
  
  reader.readAsText(file, 'UTF-8')
  
  // 清空文件输入，允许重复选择同一文件
  event.target.value = ''
}

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

// 组件销毁前清理编辑器
onBeforeUnmount(() => {
  if (editor.value) {
    editor.value.destroy()
  }
})

// 初始化
onMounted(async () => {
  initForm()
  await initEditor()
})
</script>

<style scoped>
.toast-ui-editor-container {
  max-width: 7xl;
  margin: 0 auto;
}

/* 自定义Toast UI Editor样式 */
:deep(.toast-ui-editor) {
  border: none;
}

:deep(.toast-ui-editor .toastui-editor-defaultUI) {
  border: none;
}

:deep(.toast-ui-editor .toastui-editor-defaultUI-toolbar) {
  border-bottom: 1px solid #e5e7eb;
  background-color: #f9fafb;
}

:deep(.toast-ui-editor .toastui-editor-defaultUI-toolbar .toastui-editor-toolbar-group) {
  border-right: 1px solid #e5e7eb;
}

:deep(.toast-ui-editor .toastui-editor-defaultUI-toolbar .toastui-editor-toolbar-group:last-child) {
  border-right: none;
}

/* 暗色主题适配 */
.dark :deep(.toast-ui-editor .toastui-editor-defaultUI-toolbar) {
  background-color: #374151;
  border-bottom-color: #4b5563;
}

.dark :deep(.toast-ui-editor .toastui-editor-defaultUI-toolbar .toastui-editor-toolbar-group) {
  border-right-color: #4b5563;
}
</style>
