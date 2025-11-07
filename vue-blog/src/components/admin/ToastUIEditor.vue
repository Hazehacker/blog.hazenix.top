<template>
  <div class="toast-ui-editor-container" :class="{ 'fullscreen-mode': isFullscreen }">
    <!-- 面包屑导航 -->
    <div v-show="!isFullscreen" class="mb-4">
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
    <div v-show="!isFullscreen" class="mb-6">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white">
        {{ isEdit ? '编辑文章' : '新建文章' }}
      </h1>
      <p class="text-gray-600 dark:text-gray-400 mt-2">
        {{ isEdit ? '修改您的文章内容' : '每一次的雕琢都是成就完美的作品哇' }}
      </p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-4" :class="{ 'lg:grid-cols-1 gap-0': isFullscreen, 'gap-8': !isFullscreen }">
      <!-- 左侧：编辑器 -->
      <div class="lg:col-span-3" :class="{ 'lg:col-span-1 space-y-0 w-full': isFullscreen, 'space-y-6': !isFullscreen }">
        <!-- 分类选择 -->
        <div v-show="!isFullscreen" class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
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
            <el-button type="text" class="text-blue-600 hover:text-blue-800" @click="showCreateCategoryDialog">
              没有合适的分类?新建一个叭
            </el-button>
          </div>
        </div>

        <!-- 标题输入 -->
        <div v-show="!isFullscreen" class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
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
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm" :class="isFullscreen ? 'p-2 rounded-none shadow-none' : 'p-6'">
            <div class="flex justify-between items-center mb-3" :class="isFullscreen ? 'px-2' : ''">
                <label v-show="!isFullscreen" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                <i class="fas fa-edit mr-2"></i>
                *内容:
                </label>
                <div class="flex items-center space-x-2" :class="{ 'ml-auto': isFullscreen }">
                  <!-- 预览模式切换 -->
                  <div class="flex items-center space-x-1 bg-gray-100 dark:bg-gray-700 rounded-lg p-1">
                    <button 
                      @click="setPreviewMode('markdown')"
                      :class="[
                        'px-3 py-1 text-sm rounded-md transition-colors',
                        previewMode === 'markdown' 
                          ? 'bg-white dark:bg-gray-600 text-gray-900 dark:text-white shadow-sm' 
                          : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
                      ]"
                    >
                      Markdown
                    </button>
                    <button 
                      @click="setPreviewMode('wysiwyg')"
                      :class="[
                        'px-3 py-1 text-sm rounded-md transition-colors',
                        previewMode === 'wysiwyg' 
                          ? 'bg-white dark:bg-gray-600 text-gray-900 dark:text-white shadow-sm' 
                          : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
                      ]"
                    >
                      WYSIWYG
                    </button>
                  </div>
                  <el-button 
                    type="text" 
                    @click="toggleFullscreen"
                    class="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
                  >
                    <i :class="isFullscreen ? 'fas fa-compress' : 'fas fa-expand'" class="mr-1"></i>
                    {{ isFullscreen ? '退出全屏' : '全屏编辑' }}
                  </el-button>
                </div>
          </div>
          
          <!-- 编辑器容器 -->
          <div class="border border-gray-300 dark:border-gray-600 rounded-lg overflow-hidden">
            <div ref="editorRef" class="toast-ui-editor"></div>
          </div>
          
          <!-- 导入选项 -->
          <div v-show="!isFullscreen" class="mt-4 text-sm text-gray-600 dark:text-gray-400">
            当然,你也可以选择从本地 markdown 文件导入
            <el-button 
              type="text" 
              class="text-blue-600 hover:text-blue-800 ml-2"
              @click="triggerFileUpload"
            >
              点击上传
            </el-button>
            <div class="mt-1 text-xs text-gray-500 dark:text-gray-500">
              <i class="fas fa-info-circle mr-1"></i>
              导入时会自动检测并上传文档中的图片到服务器
            </div>
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
      <div class="space-y-6" v-show="!isFullscreen">
        <!-- 发布设置 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-4">
          <h3 class="text-base font-medium text-gray-900 dark:text-gray-100 mb-3 flex items-center">
            <i class="fas fa-cog mr-2"></i>
            发布设置
          </h3>
          
          <div class="space-y-3">
            <!-- 状态 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                发布状态
              </label>
              <el-select v-model="form.status" class="w-full" size="small">
                <el-option label="草稿" value="2" />
                <el-option label="已发布" value="0" />
              </el-select>
            </div>

            <!-- 标签 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                标签
              </label>
              <div class="flex items-center space-x-3">
                <el-select
                  v-model="form.tagIds"
                  multiple
                  placeholder="添加标签"
                  class="flex-1"
                  clearable
                >
                  <el-option
                    v-for="tag in tags"
                    :key="tag.id"
                    :label="tag.name"
                    :value="tag.id"
                  />
                </el-select>
                <el-button type="text" class="text-blue-600 hover:text-blue-800" @click="showCreateTagDialog">
                  新建标签
                </el-button>
              </div>
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
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-4">
          <h3 class="text-base font-medium text-gray-900 dark:text-gray-100 mb-3 flex items-center">
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
                placeholder="[注意唯一性!] 留空自动生成"
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
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm mt-8" :class="isFullscreen ? 'p-3 rounded-none shadow-none mx-0' : 'p-6'">
      <div class="flex justify-between items-center">
        <div v-show="!isFullscreen" class="text-sm text-gray-500 dark:text-gray-400">
          <i class="fas fa-info-circle mr-1"></i>
          保存草稿会自动保存您的编辑内容
        </div>
        <div class="flex space-x-3" :class="{ 'ml-auto': isFullscreen }">
          <el-button @click="$emit('cancel')" :size="isFullscreen ? 'default' : 'large'">
            <i class="fas fa-times mr-2"></i>
            取消
          </el-button>
          <el-button @click="handleSaveDraft" :loading="saving" :size="isFullscreen ? 'default' : 'large'">
            <i class="fas fa-save mr-2"></i>
            保存草稿
          </el-button>
          <el-button @click="handlePublish" type="primary" :loading="saving" :size="isFullscreen ? 'default' : 'large'">
            <i class="fas fa-paper-plane mr-2"></i>
            发布文章
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新建分类对话框 -->
    <el-dialog
      v-model="createCategoryDialogVisible"
      title="新建分类"
      width="500px"
      :close-on-click-modal="false"
      @close="handleCreateCategoryDialogClose"
    >
      <el-form
        ref="createCategoryFormRef"
        :model="createCategoryForm"
        :rules="createCategoryRules"
        label-width="80px"
        @submit.prevent
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="createCategoryForm.name"
            placeholder="请输入分类名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="URL标识符" prop="slug">
          <el-input
            v-model="createCategoryForm.slug"
            placeholder="请输入URL标识符"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="createCategoryForm.sort"
            :min="0"
            :max="9999"
            controls-position="right"
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="createCategoryForm.status">
            <el-radio :value="0">启用</el-radio>
            <el-radio :value="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end space-x-3">
          <el-button @click="handleCreateCategoryDialogClose">取消</el-button>
          <el-button @click="handleCreateCategory" type="primary" :loading="creatingCategory">
            创建
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新建标签对话框 -->
    <el-dialog
      v-model="createTagDialogVisible"
      title="新建标签"
      width="500px"
      :close-on-click-modal="false"
      @close="handleCreateTagDialogClose"
    >
      <el-form
        ref="createTagFormRef"
        :model="createTagForm"
        :rules="createTagRules"
        label-width="80px"
        @submit.prevent
      >
        <el-form-item label="标签名称" prop="name">
          <el-input
            v-model="createTagForm.name"
            placeholder="请输入标签名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end space-x-3">
          <el-button @click="handleCreateTagDialogClose">取消</el-button>
          <el-button @click="handleCreateTag" type="primary" :loading="creatingTag">
            创建
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import { adminApi } from '@/api/admin'
import Editor from '@toast-ui/editor'
import '@toast-ui/editor/dist/toastui-editor.css'
import { batchProcessMarkdownImages, extractImageUrls } from '@/utils/markdownImageProcessor'

