<template>
  <div
    :class="[
      'flex gap-3 p-4 rounded-lg',
      message.role === 'user' ? 'bg-primary/10 flex-row-reverse' : 'bg-surface'
    ]"
  >
    <div class="flex-shrink-0">
      <span
        v-if="message.role === 'user'"
        class="material-symbols-outlined text-primary"
      >
        person
      </span>
      <span
        v-else
        class="material-symbols-outlined text-accent"
      >
        smart_toy
      </span>
    </div>

    <div class="flex-1 min-w-0">
      <div class="flex items-center gap-2 mb-1">
        <span class="text-sm font-medium text-on-surface">
          {{ message.role === 'user' ? '你' : '小博' }}
        </span>
        <span class="text-xs text-on-surface/50">
          {{ formatTime(message.createdAt) }}
        </span>
      </div>

      <div
        :class="[
          'prose prose-sm max-w-none',
          message.role === 'user' ? 'text-right' : ''
        ]"
      >
        <p class="whitespace-pre-wrap break-words">{{ message.content }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  message: {
    type: Object,
    required: true
  }
})

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>
