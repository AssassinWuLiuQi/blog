<script setup lang="ts">
import SurfaceCard from '@/components/common/SurfaceCard.vue'

interface Props {
  id: number
  title: string
  summary: string
  category: string
  date: string
  readTime: string
}

defineProps<Props>()
</script>

<template>
  <SurfaceCard
    class="hover:shadow-md transition-shadow cursor-pointer"
  >
    <div class="flex flex-col h-full">
      <span class="text-xs text-blue-800 dark:text-blue-400 font-medium mb-2">
        {{ category }}
      </span>
      <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2 line-clamp-2">
        {{ title }}
      </h3>
      <p class="text-sm text-gray-600 dark:text-gray-400 mb-4 flex-1 line-clamp-3">
        {{ summary }}
      </p>
      <div class="flex items-center justify-between text-xs text-gray-600 dark:text-gray-400">
        <span>{{ date }}</span>
        <span>{{ readTime }}</span>
      </div>
    </div>
  </SurfaceCard>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
