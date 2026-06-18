<template>
  <div
    class="relative flex-shrink-0 w-72 bg-white dark:bg-gray-800 rounded-lg shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-1 cursor-pointer overflow-hidden"
    @click="$emit('click', article)"
  >
    <!-- 推荐度徽章 -->
    <div v-if="article.recommendLevel === 5"
         class="absolute top-2 right-2 px-2 py-0.5 bg-primary text-white text-xs rounded shadow z-10">
      精华
    </div>
    <div v-else-if="article.recommendLevel === 4"
         class="absolute top-2 right-2 px-2 py-0.5 bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-gray-200 text-xs rounded shadow z-10">
      推荐
    </div>

    <div class="h-36 bg-gray-100 dark:bg-gray-700 overflow-hidden" v-if="article.coverImage && !coverError">
      <img :src="thumbnailUrl" :alt="article.title" class="w-full h-full object-cover"
           loading="lazy" decoding="async" @error="coverError = true" />
    </div>
    <div class="h-36 bg-gradient-to-br from-primary/10 to-primary/5 dark:from-primary/20 dark:to-primary/10 flex items-center justify-center" v-else>
      <span class="text-4xl text-primary/30">📄</span>
    </div>
    <div class="p-4">
      <h3 class="text-sm font-semibold text-gray-900 dark:text-white line-clamp-2 leading-snug">
        {{ article.title }}
      </h3>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getThumbnailUrl } from '@/utils/apiConfig'

const props = defineProps({
  article: { type: Object, required: true }
})
defineEmits(['click'])

const coverError = ref(false)
const thumbnailUrl = computed(() => getThumbnailUrl(props.article.coverImage, 400))
</script>