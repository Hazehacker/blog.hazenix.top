<template>
  <div class="relative w-full h-screen overflow-hidden">
    <!-- 星空背景 -->
    <GalaxyBackground
      class="absolute inset-0 z-0 pointer-events-auto"
      :focal="[0.5, 0.5]"
      :rotation="[1, 0]"
      :star-speed="0.8"
      :density="1.2"
      :hue-shift="140"
      :glow-intensity="0.4"
      :twinkle-intensity="0.5"
      :mouse-repulsion="true"
      :repulsion-strength="3"
      :auto-center-repulsion="0"
      :transparent="true"
      :speed="1.0"
      :mouse-interaction="true"
    />

    <!-- 页面内容 -->
    <div class="absolute inset-0 flex flex-col items-center justify-center text-white z-10  pointer-events-auto" style="margin-bottom: 20px;">
      <nav class="flex items-center justify-between w-full max-w-4xl px-6 py-4 rounded-full bg-black/70 backdrop-blur-sm">
        <a :href="githubUrl" target="_blank" rel="noopener noreferrer" class="flex items-center space-x-2 hover:opacity-80 transition-opacity cursor-pointer">
          <svg class="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">...</svg>
          <span class="font-bold">GitHub</span>
        </a>
        <div class="space-x-4">
          <a href="/home" class="hover:text-gray-300">Home</a>
          <a href="/articles" class="hover:text-gray-300">Articles</a>
        </div>
      </nav>

      <!-- <button class="mt-10 px-4 py-2 rounded-full bg-black/50 hover:bg-black/70 transition">
        🌌 welcome!
      </button> -->

      <h1 class="text-4xl md:text-6xl font-bold mt-8 text-center leading-tight">
        Hazenix's blog<br />
        blog.hazenix.top
      </h1>

      <div class="mt-10 flex gap-4" >
        <button @click="goToLogin" class="px-6 py-3 rounded-full bg-white text-black font-medium hover:bg-gray-200 transition">
          登录
        </button>
        <button @click="goToHome" class="px-6 py-3 rounded-full bg-black/50 text-white font-medium hover:bg-black/70 transition">
          随便看看
        </button>
      </div>
    </div>
    <!-- 登录弹窗（登陆页本地挂载） -->
    <LandingLogin ref="loginDialogRef" />
  </div>
</template>

<script setup>
import GalaxyBackground from '@/Backgrounds/Galaxy/Galaxy.vue';
import { ref } from 'vue';
import LandingLogin from '@/components/common/LandingLogin.vue';

const loginDialogRef = ref();

// GitHub 链接
const githubUrl = 'https://github.com/HazeHacker';

const goToLogin = () => {
  loginDialogRef.value?.open();
};

const goToHome = () => {
  window.location.href = '/home?fromIndex=true';
};
</script>

<style>
body {
  background-color: #000; /* 黑色背景 */
}
</style>