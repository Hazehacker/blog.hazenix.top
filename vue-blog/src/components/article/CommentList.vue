<template>
  <div>
    <!-- 评论输入框 -->
    <div v-if="userStore.token" class="mb-8">
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="4"
        placeholder="写下你的评论..."
      />
      <el-button type="primary" class="mt-2" @click="submitComment" :loading="submitting">
        发表评论
      </el-button>
    </div>
    <div v-else class="mb-8 p-4 bg-gray-100 dark:bg-gray-800 rounded">
      <router-link to="/login" class="text-primary">登录后发表评论</router-link>
    </div>
    
    <!-- 评论列表 -->
    <div v-loading="loading" class="space-y-6">
      <div v-for="comment in comments" :key="comment.id" 
           class="bg-gray-50 dark:bg-gray-800 p-4 rounded">
        <div class="flex items-start gap-3">
          <img :src="comment.userAvatar || defaultAvatar" class="w-10 h-10 rounded-full" />
          <div class="flex-1">
            <div class="font-semibold">{{ comment.username }}</div>
            <div class="text-sm text-gray-500">
              {{ formatDate(comment.createTime) }}
            </div>
            <div class="mt-2">{{ comment.content }}</div>
          </div>
        </div>
      </div>
      
      <div v-if="comments.length === 0 && !loading" class="text-center py-8 text-gray-500">
        暂无评论
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCommentList, addComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const props = defineProps({
  articleId: {
    type: [String, Number],
    required: true
  }
})

const userStore = useUserStore()
const comments = ref([])
const commentContent = ref('')
const loading = ref(false)
const submitting = ref(false)
const defaultAvatar = '/assets/avatar.jpg'

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const loadComments = async () => {
  loading.value = true
  try {
    const res = await getCommentList(props.articleId)
    comments.value = res.data || []
  } catch (error) {
    console.error('Failed to load comments:', error)
    // Mock数据作为fallback
    comments.value = [
      {
        id: 1,
        content: '这是一条示例评论，展示评论系统的功能。',
        username: '测试用户',
        userAvatar: defaultAvatar,
        createTime: '2025-01-01 10:00:00'
      }
    ]
  } finally {
    loading.value = false
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  submitting.value = true
  try {
    await addComment(props.articleId, { content: commentContent.value })
    ElMessage.success('评论成功')
    commentContent.value = ''
    loadComments()
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadComments()
})
</script>