// Props
const props = defineProps({
  article: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['save', 'cancel'])

// 响应式数据
const editorRef = ref()
const fileInputRef = ref()
const editor = ref(null)
const saving = ref(false)
const isFullscreen = ref(false)
const previewMode = ref('markdown') // 'markdown' 或 'wysiwyg'

// 分类相关
const categories = ref([])
const createCategoryDialogVisible = ref(false)
const createCategoryFormRef = ref()
const creatingCategory = ref(false)

// 标签相关
const tags = ref([])
const createTagDialogVisible = ref(false)
const createTagFormRef = ref()
const creatingTag = ref(false)

// 表单数据
const form = reactive({
  title: '',
  summary: '',
  content: '',
  status: '2',
  categoryId: '',
  tagIds: [],
  coverImage: '',
  isTop: false,  // checkbox使用boolean值
  slug: '',
  metaDescription: '',
  keywords: ''
})

// 新建分类表单数据
const createCategoryForm = reactive({
  name: '',
  slug: '',
  sort: 0,
  status: 0
})

// 新建分类表单验证规则
const createCategoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 新建标签表单数据
const createTagForm = reactive({
  name: ''
})

// 新建标签表单验证规则
const createTagRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 20, message: '标签名称长度在 1 到 20 个字符', trigger: 'blur' }
  ]
}

