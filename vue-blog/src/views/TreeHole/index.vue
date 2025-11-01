<template>
  <div :class="['container', { 'dark-mode': themeStore.isDark, 'light-mode': !themeStore.isDark }]">
    <!-- 动态背景（仅在暗色模式下显示） -->
    <div v-if="themeStore.isDark" class="background-wrapper">
      <Galaxy
        :focal="[0.5, 0.5]"
        :rotation="[1.0, 0.0]"
        :star-speed="0.3"
        :density="1.2"
        :hue-shift="240"
        :speed="0.8"
        :mouse-interaction="true"
        :glow-intensity="0.4"
        :saturation="0.2"
        :mouse-repulsion="true"
        :twinkle-intensity="0.3"
        :rotation-speed="0.05"
        :repulsion-strength="3"
        :auto-center-repulsion="0.5"
        :transparent="true"
      />
    </div>
    
    <!-- 视频背景（仅在亮色模式下显示） -->
    <div v-if="!themeStore.isDark" class="background-wrapper">
      <video
        ref="videoRef"
        class="video-background"
        autoplay
        loop
        muted
        playsinline
      >
        <source src="@/assets/video/sunrise.mp4" type="video/mp4">
      </video>
    </div>
    
    <!-- 输入区域 -->
    <div class="content_container" style="margin-top: 45px;">
      <div class="title">树洞</div>
      <div class="input_wrapper">
        <input 
          v-model="content" 
          @focus="isShowSubmit = true" 
          @blur="handleInputBlur"
          type="text" 
          placeholder="在这里留下自己的足迹吧..."
          maxlength="50"
          :disabled="loading"
          @keyup.enter="addTreeHoleBtn"
        >
        <button 
          v-show="isShowSubmit" 
          @click="addTreeHoleBtn"
          :disabled="loading"
        >
          <span class="submit-text">发送</span>
        </button>
      </div>
      <!-- 字符计数提示 -->
      <div class="char-count" v-if="content.length > 0">
        {{ content.length }}/50
      </div>
    </div>

    <!-- 弹幕组件 -->
    <vue-danmaku 
      ref="danmakuRef"
      :debounce="3000"
      random-channel
      :speeds="80"
      :channels="5"
      is-suspend
      :autoplay="true"
      v-model:danmus="treeHoleList"
      use-slot
      loop
      style="height:calc(100vh - 70px); width:100vw;"
    >
      <!-- 自定义弹幕样式 -->
      <template v-slot:dm="{ danmu }">
        <div class="barrage_container">
          <span class="nickname">{{ danmu.nickname }}：</span>
          <span class="content">{{ danmu.content }}</span>
        </div>
      </template>
    </vue-danmaku>

    <!-- 加载遮罩 -->
    <div v-if="loading" class="loading_mask">
      <el-icon class="is-loading">
        <Loading />
      </el-icon>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getTreeHoleList, addTreeHole } from '@/api/treeHole.js'
import { getToken } from '@/utils/auth.js'
import { useUserStore } from '@/stores/user.js'
import { useThemeStore } from '@/stores/theme.js'
import Galaxy from '@/Backgrounds/Galaxy/Galaxy.vue'

// 响应式数据
const treeHoleList = ref([])        // 树洞列表数据
const isShowSubmit = ref(false)     // 控制提交按钮显示
const content = ref('')             // 输入框内容
const loading = ref(false)          // 加载状态

// 获取用户信息和主题信息
const userStore = useUserStore()
const themeStore = useThemeStore()
const videoRef = ref(null)  // 视频引用
const danmakuRef = ref(null)  // 弹幕组件引用

/**
 * 获取默认头像
 */
