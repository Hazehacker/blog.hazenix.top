<template>
  <div
    class="flex-shrink-0 w-72 bg-white dark:bg-gray-800 rounded-lg shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-1 cursor-pointer overflow-hidden"
    @click="$emit('click', article)"
  >
    <div class="h-36 bg-gray-100 dark:bg-gray-700 overflow-hidden" v-if="article.coverImage">
      <img :src="article.coverImage" :alt="article.title" class="w-full h-full object-cover" />
    </div>
    <div class="h-36 bg-gradient-to-br from-primary/10 to-primary/5 dark:from-primary/20 dark:to-primary/10 flex items-center justify-center" v-else>
      <span class="text-4xl text-primary/30">📄</span>
    </div>
    <div class="p-4">
      <h3 class="text-sm font-semibold text-gray-900 dark:text-white line-clamp-2 leading-snug">
        {{ article.title }}
      </h3>
      <div class="mt-2 flex items-center gap-3 text-xs text-gray-400">
        <span>{{ article.viewCount || 0 }} 阅读</span>
        <span>{{ article.likeCount || 0 }} 点赞</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  article: { type: Object, required: true }
})
defineEmits(['click'])
</script>