<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="visible"
        class="fixed inset-0 z-[9999]"
        @click="close"
        style="pointer-events: auto;"
      >
        <!-- 遮罩层 -->
        <div class="absolute inset-0 bg-black/40 backdrop-blur-sm"></div>
        
        <!-- 引导内容 -->
        <div
          ref="guideContainer"
          class="absolute"
          :style="guideStyle"
          style="pointer-events: none;"
        >
            <!-- 箭头指向主题切换按钮 -->
          <div
            class="relative flex items-center justify-center"
            :style="arrowStyle"
            style="pointer-events: none;"
          >
            <!-- 脉冲动画圆圈 -->
            <div class="absolute w-full h-full rounded-full bg-yellow-500/20 animate-ping"></div>
            <!-- 箭头动画 -->
            <div class="absolute animate-bounce-horizontal">
              <svg
                class="w-8 h-8 text-yellow-500 drop-shadow-lg"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2.5"
                  d="M13 7l5 5m0 0l-5 5m5-5H6"
                />
              </svg>
            </div>
          </div>
          <!-- 提示框 -->
          <div
            class="theme-guide-tooltip"
            @click.stop
            style="pointer-events: auto;margin-top: 47px;"
          >
            <!-- 关闭按钮 -->
            <button
              @click="close"
              class="absolute top-2 right-2 w-6 h-6 flex items-center justify-center rounded-full transition-colors theme-guide-close-btn"
              aria-label="关闭引导"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
            
            <!-- 提示文字 -->
            <div class="theme-guide-content" >
              <div class="flex items-center gap-2 mb-2">
                <el-icon class="text-yellow-500 text-lg"><Sunny /></el-icon>
                <span class="theme-guide-title">
                  切换主题
                </span>
              </div>
              <p class="theme-guide-text">
                点击右上角的主题按钮可以切换到白色模式
              </p>
            </div>
          </div>
          
          
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import { Sunny } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const themeStore = useThemeStore()
const visible = ref(props.modelValue)
const guideContainer = ref(null)
const guideStyle = ref({})
const arrowStyle = ref({})

// 计算引导位置
const calculatePosition = async () => {
  await nextTick()
  
  // 查找主题切换按钮
  // 主题切换按钮在 AppHeader 中，通过查找包含 Sunny 或 Moon 图标的按钮
  const header = document.querySelector('header')
  if (!header) return
  
  // 查找主题切换按钮 - 使用特定的类名
  let themeButton = header.querySelector('.theme-toggle-button')
  
  // 如果找不到，尝试通过查找包含 el-icon 且是最后一个按钮
  if (!themeButton) {
    const buttons = header.querySelectorAll('.el-button')
    if (buttons.length > 0) {
      // 主题切换按钮通常是最后一个按钮
      themeButton = buttons[buttons.length - 1]
    }
  }
  
  if (themeButton) {
    const rect = themeButton.getBoundingClientRect()
    
    // 提示框宽度大约 240-280px
    const tooltipWidth = 260
    const tooltipHeight = 120
    const arrowSize = 40
    const spacing = 10 // 箭头和提示框之间的间距
    
    // 计算提示框位置：在主题按钮上方，右对齐
    let tooltipTop = rect.top - tooltipHeight - arrowSize - spacing
    const tooltipRight = Math.max(8, window.innerWidth - rect.right - 8) // 确保至少距离右边缘8px
    
    // 确保提示框不会超出屏幕顶部
    if (tooltipTop < 8) {
      tooltipTop = 8
    }
    
    // 计算箭头位置：指向主题按钮中心
    const buttonCenterX = rect.left + rect.width / 2
    const tooltipLeft = window.innerWidth - tooltipRight - tooltipWidth
    const arrowLeft = buttonCenterX - tooltipLeft - arrowSize / 2 +17
    const arrowTop = -(arrowSize + spacing)+56 // 将箭头放在提示框上方
    
    guideStyle.value = {
      top: `${tooltipTop}px`,
      right: `${tooltipRight}px`,
      zIndex: 10000
    }
    
    arrowStyle.value = {
      position: 'absolute',
      top: `${arrowTop}px`,
      left: `${arrowLeft}px`,
      width: `${arrowSize}px`,
      height: `${arrowSize}px`,
      zIndex: 9999
    }
    
    console.log('Theme guide position:', {
      tooltipTop,
      tooltipRight,
      arrowTop,
      arrowLeft,
      tooltipHeight
    })
  } else {
    // 如果找不到按钮，使用默认位置
    guideStyle.value = {
      top: '70px',
      right: '16px',
      zIndex: 10000
    }
    arrowStyle.value = {
      position: 'absolute',
      top: '135px',
      left: '50%',
      transform: 'translateX(-50%)',
      width: '40px',
      height: '40px',
      zIndex: 9999
    }
  }
}

