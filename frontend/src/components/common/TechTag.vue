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
        ? 'bg-primary text-white'
        : 'bg-primary/8 text-primary'
    ]"
  >
    {{ label }}
  </span>
</template>
