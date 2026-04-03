<script setup lang="ts">
defineProps<{
  name: string
  size?: number | string
}>()
</script>

<template>
  <svg
    :width="size || 24"
    :height="size || 24"
    :class="['svg-icon', $attrs.class]"
  >
    <use :href="`#icon-${name}`" />
  </svg>
</template>

<style scoped>
.svg-icon {
  display: inline-block;
  vertical-align: middle;
  fill: currentColor;
}
</style>
