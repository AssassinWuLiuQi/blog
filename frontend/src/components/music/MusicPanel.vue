<!-- frontend/src/components/music/MusicPanel.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { generateLyrics, generateMusic } from '@/utils/musicApi'
import type { LyricsGenerationResponse, MusicGenerationResponse } from '@/types/music'

const emit = defineEmits<{
  'lyrics-result': [lyrics: string]
  'music-result': [audioUrl: string, title?: string, genre?: string]
  'loading': [loading: boolean]
}>()

const activeTab = ref<'lyrics' | 'music'>('lyrics')

// --- Tab 1: Lyrics ---
const lyricsPrompt = ref('')
const lyricsLoading = ref(false)
const lyricsError = ref('')

const handleGenerateLyrics = async () => {
  if (!lyricsPrompt.value.trim()) return
  lyricsLoading.value = true
  lyricsError.value = ''
  emit('loading', true)
  try {
    const res: LyricsGenerationResponse = await generateLyrics({
      prompt: lyricsPrompt.value.trim(),
      mode: 'write_full_song'
    })
    if (res.statusCode === 0 && res.lyrics) {
      emit('lyrics-result', res.lyrics)
    } else {
      lyricsError.value = res.statusMsg || '生成失败'
    }
  } catch (e: any) {
    lyricsError.value = e.message || '请求失败'
  } finally {
    lyricsLoading.value = false
    emit('loading', false)
  }
}

// --- Tab 2: Music ---
const musicPrompt = ref('')
const musicLyrics = ref('')
const musicModel = ref('Mx-Music-2.6')
const isInstrumental = ref(false)
const sampleRate = ref('44.1')
const bitrate = ref('320')
const musicLoading = ref(false)
const musicError = ref('')

const handleGenerateMusic = async () => {
  if (!musicPrompt.value.trim()) return
  musicLoading.value = true
  musicError.value = ''
  emit('loading', true)
  try {
    const modelKey = musicModel.value === 'Mx-Music-2.6' ? 'music-2.6' : 'music-2.6-free'
    const res: MusicGenerationResponse = await generateMusic({
      model: modelKey,
      prompt: musicPrompt.value.trim(),
      lyrics: musicLyrics.value.trim() || undefined,
      isInstrumental: isInstrumental.value
    })
    if (res.statusCode === 0 && res.audioUrl) {
      emit('music-result', res.audioUrl, musicPrompt.value.substring(0, 30), musicPrompt.value.substring(0, 20))
    } else {
      musicError.value = res.statusMsg || '生成失败'
    }
  } catch (e: any) {
    musicError.value = e.message || '请求失败'
  } finally {
    musicLoading.value = false
    emit('loading', false)
  }
}

const insertLyricsTag = (tag: string) => {
  const textarea = document.querySelector('#music-lyrics-textarea') as HTMLTextAreaElement
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const text = musicLyrics.value
    musicLyrics.value = text.substring(0, start) + `[${tag}]\n` + text.substring(end)
    setTimeout(() => {
      textarea.focus()
      textarea.setSelectionRange(start + tag.length + 3, start + tag.length + 3)
    }, 0)
  }
}
</script>

