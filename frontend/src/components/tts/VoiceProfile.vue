<script setup>
import { ref, onMounted, watch } from 'vue'
import { fetchWithAuth } from '@/utils/api'

const emit = defineEmits(['voiceChange'])

const voices = ref([])
const selectedVoiceId = ref('')
const playbackSpeed = ref(1.2)
const isLoading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const response = await fetchWithAuth('/api/tts/voices')
    if (response.ok) {
      const data = await response.json()
      voices.value = data.data || []
      if (voices.value.length > 0) {
        selectedVoiceId.value = voices.value[0].voiceId
        emit('voiceChange', selectedVoiceId.value)
      }
    } else {
      error.value = '获取音色列表失败'
    }
  } catch (e) {
    error.value = '网络错误'
  } finally {
    isLoading.value = false
  }
})

watch(selectedVoiceId, (newVal) => {
  emit('voiceChange', newVal)
})

const selectedVoice = () => {
  return voices.value.find(v => v.voiceId === selectedVoiceId.value)?.voiceName || '请选择音色'
}
</script>

<template>
  <div class="bg-surface-container-lowest p-6 rounded-xl shadow-sm border border-outline-variant/10">
    <!-- Header -->
    <h3 class="text-xs font-bold uppercase tracking-widest text-primary mb-4 flex items-center gap-2">
      <span class="material-symbols-outlined text-sm">settings_voice</span>
      语音引擎配置
    </h3>

    <!-- Content -->
    <div class="space-y-4">
      <!-- Voice Selection -->
      <div class="flex flex-col gap-1.5">
        <label class="text-xs text-on-surface-variant font-medium">当前音色</label>
        <div v-if="isLoading" class="p-3 bg-surface-container rounded-lg border border-outline-variant/20 text-sm text-on-surface-variant">
          加载中...
        </div>
        <div v-else-if="error" class="p-3 bg-surface-container rounded-lg border border-outline-variant/20 text-sm text-error">
          {{ error }}
        </div>
        <div v-else class="flex items-center justify-between p-3 bg-surface-container rounded-lg border border-outline-variant/20">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-primary">
              <span class="material-symbols-outlined text-lg">face</span>
            </div>
            <span class="text-sm font-semibold">{{ selectedVoice() }}</span>
          </div>
          <select
            v-model="selectedVoiceId"
            class="bg-transparent border-none text-sm text-on-surface-variant cursor-pointer focus:outline-none"
          >
            <option v-for="voice in voices" :key="voice.voiceId" :value="voice.voiceId">
              {{ voice.voiceName }}
            </option>
          </select>
        </div>
      </div>

      <!-- Speed Slider -->
      <div class="flex flex-col gap-1.5">
        <div class="flex justify-between items-center">
          <label class="text-xs text-on-surface-variant font-medium">播放语速</label>
          <span class="text-xs font-bold text-primary">{{ playbackSpeed.toFixed(1) }}x</span>
        </div>
        <input
          v-model="playbackSpeed"
          type="range"
          min="0.5"
          max="2.0"
          step="0.1"
          class="w-full h-1 bg-outline-variant/30 rounded-lg appearance-none cursor-pointer accent-primary"
        />
      </div>
    </div>
  </div>
</template>
