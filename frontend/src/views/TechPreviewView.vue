<script setup lang="ts">
import { ref } from 'vue'
import type { Ref } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import RichTextEditor from '@/components/editor/RichTextEditor.vue'
import TTSPanel from '@/components/tts/TTSPanel.vue'

const editorText = ref<string>('')

const handleTextChange = (text: string): void => {
  editorText.value = text
}

const handleExport = (content: string): void => {
  console.log('Export article:', content)
}
</script>

<template>
  <AppLayout section-title="Editor-TTS - 文本相关">
    <div class="p-8 flex gap-8 flex-1 overflow-hidden h-[calc(100vh-8rem)]">
      <!-- Left Column: Rich Text Editor -->
      <div class="flex-[3] flex flex-col">
        <RichTextEditor @update:modelValue="handleTextChange" @export="handleExport" />
      </div>

      <!-- Right Column: TTS Reader -->
      <TTSPanel :text="editorText" />
    </div>
  </AppLayout>
</template>
