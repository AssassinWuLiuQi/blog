<script setup lang="ts">
interface Props {
  border?: boolean
}

withDefaults(defineProps<Props>(), {
  border: false
})
</script>

<template>
  <div
    class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6"
    :class="{ 'border border-gray-300/20 dark:border-gray-700': border }"
  >
    <slot />
  </div>
</template>
