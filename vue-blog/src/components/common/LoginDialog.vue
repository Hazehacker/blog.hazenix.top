<template>
  <div v-if="visible" class="login-dialog-overlay" @click="closeDialog">
    <div class="login-dialog-container" @click.stop>
      <div class="login-card">
        <!-- 关闭按钮 -->
        <button @click="closeDialog" class="close-btn">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
        
        <!-- 标题 -->
        <div class="login-header">
          <h1 class="login-title">登录到 Hazenix's Blog </h1>
          <p class="login-subtitle">欢迎回来! 请登录您的账号</p>
        </div>
        
        <!-- 登录方式选择界面 -->
        <div v-if="!showEmailForm" class="login-method-selection">
          <!-- 邮箱登录按钮 -->
          <button class="email-login-btn" @click="showEmailLoginForm">
            <svg class="email-icon" viewBox="0 0 24 24" width="20" height="20">
              <path fill="currentColor" d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
            </svg>
            通过邮箱 登录
          </button>
          
          <!-- 分割线 -->
          <div class="divider">
            <span class="divider-text">或通过社交账号登录</span>
          </div>
          
          <!-- 社交登录按钮 -->
          <div class="social-login-buttons">
            <button class="github-login-btn" @click="handleGitHubLogin" :disabled="githubLoading">
              <svg class="github-icon" viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
              GitHub
            </button>
            
            <button class="google-login-btn" @click="handleGoogleLogin" :disabled="googleLoading">
              <svg class="google-icon" viewBox="0 0 24 24" width="20" height="20">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              Google
            </button>
          </div>
        </div>
        
        <!-- 邮箱登录表单 -->
        <div v-else class="email-login-form">
          <!-- 返回按钮 -->
          <button @click="backToMethodSelection" class="back-btn" >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
            返回
          </button>
          
          <el-form :model="loginForm" :rules="rules" ref="formRef" class="login-form">
            <el-form-item prop="email">
              <label class="form-label">邮箱</label>
              <el-input 
                v-model="loginForm.email" 
                placeholder="your@email.com"
                class="form-input"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <label class="form-label">密码</label>
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="........"
                class="form-input"
              />
            </el-form-item>
            
            <el-form-item prop="captcha">
              <label class="form-label">验证码</label>
              <div class="captcha-container">
                <el-input 
                  v-model="loginForm.captcha" 
                  placeholder="输入验证码"
                  class="captcha-input"
                />
                <div class="captcha-image" @click="refreshCaptcha">
                  <span class="captcha-text">{{ captchaCode }}</span>
                </div>
              </div>
            </el-form-item>
            
            <!-- 忘记密码链接 -->
            <div class="forgot-password">
              <a href="#" class="forgot-link">忘记密码? <span class="underline">点击这里重置</span></a>
            </div>
            
            <!-- 登录按钮 -->
            <el-form-item>
              <el-button 
                type="primary" 
                class="login-btn" 
                @click="handleLogin" 
                :loading="loading"
              >
                登录
              </el-button>
            </el-form-item>
            
            <!-- 注册按钮 -->
            <el-form-item>
              <el-button 
                class="register-btn" 
                @click="goToRegister"
              >
                没有账号? 注册
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getGoogleAuthUrl, getGithubAuthUrl } from '@/api/auth'
import { getGoogleIdToken } from '@/utils/googleAuth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const googleLoading = ref(false)
const githubLoading = ref(false)
const visible = ref(false)
const showEmailForm = ref(false)
const captchaCode = ref('')

