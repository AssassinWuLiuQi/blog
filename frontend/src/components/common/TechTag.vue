<script setup lang="ts">
interface Props {
  label: string
  active?: boolean
}

withDefaults(defineProps<Props>(), {
  active: false,
})
</script>

<template>
  <span
    :class="[
      'inline-flex items-center px-3 py-1 rounded-full text-sm font-medium transition-colors',
      active
        ? 'bg-blue-800 dark:bg-blue-500 text-white'
        : 'bg-blue-800/20 dark:bg-blue-500/20 text-blue-800 dark:text-blue-400'
    ]"
  >
    {{ label }}
  </span>
</template>
