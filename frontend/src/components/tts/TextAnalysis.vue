<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  text?: string
}

const props = withDefaults(defineProps<Props>(), {
  text: ''
})

const readingLevel = ref('学术级')
const keyTerms = ref(0)
const syntaxComplexity = ref('高')

const analyzeText = (text: string): void => {
  if (!text) {
    readingLevel.value = '学术级'
    keyTerms.value = 0
    syntaxComplexity.value = '高'
    return
  }

  // 简单的字数统计
  const charCount = text.length
  const wordCount = text.split(/[\s,\.]+/).filter((w: string) => w.length > 0).length

  // 根据字数估算阅读难度
  if (charCount < 100) {
    readingLevel.value = '入门级'
  } else if (charCount < 500) {
    readingLevel.value = '基础级'
  } else if (charCount < 1000) {
    readingLevel.value = '进阶级'
  } else {
    readingLevel.value = '学术级'
  }

  // 估算关键术语数（简单估算）
  keyTerms.value = Math.floor(wordCount / 10)

  // 估算句法复杂度
  const sentences = text.split(/[。！？.!?]+/).filter((s: string) => s.trim().length > 0)
  const avgSentenceLength = sentences.length > 0 ? charCount / sentences.length : 0
  if (avgSentenceLength < 10) {
    syntaxComplexity.value = '低'
  } else if (avgSentenceLength < 20) {
    syntaxComplexity.value = '中'
  } else {
    syntaxComplexity.value = '高'
  }
}

watch(() => props.text, (newVal: string) => {
  analyzeText(newVal)
}, { immediate: true })
</script>

<template>
  <div class="bg-orange-100 dark:bg-orange-900/20 p-6 rounded-xl border-l-4 border-orange-800 dark:border-orange-400">
    <!-- Header -->
    <h3 class="text-sm font-bold text-orange-800 dark:text-orange-400 mb-2 flex items-center gap-2">
      <span class="material-symbols-outlined text-lg">info</span>
      文本特征分析
    </h3>

    <!-- Stats -->
    <div class="space-y-2">
      <div class="flex justify-between text-xs text-orange-700 dark:text-orange-300">
        <span>阅读难度</span>
        <span class="font-bold">{{ readingLevel }}</span>
      </div>
      <div class="flex justify-between text-xs text-orange-700 dark:text-orange-300">
        <span>关键术语</span>
        <span class="font-bold">{{ keyTerms }} 个</span>
      </div>
      <div class="flex justify-between text-xs text-orange-700 dark:text-orange-300">
        <span>句法复杂度</span>
        <span class="font-bold">{{ syntaxComplexity }}</span>
      </div>
    </div>
  </div>
</template>
