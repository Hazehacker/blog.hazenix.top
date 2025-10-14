<template>
  <div class="min-h-screen bg-background-light dark:bg-background-dark text-gray-800 dark:text-gray-200">
    <div class="container mx-auto px-4 sm:px-6 lg:px-8">
      <AppHeader />
      <main class="py-8">
        <router-view />
      </main>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import AppHeader from './AppHeader.vue'
import AppFooter from './AppFooter.vue'
</script>
