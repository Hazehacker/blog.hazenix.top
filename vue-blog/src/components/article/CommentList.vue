<template>
  <div class="comment-list">
    <div class="comment-header">
      <h3 class="comment-title">
        <el-icon class="mr-2"><ChatDotRound /></el-icon>
        评论 ({{ totalComments }})
      </h3>
    </div>

    <!-- 评论输入框 -->
    <div v-if="showCommentForm" class="comment-form">
      <div class="form-header">
        <h4 class="form-title">发表评论</h4>
      </div>
      
      <el-form :model="commentForm" :rules="commentRules" ref="commentFormRef">
        <el-form-item prop="content">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入您的评论..."
            maxlength="500"
            show-word-limit
            class="comment-textarea"
          />
        </el-form-item>
        
        <el-form-item prop="nickname">
          <el-input
            v-model="commentForm.nickname"
            placeholder="请输入昵称"
            maxlength="20"
            class="comment-nickname"
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="commentForm.email"
            placeholder="请输入邮箱（可选）"
            type="email"
            class="comment-email"
          />
        </el-form-item>
        
        <div class="form-actions">
          <el-button @click="cancelComment">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitComment"
            :loading="submitting"
          >
            发表评论
          </el-button>
        </div>
      </el-form>
    </div>

    <!-- 评论列表 -->
    <div class="comments-container">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="comments.length === 0" class="empty-comments">
        <el-empty description="暂无评论，快来抢沙发吧！">
          <el-button type="primary" @click="showCommentForm = true">
            发表评论
          </el-button>
        </el-empty>
      </div>
      
      <div v-else class="comments-list">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="comment-item"
        >
          <div class="comment-avatar">
            <el-avatar :size="40" :src="comment.avatar">
              {{ comment.nickname?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          
          <div class="comment-content">
            <div class="comment-header">
              <div class="comment-author">
                <span class="author-name">{{ comment.nickname || '匿名用户' }}</span>
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>
              
              <div class="comment-actions">
                <el-button 
                  type="text" 
                  size="small"
                  @click="replyToComment(comment)"
                >
                  回复
                </el-button>
                <el-button 
                  type="text" 
                  size="small"
                  @click="likeComment(comment)"
                  :class="{ 'liked': comment.isLiked }"
                >
                  <el-icon class="mr-1"><Star /></el-icon>
                  {{ comment.likeCount || 0 }}
                </el-button>
              </div>
            </div>
            
            <div class="comment-text">
              {{ comment.content }}
            </div>
            
            <!-- 回复列表 -->
            <div v-if="comment.replies && comment.replies.length > 0" class="replies">
              <div
                v-for="reply in comment.replies"
                :key="reply.id"
                class="reply-item"
              >
                <div class="reply-avatar">
                  <el-avatar :size="32" :src="reply.avatar">
                    {{ reply.nickname?.charAt(0) || 'U' }}
                  </el-avatar>
                </div>
                
                <div class="reply-content">
                  <div class="reply-header">
                    <span class="reply-author">{{ reply.nickname || '匿名用户' }}</span>
                    <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                  </div>
                  
                  <div class="reply-text">
                    <span v-if="reply.replyTo" class="reply-to">
                      @{{ reply.replyTo }} 
                    </span>
                    {{ reply.content }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalComments > pageSize" class="comment-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="totalComments"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Star } from '@element-plus/icons-vue'
import { getComments, createComment, likeComment as likeCommentApi } from '@/api/comment'

const props = defineProps({
  articleId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['comment-added'])

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const comments = ref([])
const totalComments = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const showCommentForm = ref(false)
const commentFormRef = ref(null)

// 评论表单
const commentForm = reactive({
  content: '',
  nickname: '',
  email: '',
  parentId: null,
  replyTo: ''
})

// 表单验证规则
const commentRules = {
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 1, max: 500, message: '评论长度在 1 到 500 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '昵称长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 加载评论列表
const loadComments = async () => {
  loading.value = true
  try {
    const response = await getComments({
      articleId: props.articleId,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    
    comments.value = response.data.records || response.data
    totalComments.value = response.data.total || response.data.length
  } catch (error) {
    console.error('加载评论失败:', error)
    ElMessage.error('加载评论失败')
  } finally {
    loading.value = false
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentFormRef.value) return
  
  try {
    await commentFormRef.value.validate()
    
    submitting.value = true
    const response = await createComment({
      ...commentForm,
      articleId: props.articleId
    })
    
    ElMessage.success('评论发表成功')
    resetCommentForm()
    showCommentForm.value = false
    loadComments()
    emit('comment-added', response.data)
  } catch (error) {
    if (error !== false) { // 不是表单验证错误
      console.error('发表评论失败:', error)
      ElMessage.error('发表评论失败')
    }
  } finally {
    submitting.value = false
  }
}

// 取消评论
const cancelComment = () => {
  resetCommentForm()
  showCommentForm.value = false
}

// 重置评论表单
const resetCommentForm = () => {
  Object.assign(commentForm, {
    content: '',
    nickname: '',
    email: '',
    parentId: null,
    replyTo: ''
  })
  commentFormRef.value?.clearValidate()
}

// 回复评论
const replyToComment = (comment) => {
  commentForm.parentId = comment.id
  commentForm.replyTo = comment.nickname
  showCommentForm.value = true
}

// 点赞评论
const likeComment = async (comment) => {
  try {
    await likeCommentApi(comment.id)
    comment.isLiked = !comment.isLiked
    comment.likeCount = (comment.likeCount || 0) + (comment.isLiked ? 1 : -1)
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('点赞失败')
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadComments()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadComments()
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  
  const date = new Date(timeString)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-list {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6;
}

.comment-header {
  @apply mb-6;
}

.comment-title {
  @apply text-xl font-semibold text-gray-900 dark:text-gray-100 flex items-center;
}

.comment-form {
  @apply mb-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg;
}

.form-header {
  @apply mb-4;
}

.form-title {
  @apply text-lg font-medium text-gray-900 dark:text-gray-100;
}

.comment-textarea {
  @apply mb-4;
}

.comment-nickname,
.comment-email {
  @apply mb-4;
}

.form-actions {
  @apply flex justify-end space-x-3;
}

.comments-container {
  @apply mb-6;
}

.loading-container {
  @apply space-y-4;
}

.empty-comments {
  @apply text-center py-8;
}

.comments-list {
  @apply space-y-6;
}

.comment-item {
  @apply flex space-x-4;
}

.comment-avatar {
  @apply flex-shrink-0;
}

.comment-content {
  @apply flex-1 min-w-0;
}

.comment-header {
  @apply flex items-center justify-between mb-2;
}

.comment-author {
  @apply flex items-center space-x-2;
}

.author-name {
  @apply font-medium text-gray-900 dark:text-gray-100;
}

.comment-time {
  @apply text-sm text-gray-500 dark:text-gray-400;
}

.comment-actions {
  @apply flex items-center space-x-2;
}

.comment-actions .el-button {
  @apply text-gray-500 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400;
}

.comment-actions .liked {
  @apply text-yellow-500 dark:text-yellow-400;
}

.comment-text {
  @apply text-gray-700 dark:text-gray-300 leading-relaxed;
}

.replies {
  @apply mt-4 space-y-3;
}

.reply-item {
  @apply flex space-x-3;
}

.reply-avatar {
  @apply flex-shrink-0;
}

.reply-content {
  @apply flex-1 min-w-0;
}

.reply-header {
  @apply flex items-center space-x-2 mb-1;
}

.reply-author {
  @apply font-medium text-gray-800 dark:text-gray-200;
}

.reply-time {
  @apply text-sm text-gray-500 dark:text-gray-400;
}

.reply-text {
  @apply text-gray-600 dark:text-gray-400 leading-relaxed;
}

.reply-to {
  @apply text-blue-600 dark:text-blue-400 font-medium;
}

.comment-pagination {
  @apply flex justify-center;
}

/* 移动端适配 */
@media (max-width: 640px) {
  .comment-list {
    @apply p-4;
  }
  
  .comment-item {
    @apply space-x-3;
  }
  
  .comment-header {
    @apply flex-col items-start space-y-2;
  }
  
  .comment-actions {
    @apply w-full justify-end;
  }
  
  .form-actions {
    @apply flex-col space-x-0 space-y-2;
  }
  
  .form-actions .el-button {
    @apply w-full;
  }
}
</style>