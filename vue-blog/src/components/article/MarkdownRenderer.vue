<template>
  <div class="markdown-body prose dark:prose-invert max-w-none" v-html="renderedContent"></div>
</template>

<script setup>
import { computed } from 'vue'
import md from '@/utils/markdown'

const props = defineProps({
  content: {
    type: String,
    required: true
  }
})

const renderedContent = computed(() => {
  return md.render(props.content)
})
</script>

<style scoped>
.markdown-body {
  @apply text-gray-800 dark:text-gray-200;
}
</style>