<template>
  <div class="h-full flex flex-col border-r border-gray-300/20 dark:border-gray-700 bg-white dark:bg-gray-800">
    <!-- Tab Header -->
    <div class="shrink-0 flex border-b border-gray-300/20 dark:border-gray-700">
      <button
        v-for="tab in [{ key: 'lyrics', label: '歌词生成' }, { key: 'music', label: '音乐生成' }]"
        :key="tab.key"
        class="flex-1 py-3 text-sm font-medium transition-colors"
        :class="activeTab === tab.key
          ? 'text-blue-800 dark:text-blue-400 border-b-2 border-blue-800 dark:border-blue-400'
          : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100'"
        @click="activeTab = tab.key as 'lyrics' | 'music'"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Tab 1: Lyrics Generation -->
    <div v-if="activeTab === 'lyrics'" class="flex-1 overflow-auto p-6 flex flex-col gap-4">
      <!-- Section Heading -->
      <div class="mb-2">
        <h2 class="text-xl font-semibold text-blue-800 dark:text-blue-400 mb-1">音频工坊 (Audio Workshop)</h2>
        <p class="text-sm text-gray-600 dark:text-gray-400">定义您的旋律，从歌词构思到风格编排。</p>
      </div>

      <div>
        <label class="block text-xs font-semibold text-gray-600 dark:text-gray-400 mb-2 tracking-wide uppercase">
          主题描述
        </label>
        <textarea
          v-model="lyricsPrompt"
          rows="4"
          placeholder="例如：一首欢乐的新年歌曲"
          class="w-full px-3 py-2 text-sm text-gray-900 dark:text-gray-100 bg-gray-50 dark:bg-gray-900 border border-gray-300/20 dark:border-gray-700 rounded-lg resize-none focus:outline-none focus:border-blue-800/50 dark:focus:border-blue-400/50 transition-colors"
        />
      </div>

      <p v-if="lyricsError" class="text-xs text-red-500">{{ lyricsError }}</p>

      <button
        class="w-full py-3 rounded-xl text-sm font-semibold text-white transition-all bg-gradient-to-r from-blue-800 to-blue-600 hover:opacity-90 disabled:opacity-40 disabled:cursor-not-allowed"
        :disabled="lyricsLoading || !lyricsPrompt.trim()"
        @click="handleGenerateLyrics"
      >
        <span v-if="lyricsLoading" class="flex items-center justify-center gap-2">
          <span class="material-symbols-outlined text-base animate-spin">progress_activity</span>
          生成中...
        </span>
        <span v-else class="flex items-center justify-center gap-2">
          <svg class="w-4 h-4" viewBox="0 0 18 20" fill="currentColor">
            <path d="M4 20l4-7.5-8-1 12-11.5 2 0-4 7.5 8 1-12 11.5-2 0 0 0"/>
          </svg>
          生成歌词
        </span>
      </button>
    </div>

    <!-- Tab 2: Music Generation -->
    <div v-if="activeTab === 'music'" class="flex-1 overflow-auto p-6 flex flex-col gap-4">
      <!-- Section Heading -->
      <div class="mb-2">
        <h2 class="text-xl font-semibold text-blue-800 dark:text-blue-400 mb-1">音频工坊 (Audio Workshop)</h2>
        <p class="text-sm text-gray-600 dark:text-gray-400">定义您的旋律，从歌词构思到风格编排。</p>
      </div>

      <!-- Lyrics Input Card -->
      <div class="bg-gray-50 dark:bg-gray-900 rounded-xl shadow-[0_40px_52.5px_-15px_rgba(25,28,30,0.1)] p-6">
        <div class="flex items-center gap-2 mb-3">
          <svg class="w-4 h-4 text-blue-800 dark:text-blue-400" viewBox="0 0 16.5 14.625" fill="currentColor">
            <path d="M8.25 14.625c-0.6-0.475-1.25-0.84375-1.95-1.10625-0.7-0.2625-1.425-0.39375-2.175-0.39375-0.525 0-1.04063 0.06875-1.54688 0.20625-0.50625 0.1375-0.99063 0.33125-1.45312 0.58125-0.2625 0.1375-0.51563 0.13125-0.75938-0.01875-0.24375-0.15-0.36562-0.36875-0.36562-0.65625l0-9.0375c0-0.1375 0.03437-0.26875 0.10313-0.39375 0.06875-0.125 0.17187-0.21875 0.30937-0.28125 0.575-0.3 1.175-0.525 1.8-0.675 0.625-0.15 1.2625-0.225 1.9125-0.225 0.725 0 1.43438 0.09375 2.12813 0.28125 0.69375 0.1875 1.35938 0.46875 1.99687 0.84375l0 9.075c0.6375-0.4 1.30625-0.7 2.00625-0.9 0.7-0.2 1.40625-0.3 2.11875-0.3 0.45 0 0.89063 0.0375 1.32188 0.1125 0.43125 0.075 0.86562 0.1875 1.30312 0.3375l0 0 0 0 0-9c0.1875 0.0625 0.37188 0.12813 0.55312 0.19687 0.18125 0.06875 0.35937 0.15313 0.53438 0.25313 0.1375 0.0625 0.24063 0.15625 0.30938 0.28125 0.06875 0.125 0.10312 0.25625 0.10312 0.39375l0 9.0375c0 0.2875-0.12188 0.50625-0.36563 0.65625-0.24375 0.15-0.49687 0.15625-0.75937 0.01875-0.4625-0.25-0.94688-0.44375-1.45313-0.58125-0.50625-0.1375-1.02187-0.20625-1.54687-0.20625-0.75 0-1.475 0.13125-2.175 0.39375-0.7 0.2625-1.35 0.63125-1.95 1.10625l0 0"/>
          </svg>
          <span class="text-sm font-bold text-gray-900 dark:text-gray-100">歌词内容 (Lyrics)</span>
        </div>

        <!-- Lyrics Tags -->
        <div class="flex gap-2 mb-3">
          <button
            v-for="tag in ['Intro', 'Chorus', 'Outro']"
            :key="tag"
            class="px-2 py-1 text-[10px] font-mono text-gray-600 dark:text-gray-400 bg-gray-200 dark:bg-gray-700 rounded hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
            @click="insertLyricsTag(tag)"
          >
            [{{ tag }}]
          </button>
        </div>

        <textarea
          id="music-lyrics-textarea"
          v-model="musicLyrics"
          rows="6"
          placeholder="[Intro]
