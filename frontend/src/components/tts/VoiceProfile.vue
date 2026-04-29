<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { fetchWithAuth } from '@/utils/api'
import type { VoiceResponse, TtsRequest } from '@/types/tts'

const emit = defineEmits<{
  'voiceChange': [voiceId: string]
  'settingsChange': [settings: Partial<TtsRequest>]
}>()

const voices = ref<VoiceResponse[]>([])
const selectedVoiceId = ref('male-qn-qingse')
const playbackSpeed = ref(1.0)
const selectedEmotion = ref('happy')
const isLoading = ref(true)
const error = ref('')
const showAdvanced = ref(false)

// 高级设置
const advancedSettings = ref({
  vol: 1,
  pitch: 0,
  channel: 1,
  forceCbr: false,
  textNormalization: false,
  latexRead: false,
  voicePitch: 0,
  voiceIntensity: 0,
  voiceTimbre: 0,
  soundEffects: '',
  subtitleEnable: false,
  aigcWatermark: false
})

const emotions = [
  { value: 'happy', label: '开心' },
  { value: 'sad', label: '悲伤' },
  { value: 'angry', label: '愤怒' },
  { value: 'fearful', label: '恐惧' },
  { value: 'disgusted', label: '厌恶' },
  { value: 'surprised', label: '惊讶' },
  { value: 'calm', label: '平静' },
  { value: 'fluent', label: '流畅' },
  { value: 'whisper', label: '低语' }
]

const soundEffects = [
  { value: '', label: '无' },
  { value: 'spacious_echo', label: '空旷回音' },
  { value: 'auditorium_echo', label: '礼堂广播' },
  { value: 'lofi_telephone', label: '电话失真' },
  { value: 'robotic', label: '电音' }
]

// 当前设置摘要（用于折叠面板显示）
const settingsSummary = computed(() => {
  const parts: string[] = []
  if (advancedSettings.value.channel === 2) parts.push('双声道')
  else parts.push('单声道')
  if (advancedSettings.value.forceCbr) parts.push('CBR')
  if (advancedSettings.value.textNormalization) parts.push('文本规范化')
  if (advancedSettings.value.latexRead) parts.push('Latex')
  if (advancedSettings.value.soundEffects) {
    const effect = soundEffects.find(e => e.value === advancedSettings.value.soundEffects)
    parts.push(effect?.label || '')
  }
  return parts.length > 0 ? parts.join(' | ') : ''
})

const allSettings = computed<Partial<TtsRequest>>(() => ({
  voiceId: selectedVoiceId.value,
  speed: playbackSpeed.value,
  emotion: selectedEmotion.value,
  ...advancedSettings.value
}))

