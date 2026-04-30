<template>
  <div class="comment-list">
    <div class="comment-header">
      <h3 class="comment-title">
        <el-icon class="mr-2"><ChatDotRound /></el-icon>
        评论 ({{ totalComments }})
      </h3>
    </div>

    <!-- 顶部评论输入框（非回复状态时显示） -->
    <div v-if="showCommentForm && !activeReplyCommentId" class="comment-form">
      <div class="form-header">
        <div class="form-user-info" v-if="isLoggedIn">
          <img
            :src="getUserAvatar(userInfo)"
            :alt="userInfo?.username || '用户'"
            class="w-8 h-8 rounded-full object-cover border border-gray-200 dark:border-gray-600"
            @error="handleAvatarError"
          />
          <span class="form-username">{{ userInfo?.username || '用户' }}</span>
        </div>
        <div class="form-user-info" v-else>
          <span class="form-username" style="color: var(--el-text-color-secondary)">匿名评论</span>
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

      <el-form :model="commentForm" :rules="commentRules" :ref="setCommentFormRef">
        <div v-if="!isLoggedIn" class="anonymous-fields">
          <el-form-item prop="username" style="margin-bottom: 12px">
            <el-input
              v-model="commentForm.username"
              placeholder="昵称（必填）"
              maxlength="30"
              clearable
            />
          </el-form-item>
          <el-form-item prop="email" style="margin-bottom: 12px">
            <el-input
              v-model="commentForm.email"
              placeholder="邮箱（选填，不会公开）"
              maxlength="100"
              clearable
            />
          </el-form-item>
        </div>
        <el-form-item prop="content">
          <el-input
            ref="setCommentInputRef"
            v-model="commentForm.content"
            type="textarea"
            :rows="4"
            :placeholder="commentForm.replyTo ? `回复 @${commentForm.replyTo}:` : '请输入您的评论...'"
            maxlength="500"
            show-word-limit
            class="comment-textarea"
          />
          <div class="emoji-toolbar">
            <el-popover placement="top-start" trigger="click" :width="280" popper-class="emoji-popper" :teleported="true">
              <template #reference>
                <el-button text size="small">🙂 表情</el-button>
              </template>
              <div class="emoji-grid">
                <button
                  v-for="e in emojis"
                  :key="e"
                  type="button"
                  class="emoji-btn"
                  @click="insertEmoji(e)"
                >{{ e }}</button>
              </div>
            </el-popover>
          </div>
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

    <!-- 发表评论按钮（未显示表单时） -->
    <div v-if="!showCommentForm" class="comment-form-toggle">
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
            type="primary"
            @click="showCommentForm = true"
          >
            发表评论
          </el-button>
        </el-empty>
      </div>
      
      <div v-else class="comments-list">
        <CommentItem
          v-for="comment in comments"
          :key="comment.id"
          :comment="comment"
          :depth="0"
          :is-logged-in="isLoggedIn"
          :user-info="userInfo"
          :active-reply-comment-id="activeReplyCommentId"
          :comment-form="commentForm"
          :comment-form-ref="commentFormRef"
          :comment-input-ref="commentInputRef"
          :emojis="emojis"
          :comment-rules="commentRules"
          :submitting="submitting"
          :format-time="formatTime"
          @reply="replyToComment"
          @cancel-reply="cancelReply"
          @submit="submitComment"
          @insert-emoji="insertEmoji"
        />
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
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Edit } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getComments, createComment, likeComment as likeCommentApi } from '@/api/comment'
import dayjs from 'dayjs'
import { getAvatarUrl } from '@/utils/helpers'
import avatarFallback from '@/assets/img/avatar.jpg'
import CommentItem from './CommentItem.vue'

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
// 当前激活的输入框引用（主评论或行内回复）
const commentInputRef = ref(null)
// 用于定位正在回复的评论（用于UI定位，不参与提交）
const activeReplyCommentId = ref(null)

