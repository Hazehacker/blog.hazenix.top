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
      :key="danmakuKey"
      :debounce="3000"
      random-channel
      :speeds="80"
      :channels="5"
      is-suspend
      :autoplay="true"
      v-model:danmus="treeHoleList"
      use-slot
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
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getTreeHoleList, addTreeHole } from '@/api/treeHole.js'
import { getToken } from '@/utils/auth.js'
import { useUserStore } from '@/stores/user.js'
import { useThemeStore } from '@/stores/theme.js'
import Galaxy from '@/Backgrounds/Galaxy/Galaxy.vue'

// 响应式数据
const originalTreeHoleList = ref([])  // 原始弹幕列表（所有弹幕）
const treeHoleList = ref([])          // 当前显示的弹幕列表
const isShowSubmit = ref(false)       // 控制提交按钮显示
const content = ref('')               // 输入框内容
const loading = ref(false)            // 加载状态
const isRefilling = ref(false)        // 是否正在重新填充，防止重复触发
const checkInterval = ref(null)       // 检查定时器
const lastDanmuCount = ref(0)         // 上次检查时的弹幕数量
const lowDanmuStartTime = ref(null)   // 弹幕数量降到很低时的时间戳
const lastRefillTime = ref(0)         // 上次重新填充的时间戳
const isIntervalRunning = ref(false)  // 定时器是否正在运行，防止重复启动
const danmakuKey = ref(0)             // 弹幕组件的key，用于强制重新渲染

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
      // 转换数据格式并保存到原始列表
      const formattedList = (res.data || []).map(item => {
        const username = item.username || '匿名用户'
        return {
          id: item.id || item.user_id,
          nickname: username,
          avatar: item.avatar || getDefaultAvatar(username),
          content: item.content,
          createTime: item.create_time
        }
      })
      
      originalTreeHoleList.value = formattedList
      
      // 如果是首次加载或当前弹幕列表为空，则填充弹幕
      if (treeHoleList.value.length === 0) {
        // 添加时间戳，确保组件认为这是新数据
        treeHoleList.value = formattedList.map(item => ({
          ...item,
          _refreshTime: Date.now()
        }))
      }
      
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
 * 检查当前屏幕上是否有弹幕元素
 */
function checkDanmuElements() {
  if (!danmakuRef.value) return 0
  
  try {
    // 尝试获取弹幕容器元素
    const container = danmakuRef.value.$el || danmakuRef.value
    if (!container) return 0
    
    // 查找所有弹幕元素
    // 尝试多种选择器来查找弹幕元素
    const selectors = [
      '.barrage_container',
      '[class*="danmu"]',
      '[class*="barrage"]',
      'div[style*="position"]', // 弹幕通常有绝对定位
    ]
    
    let danmuCount = 0
    for (const selector of selectors) {
      const elements = container.querySelectorAll(selector)
      if (elements.length > 0) {
        // 检查元素是否在可视区域内
        const visibleElements = Array.from(elements).filter(el => {
          const rect = el.getBoundingClientRect()
          return rect.width > 0 && rect.height > 0
        })
        danmuCount = Math.max(danmuCount, visibleElements.length)
      }
    }
    
    return danmuCount
  } catch (error) {
    console.warn('检查弹幕元素时出错:', error)
    return 0
  }
}

/**
 * 重新填充弹幕列表
 */
function refillDanmuList() {
  // 如果正在重新填充或原始列表为空，则跳过
  if (isRefilling.value || originalTreeHoleList.value.length === 0) {
    return
  }
  
  isRefilling.value = true
  
  // 先清空数组
  treeHoleList.value = []
  
  // 等待一小段时间，确保组件处理完清空操作
  setTimeout(() => {
    // 更新组件的key，强制重新渲染组件
    danmakuKey.value = Date.now()
    
    // 重新填充所有弹幕，为每个弹幕生成新的唯一ID，确保组件认为这是全新的数据
    const timestamp = Date.now()
    const newList = originalTreeHoleList.value.map((item, index) => ({
      ...item,
      // 生成新的唯一ID，使用原始ID + 时间戳 + 索引
      id: `${item.id}_${timestamp}_${index}`,
      // 添加一个时间戳字段，确保组件认为这是新数据
      _refreshTime: timestamp
    }))
    
    // 使用 nextTick 确保 DOM 更新完成
    nextTick(() => {
      treeHoleList.value = newList
      lastDanmuCount.value = treeHoleList.value.length
      lowDanmuStartTime.value = null  // 重置计时
      lastRefillTime.value = Date.now()  // 记录重新填充的时间
      
      // 再次等待，确保组件重新渲染和数据绑定完成
      setTimeout(() => {
        // 尝试手动触发播放（如果组件支持）
        if (danmakuRef.value) {
          try {
            // 先尝试恢复播放
            if (typeof danmakuRef.value.play === 'function') {
              danmakuRef.value.play()
            }
            // 或者尝试调用 start 方法
            if (typeof danmakuRef.value.start === 'function') {
              danmakuRef.value.start()
            }
            // 或者尝试调用 resume 方法
            if (typeof danmakuRef.value.resume === 'function') {
              danmakuRef.value.resume()
            }
          } catch (e) {
            console.warn('启动弹幕播放失败:', e)
          }
        }
        
        isRefilling.value = false
        console.log('弹幕已重新填充，数量:', treeHoleList.value.length)
      }, 300)
    })
  }, 200)
}

