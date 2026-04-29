<template>
  <div class="flex items-center gap-2">
    <button
      @click="toggleRecording"
      :class="[
        'p-2 rounded-full transition-all',
        isRecording
          ? 'bg-red-500 text-white animate-pulse'
          : 'bg-gray-100 dark:bg-gray-700 hover:bg-red-500/10 text-gray-900 dark:text-gray-100'
      ]"
      :title="isRecording ? '停止录音' : '开始语音输入'"
    >
      <span class="material-symbols-outlined">
        {{ isRecording ? 'stop' : 'mic' }}
      </span>
    </button>

    <div v-if="isRecording" class="flex items-center gap-1">
      <span class="w-2 h-2 bg-red-500 rounded-full animate-bounce" />
      <span class="text-sm text-red-500">录音中...</span>
    </div>

    <div v-if="transcript" class="flex-1 text-sm text-gray-700 dark:text-gray-300">
      {{ transcript }}
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'

const emit = defineEmits(['transcript', 'error'])

const isRecording = ref(false)
const transcript = ref('')
let recognition = null

function initSpeechRecognition() {
  if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
    emit('error', '当前浏览器不支持语音识别')
    return null
  }

  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  const rec = new SpeechRecognition()

  rec.continuous = false
  rec.interimResults = true
  rec.lang = 'zh-CN'

  rec.onresult = (event) => {
    const results = event.results
    const finalTranscript = results[results.length - 1][0].transcript
    transcript.value = finalTranscript
  }

  rec.onerror = (event) => {
    emit('error', event.error)
    isRecording.value = false
  }

  rec.onend = () => {
    isRecording.value = false
    if (transcript.value) {
      emit('transcript', transcript.value)
    }
  }

  return rec
}

function toggleRecording() {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

function startRecording() {
  if (!recognition) {
    recognition = initSpeechRecognition()
  }

  if (recognition) {
    transcript.value = ''
    isRecording.value = true
    recognition.start()
  }
}

function stopRecording() {
  if (recognition) {
    recognition.stop()
    isRecording.value = false
  }
}

onUnmounted(() => {
  if (recognition) {
    recognition.abort()
  }
})
</script>
