<template>
  <el-dialog
    v-model="visible"
    title="申请友链"
    width="600px"
    :before-close="handleClose"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      label-position="left"
    >
      <el-form-item label="网站名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入网站名称"
          maxlength="48"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="网站描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入网站描述"
          maxlength="255"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="网站地址" prop="url">
        <el-input
          v-model="form.url"
          placeholder="请输入网站地址，如：https://example.com"
        />
      </el-form-item>

      <el-form-item label="网站图标" prop="avatar">
        <div class="flex items-center space-x-4">
          <!-- 图片预览 -->
          <div v-if="form.avatar" class="flex-shrink-0">
            <div class="relative">
              <img
                :src="form.avatar"
                alt="网站图标"
                class="w-16 h-16 rounded-lg object-cover border border-gray-200"
                @error="handleImageError"
              />
              <!-- 默认图标 -->
              <div 
                class="absolute inset-0 w-16 h-16 rounded-lg border border-gray-200 bg-gray-100 flex items-center justify-center text-gray-400"
                style="display: none;"
              >
                <el-icon size="24"><Picture /></el-icon>
              </div>
            </div>
          </div>
          
          <!-- 上传按钮 -->
          <div class="flex-1">
            <el-upload
              ref="uploadRef"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              accept="image/*"
              class="upload-demo"
            >
              <el-button type="primary" plain>
                <el-icon class="mr-1"><Upload /></el-icon>
                上传图标
              </el-button>
            </el-upload>
            
            <!-- URL输入框 -->
            <div class="mt-2">
              <el-input
                v-model="form.avatar"
                placeholder="或直接输入图标URL地址"
                size="small"
              />
            </div>
          </div>
        </div>
        
        <div class="text-xs text-gray-500 mt-1" style="margin-bottom: 42px;">
          支持 JPG、PNG、GIF 格式，建议尺寸 64x64 像素
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          提交申请
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Picture } from '@element-plus/icons-vue'
import { frontendApi } from '@/api/frontend'
import { useUserStore } from '@/stores/user'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'success'])

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const formRef = ref()
const uploadRef = ref()
const submitting = ref(false)

// 表单数据
const form = reactive({
  name: '',
  description: '',
  url: '',
  avatar: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入网站名称', trigger: 'blur' },
    { min: 2, max: 48, message: '网站名称长度在 2 到 48 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 255, message: '网站描述不能超过 255 个字符', trigger: 'blur' }
  ],
  url: [
    { required: true, message: '请输入网站地址', trigger: 'blur' },
    { 
      pattern: /^https?:\/\/.+/, 
      message: '请输入有效的网站地址，如：https://example.com', 
      trigger: 'blur' 
    }
  ],
  avatar: [
    { 
      pattern: /^https?:\/\/.+\.(jpg|jpeg|png|gif|webp)$/i, 
      message: '请输入有效的图片URL地址', 
      trigger: 'blur' 
    }
  ]
}

// 用户store
const userStore = useUserStore()

// 上传配置
const uploadUrl = '/user/common/upload'
const uploadHeaders = computed(() => {
  const token = userStore.token
  return token ? { Authorization: `Bearer ${token}` } : {}
})

// 图片上传前验证
const beforeUpload = (file) => {
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

// 上传成功处理
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    form.avatar = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || '图片上传失败')
  }
}

// 上传失败处理
const handleUploadError = (error) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
}

// 图片加载失败处理
const handleImageError = (event) => {
  // 使用Element Plus的默认图标
  event.target.style.display = 'none'
  if (event.target.nextElementSibling) {
    event.target.nextElementSibling.style.display = 'block'
  }
}

// 重置表单
const resetForm = () => {
  form.name = ''
  form.description = ''
  form.url = ''
  form.avatar = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 关闭弹窗
const handleClose = () => {
  resetForm()
  visible.value = false
}

// 提交申请
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const response = await frontendApi.applyLink({
      name: form.name,
      description: form.description,
      url: form.url,
      avatar: form.avatar,
      status: 1 // 申请状态，需要审核
    })
    
    if (response.code === 200) {
      ElMessage.success('友链申请提交成功，请等待审核')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(response.msg || '申请提交失败')
    }
  } catch (error) {
    console.error('提交申请失败:', error)
    ElMessage.error('申请提交失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.upload-demo {
  display: inline-block;
}

:deep(.el-upload) {
  display: inline-block;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