watch(() => props.modelValue, async (val) => {
  visible.value = val
  if (val) {
    // 显示时重新计算位置
    await calculatePosition()
    // 监听窗口大小变化和滚动，重新计算位置
    window.addEventListener('resize', calculatePosition)
    window.addEventListener('scroll', calculatePosition, true)
  } else {
    // 隐藏时移除监听器
    window.removeEventListener('resize', calculatePosition)
    window.removeEventListener('scroll', calculatePosition, true)
  }
})

// 监听主题切换，如果用户切换到白色模式，自动关闭引导
watch(() => themeStore.isDark, (isDark) => {
  if (!isDark && visible.value) {
    close()
  }
})

const close = () => {
  visible.value = false
  emit('update:modelValue', false)
  window.removeEventListener('resize', calculatePosition)
  window.removeEventListener('scroll', calculatePosition, true)
}

onMounted(() => {
  if (visible.value) {
    calculatePosition()
  }
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 提示框样式 - 确保可见 */
:deep(.theme-guide-tooltip) {
  position: relative;
  background-color: #ffffff !important;
  border-radius: 0.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  padding: 1rem;
  padding-right: 2rem;
  min-width: 240px;
  max-width: 280px;
  margin-bottom: 0.5rem;
  border: 1px solid #e5e7eb;
  z-index: 10000 !important;
  opacity: 1 !important;
  visibility: visible !important;
  display: block !important;
}

.dark :deep(.theme-guide-tooltip) {
  background-color: #1f2937 !important;
  border-color: #374151 !important;
}

/* 关闭按钮样式 */
:deep(.theme-guide-close-btn) {
  color: #9ca3af;
  background-color: transparent;
}

:deep(.theme-guide-close-btn:hover) {
  color: #4b5563;
  background-color: #f3f4f6;
}

.dark :deep(.theme-guide-close-btn:hover) {
  color: #d1d5db;
  background-color: #374151;
}

/* 文字内容样式 */
:deep(.theme-guide-content) {
  position: relative;
  z-index: 10;
  opacity: 1 !important;
  visibility: visible !important;
  display: block !important;
}

:deep(.theme-guide-title) {
  font-weight: 600;
  font-size: 1rem;
  color: #1f2937 !important;
  display: inline-block !important;
}

.dark :deep(.theme-guide-title) {
  color: #e5e7eb !important;
}

:deep(.theme-guide-text) {
  font-size: 0.875rem;
  line-height: 1.5;
  color: #4b5563 !important;
  display: block !important;
  margin: 0 !important;
}

.dark :deep(.theme-guide-text) {
  color: #d1d5db !important;
}

@keyframes bounce-horizontal {
  0%, 100% {
    transform: translateX(0);
  }
  50% {
    transform: translateX(-12px);
  }
}

.animate-bounce-horizontal {
  animation: bounce-horizontal 1.5s ease-in-out infinite;
}

.animate-ping {
  animation: ping 2s cubic-bezier(0, 0, 0.2, 1) infinite;
}

@keyframes ping {
  75%, 100% {
    transform: scale(1.5);
    opacity: 0;
  }
}
</style>

