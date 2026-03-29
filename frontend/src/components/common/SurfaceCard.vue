<script setup>
defineProps({
  border: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <div
    class="bg-surface-container-lowest rounded-xl shadow-sm p-6"
    :class="{ 'border border-outline-variant/10': border }"
  >
    <slot />
  </div>
</template>
