<template>
  <div class="comment-list">
    <div class="comment-header">
      <h3 class="comment-title">
        <el-icon class="mr-2"><ChatDotRound /></el-icon>
        评论 ({{ totalComments }})
      </h3>
    </div>

    <!-- 未登录用户提示 -->
    <div v-if="!isLoggedIn" class="login-prompt">
      <div class="prompt-content">
        <el-icon class="prompt-icon"><Warning /></el-icon>
        <span class="prompt-text">登录后才能发表评论</span>
        <el-button type="primary" size="small" @click="openLoginDialog">
          立即登录
        </el-button>
      </div>
    </div>

    <!-- 评论输入框（仅登录用户可见） -->
    <div v-if="isLoggedIn && showCommentForm" class="comment-form">
      <div class="form-header">
        <div class="form-user-info">
          <el-avatar :size="32" :src="userInfo?.avatar">
            {{ userInfo?.username?.charAt(0) || 'U' }}
          </el-avatar>
          <span class="form-username">{{ userInfo?.username || '用户' }}</span>
        </div>
        <el-button 
          v-if="commentForm.parentId" 
          type="text" 
          size="small"
          @click="cancelReply"
        >
          取消回复
        </el-button>
      </div>
      
      <el-form :model="commentForm" :rules="commentRules" ref="commentFormRef">
        <el-form-item prop="content">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="4"
            :placeholder="commentForm.replyTo ? `回复 @${commentForm.replyTo}:` : '请输入您的评论...'"
            maxlength="500"
            show-word-limit
            class="comment-textarea"
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

    <!-- 发表评论按钮（登录用户且未显示表单时） -->
    <div v-if="isLoggedIn && !showCommentForm" class="comment-form-toggle">
      <el-button type="primary" @click="showCommentForm = true">
        <el-icon class="mr-1"><Edit /></el-icon>
        发表评论
      </el-button>
    </div>

    <!-- 评论列表 -->
    <div class="comments-container">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="comments.length === 0" class="empty-comments">
        <el-empty description="暂无评论，快来抢沙发吧！">
          <el-button 
            v-if="isLoggedIn" 
            type="primary" 
            @click="showCommentForm = true"
          >
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
          <!-- 头像 -->
          <div class="comment-avatar">
            <el-avatar :size="40" :src="comment.avatar || comment.avatarUrl">
              {{ (comment.username || comment.nickname)?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          
          <!-- 评论内容 -->
          <div class="comment-content">
            <!-- 用户名 -->
              <div class="comment-author">
              <span class="author-name">{{ comment.username || comment.nickname || '匿名用户' }}</span>
            </div>
            
            <!-- 评论文本 -->
            <div class="comment-text">
              <span v-if="comment.replyId && comment.replyUsername" class="reply-prefix">
                回复 @{{ comment.replyUsername }}:
              </span>
              {{ comment.content }}
            </div>
            
            <!-- 时间戳和回复链接 -->
            <div class="comment-meta">
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              <span 
                v-if="isLoggedIn"
                class="reply-link" 
                @click="replyToComment(comment)"
              >
                回复
                    </span>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Edit, Warning } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getComments, createComment, likeComment as likeCommentApi } from '@/api/comment'
import dayjs from 'dayjs'

const props = defineProps({
  articleId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['comment-added'])

// 用户状态
const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.token)
const userInfo = computed(() => userStore.userInfo)

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
  parentId: null,
  replyTo: ''
})

// 表单验证规则
const commentRules = {
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 1, max: 500, message: '评论长度在 1 到 500 个字符', trigger: 'blur' }
  ]
}

// 打开登录对话框
const openLoginDialog = () => {
  window.dispatchEvent(new Event('open-login-dialog'))
}