// 评论表单
const commentForm = reactive({
  content: '',
  parentId: null,
  replyUserId: null,
  replyTo: '',
  username: '',
  email: ''
})

// 常用表情（Unicode），无需后端改动
const emojis = [
  '😀','😁','😂','🤣','😃','😄','😅','😊','😍','😘','😜','🤪','🤗','🤔','🤨','😎','😏','😮','🤯','🥹','😢','😭','😤','😡','👍','👎','👏','🙏','🙌','💪','🫶','💯','🎉','✨','🔥','🌟','💫','⚡','🧡','💙','💚','💖','🍻','☕','🍰','🍕','🍔','🍟','🍗','🍣','🍜','🍫','🍩','🍦','🍉','🍓','🥭',
  // 动物和表情包常见替代（狗头/吃瓜等）
  '🐶','🐱','🐭','🐹','🐰','🦊','🐻','🐼','🐨','🐯','🐷','🐔','🐧','🐣','😺','😹','😼','😾',
  // 手势
  '👉','👈','👇','👆','🖖','✌️','🤟','👌','🤌','🤝','👏','🫰',
  // 其他趣味
  '🤡','🤠','🥳','🫠','😇','🤤','🥱','🤒','🤕','🤧','🤑','😈','👻','💀','🤖'
]

// 表单验证规则
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const commentRules = computed(() => {
  const rules = {
    content: [
      { required: true, message: '请输入评论内容', trigger: 'blur' },
      { min: 1, max: 500, message: '评论长度在 1 到 500 个字符', trigger: 'blur' }
    ]
  }
  if (!isLoggedIn.value) {
    rules.username = [
      { required: true, message: '请输入昵称', trigger: 'blur' },
      { min: 1, max: 30, message: '昵称长度在 1 到 30 个字符', trigger: 'blur' }
    ]
    rules.email = [
      { validator: (rule, value, callback) => {
          if (value && !emailRegex.test(value)) {
            callback(new Error('邮箱格式不正确'))
          } else {
            callback()
          }
        }, trigger: 'blur' }
    ]
  }
  return rules
})

// 打开登录对话框
const openLoginDialog = () => {
  window.dispatchEvent(new Event('open-login-dialog'))
}

// 获取用户头像
const getUserAvatar = (userInfo) => {
  if (!userInfo || !userInfo.avatar) {
    return avatarFallback
  }
  const avatarUrl = getAvatarUrl(userInfo.avatar, avatarFallback)
  return avatarUrl || avatarFallback
}

// 头像加载错误处理
// 注意：el-avatar组件的error事件可能不会传递标准的event对象
const handleAvatarError = (e) => {
  // console.warn('评论头像加载失败:', e?.target?.src || e)
  // el-avatar在图片加载失败时会自动显示用户名首字母，但我们仍然尝试设置默认头像
  // 如果e是event对象，尝试设置src
  if (e?.target && e.target.src !== avatarFallback) {
    e.target.src = avatarFallback
  }
}

// 设置输入框引用（主输入与回复输入共用此 setter）
const setCommentInputRef = (el) => {
  if (el) commentInputRef.value = el
}

// 在光标处插入表情
const insertEmoji = (emoji) => {
  const input = commentInputRef.value?.textarea
  if (!input) {
    commentForm.content += emoji
    return
  }
  const start = input.selectionStart || 0
  const end = input.selectionEnd || 0
  const text = commentForm.content || ''
  commentForm.content = text.slice(0, start) + emoji + text.slice(end)
  // 将光标移动到插入后的末尾
  nextTick(() => {
    const pos = start + emoji.length
    input.focus()
    input.setSelectionRange(pos, pos)
  })
}

