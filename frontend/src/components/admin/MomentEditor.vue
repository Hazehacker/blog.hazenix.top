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
        <el-select
          v-model="form.tagIds"
          multiple
          filterable
          placeholder="选择标签"
          class="w-full"
          clearable
        >
          <el-option
            v-for="tag in tagOptions"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>
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
import { ref, reactive } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import { momentApi } from '@/api/moment'
import { getTagList } from '@/api/tag'

const visible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref()
const fileList = ref([])
const tagOptions = ref([])

const form = reactive({
  title: '',
  content: '',
  tagIds: [],
  status: 0
})

const rules = {
  content: [{ required: true, message: '正文不能为空', trigger: 'blur' }]
}

const emit = defineEmits(['saved'])

async function loadTagOptions() {
  try {
    const res = await getTagList()
    tagOptions.value = res.data || []
  } catch (e) {
    tagOptions.value = []
  }
}

async function open(moment = null) {
  isEdit.value = !!moment
  editId.value = moment?.id || null
  form.title = moment?.title || ''
  form.content = moment?.content || ''
  form.status = moment?.status ?? 0
  fileList.value = (moment?.images || []).map((url, i) => ({ name: `img-${i}`, url, status: 'success' }))

  await loadTagOptions()

  if (moment?.tags && moment.tags.length > 0) {
    // moment.tags is an array of tag name strings; map to ids using loaded tagOptions
    form.tagIds = moment.tags
      .map(name => tagOptions.value.find(t => t.name === name)?.id)
      .filter(id => id != null)
  } else {
    form.tagIds = []
  }

  visible.value = true
}

function handleClose() {
  formRef.value?.resetFields()
  fileList.value = []
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
      tagIds: form.tagIds,
      status: form.status
    }
    if (isEdit.value) {
      await momentApi.update(editId.value, payload)
    } else {
      await momentApi.create(payload)
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
