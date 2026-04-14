<!-- frontend/src/components/music/MusicPanel.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { generateLyrics, generateMusic } from '@/utils/musicApi'
import type { LyricsGenerationResponse, MusicGenerationResponse } from '@/types/music'

const emit = defineEmits<{
  'lyrics-result': [lyrics: string]
  'music-result': [audioUrl: string]
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
const musicModel = ref('music-2.6')
const isInstrumental = ref(false)
const musicLoading = ref(false)
const musicError = ref('')

const handleGenerateMusic = async () => {
  if (!musicPrompt.value.trim()) return
  musicLoading.value = true
  musicError.value = ''
  emit('loading', true)
  try {
    const res: MusicGenerationResponse = await generateMusic({
      model: musicModel.value,
      prompt: musicPrompt.value.trim(),
      lyrics: musicLyrics.value.trim() || undefined,
      isInstrumental: isInstrumental.value
    })
    if (res.statusCode === 0 && res.audioUrl) {
      emit('music-result', res.audioUrl)
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
</script>

<template>
  <div class="h-full flex flex-col border-r border-[#c2c6d4]/10 bg-white">
    <!-- Tab Header -->
    <div class="shrink-0 flex border-b border-[#c2c6d4]/10">
      <button
        v-for="tab in [{ key: 'lyrics', label: '歌词生成' }, { key: 'music', label: '音乐生成' }]"
        :key="tab.key"
        class="flex-1 py-3 text-sm font-medium transition-colors"
        :class="activeTab === tab.key
          ? 'text-[#003f87] border-b-2 border-[#003f87]'
          : 'text-[#424752] hover:text-[#191c1e]'"
        @click="activeTab = tab.key as 'lyrics' | 'music'"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Tab 1: Lyrics Generation -->
    <div v-if="activeTab === 'lyrics'" class="flex-1 overflow-auto p-6 flex flex-col gap-4">
      <div>
        <label class="block text-xs font-semibold text-[#424752] mb-2 tracking-wide uppercase">
          主题描述
        </label>
        <textarea
          v-model="lyricsPrompt"
          rows="4"
          placeholder="例如：一首欢乐的新年歌曲"
          class="w-full px-3 py-2 text-sm text-[#191c1e] bg-[#f7f9fb] border border-[#c2c6d4]/20 rounded-lg resize-none focus:outline-none focus:border-[#003f87]/50 transition-colors"
        />
      </div>

      <p v-if="lyricsError" class="text-xs text-red-500">{{ lyricsError }}</p>

      <button
        class="w-full py-2.5 rounded-xl text-sm font-semibold text-white transition-all"
        :class="lyricsLoading || !lyricsPrompt.trim()
          ? 'bg-[#003f87]/40 cursor-not-allowed'
          : 'bg-gradient-to-r from-[#003f87] to-[#0056b3] hover:opacity-90'"
        :disabled="lyricsLoading || !lyricsPrompt.trim()"
        @click="handleGenerateLyrics"
      >
        <span v-if="lyricsLoading" class="flex items-center justify-center gap-2">
          <span class="material-symbols-outlined text-base animate-spin">progress_activity</span>
          生成中...
        </span>
        <span v-else>生成歌词</span>
      </button>
    </div>

    <!-- Tab 2: Music Generation -->
    <div v-if="activeTab === 'music'" class="flex-1 overflow-auto p-6 flex flex-col gap-4">
      <div>
        <label class="block text-xs font-semibold text-[#424752] mb-2 tracking-wide uppercase">
          风格描述
        </label>
        <textarea
          v-model="musicPrompt"
          rows="3"
          placeholder="例如：Mandopop, Festive, Upbeat"
          class="w-full px-3 py-2 text-sm text-[#191c1e] bg-[#f7f9fb] border border-[#c2c6d4]/20 rounded-lg resize-none focus:outline-none focus:border-[#003f87]/50 transition-colors"
        />
      </div>

      <div>
        <label class="block text-xs font-semibold text-[#424752] mb-2 tracking-wide uppercase">
          歌词（可选，留空自动生成）
        </label>
        <textarea
          v-model="musicLyrics"
          rows="5"
          placeholder="[Verse]\n..."
          class="w-full px-3 py-2 text-sm text-[#191c1e] bg-[#f7f9fb] border border-[#c2c6d4]/20 rounded-lg resize-none focus:outline-none focus:border-[#003f87]/50 transition-colors"
        />
      </div>

      <div>
        <label class="block text-xs font-semibold text-[#424752] mb-2 tracking-wide uppercase">
          模型
        </label>
        <select
          v-model="musicModel"
          class="w-full px-3 py-2 text-sm text-[#191c1e] bg-[#f7f9fb] border border-[#c2c6d4]/20 rounded-lg focus:outline-none focus:border-[#003f87]/50"
        >
          <option value="music-2.6">music-2.6（推荐）</option>
          <option value="music-2.6-free">music-2.6-free（免费）</option>
        </select>
      </div>

      <label class="flex items-center gap-2 cursor-pointer select-none">
        <div
          class="w-10 h-5 rounded-full transition-colors relative"
          :class="isInstrumental ? 'bg-[#003f87]' : 'bg-[#c2c6d4]/40'"
          @click="isInstrumental = !isInstrumental"
        >
          <div
            class="absolute top-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform"
            :class="isInstrumental ? 'translate-x-5' : 'translate-x-0.5'"
          />
        </div>
        <span class="text-sm text-[#424752]">纯音乐（无人声）</span>
      </label>

      <p v-if="musicError" class="text-xs text-red-500">{{ musicError }}</p>

      <button
        class="w-full py-2.5 rounded-xl text-sm font-semibold text-white transition-all"
        :class="musicLoading || !musicPrompt.trim()
          ? 'bg-[#003f87]/40 cursor-not-allowed'
          : 'bg-gradient-to-r from-[#003f87] to-[#0056b3] hover:opacity-90'"
        :disabled="musicLoading || !musicPrompt.trim()"
        @click="handleGenerateMusic"
      >
        <span v-if="musicLoading" class="flex items-center justify-center gap-2">
          <span class="material-symbols-outlined text-base animate-spin">progress_activity</span>
          生成中...
        </span>
        <span v-else>开始生成</span>
      </button>
    </div>
  </div>
</template>
