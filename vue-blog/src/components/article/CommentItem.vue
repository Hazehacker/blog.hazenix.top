<template>
  <div class="comment-item-wrapper" :class="{ 'is-reply': depth > 0 }">
    <div
      class="comment-item"
      :data-comment-id="comment.id"
    >
      <!-- 头像 -->
      <div class="comment-avatar">
        <el-avatar :size="depth > 0 ? 32 : 40" :src="comment.avatar || comment.avatarUrl">
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
              <el-avatar :size="28" :src="userInfo?.avatar">
                {{ userInfo?.username?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="form-username">{{ userInfo?.username || '用户' }}</span>
            </div>
            <el-button type="text" size="small" @click="handleCancelReply">取消回复</el-button>
          </div>
          <el-form :model="commentForm" :rules="commentRules" :ref="setCommentFormRef">
            <el-form-item prop="content">
              <el-input
                ref="setCommentInputRef"
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
                      @click="insertEmoji(e)"
                    >{{ e }}</button>
                  </div>
                </el-popover>
              </div>
            </el-form-item>

            <div class="form-actions">
              <el-button @click="handleCancelReply">取消</el-button>
              <el-button 
                type="primary" 
                @click="handleSubmit"
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
import { nextTick } from 'vue'
import dayjs from 'dayjs'

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

const handleReply = () => {
  emit('reply', props.comment)
}

const handleCancelReply = () => {
  emit('cancel-reply')
}

const handleSubmit = () => {
  emit('submit')
}

const insertEmoji = (emoji) => {
  emit('insert-emoji', emoji)
}

// 设置表单和输入框的ref
const setCommentFormRef = (el) => {
  if (el && props.commentFormRef) {
    props.commentFormRef.value = el
  }
}

const setCommentInputRef = (el) => {
  if (el && props.commentInputRef) {
    props.commentInputRef.value = el
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