夜色温柔，灯火阑珊...
[Chorus]
在代码的海洋里寻找诗意..."
          class="w-full px-4 py-3 text-sm text-gray-900 dark:text-gray-100 bg-gray-100 dark:bg-gray-800 rounded-lg resize-none focus:outline-none focus:ring-1 focus:ring-blue-800/50 dark:focus:ring-blue-400/50 transition-all font-mono"
        />
      </div>

      <!-- Prompt Input Card -->
      <div class="bg-gray-50 dark:bg-gray-900 rounded-xl shadow-[0_40px_52.5px_-15px_rgba(25,28,30,0.1)] p-6">
        <div class="flex items-center gap-2 mb-3">
          <svg class="w-4 h-4 text-blue-800 dark:text-blue-400" viewBox="0 0 14.26 15" fill="currentColor">
            <path d="M2.25 15l0-3.225c-0.7125-0.65-1.26563-1.40938-1.65938-2.27813-0.39375-0.86875-0.59062-1.78437-0.59062-2.74687 0-1.875 0.65625-3.46875 1.96875-4.78125 1.3125-1.3125 2.90625-1.96875 4.78125-1.96875 1.5625 0 2.94687 0.45937 4.15312 1.37813 1.20625 0.91875 1.99063 2.11563 2.35313 3.59062l0.975 3.84375c0.0625 0.2375 0.01875 0.45313-0.13125 0.64688-0.15 0.19375-0.35 0.29062-0.6 0.29062l-1.5 0 0 2.25c0 0.4125-0.14687 0.76563-0.44063 1.05937-0.29375 0.29375-0.64687 0.44063-1.05937 0.44063l-1.5 0 0 1.5-1.5 0 0-3 3 0 0 0 0 0 0-3.75 2.025 0-0.7125-2.90625c-0.2875-1.1375-0.9-2.0625-1.8375-2.775-0.9375-0.7125-2.0125-1.06875-3.225-1.06875-1.45 0-2.6875 0.50625-3.7125 1.51875-1.025 1.0125-1.5375 2.24375-1.5375 3.69375 0 0.75 0.15312 1.4625 0.45937 2.1375 0.30625 0.675 0.74063 1.275 1.30313 1.8l0.4875 0.45 0 3.9-1.5 0 0 0"/>
          </svg>
          <span class="text-sm font-bold text-gray-900 dark:text-gray-100">风格与描述 (Prompt)</span>
        </div>

        <textarea
          v-model="musicPrompt"
          rows="2"
          placeholder="例如：Lo-fi, 爵士钢琴, 治愈系, 适合雨夜阅读的氛围..."
          class="w-full px-3 py-2 text-sm text-gray-900 dark:text-gray-100 bg-gray-100 dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg resize-none focus:outline-none focus:border-blue-800/50 dark:focus:border-blue-400/50 transition-colors"
        />
      </div>

      <!-- Settings Grid -->
      <div class="grid grid-cols-2 gap-4">
        <!-- AI Model Selection -->
        <div class="bg-gray-50 dark:bg-gray-900 rounded-xl shadow-[0_40px_52.5px_-15px_rgba(25,28,30,0.1)] p-5">
          <label class="block text-xs font-bold text-gray-600 dark:text-gray-400 mb-2 tracking-wider uppercase">AI 模型选择</label>
          <div class="bg-gray-100 dark:bg-gray-800 rounded p-2 flex items-center gap-2">
            <svg class="w-5 h-5 text-gray-500" viewBox="0 0 21 21" fill="none" stroke="currentColor" stroke-width="1.57" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6.3 8.4L10.5 12.6L14.7 8.4"/>
              <circle cx="10.5" cy="14.85" r="4.2"/>
            </svg>
            <select
              v-model="musicModel"
              class="flex-1 bg-transparent text-sm text-gray-900 dark:text-gray-100 focus:outline-none cursor-pointer"
            >
              <option value="Mx-Music-2.6">Mx-Music-2.6 (高保真)</option>
              <option value="Mx-Music-2.6-free">Mx-Music-2.6-free (免费)</option>
            </select>
          </div>
        </div>

        <!-- Generation Mode -->
        <div class="bg-gray-50 dark:bg-gray-900 rounded-xl shadow-[0_40px_52.5px_-15px_rgba(25,28,30,0.1)] p-5">
          <label class="block text-xs font-bold text-gray-600 dark:text-gray-400 mb-2 tracking-wider uppercase">生成模式</label>
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-900 dark:text-gray-100">纯音乐模式<br><span class="text-gray-600 dark:text-gray-400 text-xs">(Instrumental)</span></span>
            <button
              class="w-11 h-6 rounded-full transition-colors relative"
              :class="isInstrumental ? 'bg-blue-800 dark:bg-blue-500' : 'bg-gray-300 dark:bg-gray-600'"
              @click="isInstrumental = !isInstrumental"
            >
              <div
                class="absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform"
                :class="isInstrumental ? 'translate-x-5' : 'translate-x-0.5'"
              />
            </button>
          </div>
        </div>

        <!-- Sample Rate -->
        <div class="bg-gray-50 dark:bg-gray-900 rounded-xl shadow-[0_40px_52.5px_-15px_rgba(25,28,30,0.1)] p-5">
          <label class="block text-xs font-bold text-gray-600 dark:text-gray-400 mb-2 tracking-wider uppercase">采样率 (SAMPLE RATE)</label>
          <div class="flex gap-2">
            <button
              class="flex-1 py-1.5 text-xs font-medium rounded-md border-2 transition-colors"
              :class="sampleRate === '44.1'
                ? 'border-blue-800 dark:border-blue-400 text-blue-800 dark:text-blue-400 bg-blue-800/5 dark:bg-blue-500/5'
                : 'border-transparent text-gray-600 dark:text-gray-400 bg-gray-200 dark:bg-gray-700'"
              @click="sampleRate = '44.1'"
            >
              44.1 kHz
            </button>
            <button
              class="flex-1 py-1.5 text-xs font-medium rounded-md border-2 transition-colors"
              :class="sampleRate === '48'
                ? 'border-blue-800 dark:border-blue-400 text-blue-800 dark:text-blue-400 bg-blue-800/5 dark:bg-blue-500/5'
                : 'border-transparent text-gray-600 dark:text-gray-400 bg-gray-200 dark:bg-gray-700'"
              @click="sampleRate = '48'"
            >
              48 kHz
            </button>
          </div>
        </div>

        <!-- Bitrate -->
        <div class="bg-gray-50 dark:bg-gray-900 rounded-xl shadow-[0_40px_52.5px_-15px_rgba(25,28,30,0.1)] p-5">
          <label class="block text-xs font-bold text-gray-600 dark:text-gray-400 mb-2 tracking-wider uppercase">比特率 (BITRATE)</label>
          <div class="flex gap-2">
            <button
              class="flex-1 py-1.5 text-xs font-medium rounded-md border-2 transition-colors"
              :class="bitrate === '192'
                ? 'border-blue-800 dark:border-blue-400 text-blue-800 dark:text-blue-400 bg-blue-800/5 dark:bg-blue-500/5'
                : 'border-transparent text-gray-600 dark:text-gray-400 bg-gray-200 dark:bg-gray-700'"
              @click="bitrate = '192'"
            >
              192kbps
            </button>
            <button
              class="flex-1 py-1.5 text-xs font-medium rounded-md border-2 transition-colors"
              :class="bitrate === '320'
                ? 'border-blue-800 dark:border-blue-400 text-blue-800 dark:text-blue-400 bg-blue-800/5 dark:bg-blue-500/5'
                : 'border-transparent text-gray-600 dark:text-gray-400 bg-gray-200 dark:bg-gray-700'"
              @click="bitrate = '320'"
            >
              320kbps
            </button>
          </div>
        </div>
      </div>

      <!-- Error Message -->
      <p v-if="musicError" class="text-xs text-red-500">{{ musicError }}</p>

      <!-- Generate Button -->
      <button
        class="w-full py-4 rounded-xl text-sm font-bold text-white transition-all bg-gradient-to-r from-blue-800 to-blue-600 hover:opacity-90 disabled:opacity-40 disabled:cursor-not-allowed shadow-[0_40px_52.5px_-15px_rgba(25,28,30,0.1)]"
        :disabled="musicLoading || !musicPrompt.trim()"
        @click="handleGenerateMusic"
      >
        <span v-if="musicLoading" class="flex items-center justify-center gap-2">
          <span class="material-symbols-outlined text-base animate-spin">progress_activity</span>
          生成中...
        </span>
        <span v-else class="flex items-center justify-center gap-2">
          <svg class="w-4 h-5" viewBox="0 0 18 20" fill="currentColor">
            <path d="M4 20l4-7.5-8-1 12-11.5 2 0-4 7.5 8 1-12 11.5-2 0 0 0"/>
          </svg>
          开始生成 (Generate Audio)
        </span>
      </button>
    </div>
  </div>
</template>
