<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits<{
  insert: [tag: string]
}>()

// 停顿控制
const pauseDuration = ref(1.5)

const insertPause = (): void => {
  emit('insert', `<#${pauseDuration.value}#>`)
}

// 语气词标签
interface TagItem {
  tag: string
  label: string
}

const tagGroups: { title: string; tags: TagItem[] }[] = [
  {
    title: '情感',
    tags: [
      { tag: '(laughs)', label: '大笑' },
      { tag: '(chuckle)', label: '轻笑' },
      { tag: '(sighs)', label: '叹气' },
      { tag: '(groans)', label: '呻吟' },
      { tag: '(snorts)', label: '哼气' },
      { tag: '(crying)', label: '哭泣' },
    ]
  },
  {
    title: '呼吸',
    tags: [
      { tag: '(breath)', label: '呼吸' },
      { tag: '(pant)', label: '喘气' },
      { tag: '(inhale)', label: '吸气' },
      { tag: '(exhale)', label: '呼气' },
      { tag: '(gasps)', label: '倒吸一口凉气' },
    ]
  },
  {
    title: '人声',
    tags: [
      { tag: '(coughs)', label: '咳嗽' },
      { tag: '(clear-throat)', label: '清嗓子' },
      { tag: '(humming)', label: '哼唱' },
      { tag: '(hissing)', label: '发出嘶嘶声' },
      { tag: '(sneezes)', label: '打喷嚏' },
      { tag: '(emm)', label: '嗯(填充音)' },
      { tag: '(sniffs)', label: '吸鼻子' },
      { tag: '(whistles)', label: '吹口哨' },
    ]
  }
]
</script>

<template>
  <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-300/20 dark:border-gray-700 p-5">
    <!-- Header -->
    <div class="flex items-center gap-2 mb-4">
      <span class="material-symbols-outlined text-blue-800 dark:text-blue-400 text-xl">sound_sampler</span>
      <h4 class="text-sm font-semibold text-gray-800 dark:text-gray-200">语音标记</h4>
    </div>

    <!-- Pause Control -->
    <div class="mb-5">
      <div class="flex items-center justify-between mb-2">
        <span class="text-xs text-gray-600 dark:text-gray-400">插入停顿</span>
        <span class="text-xs font-mono text-blue-800 dark:text-blue-400 font-medium">{{ pauseDuration.toFixed(1) }}秒</span>
      </div>
      <input
        type="range"
        v-model.number="pauseDuration"
        min="0.5"
        max="5"
        step="0.5"
        class="w-full h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full appearance-none cursor-pointer accent-blue-800 dark:accent-blue-500"
      />
      <button
        @click="insertPause"
        class="mt-2 w-full bg-gradient-to-br from-blue-800 to-blue-700 dark:from-blue-500 dark:to-blue-700 text-white rounded-md shadow-sm px-4 py-1.5 text-xs font-medium hover:opacity-90 transition-opacity"
      >
        插入 &lt;#{{ pauseDuration.toFixed(1) }}#&gt;
      </button>
    </div>

    <!-- Interjection Tags -->
    <div v-for="group in tagGroups" :key="group.title" class="mb-4 last:mb-0">
      <h5 class="text-xs text-gray-500 dark:text-gray-400 mb-2 font-medium">{{ group.title }}</h5>
      <div class="grid grid-cols-4 gap-1.5">
        <button
          v-for="item in group.tags"
          :key="item.tag"
          @click="emit('insert', item.tag)"
          :title="item.label"
          class="px-2 py-1.5 text-xs font-mono rounded-md border border-gray-200 dark:border-gray-600 text-gray-700 dark:text-gray-300 bg-gray-50 dark:bg-gray-700/50 hover:bg-blue-50 dark:hover:bg-blue-900/30 hover:border-blue-300 dark:hover:border-blue-600 hover:text-blue-800 dark:hover:text-blue-400 transition-colors truncate"
        >
          {{ item.tag }}
        </button>
      </div>
    </div>
  </div>
</template>