// 上传配置
const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL}/common/upload`)
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${getToken()}`
}))

// 计算属性
const isEdit = computed(() => !!props.article)

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await adminApi.getCategories({ page: 1, pageSize: 100 })
    categories.value = response.data.records || response.data.list || []
  } catch (error) {
    console.error('加载分类列表失败:', error)
    ElMessage.error('加载分类列表失败')
  }
}

// 加载标签列表
const loadTags = async () => {
  try {
    const response = await adminApi.getTags({ page: 1, pageSize: 100 })
    tags.value = response.data.records || response.data.list || []
  } catch (error) {
    console.error('加载标签列表失败:', error)
    ElMessage.error('加载标签列表失败')
  }
}

// 初始化表单
const initForm = () => {
  if (props.article) {
    // 处理 tagIds：如果 tags 是对象数组，提取 id；如果 tagIds 是 id 数组，直接使用
    let tagIds = []
    if (props.article.tagIds && Array.isArray(props.article.tagIds)) {
      // 如果 tagIds 是 id 数组
      tagIds = props.article.tagIds
    } else if (props.article.tags && Array.isArray(props.article.tags)) {
      // 如果 tags 是对象数组，提取 id
      tagIds = props.article.tags.map(tag => {
        if (typeof tag === 'object' && tag !== null && tag.id) {
          return tag.id
        }
        return tag
      })
    }
    
    Object.assign(form, {
      title: props.article.title || '',
      summary: props.article.summary || '',
      content: props.article.content || '',
      status: String(props.article.status || '2'),
      categoryId: props.article.categoryId || props.article.category?.id || '',
      tagIds: tagIds,
      coverImage: props.article.coverImage || '',
      // 将isTop从0/1转换为boolean（0=false，1=true）
      isTop: props.article.isTop === 1 || props.article.isTop === true,
      slug: props.article.slug || '',
      metaDescription: props.article.metaDescription || '',
      keywords: props.article.keywords || ''
    })
    
    // 如果编辑器已初始化，更新编辑器内容
    if (editor.value && form.content) {
      nextTick(() => {
        try {
          editor.value.setMarkdown(form.content)
        } catch (error) {
          console.error('设置编辑器内容失败:', error)
        }
      })
    }
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
      isTop: false,  // checkbox使用boolean值
      slug: '',
      metaDescription: '',
      keywords: ''
    })
    
    // 清空编辑器内容
    if (editor.value) {
      nextTick(() => {
        try {
          editor.value.setMarkdown('')
        } catch (error) {
          console.error('清空编辑器内容失败:', error)
        }
      })
    }
  }
}

