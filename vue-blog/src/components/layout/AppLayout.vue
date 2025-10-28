<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 text-gray-800 dark:text-gray-200">
    <AppHeader />
    <main class="pt-20">
      <div class="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <router-view />
      </div>
    </main>
    <AppFooter />
    
    <!-- 全局对话框组件 -->
    <SearchDialog ref="searchDialogRef" />
    <LoginDialog ref="loginDialogRef" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import AppHeader from './AppHeader.vue'
import AppFooter from './AppFooter.vue'
import SearchDialog from '@/components/common/SearchDialog.vue'
import LoginDialog from '@/components/common/LoginDialog.vue'

const searchDialogRef = ref()
const loginDialogRef = ref()

// 监听对话框打开事件
const handleOpenSearchDialog = () => {
  if (searchDialogRef.value) {
    searchDialogRef.value.open()
  }
}

const handleOpenLoginDialog = () => {
  if (loginDialogRef.value) {
    loginDialogRef.value.open()
  }
}

onMounted(() => {
  window.addEventListener('open-search-dialog', handleOpenSearchDialog)
  window.addEventListener('open-login-dialog', handleOpenLoginDialog)
})

onUnmounted(() => {
  window.removeEventListener('open-search-dialog', handleOpenSearchDialog)
  window.removeEventListener('open-login-dialog', handleOpenLoginDialog)
})
</script>
