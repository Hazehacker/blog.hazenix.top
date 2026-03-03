<template>
  <div class="comment-item-wrapper" :class="{ 'is-reply': depth > 0 }">
    <div
      class="comment-item"
      :data-comment-id="comment.id"
    >
      <!-- 头像 -->
      <div class="comment-avatar">
        <el-avatar 
          :size="depth > 0 ? 32 : 40" 
          :src="getCommentAvatar(comment)"
          @error="handleAvatarError"
        >
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
            @click="handleReply"
          >
            回复
          </span>
        </div>

        <!-- 行内回复表单（仅当正在回复该评论时显示） -->
        <div 
          v-if="isLoggedIn && activeReplyCommentId === comment.id"
          class="inline-reply-form"
        >
          <div class="form-header">
            <div class="form-user-info">
              <img 
                :src="getUserAvatar(userInfo)" 
                :alt="userInfo?.username || '用户'"
                class="w-7 h-7 rounded-full object-cover border border-gray-200 dark:border-gray-600"
                @error="handleAvatarError"
              />
              <span class="form-username">{{ userInfo?.username || '用户' }}</span>
            </div>
            <el-button type="text" size="small" @click="handleCancelReply">取消回复</el-button>
          </div>
          <el-form :model="commentForm" :rules="commentRules" :ref="(el) => setFormRef(el, comment.id)">
            <el-form-item prop="content">
              <el-input
                :ref="(el) => setInputRef(el, comment.id)"
                v-model="commentForm.content"
                type="textarea"
                :rows="3"
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
                      @click="insertEmoji(e, comment.id)"
                    >{{ e }}</button>
                  </div>
                </el-popover>
              </div>
            </el-form-item>

            <div class="form-actions">
              <el-button @click="handleCancelReply">取消</el-button>
              <el-button 
                type="primary" 
                @click="handleSubmit(comment.id)"
                :loading="submitting"
              >
                发布
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 递归渲染回复评论 -->
    <div v-if="comment.replies && comment.replies.length > 0" class="comment-replies">
      <CommentItem
        v-for="reply in comment.replies"
        :key="reply.id"
        :comment="reply"
        :depth="depth + 1"
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
        @reply="$emit('reply', $event)"
        @cancel-reply="$emit('cancel-reply')"
        @submit="$emit('submit')"
        @insert-emoji="$emit('insert-emoji', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import dayjs from 'dayjs'
import { getAvatarUrl } from '@/utils/helpers'
import avatarFallback from '@/assets/img/avatar.jpg'

// 递归组件需要名称
defineOptions({
  name: 'CommentItem'
})

const props = defineProps({
  comment: {
    type: Object,
    required: true
  },
  depth: {
    type: Number,
    default: 0
  },
  isLoggedIn: {
    type: Boolean,
    default: false
  },
  userInfo: {
    type: Object,
    default: null
  },
  activeReplyCommentId: {
    type: [Number, String, null],
    default: null
  },
  commentForm: {
    type: Object,
    required: true
  },
  commentFormRef: {
    type: Object,
    default: null
  },
  commentInputRef: {
    type: Object,
    default: null
  },
  emojis: {
    type: Array,
    default: () => []
  },
  commentRules: {
    type: Object,
    required: true
  },
  submitting: {
    type: Boolean,
    default: false
  },
  formatTime: {
    type: Function,
    default: (timeString) => {
      if (!timeString) return ''
      try {
        return dayjs(timeString).format('YYYY-MM-DD HH:mm')
      } catch (error) {
        return timeString
      }
    }
  }
})

const emit = defineEmits(['reply', 'cancel-reply', 'submit', 'insert-emoji'])

// 存储每个评论的表单引用
const formRefs = ref({})
const inputRefs = ref({})

// 获取评论头像 - 与友链和用户头像保持一致的处理方式
const getCommentAvatar = (comment) => {
  const avatar = comment.avatar || comment.avatarUrl || comment.userAvatar || ''
  // 使用getAvatarUrl处理头像URL，如果为空或无效，返回默认头像
  // 与友链和用户头像的处理方式完全一致
  return getAvatarUrl(avatar, avatarFallback)
}

// 获取用户头像
const getUserAvatar = (userInfo) => {
  if (!userInfo || !userInfo.avatar) {
    return avatarFallback
  }
  const avatarUrl = getAvatarUrl(userInfo.avatar, avatarFallback)
  return avatarUrl || avatarFallback
}

// 头像加载错误处理 - 与友链和用户头像完全一致
const handleAvatarError = (event) => {
  // console.warn('评论头像加载失败，使用默认头像:', event.target.src)
  // 直接使用默认头像，与友链和用户头像处理方式一致
  // 避免无限循环：如果已经是默认头像还失败，则保持现状
  if (event.target.src !== avatarFallback && !event.target.src.includes(avatarFallback)) {
    event.target.src = avatarFallback
  }
}

