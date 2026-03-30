<script setup>
defineProps({
  label: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'button'
  },
  disabled: {
    type: Boolean,
    default: false
  }
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
