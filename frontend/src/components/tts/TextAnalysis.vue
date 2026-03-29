<script setup>
defineProps({
  readingLevel: {
    type: String,
    default: '学术级'
  },
  keyTerms: {
    type: Number,
    default: 12
  },
  syntaxComplexity: {
    type: String,
    default: '高'
  }
})
</script>

<template>
  <div class="bg-tertiary-fixed p-6 rounded-xl border-l-4 border-tertiary">
    <!-- Header -->
    <h3 class="text-sm font-bold text-tertiary mb-2 flex items-center gap-2">
      <span class="material-symbols-outlined text-lg">info</span>
      文本特征分析
    </h3>

    <!-- Stats -->
    <div class="space-y-2">
      <div class="flex justify-between text-xs text-on-tertiary-fixed-variant">
        <span>阅读难度</span>
        <span class="font-bold">{{ readingLevel }}</span>
      </div>
      <div class="flex justify-between text-xs text-on-tertiary-fixed-variant">
        <span>关键术语</span>
        <span class="font-bold">{{ keyTerms }} 个</span>
      </div>
      <div class="flex justify-between text-xs text-on-tertiary-fixed-variant">
        <span>句法复杂度</span>
        <span class="font-bold">{{ syntaxComplexity }}</span>
      </div>
    </div>
  </div>
</template>
