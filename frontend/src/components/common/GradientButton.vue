<script setup lang="ts">
interface Props {
  label: string
  type?: 'button' | 'submit' | 'reset'
  disabled?: boolean
}

withDefaults(defineProps<Props>(), {
  type: 'button',
  disabled: false
})
</script>

<template>
  <button
    :type="type"
    :disabled="disabled"
    class="bg-gradient-to-br from-primary to-primary-container text-white rounded-md shadow-sm px-4 py-1.5 font-medium hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed"
  >
    {{ label }}
  </button>
</template>