onMounted(async () => {
  try {
    const response = await fetchWithAuth('/api/tts/voices')
    if (response.ok) {
      const data = await response.json()
      voices.value = data.data || []
      if (voices.value.length > 0) {
        // 默认选择第一个音色
        selectedVoiceId.value = voices.value[0].voiceId
        emit('voiceChange', selectedVoiceId.value)
        emit('settingsChange', allSettings.value)
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

watch(selectedVoiceId, (newVal: string) => {
  emit('voiceChange', newVal)
  emit('settingsChange', allSettings.value)
})

watch(playbackSpeed, () => emit('settingsChange', allSettings.value))
watch(selectedEmotion, () => emit('settingsChange', allSettings.value))
watch(advancedSettings, () => emit('settingsChange', allSettings.value), { deep: true })

const selectedVoice = (): string => {
  return voices.value.find(v => v.voiceId === selectedVoiceId.value)?.voiceName || '请选择音色'
}
</script>

<template>
  <div class="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-300/20 dark:border-gray-700">
    <!-- Header -->
    <h3 class="text-xs font-bold uppercase tracking-widest text-blue-800 dark:text-blue-400 mb-4 flex items-center gap-2">
      <span class="material-symbols-outlined text-sm">settings_voice</span>
      语音引擎配置
      <button
        v-if="settingsSummary"
        @click="showAdvanced = !showAdvanced"
        class="ml-auto text-xs font-normal uppercase tracking-normal text-gray-600 dark:text-gray-400 hover:text-blue-800 dark:hover:text-blue-400 flex items-center gap-1"
      >
        <span>{{ settingsSummary }}</span>
        <span class="material-symbols-outlined text-sm">{{ showAdvanced ? 'expand_less' : 'expand_more' }}</span>
      </button>
      <button
        v-else
        @click="showAdvanced = !showAdvanced"
        class="ml-auto material-symbols-outlined text-gray-600 dark:text-gray-400 hover:text-blue-800 dark:hover:text-blue-400"
      >
        {{ showAdvanced ? 'expand_less' : 'expand_more' }}
      </button>
    </h3>

    <!-- Content -->
    <div class="space-y-4">
      <!-- Voice Selection -->
      <div class="flex flex-col gap-1.5">
        <label class="text-xs text-gray-600 dark:text-gray-400 font-medium">当前音色</label>
        <div v-if="isLoading" class="p-3 bg-gray-100 dark:bg-gray-700 rounded-lg border border-gray-300/20 dark:border-gray-600/20 text-sm text-gray-600 dark:text-gray-400">
          加载中...
        </div>
        <div v-else-if="error" class="p-3 bg-gray-100 dark:bg-gray-700 rounded-lg border border-gray-300/20 dark:border-gray-600/20 text-sm text-red-600 dark:text-red-400">
          {{ error }}
        </div>
        <div v-else class="flex items-center justify-between p-3 bg-gray-100 dark:bg-gray-700 rounded-lg border border-gray-300/20 dark:border-gray-600/20">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center text-blue-800 dark:text-blue-400">
              <span class="material-symbols-outlined text-lg">face</span>
            </div>
            <span class="text-sm font-semibold text-gray-900 dark:text-gray-100">{{ selectedVoice() }}</span>
          </div>
          <select
            v-model="selectedVoiceId"
            class="bg-transparent border-none text-sm text-gray-600 dark:text-gray-400 cursor-pointer focus:outline-none"
          >
            <option v-for="voice in voices" :key="voice.voiceId" :value="voice.voiceId">
              {{ voice.voiceName }}
            </option>
          </select>
        </div>
      </div>

      <!-- Emotion Selection -->
      <div class="flex flex-col gap-1.5">
        <label class="text-xs text-gray-600 dark:text-gray-400 font-medium">情绪</label>
        <select
          v-model="selectedEmotion"
          class="p-3 bg-gray-100 dark:bg-gray-700 rounded-lg border border-gray-300/20 dark:border-gray-600/20 text-sm focus:outline-none focus:ring-2 focus:ring-blue-800/50 dark:focus:ring-blue-500/50 cursor-pointer"
        >
          <option v-for="emo in emotions" :key="emo.value" :value="emo.value">
            {{ emo.label }}
          </option>
        </select>
      </div>

      <!-- Speed Slider -->
      <div class="flex flex-col gap-1.5">
        <div class="flex justify-between items-center">
          <label class="text-xs text-gray-600 dark:text-gray-400 font-medium">播放语速</label>
          <span class="text-xs font-bold text-blue-800 dark:text-blue-400">{{ playbackSpeed.toFixed(1) }}x</span>
        </div>
        <input
          v-model="playbackSpeed"
          type="range"
          min="0.5"
          max="2.0"
          step="0.1"
          class="w-full h-1 bg-gray-300/30 dark:bg-gray-600/30 rounded-lg appearance-none cursor-pointer accent-blue-800 dark:accent-blue-400"
        />
      </div>

      <!-- Advanced Settings Toggle -->
      <button
        @click="showAdvanced = !showAdvanced"
        class="flex items-center justify-between w-full p-3 bg-gray-100 dark:bg-gray-700 rounded-lg border border-gray-300/20 dark:border-gray-600/20 text-sm text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
      >
        <span class="flex items-center gap-2">
          <span class="material-symbols-outlined text-base">tune</span>
          高级设置
        </span>
        <span class="material-symbols-outlined text-lg">{{ showAdvanced ? 'expand_less' : 'expand_more' }}</span>
      </button>

      <!-- Advanced Settings Panel -->
      <div v-if="showAdvanced" class="space-y-4 p-4 bg-gray-100 dark:bg-gray-700 rounded-lg border border-gray-300/20 dark:border-gray-600/20">
        <!-- Volume -->
        <div class="flex flex-col gap-1.5">
          <div class="flex justify-between items-center">
            <label class="text-xs text-gray-600 dark:text-gray-400 font-medium">音量</label>
            <span class="text-xs font-bold text-blue-800 dark:text-blue-400">{{ advancedSettings.vol }}</span>
          </div>
          <input
            v-model="advancedSettings.vol"
            type="range"
            min="1"
            max="10"
            step="0.5"
            class="w-full h-1 bg-gray-300/30 dark:bg-gray-600/30 rounded-lg appearance-none cursor-pointer accent-blue-800 dark:accent-blue-400"
          />
        </div>

        <!-- Pitch -->
        <div class="flex flex-col gap-1.5">
          <div class="flex justify-between items-center">
            <label class="text-xs text-gray-600 dark:text-gray-400 font-medium">语调</label>
            <span class="text-xs font-bold text-blue-800 dark:text-blue-400">{{ advancedSettings.pitch }}</span>
          </div>
          <input
            v-model="advancedSettings.pitch"
            type="range"
            min="-12"
            max="12"
            step="1"
            class="w-full h-1 bg-gray-300/30 dark:bg-gray-600/30 rounded-lg appearance-none cursor-pointer accent-blue-800 dark:accent-blue-400"
          />
        </div>

        <!-- Channel -->
        <div class="flex flex-col gap-1.5">
          <label class="text-xs text-gray-600 dark:text-gray-400 font-medium">声道</label>
          <div class="flex gap-4">
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                v-model="advancedSettings.channel"
                :value="1"
                class="accent-blue-800 dark:accent-blue-400"
              />
              <span class="text-sm text-gray-900 dark:text-gray-100">单声道</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                v-model="advancedSettings.channel"
                :value="2"
                class="accent-blue-800 dark:accent-blue-400"
              />
              <span class="text-sm text-gray-900 dark:text-gray-100">双声道</span>
            </label>
          </div>
        </div>

        <!-- Sound Effects -->
        <div class="flex flex-col gap-1.5">
          <label class="text-xs text-gray-600 dark:text-gray-400 font-medium">声音效果</label>
          <select
            v-model="advancedSettings.soundEffects"
            class="p-2 bg-white dark:bg-gray-800 rounded-lg border border-gray-300/20 dark:border-gray-600/20 text-sm focus:outline-none focus:ring-2 focus:ring-blue-800/50 dark:focus:ring-blue-500/50 cursor-pointer"
          >
            <option v-for="effect in soundEffects" :key="effect.value" :value="effect.value">
              {{ effect.label }}
            </option>
          </select>
        </div>

        <!-- Voice Effect Sliders -->
        <div class="pt-2 border-t border-gray-300/20 dark:border-gray-600/20">
          <p class="text-xs text-gray-600 dark:text-gray-400 font-medium mb-3">声音效果器</p>

          <!-- Voice Pitch -->
          <div class="flex flex-col gap-1.5 mb-3">
            <div class="flex justify-between items-center">
              <label class="text-xs text-gray-600 dark:text-gray-400">音高</label>
              <span class="text-xs font-bold text-blue-800 dark:text-blue-400">{{ advancedSettings.voicePitch }}</span>
            </div>
            <input
              v-model="advancedSettings.voicePitch"
              type="range"
              min="-100"
              max="100"
              step="5"
              class="w-full h-1 bg-gray-300/30 dark:bg-gray-600/30 rounded-lg appearance-none cursor-pointer accent-blue-800 dark:accent-blue-400"
            />
          </div>

          <!-- Voice Intensity -->
          <div class="flex flex-col gap-1.5 mb-3">
            <div class="flex justify-between items-center">
              <label class="text-xs text-gray-600 dark:text-gray-400">强度</label>
              <span class="text-xs font-bold text-blue-800 dark:text-blue-400">{{ advancedSettings.voiceIntensity }}</span>
            </div>
            <input
              v-model="advancedSettings.voiceIntensity"
              type="range"
              min="-100"
              max="100"
              step="5"
              class="w-full h-1 bg-gray-300/30 dark:bg-gray-600/30 rounded-lg appearance-none cursor-pointer accent-blue-800 dark:accent-blue-400"
            />
          </div>

          <!-- Voice Timbre -->
          <div class="flex flex-col gap-1.5">
            <div class="flex justify-between items-center">
              <label class="text-xs text-gray-600 dark:text-gray-400">音色</label>
              <span class="text-xs font-bold text-blue-800 dark:text-blue-400">{{ advancedSettings.voiceTimbre }}</span>
            </div>
            <input
              v-model="advancedSettings.voiceTimbre"
              type="range"
              min="-100"
              max="100"
              step="5"
              class="w-full h-1 bg-gray-300/30 dark:bg-gray-600/30 rounded-lg appearance-none cursor-pointer accent-blue-800 dark:accent-blue-400"
            />
          </div>
        </div>

        <!-- Toggles -->
        <div class="pt-2 border-t border-gray-300/20 dark:border-gray-600/20 space-y-2">
          <label class="flex items-center justify-between cursor-pointer">
            <span class="text-xs text-gray-600 dark:text-gray-400">恒定比特率（仅mp3）</span>
            <input
              type="checkbox"
              v-model="advancedSettings.forceCbr"
              class="accent-blue-800 dark:accent-blue-400"
            />
          </label>
          <label class="flex items-center justify-between cursor-pointer">
            <span class="text-xs text-gray-600 dark:text-gray-400">文本规范化</span>
            <input
              type="checkbox"
              v-model="advancedSettings.textNormalization"
              class="accent-blue-800 dark:accent-blue-400"
            />
          </label>
          <label class="flex items-center justify-between cursor-pointer">
            <span class="text-xs text-gray-600 dark:text-gray-400">Latex 公式朗读</span>
            <input
              type="checkbox"
              v-model="advancedSettings.latexRead"
              class="accent-blue-800 dark:accent-blue-400"
            />
          </label>
          <label class="flex items-center justify-between cursor-pointer">
            <span class="text-xs text-gray-600 dark:text-gray-400">开启字幕</span>
            <input
              type="checkbox"
              v-model="advancedSettings.subtitleEnable"
              class="accent-blue-800 dark:accent-blue-400"
            />
          </label>
          <label class="flex items-center justify-between cursor-pointer">
            <span class="text-xs text-gray-600 dark:text-gray-400">AIGC 水印</span>
            <input
              type="checkbox"
              v-model="advancedSettings.aigcWatermark"
              class="accent-blue-800 dark:accent-blue-400"
            />
          </label>
        </div>
      </div>
    </div>
  </div>
</template>
