<template>
  <div v-if="visible" class="login-dialog-overlay" @click="closeDialog">
    <div class="login-dialog-container" @click.stop>
      <div class="login-card">
        <!-- 关闭按钮 -->
        <button @click="closeDialog" class="close-btn" aria-label="关闭">
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
          <button class="email-login-btn" @click="showEmailLoginForm">
            <svg class="email-icon" viewBox="0 0 24 24" width="20" height="20">
              <path fill="currentColor" d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z" />
            </svg>
            通过邮箱 登录
          </button>

          <div class="divider">
            <span class="divider-text">或通过社交账号登录</span>
          </div>

          <div class="social-login-buttons">
            <button class="github-login-btn" @click="handleGitHubLogin" :disabled="githubLoading">
              <svg class="github-icon" viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z" />
              </svg>
              GitHub
            </button>

            <button class="google-login-btn" @click="handleGoogleLogin" :disabled="googleLoading">
              <svg class="google-icon" viewBox="0 0 24 24" width="20" height="20">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
              </svg>
              Google
            </button>
          </div>

          <button class="wechat-login-btn" @click="handleWechatLogin" :disabled="wechatLoading">
            <svg class="wechat-icon" viewBox="0 0 24 24" width="24" height="24">
              <path fill="#07C160" d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.496-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.68c-2.316 0-4.198 1.807-4.198 4.034 0 2.227 1.882 4.034 4.198 4.034a4.2 4.2 0 0 0 3.113-1.356.662.662 0 0 1 .51-.24c.06 0 .12.01.175.03l.854.496a.236.236 0 0 0 .22.01.238.238 0 0 0 .12-.14l.2-.75a.55.55 0 0 1 .33-.38c.64-.25 1.22-.6 1.71-1.03a4.06 4.06 0 0 0 1.418-3.054c0-2.227-1.882-4.034-4.198-4.034zm-2.51 2.68c.356 0 .645.293.645.654a.642.642 0 0 1-.645.652.642.642 0 0 1-.645-.652c0-.361.289-.654.645-.654zm3.79 0c.356 0 .645.293.645.654a.642.642 0 0 1-.645.652.642.642 0 0 1-.645-.652c0-.361.289-.654.645-.654z" />
            </svg>
            <span class="wechat-text">微信</span>
          </button>
        </div>

        <!-- 邮箱登录表单 -->
        <div v-else class="email-login-form">
          <button @click="backToMethodSelection" class="back-btn">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
            返回
          </button>

          <form class="login-form" @submit.prevent="handleLogin" novalidate>
            <div class="el-form-item">
              <label class="form-label">邮箱</label>
              <div class="form-input">
                <div class="el-input__wrapper" :class="{ 'is-focus': focusKey === 'email' }">
                  <input
                    v-model="loginForm.email"
                    type="email"
                    placeholder="your@email.com"
                    class="el-input__inner"
                    @focus="focusKey = 'email'"
                    @blur="focusKey = ''"
                  />
                </div>
              </div>
              <div v-if="errors.email" class="form-error">{{ errors.email }}</div>
            </div>

            <div class="el-form-item">
              <label class="form-label">密码</label>
              <div class="form-input">
                <div class="el-input__wrapper" :class="{ 'is-focus': focusKey === 'password' }">
                  <input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="........"
                    class="el-input__inner"
                    @focus="focusKey = 'password'"
                    @blur="focusKey = ''"
                  />
                </div>
              </div>
              <div v-if="errors.password" class="form-error">{{ errors.password }}</div>
            </div>

            <div class="el-form-item">
              <label class="form-label">验证码</label>
              <div class="captcha-container">
                <div class="captcha-input">
                  <div class="el-input__wrapper" :class="{ 'is-focus': focusKey === 'captcha' }">
                    <input
                      v-model="loginForm.captcha"
                      placeholder="输入验证码"
                      class="el-input__inner"
                      @focus="focusKey = 'captcha'"
                      @blur="focusKey = ''"
                    />
                  </div>
                </div>
                <div class="captcha-image" @click="refreshCaptcha">
                  <span class="captcha-text">{{ captchaCode }}</span>
                </div>
              </div>
              <div v-if="errors.captcha" class="form-error">{{ errors.captcha }}</div>
            </div>

            <div class="forgot-password">
              <a href="#" class="forgot-link" @click.prevent>忘记密码? <span class="underline">点击这里重置</span></a>
            </div>

            <div class="el-form-item">
              <button type="submit" class="login-btn" :disabled="loading">
                <span v-if="loading" class="btn-spinner"></span>
                <span>登录</span>
              </button>
            </div>

            <div class="el-form-item">
              <button type="button" class="register-btn" @click="goToRegister">
                没有账号? 注册
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>

  <!-- 轻量 Toast -->
  <div v-if="toast.visible" class="landing-toast" :class="`landing-toast--${toast.type}`">
    {{ toast.message }}
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

