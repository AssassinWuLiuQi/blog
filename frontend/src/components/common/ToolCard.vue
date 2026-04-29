<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  title: string
  icon: string
  description: string
}>()

const isHovered = ref(false)
</script>

<template>
  <div
    class="relative h-48 bg-white dark:bg-gray-800 rounded-xl flex flex-col items-center justify-center gap-3 p-6 cursor-pointer"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <span class="material-symbols-outlined text-4xl text-gray-900 dark:text-gray-100">{{ icon }}</span>
    <span class="text-lg font-semibold text-gray-900 dark:text-gray-100">{{ title }}</span>
    <span class="text-sm text-gray-600 dark:text-gray-400 text-center line-clamp-2">{{ description }}</span>

    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0 translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-1"
    >
      <div
        v-if="isHovered"
        class="absolute bottom-full mb-2 left-1/2 -translate-x-1/2 bg-gray-900 dark:bg-gray-100 text-gray-100 dark:text-gray-900 px-4 py-3 rounded-lg shadow-lg w-64 z-50"
      >
        <span class="font-semibold block mb-1">{{ title }}</span>
        <span class="text-sm">{{ description }}</span>
      </div>
    </Transition>
  </div>
</template>