// 加载评论列表
const loadComments = async () => {
  loading.value = true
  try {
    const response = await getComments({
      articleId: props.articleId,
      status: '0' // 只显示正常状态的评论
    })
    
    // 处理响应数据，可能是数组或对象
    let commentList = []
    if (Array.isArray(response.data)) {
      commentList = response.data
    } else if (response.data?.list) {
      commentList = response.data.list
      totalComments.value = response.data.total || response.data.list.length
    } else if (response.data) {
      // 如果是单个对象，转为数组
      commentList = [response.data]
    }
    
    comments.value = commentList
    if (!totalComments.value) {
    totalComments.value = comments.value.length
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    ElMessage.error('加载评论失败')
  } finally {
    loading.value = false
  }
}

// 提交评论
const submitComment = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('登录后才能发表评论')
    return
  }

  if (!commentFormRef.value) return
  
  try {
    await commentFormRef.value.validate()
    
    submitting.value = true
    const response = await createComment({
      articleId: props.articleId,
      content: commentForm.content,
      replyId: commentForm.parentId || null,
      // 后端需要展示用户名，显式传入用户名（或昵称）
      username: userInfo.value?.username || userInfo.value?.nickname
    })
    
    ElMessage.success('评论发表成功')
    resetCommentForm()
    showCommentForm.value = false
    await loadComments()
    emit('comment-added', response.data)
  } catch (error) {
    if (error !== false) { // 不是表单验证错误
      console.error('发表评论失败:', error)
      if (error.response?.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        openLoginDialog()
      } else {
      ElMessage.error('发表评论失败')
      }
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

// 取消回复
const cancelReply = () => {
  commentForm.parentId = null
  commentForm.replyTo = ''
}

// 重置评论表单
const resetCommentForm = () => {
  Object.assign(commentForm, {
    content: '',
    parentId: null,
    replyTo: ''
  })
  commentFormRef.value?.clearValidate()
}

// 回复评论
const replyToComment = (comment) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('登录后才能发表评论')
    return
  }

  commentForm.parentId = comment.id
  commentForm.replyTo = comment.username || comment.nickname || '用户'
  showCommentForm.value = true
  
  // 滚动到评论表单
  setTimeout(() => {
    const formElement = document.querySelector('.comment-form')
    if (formElement) {
      formElement.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
    }
  }, 100)
}

// 点赞评论
const likeComment = async (comment) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    openLoginDialog()
    return
  }

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

// 格式化时间 - 使用 YYYY-MM-DD HH:mm 格式
const formatTime = (timeString) => {
  if (!timeString) return ''
  
  try {
    return dayjs(timeString).format('YYYY-MM-DD HH:mm')
  } catch (error) {
    console.error('时间格式化失败:', error)
    return timeString
  }
}

// 监听用户登录状态变化
watch(isLoggedIn, (newVal) => {
  if (!newVal && showCommentForm.value) {
    showCommentForm.value = false
    resetCommentForm()
  }
})

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

/* 登录提示 */
.login-prompt {
  @apply mb-6 p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg border border-gray-200 dark:border-gray-600;
}

.prompt-content {
  @apply flex items-center gap-3;
}

.prompt-icon {
  @apply text-orange-500 text-xl;
}

.prompt-text {
  @apply flex-1 text-gray-700 dark:text-gray-300;
}

/* 评论表单 */
.comment-form {
  @apply mb-6 p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg border border-gray-200 dark:border-gray-600;
}

.form-header {
  @apply flex items-center justify-between mb-4;
}

.form-user-info {
  @apply flex items-center gap-2;
}

.form-username {
  @apply text-sm font-medium text-gray-700 dark:text-gray-300;
}

.comment-textarea {
  @apply mb-4;
}

.form-actions {
  @apply flex justify-end gap-3;
}

/* 发表评论按钮 */
.comment-form-toggle {
  @apply mb-6;
}

/* 评论列表 */
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
  @apply space-y-0;
}

.comment-item {
  @apply flex gap-3 py-4 border-b border-gray-100 dark:border-gray-700 last:border-b-0;
}

.comment-avatar {
  @apply flex-shrink-0;
}

.comment-content {
  @apply flex-1 min-w-0;
}

.comment-author {
  @apply mb-1;
}

.author-name {
  @apply text-sm font-medium text-gray-900 dark:text-gray-100;
}

.comment-text {
  @apply text-gray-700 dark:text-gray-300 leading-relaxed mb-2 text-sm;
  word-wrap: break-word;
  word-break: break-word;
}

.reply-prefix {
  @apply text-blue-600 dark:text-blue-400 font-medium mr-1;
}

.comment-meta {
  @apply flex items-center gap-3 text-xs text-gray-500 dark:text-gray-400;
}

.comment-time {
  @apply text-xs;
}

.reply-link {
  @apply text-blue-600 dark:text-blue-400 cursor-pointer hover:text-blue-700 dark:hover:text-blue-300 transition-colors;
}

.comment-pagination {
  @apply flex justify-center mt-6;
}

/* 移动端适配 */
@media (max-width: 640px) {
  .comment-list {
    @apply p-4;
  }
  
  .comment-item {
    @apply gap-2 py-3;
  }
  
  .comment-avatar :deep(.el-avatar) {
    width: 32px !important;
    height: 32px !important;
  }
  
  .form-actions {
    @apply flex-col;
  }
  
  .form-actions .el-button {
    @apply w-full;
  }
}
</style>