const loginForm = ref({
  email: '',
  password: '',
  captcha: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 生成验证码
const generateCaptcha = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < 5; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  captchaCode.value = result
}

// 刷新验证码
const refreshCaptcha = () => {
  generateCaptcha()
}

// 显示邮箱登录表单
const showEmailLoginForm = () => {
  showEmailForm.value = true
  generateCaptcha()
}

// 返回登录方式选择
const backToMethodSelection = () => {
  showEmailForm.value = false
}

// 设置 Message z-index 高于登录框
const setMessageZIndex = () => {
  // 立即设置（如果元素已存在）
  const messageContainer = document.getElementById('el-message-container')
  if (messageContainer) {
    messageContainer.style.zIndex = '10000000'
  }
  
  const messages = document.querySelectorAll('.el-message')
  messages.forEach(msg => {
    if (msg instanceof HTMLElement) {
      msg.style.zIndex = '10000000'
    }
  })
  
  // 使用 nextTick 确保 DOM 更新后再设置
  nextTick(() => {
    const container = document.getElementById('el-message-container')
    if (container) {
      container.style.zIndex = '10000000'
    }
    
    const allMessages = document.querySelectorAll('.el-message')
    allMessages.forEach(msg => {
      if (msg instanceof HTMLElement) {
        msg.style.zIndex = '10000000'
      }
    })
  })
  
  // 如果还没有 observer，创建一个来监听新添加的 Message
  if (!window.__messageObserver) {
    const observer = new MutationObserver(() => {
      const newMessages = document.querySelectorAll('.el-message')
      newMessages.forEach(msg => {
        if (msg instanceof HTMLElement) {
          msg.style.zIndex = '10000000'
        }
      })
      const container = document.getElementById('el-message-container')
      if (container) {
        container.style.zIndex = '10000000'
      }
    })
    
    // 观察 body 的变化
    observer.observe(document.body, {
      childList: true,
      subtree: true
    })
    
    // 保存 observer 以便清理
    window.__messageObserver = observer
  }
}

// 打开登录对话框
const openDialog = () => {
  visible.value = true
  showEmailForm.value = false
  // 阻止背景滚动
  document.body.style.overflow = 'hidden'
  // 设置 Message z-index
  setMessageZIndex()
}

// 关闭登录对话框
const closeDialog = () => {
  visible.value = false
  showEmailForm.value = false
  // 恢复背景滚动
  document.body.style.overflow = ''
  
  // 清理 observer
  if (window.__messageObserver) {
    window.__messageObserver.disconnect()
    delete window.__messageObserver
  }
  
  // 重置表单
  loginForm.value = {
    email: '',
    password: '',
    captcha: ''
  }
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 跳转到注册页面
const goToRegister = () => {
  closeDialog()
  router.push('/register')
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    
    // 验证验证码
    if (loginForm.value.captcha.toLowerCase() !== captchaCode.value.toLowerCase()) {
      ElMessage.error('验证码错误')
      // 确保 Message 显示在登录框之上（延迟一下确保 DOM 已更新）
      setTimeout(() => {
        setMessageZIndex()
      }, 10)
      refreshCaptcha()
      return
    }
    
    loading.value = true
    
    // 根据新的API接口，直接使用邮箱和密码
    const loginData = {
      email: loginForm.value.email,
      password: loginForm.value.password
    }
    
    await userStore.login(loginData)
    // 登录成功后，获取完整的用户信息（包括头像）
    try {
      await userStore.getUserInfo()
    } catch (error) {
      // console.warn('获取用户信息失败，但登录已成功:', error)
    }
    ElMessage.success('登录成功')
    closeDialog()
    // 登录后跳转：优先使用 redirect 参数，其次回到主页
    const redirectTarget = route?.query?.redirect
    if (typeof redirectTarget === 'string' && redirectTarget) {
      router.replace(redirectTarget)
    } else {
      router.replace('/home')
    }
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('登录失败')
    }
    // 确保 Message 显示在登录框之上（延迟一下确保 DOM 已更新）
    setTimeout(() => {
      setMessageZIndex()
    }, 10)
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

// GitHub登录处理
const handleGitHubLogin = async () => {
  // 防止重复点击
  if (githubLoading.value) {
    return
  }
  
  try {
    githubLoading.value = true
    
    // 使用 sessionStorage 记录 GitHub 登录（作为备用方案）
    sessionStorage.setItem('oauth_source', 'github')
    
    // 获取GitHub授权URL
    const res = await getGithubAuthUrl()
    let authUrl = res.data
    
    // GitHub 登录可以在 redirect_uri 中包含 source 参数
    // 尝试解析 URL 并修改 redirect_uri
    try {
      const url = new URL(authUrl)
      let redirectUri = url.searchParams.get('redirect_uri')
      
      if (redirectUri) {
        // 解码现有的 redirect_uri
        const decodedUri = decodeURIComponent(redirectUri)
        const redirectUrl = new URL(decodedUri)
        // 添加 source 参数
        redirectUrl.searchParams.set('source', 'github')
        // 重新编码并设置
        url.searchParams.set('redirect_uri', redirectUrl.toString())
        authUrl = url.toString()
      } else {
        // 如果没有 redirect_uri，添加它
        url.searchParams.set('redirect_uri', `${window.location.origin}/home?source=github`)
        authUrl = url.toString()
      }
    } catch (urlError) {
      // 如果 URL 解析失败，尝试简单替换或追加
      // console.warn('URL 解析失败，尝试简单处理:', urlError)
      // 如果后端返回的 URL 已经是完整的，我们相信后端已经处理好了
      // 否则，这里可能需要根据实际情况调整
    }
    
    // console.log('跳转到 GitHub 授权页面:', authUrl)
    // 跳转到 GitHub 授权页面
    window.location.href = authUrl
  } catch (error) {
    ElMessage.error('获取GitHub授权链接失败: ' + (error.message || '未知错误'))
    // console.error('GitHub login error:', error)
    githubLoading.value = false
    // 清除 sessionStorage
    sessionStorage.removeItem('oauth_source')
  }
  // 注意：跳转后页面会卸载，不需要在 finally 中重置 loading
}

// Google登录处理（前端流程：獲取 id_token 再交給後端換取應用 token）
const handleGoogleLogin = async () => {
  if (googleLoading.value) return
  try {
    googleLoading.value = true
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID
    const idToken = await getGoogleIdToken({ clientId, useOneTap: false })
    await userStore.googleLoginByIdToken(idToken)
    try {
      await userStore.getUserInfo()
    } catch (e) {
      // console.warn('获取用户信息失败，但登录已成功:', e)
    }
    ElMessage.success('登录成功')
    closeDialog()
    const redirectTarget = route?.query?.redirect
    if (typeof redirectTarget === 'string' && redirectTarget) {
      router.replace(redirectTarget)
    } else {
      router.replace('/home')
    }
  } catch (error) {
    ElMessage.error(error?.message || 'Google 登录失败')
    setTimeout(() => setMessageZIndex(), 10)
  } finally {
    googleLoading.value = false
  }
}

onMounted(() => {
  generateCaptcha()
})

// 暴露方法给父组件调用
defineExpose({
  open: openDialog,
  close: closeDialog
})
</script>

<style scoped>
.login-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 999999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: fadeIn 0.3s ease-out;
  /* 使用backdrop-filter实现背景模糊效果 */
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.login-dialog-container {
  position: relative;
  animation: slideIn 0.3s ease-out;
  width: 100%;
  max-width: 420px;
  margin: auto;
}

.login-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
  padding: 40px;
  width: 100%;
  position: relative;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: none;
  border: none;
  cursor: pointer;
  color: #666;
  transition: color 0.2s ease;
  z-index: 10;
}

.close-btn:hover {
  color: #333;
}

/* 标题 */
.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.2;
}