function getDefaultAvatar(username) {
  // 根据用户名生成头像URL
  if (username) {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(username)}&background=random&size=40`
  }
  // 默认头像
  return 'https://ui-avatars.com/api/?name=匿名&background=random&size=40'
}

/**
 * 获取树洞列表
 */
async function getTreeHole() {
  try {
    loading.value = true
    const res = await getTreeHoleList()
    
    if (res.code === 200) {
      // 转换数据格式
      treeHoleList.value = (res.data || []).map(item => {
        const username = item.username || '匿名用户'
        return {
          id: item.id || item.user_id,
          nickname: username,
          avatar: item.avatar || getDefaultAvatar(username),
          content: item.content,
          createTime: item.create_time
        }
      })
      
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('获取树洞列表失败:', error)
    ElMessage.error('获取数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 处理输入框失焦
 */
function handleInputBlur() {
  // 延迟隐藏，避免点击提交按钮时按钮消失
  setTimeout(() => {
    if (content.value === '') {
      isShowSubmit.value = false
    }
  }, 200)
}

/**
 * 提交树洞内容
 */
async function addTreeHoleBtn() {
  // 验证输入
  if (!content.value || content.value.trim() === '') {
    ElMessage.warning('请输入内容')
    return
  }

  // 验证长度
  if (content.value.trim().length > 50) {
    ElMessage.warning('内容不能超过50个字符')
    return
  }

  // 检查是否登录
  const token = getToken()
  if (!token) {
    ElMessage.warning('请先登录后再发表弹幕')
    return
  }

  // 获取用户信息
  let userId = null
  let username = ''

  // 从 userStore 获取用户信息
  if (userStore.userInfo) {
    userId = userStore.userInfo.id || userStore.userInfo.userId
    username = userStore.userInfo.username || userStore.userInfo.nickname || ''
  } else {
    // 如果 userStore 中没有用户信息，尝试获取
    try {
      await userStore.getUserInfo()
      if (userStore.userInfo) {
        userId = userStore.userInfo.id || userStore.userInfo.userId
        username = userStore.userInfo.username || userStore.userInfo.nickname || ''
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  if (!userId) {
    ElMessage.error('无法获取用户信息，请重新登录')
    return
  }

  if (!username) {
    username = '匿名用户'
  }

  try {
    loading.value = true
    const res = await addTreeHole({
      userId: userId,
      username: username,
      content: content.value.trim()
    })

    if (res.code === 200) {
      ElMessage.success(res.msg || '发表成功')
      content.value = ''           // 清空输入
      isShowSubmit.value = false   // 隐藏提交按钮
      await getTreeHole()          // 刷新列表
    } else {
      ElMessage.error(res.msg || '发表失败')
    }
  } catch (error) {
    console.error('发表弹幕失败:', error)
    
    // 处理特定错误
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        ElMessage.error('没有权限发表弹幕')
      } else {
        ElMessage.error(error.response.data?.msg || '发表失败，请稍后重试')
      }
    } else {
      ElMessage.error('发表失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getTreeHole()
})
</script>

<style scoped>
.container {
  position: fixed;
  top: 70px; /* 从导航栏下方开始 */
  left: 0;
  min-width: 100vw;
  width: 100vw;
  height: calc(100vh - 70px);
  overflow: hidden;
  z-index: 1; /* 低于导航栏(z-50) */
  transition: background-color 0.3s ease;
}

/* 暗色模式下的背景 */
.container.dark-mode {
  background: #0a0e27; /* 深色背景，适合星空效果 */
}

/* 亮色模式下的背景 */
.container.light-mode {
  background: #f3f4f6; /* 浅灰色背景 */
}

/* 动态背景容器 */
.background-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

/* 视频背景样式 */
.video-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;  /* 覆盖整个容器，保持宽高比 */
  z-index: 0;
  pointer-events: none;  /* 不影响鼠标事件 */
}

/* 输入区域样式 */
.content_container {
  position: absolute;
  top: 40%;
  left: 50%;
  z-index: 10; /* 确保在背景之上 */
  transform: translate(-50%, -50%);
  text-align: center;
}

.title {
  font-size: 2rem;
  font-weight: bold;
  text-align: center;
  text-transform: uppercase;
  letter-spacing: 0.2rem;
  margin-bottom: 1rem;
  transition: color 0.3s ease;
}

/* 暗色模式下的标题 */
.container.dark-mode .title {
  color: white;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.8);
}

/* 亮色模式下的标题 */
.container.light-mode .title {
  color: #1f2937;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
}

.input_wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
}

.input_wrapper input {
  width: 16rem;
  height: 2rem;
  border: #409EFF solid 1px;
  border-radius: 1rem;
  outline: none;
  padding: 0 1rem;
  font-size: 1rem;
  transition: all 0.3s ease;
}

/* 暗色模式下的输入框 */
.container.dark-mode .input_wrapper input {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
}

.container.dark-mode .input_wrapper input:focus {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: #66b1ff;
}

.container.dark-mode .input_wrapper input::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

/* 亮色模式下的输入框 */
.container.light-mode .input_wrapper input {
  background-color: rgba(255, 255, 255, 0.9);
  color: #1f2937;
  border-color: #3b82f6;
}

.container.light-mode .input_wrapper input:focus {
  background-color: rgba(255, 255, 255, 1);
  border-color: #2563eb;
}

.container.light-mode .input_wrapper input::placeholder {
  color: rgba(0, 0, 0, 0.5);
}

.input_wrapper input::placeholder {
  font-style: italic;
}

.input_wrapper input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.input_wrapper button {
  width: 5rem;
  height: 2rem;
  border-radius: 1rem;
  outline: none;
  border: #409EFF solid 1px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

/* 暗色模式下的按钮 */
.container.dark-mode .input_wrapper button {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
}

.container.dark-mode .input_wrapper button:hover:not(:disabled) {
  background-color: rgba(255, 255, 255, 0.5);
  border-color: #66b1ff;
}

/* 亮色模式下的按钮 */
.container.light-mode .input_wrapper button {
  background-color: rgba(59, 130, 246, 0.1);
  color: #1f2937;
}

.container.light-mode .input_wrapper button:hover:not(:disabled) {
  background-color: rgba(59, 130, 246, 0.2);
  border-color: #2563eb;
}

.input_wrapper button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submit-text {
  font-style: italic;
  font-weight: bold;
}

/* 暗色模式下的提交文本 */
.container.dark-mode .submit-text {
  color: white;
}

/* 亮色模式下的提交文本 */
.container.light-mode .submit-text {
  color: #1f2937;
}

.char-count {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  transition: color 0.3s ease;
}

/* 暗色模式下的字符计数 */
.container.dark-mode .char-count {
  color: rgba(255, 255, 255, 0.8);
}

/* 亮色模式下的字符计数 */
.container.light-mode .char-count {
  color: rgba(0, 0, 0, 0.6);
}

/* 弹幕项样式 */
.barrage_container {
  display: inline-flex;
  align-items: center;
  padding: 0.2rem 0.4rem;
  border-radius: 0.25rem;
  backdrop-filter: blur(10px);
  z-index: 100;
  white-space: nowrap;
}

/* 暗色模式下的弹幕容器 */
.container.dark-mode .barrage_container {
  background-color: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 亮色模式下的弹幕容器 */
.container.light-mode .barrage_container {
  background-color: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.nickname {
  /* font-weight: bold; */
  font-size: 0.9rem;
}

/* 暗色模式下的昵称 */
.container.dark-mode .nickname {
  color: white;
}

/* 亮色模式下的昵称 */
.container.light-mode .nickname {
  color: #1f2937;
}

.content {
  transition: color 0.3s ease;
  font-size: 0.9rem;
}

/* 暗色模式下的内容 */
.container.dark-mode .content {
  color: white;
}

/* 亮色模式下的内容 */
.container.light-mode .content {
  color: #1f2937;
}

/* 加载遮罩 */
.loading_mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading_mask .el-icon {
  font-size: 2rem;
  color: white;
}
</style>

