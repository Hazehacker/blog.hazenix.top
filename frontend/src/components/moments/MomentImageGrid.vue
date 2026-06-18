<!-- frontend/src/components/moments/MomentImageGrid.vue -->
<template>
  <div v-if="images.length > 0" :class="gridClass">
    <el-image
      v-for="(url, idx) in displayImages"
      :key="idx"
      :src="url"
      fit="cover"
      class="w-full h-full object-cover"
      :preview-src-list="images"
      :initial-index="idx"
      lazy
    >
      <template #error>
        <div class="w-full h-full flex items-center justify-center bg-gray-100 dark:bg-gray-700 text-gray-400 text-sm">
          加载失败
        </div>
      </template>
    </el-image>
    <!-- +N overlay for 5+ images -->
    <div
      v-if="images.length > 4"
      class="relative flex items-center justify-center bg-black/50 text-white text-2xl font-bold cursor-pointer"
    >
      +{{ images.length - 4 }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  }
})

const displayImages = computed(() => props.images.slice(0, 4))

const gridClass = computed(() => {
  const n = props.images.length
  const base = 'grid gap-0.5 w-full overflow-hidden rounded-t-2xl'
  if (n === 1) return `${base} grid-cols-1 h-48`
  if (n === 2) return `${base} grid-cols-2 h-48`
  if (n === 3) return `${base} grid-cols-3 h-44`
  return `${base} grid-cols-2 h-56`
})
</script>