// 初始化编辑器
const initEditor = async () => {
  await nextTick()
  
  if (editorRef.value) {
    try {
      // 如果编辑器已存在，先销毁
      if (editor.value) {
        editor.value.destroy()
        editor.value = null
      }
      
      // 清空容器
      editorRef.value.innerHTML = ''
      
      editor.value = new Editor({
        el: editorRef.value,
        height: '800px',
        initialEditType: 'markdown',
        previewStyle: 'vertical', // 垂直分屏模式：左边编辑，右边预览
        initialValue: form.content || '',
        usageStatistics: false,
        toolbarItems: [
          ['heading', 'bold', 'italic', 'strike'],
          ['hr', 'quote'],
          ['ul', 'ol', 'task', 'indent', 'outdent'],
          ['table', 'image', 'link'],
          ['code', 'codeblock'],
          ['scrollSync']
        ],
        events: {
          change: () => {
            // 内容变化时更新表单数据
            if (editor.value) {
              form.content = editor.value.getMarkdown()
            }
          }
        },
        hooks: {
          addImageBlobHook: (blob, callback) => {
            // 处理编辑器中的图片上传
            handleEditorImageUpload(blob, callback)
          }
        }
      })
      
      console.log('编辑器初始化成功')
    } catch (error) {
      console.error('编辑器初始化失败:', error)
      ElMessage.error('编辑器初始化失败')
    }
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

// 处理编辑器中的图片上传
const handleEditorImageUpload = async (blob, callback) => {
  try {
    // 创建FormData对象
    const formData = new FormData()
    
    // 生成更好的文件名
    const timestamp = Date.now()
    const fileName = `image_${timestamp}.png`
    formData.append('file', blob, fileName)
    
    // 使用fetch上传图片到服务器
    const response = await fetch(uploadUrl.value, {
      method: 'POST',
      headers: uploadHeaders.value,
      body: formData
    })
    
    const result = await response.json()
    
    if (result.code === 200) {
      // 上传成功，返回图片URL给编辑器
      // Toast UI Editor的callback格式: callback(imageUrl, altText)
      // 使用文件名作为alt文本，这样markdown会显示 ![文件名](URL)
      const altText = fileName.replace(/\.[^/.]+$/, '') // 去掉文件扩展名
      callback(result.data, altText)
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

// 设置预览模式
const setPreviewMode = (mode) => {
  previewMode.value = mode
  if (editor.value) {
    try {
      // 切换编辑模式
      if (mode === 'wysiwyg') {
        editor.value.changeMode('wysiwyg')
      } else {
        editor.value.changeMode('markdown')
      }
    } catch (error) {
      console.error('切换预览模式失败:', error)
      // 如果切换失败，重新初始化编辑器
      nextTick(() => {
        initEditor()
      })
    }
  }
}

// 切换全屏模式
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  // 重新调整编辑器高度
  if (editor.value) {
    nextTick(() => {
      // 全屏时使用更大的高度，减去工具栏和按钮的高度
      const newHeight = isFullscreen.value ? 'calc(100vh - 100px)' : '800px'
      editor.value.setHeight(newHeight)
    })
  }
}


// 处理文件导入
const handleFileImport = async (event) => {
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
  reader.onload = async (e) => {
    try {
      const content = e.target.result
      console.log('文件内容读取成功，长度:', content.length)
      console.log('文件内容预览:', content.substring(0, 200) + '...')
      
      // 检查是否有图片需要处理
      const imageUrls = extractImageUrls(content)
      let processedContent = content
      
      if (imageUrls.length > 0) {
        ElMessage.info(`检测到 ${imageUrls.length} 张图片，开始自动上传处理...`)
        
        try {
          // 处理图片自动上传
          processedContent = await batchProcessMarkdownImages(content)
          ElMessage.success('图片自动上传处理完成！')
        } catch (error) {
          console.error('图片处理失败:', error)
          ElMessage.warning('图片自动上传失败，将使用原始内容')
          processedContent = content
        }
      }
      
      // 设置编辑器内容
      if (editor.value) {
        try {
          // 先重置编辑器内容，避免transaction冲突
          editor.value.reset()
          // 使用setMarkdown方法设置内容
          editor.value.setMarkdown(processedContent)
          form.content = processedContent
          ElMessage.success('文件导入成功，左右两边都已更新')
          console.log('编辑器内容已更新')
        } catch (error) {
          console.error('设置编辑器内容失败:', error)
          // 如果设置失败，尝试重新初始化编辑器
          form.content = processedContent
          nextTick(() => {
            initEditor()
          })
          ElMessage.success('文件已读取，编辑器将重新初始化')
        }
      } else {
        // 如果编辑器还没初始化，先保存内容
        form.content = processedContent
        ElMessage.success('文件已读取，编辑器初始化后将自动加载')
        console.log('编辑器未初始化，内容已保存')
      }
    } catch (error) {
      console.error('文件读取失败:', error)
      ElMessage.error('文件读取失败: ' + error.message)
    }
  }
  
  reader.onerror = (error) => {
    console.error('文件读取错误:', error)
    ElMessage.error('文件读取失败，请检查文件格式和编码')
  }
  
  // 使用UTF-8编码读取文件
  try {
    reader.readAsText(file, 'UTF-8')
  } catch (error) {
    console.error('开始读取文件时出错:', error)
    ElMessage.error('无法读取文件: ' + error.message)
  }
  
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
  // 验证分类：必须选择分类
  if (!form.categoryId) {
    ElMessage.error('请选择文章分类')
    return
  }

  saving.value = true
  try {
    // 确保isTop被正确转换为0或1（0=不置顶，1=置顶）
    const isTopValue = form.isTop === true || form.isTop === 1 ? 1 : 0
    
    const articleData = {
      ...form,
      status,
      isTop: isTopValue
    }
    
    console.log('保存文章数据:', articleData)
    emit('save', articleData)
  } catch (error) {
    console.error('保存文章失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 显示新建分类对话框
const showCreateCategoryDialog = () => {
  createCategoryDialogVisible.value = true
}

// 关闭新建分类对话框
const handleCreateCategoryDialogClose = () => {
  createCategoryDialogVisible.value = false
  resetCreateCategoryForm()
}

// 重置新建分类表单
const resetCreateCategoryForm = () => {
  Object.assign(createCategoryForm, {
    name: '',
    slug: '',
    sort: 0,
    status: 0
  })
  if (createCategoryFormRef.value) {
    createCategoryFormRef.value.clearValidate()
  }
}

// 创建分类
const handleCreateCategory = async () => {
  if (!createCategoryFormRef.value) return
  
  try {
    await createCategoryFormRef.value.validate()
    
    creatingCategory.value = true
    
    const response = await adminApi.createCategory(createCategoryForm)
    
    ElMessage.success('分类创建成功')
    
    // 关闭对话框
    createCategoryDialogVisible.value = false
    
    // 重新加载分类列表
    await loadCategories()
    
    // 自动选择新创建的分类
    if (response.data && response.data.id) {
      form.categoryId = response.data.id
    }
    
  } catch (error) {
    if (error !== false) {
      console.error('创建分类失败:', error)
      ElMessage.error('创建分类失败')
    }
  } finally {
    creatingCategory.value = false
  }
}

// 显示新建标签对话框
const showCreateTagDialog = () => {
  createTagDialogVisible.value = true
}

// 关闭新建标签对话框
const handleCreateTagDialogClose = () => {
  createTagDialogVisible.value = false
  resetCreateTagForm()
}

// 重置新建标签表单
const resetCreateTagForm = () => {
  Object.assign(createTagForm, {
    name: ''
  })
  if (createTagFormRef.value) {
    createTagFormRef.value.clearValidate()
  }
}

// 创建标签
const handleCreateTag = async () => {
  if (!createTagFormRef.value) return
  
  try {
    await createTagFormRef.value.validate()
    
    creatingTag.value = true
    
    const response = await adminApi.createTag(createTagForm)
    
    ElMessage.success('标签创建成功')
    
    // 关闭对话框
    createTagDialogVisible.value = false
    
    // 重新加载标签列表
    await loadTags()
    
    // 自动选择新创建的标签
    if (response.data && response.data.id) {
      if (!form.tagIds) {
        form.tagIds = []
      }
      form.tagIds.push(response.data.id)
    }
    
  } catch (error) {
    if (error !== false) {
      console.error('创建标签失败:', error)
      ElMessage.error('创建标签失败')
    }
  } finally {
    creatingTag.value = false
  }
}

// 组件销毁前清理编辑器
onBeforeUnmount(() => {
  if (editor.value) {
    editor.value.destroy()
  }
})

// 监听 article prop 变化，重新初始化表单
watch(() => props.article, (newArticle) => {
  if (newArticle) {
    initForm()
    // 如果编辑器已初始化，更新编辑器内容
    if (editor.value) {
      nextTick(() => {
        if (newArticle.content) {
          try {
            editor.value.setMarkdown(newArticle.content)
          } catch (error) {
            console.error('更新编辑器内容失败:', error)
          }
        }
      })
    }
  }
}, { deep: true, immediate: false })

// 初始化
onMounted(async () => {
  initForm()
  await loadCategories()
  await loadTags()
  await initEditor()
})
</script>

<style scoped>
.toast-ui-editor-container {
  max-width: 7xl;
  margin: 0 auto;
}

.toast-ui-editor-container.fullscreen-mode {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  max-width: 100vw;
  width: 100vw;
  margin: 0;
  padding: 10px;
  z-index: 1000;
  background-color: #f8fafc;
  overflow-y: auto;
}

.toast-ui-editor-container.fullscreen-mode > .grid {
  width: 100%;
  max-width: 100%;
}

.toast-ui-editor-container.fullscreen-mode > .grid > div {
  width: 100%;
  max-width: 100%;
}

.dark .toast-ui-editor-container.fullscreen-mode {
  background-color: #1a202c;
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