const visible = ref(false)
const showEmailForm = ref(false)
const loading = ref(false)
const googleLoading = ref(false)
const githubLoading = ref(false)
const wechatLoading = ref(false)
const captchaCode = ref('')
const focusKey = ref('')

const loginForm = reactive({
  email: '',
  password: '',
  captcha: '',
})

const errors = reactive({
  email: '',
  password: '',
  captcha: '',
})

const toast = reactive({ visible: false, message: '', type: 'info', timer: null })

function showToast(message, type = 'info', duration = 2500) {
  if (toast.timer) clearTimeout(toast.timer)
  toast.message = message
  toast.type = type
  toast.visible = true
  toast.timer = setTimeout(() => { toast.visible = false }, duration)
}

function generateCaptcha() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < 5; i++) result += chars.charAt(Math.floor(Math.random() * chars.length))
  captchaCode.value = result
}

function refreshCaptcha() { generateCaptcha() }

function showEmailLoginForm() {
  showEmailForm.value = true
  generateCaptcha()
}

function backToMethodSelection() {
  showEmailForm.value = false
}

function resetForm() {
  loginForm.email = ''
  loginForm.password = ''
  loginForm.captcha = ''
  errors.email = ''
  errors.password = ''
  errors.captcha = ''
}

function openDialog() {
  visible.value = true
  showEmailForm.value = false
  document.body.style.overflow = 'hidden'
}

function closeDialog() {
  visible.value = false
  showEmailForm.value = false
  document.body.style.overflow = ''
  resetForm()
}

function validate() {
  errors.email = ''
  errors.password = ''
  errors.captcha = ''
  let ok = true
  if (!loginForm.email) { errors.email = '请输入邮箱'; ok = false }
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(loginForm.email)) { errors.email = '请输入正确的邮箱格式'; ok = false }
  if (!loginForm.password) { errors.password = '请输入密码'; ok = false }
  if (!loginForm.captcha) { errors.captcha = '请输入验证码'; ok = false }
  return ok
}

async function handleLogin() {
  if (!validate()) return
  if (loginForm.captcha.toLowerCase() !== captchaCode.value.toLowerCase()) {
    showToast('验证码错误', 'error')
    refreshCaptcha()
    return
  }
  loading.value = true
  try {
    const res = await fetch(`${API_BASE}/user/user/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: loginForm.email, password: loginForm.password }),
    })
    const data = await res.json()
    if (!res.ok || (data.code !== undefined && data.code !== 200 && data.code !== 0)) {
      throw new Error(data.msg || data.message || '登录失败')
    }
    const token = data.data?.token || data.token
    if (token) localStorage.setItem('token', token)
    showToast('登录成功', 'success', 1200)
    const params = new URLSearchParams(window.location.search)
    const redirect = params.get('redirect') || '/home'
    setTimeout(() => { window.location.href = redirect }, 600)
  } catch (err) {
    showToast(err.message || '登录失败', 'error')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

async function handleGitHubLogin() {
  if (githubLoading.value) return
  githubLoading.value = true
  try {
    sessionStorage.setItem('oauth_source', 'github')
    const res = await fetch(`${API_BASE}/user/user/github/url`)
    const data = await res.json()
    let authUrl = data.data || data.url
    try {
      const url = new URL(authUrl)
      let redirectUri = url.searchParams.get('redirect_uri')
      if (redirectUri) {
        const decoded = decodeURIComponent(redirectUri)
        const redirectUrl = new URL(decoded)
        redirectUrl.searchParams.set('source', 'github')
        url.searchParams.set('redirect_uri', redirectUrl.toString())
        authUrl = url.toString()
      } else {
        url.searchParams.set('redirect_uri', `${window.location.origin}/home?source=github`)
        authUrl = url.toString()
      }
    } catch (_) { /* 后端 URL 已就绪 */ }
    window.location.href = authUrl
  } catch (err) {
    showToast('获取 GitHub 授权链接失败：' + (err.message || '未知错误'), 'error')
    githubLoading.value = false
    sessionStorage.removeItem('oauth_source')
  }
}

let googleScriptLoading = null
function loadGoogleScript() {
  if (window.google?.accounts?.id) return Promise.resolve()
  if (googleScriptLoading) return googleScriptLoading
  googleScriptLoading = new Promise((resolve, reject) => {
    const s = document.createElement('script')
    s.src = 'https://accounts.google.com/gsi/client'
    s.async = true
    s.defer = true
    s.onload = () => resolve()
    s.onerror = () => reject(new Error('Google Identity Services 脚本加载失败'))
    document.head.appendChild(s)
  })
  return googleScriptLoading
}

async function handleGoogleLogin() {
  if (googleLoading.value) return
  googleLoading.value = true
  try {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID
    if (!clientId) throw new Error('缺少 Google Client ID')
    await loadGoogleScript()
    const idToken = await new Promise((resolve, reject) => {
      window.google.accounts.id.initialize({
        client_id: clientId,
        auto_select: false,
        callback: (resp) => {
          if (resp?.credential) resolve(resp.credential)
          else reject(new Error('未取得 Google id_token'))
        },
      })
      window.google.accounts.id.prompt()
    })
    const res = await fetch(`${API_BASE}/user/user/google/idtoken-login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ idToken }),
    })
    const data = await res.json()
    if (!res.ok || (data.code !== undefined && data.code !== 200 && data.code !== 0)) {
      throw new Error(data.msg || data.message || 'Google 登录失败')
    }
    const token = data.data?.token || data.token
    if (token) localStorage.setItem('token', token)
    showToast('登录成功', 'success', 1200)
    setTimeout(() => { window.location.href = '/home' }, 600)
  } catch (err) {
    showToast(err.message || 'Google 登录失败', 'error')
  } finally {
    googleLoading.value = false
  }
}

