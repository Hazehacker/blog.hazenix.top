<template>
  <el-drawer
    v-model="visible"
    :title="isEdit ? '编辑手记' : '新建手记'"
    direction="rtl"
    size="520px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="标题（选填）">
        <el-input v-model="form.title" placeholder="给这条手记取个标题..." maxlength="100" show-word-limit />
      </el-form-item>

      <el-form-item label="正文" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="写下此刻的想法..."
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="图片（最多 9 张）">
        <el-upload
          v-model:file-list="fileList"
          :http-request="handleUpload"
          list-type="picture-card"
          :limit="9"
          :before-upload="beforeUpload"
          @exceed="() => ElMessage.warning('最多上传 9 张图片')"
        >
          <el-icon><Plus /></el-icon>
          <template #file="{ file }">
            <div class="relative w-full h-full">
              <img :src="file.url" class="w-full h-full object-cover" />
              <el-icon
                class="absolute top-0.5 right-0.5 cursor-pointer text-white bg-red-500 rounded-full p-0.5 text-xs"
                @click="removeImage(file)"
              ><Delete /></el-icon>
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item label="标签">
        <div class="flex flex-wrap gap-2 w-full">
          <el-tag
            v-for="tag in form.tags"
            :key="tag"
            closable
            @close="removeTag(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInputRef"
            v-model="tagInputValue"
            style="width: 100px"
            size="small"
            @keyup.enter="addTag"
            @blur="addTag"
          />
          <el-button v-else size="small" @click="showTagInput">+ 添加标签</el-button>
        </div>
      </el-form-item>

      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :label="0">发布</el-radio>
          <el-radio :label="1">草稿</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ form.status === 1 ? '存草稿' : '发布' }}
      </el-button>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'

const visible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref()
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref()
const fileList = ref([])

const form = reactive({
  title: '',
  content: '',
  tags: [],
  status: 0
})

const rules = {
  content: [{ required: true, message: '正文不能为空', trigger: 'blur' }]
}

const emit = defineEmits(['saved'])

function open(moment = null) {
  isEdit.value = !!moment
  editId.value = moment?.id || null
  form.title = moment?.title || ''
  form.content = moment?.content || ''
  form.tags = moment?.tags ? [...moment.tags] : []
  form.status = moment?.status ?? 0
  fileList.value = (moment?.images || []).map((url, i) => ({ name: `img-${i}`, url, status: 'success' }))
  visible.value = true
}

function handleClose() {
  formRef.value?.resetFields()
  fileList.value = []
  tagInputVisible.value = false
  tagInputValue.value = ''
}

async function handleUpload({ file, onSuccess, onError }) {
  try {
    const res = await adminApi.uploadImage(file, file.name)
    onSuccess({ url: res.data })
  } catch (e) {
    onError(e)
    ElMessage.error('图片上传失败')
  }
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt10M) ElMessage.error('图片大小不能超过 10MB')
  return isImage && isLt10M
}

function removeImage(file) {
  fileList.value = fileList.value.filter(f => f.uid !== file.uid)
}

function showTagInput() {
  tagInputVisible.value = true
  nextTick(() => tagInputRef.value?.focus())
}

function addTag() {
  const val = tagInputValue.value.trim()
  if (val && !form.tags.includes(val) && form.tags.length < 10) {
    form.tags.push(val)
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

function removeTag(tag) {
  form.tags = form.tags.filter(t => t !== tag)
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const imageUrls = fileList.value
      .filter(f => f.status === 'success')
      .map(f => f.response?.url || f.url)
    const payload = {
      title: form.title || null,
      content: form.content,
      imageUrls,
      tags: form.tags,
      status: form.status
    }
    if (isEdit.value) {
      await adminApi.updateMoment(editId.value, payload)
    } else {
      await adminApi.createMoment(payload)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '发布成功')
    visible.value = false
    emit('saved')
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