// 获取表单引用的key
const getFormRefKey = (commentId) => {
  return `form-ref-${commentId}`
}

// 获取输入框引用的key
const getInputRefKey = (commentId) => {
  return `input-ref-${commentId}`
}

const handleReply = () => {
  emit('reply', props.comment)
}

const handleCancelReply = () => {
  emit('cancel-reply')
}

// 处理提交 - 需要验证当前评论的表单
const handleSubmit = async (commentId) => {
  // 获取当前评论的表单引用
  const formRef = formRefs.value[commentId]
  
  // 如果表单引用不存在，尝试使用父组件传递的引用
  if (!formRef && props.commentFormRef?.value) {
    try {
      // 使用父组件的表单引用进行验证
      await props.commentFormRef.value.validate()
      // 验证通过后，触发提交事件
      emit('submit')
    } catch (error) {
      // 表单验证失败，不提交
      if (error !== false) {
        // console.log('表单验证失败:', error)
      }
    }
    return
  }
  
  if (!formRef) {
    // console.error('表单引用不存在，commentId:', commentId)
    // 即使没有表单引用，也尝试提交（可能表单引用还没设置好）
    // 让父组件来处理验证
    emit('submit')
    return
  }
  
  try {
    // 验证表单
    await formRef.validate()
    // 验证通过后，触发提交事件
    emit('submit')
  } catch (error) {
    // 表单验证失败，不提交
    if (error !== false) {
      // console.log('表单验证失败:', error)
    }
  }
}

// 插入表情
const insertEmoji = (emoji, commentId) => {
  // 获取当前评论的输入框引用
  const inputRef = inputRefs.value[commentId]
  if (inputRef && inputRef.textarea) {
    const input = inputRef.textarea
    const start = input.selectionStart || 0
    const end = input.selectionEnd || 0
    const text = props.commentForm.content || ''
    props.commentForm.content = text.slice(0, start) + emoji + text.slice(end)
    // 将光标移动到插入后的末尾
    nextTick(() => {
      const pos = start + emoji.length
      input.focus()
      input.setSelectionRange(pos, pos)
    })
  } else {
    // 如果输入框引用不存在，直接追加到内容
    props.commentForm.content = (props.commentForm.content || '') + emoji
  }
}

// 设置表单引用
const setFormRef = (el, commentId) => {
  if (el) {
    formRefs.value[commentId] = el
    // 同时更新父组件的引用（用于兼容，确保父组件也能访问到）
    if (props.commentFormRef) {
      props.commentFormRef.value = el
    }
  } else {
    // 当元素被卸载时，清除引用
    if (formRefs.value[commentId]) {
      delete formRefs.value[commentId]
    }
  }
}

// 设置输入框引用
const setInputRef = (el, commentId) => {
  if (el) {
    inputRefs.value[commentId] = el
    // 同时更新父组件的引用（用于兼容）
    if (props.commentInputRef) {
      props.commentInputRef.value = el
    }
  } else {
    // 当元素被卸载时，清除引用
    if (inputRefs.value[commentId]) {
      delete inputRefs.value[commentId]
    }
  }
}

</script>

<style scoped>
.comment-item-wrapper {
  position: relative;
}

.comment-item-wrapper.is-reply {
  margin-top: 12px;
  border-left: 2px solid #e5e7eb;
  padding-left: 16px;
  margin-left: 24px;
}

.dark .comment-item-wrapper.is-reply {
  border-left-color: #4b5563;
}

.comment-item {
  @apply flex gap-3 py-4;
}

.comment-item-wrapper:not(.is-reply) .comment-item {
  @apply border-b border-gray-100 dark:border-gray-700 pb-4;
}

.comment-replies {
  margin-top: 4px;
}

.comment-avatar {
  @apply flex-shrink-0;
}

.comment-avatar-img {
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(0, 0, 0, 0.1);
  display: block;
  background-color: #f5f5f5;
}

.dark .comment-avatar-img {
  border-color: rgba(255, 255, 255, 0.1);
  background-color: #2d2d2d;
}

.avatar-normal {
  width: 40px;
  height: 40px;
}

.avatar-small {
  width: 32px;
  height: 32px;
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

/* 行内回复表单样式 */
.inline-reply-form {
  @apply mt-2 p-3 bg-gray-50 dark:bg-gray-700/40 rounded-lg border border-gray-200 dark:border-gray-600;
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

/* 移动端适配 */
@media (max-width: 640px) {
  .comment-item-wrapper.is-reply {
    margin-left: 16px;
    padding-left: 12px !important;
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
