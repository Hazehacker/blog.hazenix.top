<template>
  <el-dialog
    v-model="visible"
    title="选择你感兴趣的标签"
    width="500px"
    :close-on-click-modal="false"
  >
    <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">
      选择感兴趣的标签，我们会为你推荐相关文章（可跳过）
    </p>
    <div class="flex flex-wrap gap-2">
      <el-tag
        v-for="tag in allTags"
        :key="tag.id"
        :type="selectedIds.includes(tag.id) ? '' : 'info'"
        :effect="selectedIds.includes(tag.id) ? 'dark' : 'plain'"
        class="cursor-pointer text-sm"
        @click="toggleTag(tag.id)"
      >
        {{ tag.name }}
      </el-tag>
    </div>
    <p class="mt-3 text-xs text-gray-400">
      已选 {{ selectedIds.length }} / 10 个标签
    </p>
    <template #footer>
      <el-button @click="skip">跳过</el-button>
      <el-button type="primary" @click="confirm" :disabled="selectedIds.length === 0">
        确认 ({{ selectedIds.length }})
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getTagList } from '@/api/tag'
import { setUserInterests } from '@/api/recommend'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'done'])

const visible = ref(props.modelValue)
const allTags = ref([])
const selectedIds = ref([])

const toggleTag = (id) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else if (selectedIds.value.length < 10) {
    selectedIds.value.push(id)
  } else {
    ElMessage.warning('最多选择 10 个标签')
  }
}

const confirm = async () => {
  try {
    await setUserInterests(selectedIds.value)
    ElMessage.success('兴趣标签设置成功')
    visible.value = false
    emit('update:modelValue', false)
    emit('done')
  } catch (error) {
    ElMessage.error('设置失败，请稍后重试')
  }
}

const skip = () => {
  visible.value = false
  emit('update:modelValue', false)
  emit('done')
}

onMounted(async () => {
  try {
    const res = await getTagList()
    if (res && res.data) {
      allTags.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (error) {
    allTags.value = []
  }
})

watch(() => props.modelValue, (val) => { visible.value = val })
watch(visible, (val) => { emit('update:modelValue', val) })
</script>