/**
 * 清除检查定时器
 */
function clearCheckInterval() {
  if (checkInterval.value) {
    clearInterval(checkInterval.value)
    checkInterval.value = null
  }
  isIntervalRunning.value = false
}

/**
 * 启动定时检查机制
 * 定期检查弹幕状态，当所有弹幕播放完毕时重新填充
 */
function startCheckInterval() {
  // 如果定时器已经在运行，先清除
  if (isIntervalRunning.value) {
    clearCheckInterval()
  }
  
  // 如果没有弹幕数据，不启动定时器
  if (originalTreeHoleList.value.length === 0) {
    return
  }
  
  // 标记定时器正在运行
  isIntervalRunning.value = true
  lastDanmuCount.value = treeHoleList.value.length
  lowDanmuStartTime.value = null  // 重置计时
  
  // 每1秒检查一次弹幕状态
  checkInterval.value = setInterval(() => {
    // 如果正在重新填充，跳过本次检查
    if (isRefilling.value) {
      return
    }
    
    // 检查当前弹幕列表长度
    const currentLength = treeHoleList.value.length
    const screenDanmuCount = checkDanmuElements()
    const now = Date.now()
    
    // 如果弹幕列表为空，立即重新填充
    if (currentLength === 0 && originalTreeHoleList.value.length > 0) {
      console.log('弹幕列表为空，立即重新填充')
      lowDanmuStartTime.value = null
      // 清除定时器
      clearCheckInterval()
      // 重新填充弹幕
      refillDanmuList()
      // 重新启动定时器
      setTimeout(() => {
        startCheckInterval()
      }, 2000)
      return
    }
    
    // 如果屏幕上弹幕数量很少（0-1条），开始计时
    // 但需要确保不是刚刚重新填充后的短暂时间（给弹幕一些时间开始播放）
    const timeSinceRefill = now - lastRefillTime.value
    
    // 如果距离上次重新填充不到5秒，不进行检测（给弹幕时间开始播放）
    if (screenDanmuCount <= 1 && currentLength > 0 && timeSinceRefill >= 5000) {
      // 如果还没有开始计时，记录开始时间
      if (lowDanmuStartTime.value === null) {
        lowDanmuStartTime.value = now
        console.log('弹幕数量降到很低，开始计时。屏幕弹幕数:', screenDanmuCount, '数组长度:', currentLength, '距离上次填充:', Math.round(timeSinceRefill/1000) + '秒')
      } else {
        // 如果已经计时超过5秒，说明弹幕真的播放完了
        const elapsed = now - lowDanmuStartTime.value
        if (elapsed >= 5000) {
          console.log('弹幕数量持续很低超过5秒，重新填充。屏幕弹幕数:', screenDanmuCount, '数组长度:', currentLength)
          lowDanmuStartTime.value = null
          // 清除定时器
          clearCheckInterval()
          // 重新填充弹幕
          refillDanmuList()
          // 等待重新填充完成后再启动定时器（给更多时间让弹幕开始播放）
          setTimeout(() => {
            startCheckInterval()
          }, 5000)
          return
        }
      }
    } else {
      // 如果弹幕数量增加了（>1条），重置计时
      if (screenDanmuCount > 1) {
        if (lowDanmuStartTime.value !== null) {
          console.log('弹幕数量增加，重置计时。屏幕弹幕数:', screenDanmuCount)
        }
        lowDanmuStartTime.value = null
      }
      // 如果距离上次重新填充不到5秒，也重置计时（避免误判）
      if (timeSinceRefill < 5000) {
        lowDanmuStartTime.value = null
      }
    }
    
    // 更新上次的弹幕数量
    lastDanmuCount.value = currentLength
  }, 1000)
}

// 监听弹幕列表的变化
watch(
  () => treeHoleList.value.length,
  (newLength, oldLength) => {
    // 当弹幕列表从有变为空时，立即重新填充
    if (oldLength > 0 && newLength === 0 && originalTreeHoleList.value.length > 0 && !isRefilling.value) {
      console.log('弹幕列表变为空，立即重新填充')
      refillDanmuList()
    }
  }
)

// 监听原始弹幕列表的变化，当有新数据时启动检查定时器
watch(
  () => originalTreeHoleList.value.length,
  (newLength) => {
    if (newLength > 0) {
      // 只有在定时器未运行时才启动
      if (!isIntervalRunning.value) {
        // 延迟启动，避免重复触发
        setTimeout(() => {
          if (!isIntervalRunning.value && originalTreeHoleList.value.length > 0) {
            startCheckInterval()
          }
        }, 100)
      }
    } else {
      // 如果没有弹幕数据，清除定时器
      clearCheckInterval()
    }
  },
  { immediate: true }
)

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
      
      // 刷新列表
      await getTreeHole()
      
      // 如果当前弹幕列表为空，立即填充新弹幕
      if (treeHoleList.value.length === 0 && originalTreeHoleList.value.length > 0) {
        treeHoleList.value = [...originalTreeHoleList.value]
      } else if (treeHoleList.value.length > 0) {
        // 如果当前有弹幕在播放，将新弹幕添加到原始列表即可
        // 新弹幕会在当前弹幕播放完毕后自动加入（通过 play-end 事件）
      }
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

// 组件卸载时清理定时器
onUnmounted(() => {
  clearCheckInterval()
  isRefilling.value = false
  lowDanmuStartTime.value = null
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