.login-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* 登录方式选择界面 */
.login-method-selection {
  margin-bottom: 24px;
}

.email-login-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: #409eff;
  border: none;
  color: white;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
  cursor: pointer;
}

.email-login-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

.email-icon {
  flex-shrink: 0;
}

/* 社交登录按钮容器 */
.social-login-buttons {
  display: flex;
  gap: 12px;
}

.github-login-btn,
.google-login-btn {
  flex: 1;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: white;
  border: 1px solid #e0e0e0;
  color: #333;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
}

.github-login-btn:hover,
.google-login-btn:hover {
  background: #f5f5f5;
  border-color: #d0d0d0;
  transform: translateY(-1px);
}

.github-login-btn:disabled,
.google-login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.github-icon {
  flex-shrink: 0;
}

.google-icon {
  flex-shrink: 0;
}

/* 邮箱登录表单 */
.email-login-form {
  position: relative;
}

.back-btn {
  position: absolute;
  top: -25px;
  left: 0;
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  transition: color 0.2s ease;
}

.back-btn:hover {
  color: #333;
}

/* 表单样式 */
.login-form {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
}

.form-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  box-shadow: none;
  transition: border-color 0.2s ease;
}

.form-input :deep(.el-input__wrapper:hover) {
  border-color: #409eff;
}