// 加载评论列表
const loadComments = async () => {
  loading.value = true
  try {
    const response = await getComments({
      articleId: props.articleId
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
    
    // 计算总评论数（包括所有回复）
    const countAllComments = (comments) => {
      let count = 0
      for (const comment of comments) {
        count += 1
        if (comment.replies && comment.replies.length > 0) {
          count += countAllComments(comment.replies)
        }
      }
      return count
    }
    
    if (!totalComments.value) {
      totalComments.value = countAllComments(commentList)
    }
  } catch (error) {
    // console.error('加载评论失败:', error)
    ElMessage.error('加载评论失败')
  } finally {
    loading.value = false
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentForm.content || commentForm.content.trim() === '') {
    ElMessage.warning('请输入评论内容')
    return
  }

  if (commentFormRef.value) {
    try {
      await commentFormRef.value.validate()
    } catch (error) {
      return
    }
  }

  try {
    submitting.value = true
    const payload = {
      articleId: props.articleId,
      content: commentForm.content.trim()
    }

    if (isLoggedIn.value) {
      payload.username = userInfo.value?.username || userInfo.value?.nickname
    } else {
      payload.username = commentForm.username.trim()
      if (commentForm.email && commentForm.email.trim()) {
        payload.email = commentForm.email.trim()
      }
    }

    if (commentForm.parentId) {
      payload.parentId = commentForm.parentId
      if (commentForm.replyUserId) {
        payload.replyId = commentForm.replyUserId
      }
    }
    const response = await createComment(payload)

    ElMessage.success('评论发表成功')
    resetCommentForm()
    showCommentForm.value = false
    activeReplyCommentId.value = null
    await loadComments()
    emit('comment-added', response.data)
  } catch (error) {
    if (error.response?.data?.msg) {
      ElMessage.error(error.response.data.msg)
    } else {
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
  activeReplyCommentId.value = null
}

// 取消回复
const cancelReply = () => {
  commentForm.parentId = null
  commentForm.replyUserId = null
  commentForm.replyTo = ''
  showCommentForm.value = false
  activeReplyCommentId.value = null
}

// 重置评论表单
const resetCommentForm = () => {
  Object.assign(commentForm, {
    content: '',
    parentId: null,
    replyUserId: null,
    replyTo: '',
    username: '',
    email: ''
  })
  commentFormRef.value?.clearValidate()
}

// 由于回复表单位于 v-for 内部，使用函数 ref 保证拿到单个实例而不是数组
const setCommentFormRef = (el) => {
  commentFormRef.value = el
}

// 回复评论
const replyToComment = (comment) => {
  commentForm.parentId = comment.id
  commentForm.replyUserId = comment.userId || comment.user_id || null
  commentForm.replyTo = comment.username || comment.nickname || '用户'
  showCommentForm.value = true
  activeReplyCommentId.value = comment.id
  
  // 滚动到当前评论底部显示的表单
  setTimeout(() => {
    const commentEl = document.querySelector(`[data-comment-id="${comment.id}"]`)
    if (commentEl) {
      commentEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
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
    // console.error('点赞失败:', error)
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
    // console.error('时间格式化失败:', error)
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

// 监听文章ID变化，重新加载评论
watch(() => props.articleId, (newId, oldId) => {
  if (newId && newId !== oldId) {
    // 重置评论相关状态
    comments.value = []
    totalComments.value = 0
    currentPage.value = 1
    showCommentForm.value = false
    activeReplyCommentId.value = null
    resetCommentForm()
    // 重新加载评论
    loadComments()
  }
}, { immediate: false })

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

/* 表情选择 */
.emoji-toolbar {
  @apply -mt-2 mb-2;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 6px;
  max-height: 220px;
  overflow: auto;
}

.emoji-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  padding: 4px;
}
.emoji-btn:hover {
  background-color: rgba(0,0,0,0.06);
  border-radius: 6px;
}

/* 让弹层在小屏不超出视口 */
:deep(.emoji-popper) {
  max-width: calc(100vw - 32px);
}

@media (max-width: 640px) {
  .emoji-grid {
    grid-template-columns: repeat(6, 1fr);
  }
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

/* 行内回复表单样式 */
.inline-reply-form {
  @apply mt-2 p-3 bg-gray-50 dark:bg-gray-700/40 rounded-lg border border-gray-200 dark:border-gray-600;
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