function handleWechatLogin() {
  showToast('该功能暂未开放', 'info')
}

function goToRegister() {
  closeDialog()
  window.location.href = '/register'
}

onMounted(() => {
  generateCaptcha()
})

defineExpose({ open: openDialog, close: closeDialog })
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
  padding: 0;
}

.close-btn:hover {
  color: #333;
}

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

.github-icon,
.google-icon {
  flex-shrink: 0;
}

.wechat-login-btn {
  width: 100%;
  height: 52px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 500;
  background: white;
  border: 1px solid #07C160;
  color: #07C160;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  margin-top: 12px;
}

.wechat-login-btn:hover {
  background: #E8F5E9;
  border-color: #07C160;
  transform: translateY(-1px);
}

.wechat-login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.wechat-icon {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
}

.wechat-text {
  flex: 0 0 auto;
}

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
  padding: 0;
}

.back-btn:hover {
  color: #333;
}

.login-form {
  margin-bottom: 24px;
}

.el-form-item {
  margin-bottom: 18px;
}

.el-form-item:last-child {
  margin-bottom: 0;
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

.el-input__wrapper {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  box-shadow: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  padding: 0 12px;
  height: 40px;
  box-sizing: border-box;
}

.el-input__wrapper:hover {
  border-color: #409eff;
}

.el-input__wrapper.is-focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.el-input__inner {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  font-family: inherit;
}

.el-input__inner::placeholder {
  color: #a8abb2;
}

.form-error {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
  line-height: 1;
}

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
  user-select: none;
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

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: #409eff;
  border: none;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-btn:hover:not(:disabled) {
  background: #66b1ff;
  transform: translateY(-1px);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.btn-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
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
  cursor: pointer;
  transition: all 0.2s ease;
}

.register-btn:hover {
  background: #f5f5f5;
  border-color: #409eff;
}

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
  position: relative;
}

.landing-toast {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  min-width: 240px;
  max-width: 80vw;
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 14px;
  color: #fff;
  z-index: 10000000;
  text-align: center;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.16);
  animation: toastIn 0.2s ease-out;
}

.landing-toast--success { background: #67c23a; }
.landing-toast--error { background: #f56c6c; }
.landing-toast--info { background: #909399; }

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
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

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes toastIn {
  from { opacity: 0; transform: translate(-50%, -8px); }
  to   { opacity: 1; transform: translate(-50%, 0); }
}

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

  .login-btn,
  .register-btn {
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

  .wechat-login-btn {
    width: 100%;
    height: 52px;
    font-size: 16px;
    margin-top: 10px;
  }

  .email-login-btn {
    height: 44px;
    font-size: 15px;
  }
}

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

  .el-input__wrapper {
    background: #2a2a2a;
    border-color: #444;
  }

  .el-input__inner {
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

  .wechat-login-btn {
    background: #2a2a2a;
    border-color: #07C160;
    color: #07C160;
  }

  .wechat-login-btn:hover {
    background: #1a3a1a;
    border-color: #07C160;
  }

  .back-btn {
    color: #ccc;
  }

  .back-btn:hover {
    color: #fff;
  }
}
</style>