.form-input :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

/* 验证码容器 */
.captcha-container {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 100px;
  height: 40px;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.captcha-image:hover {
  background: #e8e8e8;
}

.captcha-text {
  font-family: 'Courier New', monospace;
  font-size: 18px;
  font-weight: bold;
  color: #333;
  letter-spacing: 2px;
  transform: rotate(-5deg);
  text-decoration: line-through;
}

/* 忘记密码 */
.forgot-password {
  text-align: right;
  margin-bottom: 24px;
}

.forgot-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.underline {
  text-decoration: underline;
}

.forgot-link:hover {
  color: #66b1ff;
}

/* 按钮样式 */
.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: #409eff;
  border: none;
  transition: all 0.2s ease;
}

.login-btn:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

.register-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: white;
  border: 1px solid #e0e0e0;
  color: #409eff;
  transition: all 0.2s ease;
}

.register-btn:hover {
  background: #f5f5f5;
  border-color: #409eff;
}

/* 分割线 */
.divider {
  position: relative;
  text-align: center;
  margin: 24px 0;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e0e0e0;
}

.divider-text {
  background: white;
  padding: 0 16px;
  color: #999;
  font-size: 14px;
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    margin: 20px;
    padding: 32px;
    max-width: none;
    border-radius: 16px;
  }
  
  .login-title {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .login-card {
    margin: 16px;
    padding: 24px;
    border-radius: 12px;
  }
  
  .login-title {
    font-size: 22px;
  }
  
  .login-subtitle {
    font-size: 14px;
  }
  
  .captcha-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .captcha-image {
    width: 100%;
    height: 50px;
  }
  
  .form-label {
    font-size: 13px;
  }
  
  .login-btn, .register-btn {
    height: 44px;
    font-size: 15px;
  }
  
  .social-login-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .github-login-btn,
  .google-login-btn {
    width: 100%;
    height: 44px;
    font-size: 15px;
  }
  
  .email-login-btn {
    height: 44px;
    font-size: 15px;
  }
}

/* 暗色主题支持 */
@media (prefers-color-scheme: dark) {
  .login-card {
    background: #1a1a1a;
    color: #fff;
  }
  
  .login-title {
    color: #fff;
  }
  
  .login-subtitle {
    color: #ccc;
  }
  
  .form-label {
    color: #fff;
  }
  
  .captcha-image {
    background: #2a2a2a;
    border-color: #444;
  }
  
  .captcha-text {
    color: #fff;
  }
  
  .register-btn {
    background: #2a2a2a;
    border-color: #444;
    color: #409eff;
  }
  
  .register-btn:hover {
    background: #333;
  }
  
  .divider-text {
    background: #1a1a1a;
    color: #ccc;
  }
  
  .email-login-btn {
    background: #409eff;
  }
  
  .email-login-btn:hover {
    background: #66b1ff;
  }
  
  .github-login-btn,
  .google-login-btn {
    background: #2a2a2a;
    border-color: #444;
    color: #fff;
  }
  
  .github-login-btn:hover,
  .google-login-btn:hover {
    background: #333;
    border-color: #555;
  }
  
  .back-btn {
    color: #ccc;
  }
  
  .back-btn:hover {
    color: #fff;
  }
}
</style>

<style>
/* 确保异常提示信息显示在登录框之上 */
#el-message-container,
.el-message-container,
.el-message {
  z-index: 10000000 !important;
}

/* 确保所有 Message 相关元素都在登录框之上 */
.el-message__wrapper,
.el-message__content {
  z-index: 10000000 !important;
}
</style>