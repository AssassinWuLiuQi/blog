<script setup>
import { ref } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import RichTextEditor from '@/components/editor/RichTextEditor.vue'
import TTSPanel from '@/components/tts/TTSPanel.vue'

const editorText = ref('')

const handleTextChange = (text) => {
  editorText.value = text
}

const handleExport = (content) => {
  console.log('Export article:', content)
}
</script>

<template>
  <AppLayout section-title="技术预览 - 文本相关">
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
