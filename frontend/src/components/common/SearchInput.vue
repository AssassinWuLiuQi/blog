<script setup>
defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '搜索文献...'
  }
})

defineEmits(['update:modelValue'])
</script>

<template>
  <div class="relative group">
    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant pointer-events-none">
      search
    </span>
    <input
      type="text"
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      :placeholder="placeholder"
      class="w-full pl-10 pr-4 py-1.5 bg-surface-container-low border-none rounded-full focus:outline-none focus:ring-2 focus:ring-primary/20 transition-all text-sm"
    />
  </div>
</template>
