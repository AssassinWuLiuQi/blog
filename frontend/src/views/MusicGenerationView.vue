<!-- frontend/src/views/MusicGenerationView.vue -->
<script setup lang="ts">
import { ref, watch } from 'vue'
import MusicPanel from '@/components/music/MusicPanel.vue'
import { fetchMusicHistory, deleteMusicHistory } from '@/utils/musicApi'
import type { MusicHistoryItem } from '@/types/music'

// Result state
type ResultMode = 'empty' | 'lyrics' | 'audio'
const resultMode = ref<ResultMode>('empty')
const currentLyrics = ref('')
const currentAudioUrl = ref('')
const currentTitle = ref('')
const currentGenre = ref('')
const currentDuration = ref('0:00')
const currentSampleRate = ref('44.1 kHz')
const isLoading = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const isPlaying = ref(false)

// History drawer
const historyDrawerVisible = ref(false)
const historyList = ref<MusicHistoryItem[]>([])
const currentPage = ref(0)
const pageSize = ref(10)
const hasMore = ref(true)
const loadingMore = ref(false)
const refreshing = ref(false)

// Audio element ref
const audioRef = ref<HTMLAudioElement | null>(null)

const handleLyricsResult = (lyrics: string) => {
  currentLyrics.value = lyrics
  resultMode.value = 'lyrics'
}

const handleMusicResult = (audioUrl: string, title?: string, genre?: string) => {
  currentAudioUrl.value = audioUrl
  currentTitle.value = title || 'Untitled'
  currentGenre.value = genre || ''
  resultMode.value = 'audio'
  if (audioRef.value) {
    audioRef.value.src = audioUrl
    audioRef.value.load()
  }
}

const copyLyrics = async () => {
  await navigator.clipboard.writeText(currentLyrics.value)
}

const downloadAudio = () => {
  const a = document.createElement('a')
  a.href = currentAudioUrl.value
  a.download = currentTitle.value + '.mp3' || 'generated-music.mp3'
  a.target = '_blank'
  a.click()
}

const loadHistory = async () => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    const items = await fetchMusicHistory(currentPage.value, pageSize.value)
    if (items.length < pageSize.value) hasMore.value = false
    historyList.value.push(...items)
    currentPage.value++
  } finally {
    loadingMore.value = false
  }
}

const handleRefresh = async () => {
  if (refreshing.value) return
  refreshing.value = true
  currentPage.value = 0
  hasMore.value = true
  historyList.value = []
  try {
    const items = await fetchMusicHistory(0, pageSize.value)
    if (items.length < pageSize.value) hasMore.value = false
    historyList.value = items
    currentPage.value = 1
  } finally {
    refreshing.value = false
  }
}

const handleDeleteHistory = async (id: number) => {
  await deleteMusicHistory(id)
  historyList.value = historyList.value.filter(item => item.id !== id)
}

const handleHistoryScroll = (event: Event) => {
  const el = event.target as HTMLElement
  const { scrollTop, scrollHeight, clientHeight } = el
  if (scrollHeight - scrollTop - clientHeight < 50 && !loadingMore.value && hasMore.value) {
    loadHistory()
  }
}

const playFromHistory = (item: MusicHistoryItem) => {
  if (item.audioUrl) {
    currentAudioUrl.value = item.audioUrl
    currentTitle.value = item.prompt.substring(0, 30) || 'Untitled'
    currentGenre.value = item.isInstrumental ? '纯音乐' : ''
    resultMode.value = 'audio'
    historyDrawerVisible.value = false
    if (audioRef.value) {
      audioRef.value.src = item.audioUrl
      audioRef.value.load()
    }
  }
}

const formatTime = (seconds: number): string => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const onTimeUpdate = () => {
  if (audioRef.value) {
    currentTime.value = audioRef.value.currentTime
  }
}

const onLoadedMetadata = () => {
  if (audioRef.value) {
    duration.value = audioRef.value.duration
    currentDuration.value = formatTime(audioRef.value.duration)
  }
}

const onEnded = () => {
  isPlaying.value = false
}

const togglePlay = () => {
  if (audioRef.value) {
    if (isPlaying.value) {
      audioRef.value.pause()
    } else {
      audioRef.value.play()
    }
    isPlaying.value = !isPlaying.value
  }
}

const seek = (event: MouseEvent) => {
  const el = event.currentTarget as HTMLElement
  const rect = el.getBoundingClientRect()
  const percent = (event.clientX - rect.left) / rect.width
  if (audioRef.value && duration.value > 0) {
    audioRef.value.currentTime = percent * duration.value
    currentTime.value = audioRef.value.currentTime
  }
}

watch(historyDrawerVisible, (visible) => {
  if (visible) {
    currentPage.value = 0
    hasMore.value = true
    historyList.value = []
    loadHistory()
  }
})
</script>

<template>
  <div class="flex flex-1 h-[calc(100vh-4rem)] bg-gray-50 dark:bg-gray-900">
      <!-- Left: MusicPanel (Creation Zone) -->
      <MusicPanel
        class="w-[880px] shrink-0"
        @lyrics-result="handleLyricsResult"
        @music-result="handleMusicResult"
        @loading="isLoading = $event"
      />

      <!-- Right: Preview & History -->
      <div class="flex-1 flex flex-col gap-6 p-8 overflow-auto">
        <!-- Current Result Player -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-[0_40px_52.5px_-15px_rgba(25,28,30,0.1)] p-6">
          <div class="flex items-center gap-2 mb-4">
            <svg class="w-4 h-4 text-blue-800 dark:text-blue-400" viewBox="0 0 10.5 11.67" fill="currentColor">
              <path d="M2.33333 9.33333l0-7 1.16667 0 0 7-1.16667 0 0 0m2.33334 2.33334l0-11.66667 1.16666 0 0 11.66667-1.16666 0 0 0m-4.66667-4.66667l0-2.33333 1.16667 0 0 2.33333-1.16667 0 0 0m7 2.33333l0-7 1.16667 0 0 7-1.16667 0 0 0m2.33333-2.33333l0-2.33333 1.16667 0 0 2.33333-1.16667 0 0 0"/>
            </svg>
            <span class="text-xs font-bold text-blue-800 dark:text-blue-400">正在预览 (Current Preview)</span>
          </div>

          <!-- Album Art / Play Area -->
          <div class="relative w-full aspect-video bg-gray-100 dark:bg-gray-700 rounded-xl overflow-hidden mb-4 flex items-center justify-center">
            <template v-if="resultMode === 'audio' && currentAudioUrl">
              <div class="w-full h-full bg-gradient-to-br from-blue-800/10 to-blue-600/10 dark:from-blue-500/10 dark:to-blue-600/10 flex items-center justify-center">
                <span class="material-symbols-outlined text-8xl text-blue-800/30 dark:text-blue-400/30">music_note</span>
              </div>
              <button
                class="absolute inset-0 flex items-center justify-center bg-black/10 opacity-0 hover:opacity-100 transition-opacity"
                @click="togglePlay"
              >
                <div class="w-16 h-16 bg-white/90 dark:bg-gray-800/90 rounded-xl shadow-lg flex items-center justify-center">
                  <span class="material-symbols-outlined text-4xl text-blue-800 dark:text-blue-400 ml-1">
                    {{ isPlaying ? 'pause' : 'play_arrow' }}
                  </span>
                </div>
              </button>
            </template>
            <template v-else>
              <div class="flex flex-col items-center gap-3 text-gray-600 dark:text-gray-400">
                <span class="material-symbols-outlined text-6xl">music_note</span>
                <span class="text-sm">暂无预览</span>
              </div>
            </template>
          </div>

          <!-- Track Info -->
          <div class="text-center mb-4">
            <h3 class="text-lg font-bold text-gray-900 dark:text-gray-100 truncate">
              {{ resultMode === 'audio' ? currentTitle : 'Manuscript_Nocturne_v2.mp3' }}
            </h3>
            <p class="text-xs text-gray-600 dark:text-gray-400">
              {{ currentGenre || 'Lofi Jazz' }} • {{ currentDuration || '3:42' }} • {{ currentSampleRate }}
            </p>
          </div>

          <!-- Progress Bar -->
          <div class="mb-2 cursor-pointer" @click="seek">
            <div class="w-full h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
              <div
                class="h-full bg-blue-800 dark:bg-blue-400 rounded-full transition-all"
                :style="{ width: duration > 0 ? `${(currentTime / duration) * 100}%` : '0%' }"
              />
            </div>
            <div class="flex justify-between mt-1">
              <span class="text-[10px] text-gray-600 dark:text-gray-400 font-mono">{{ formatTime(currentTime) }}</span>
              <span class="text-[10px] text-gray-600 dark:text-gray-400 font-mono">{{ currentDuration }}</span>
            </div>
          </div>

          <!-- Controls -->
          <div class="flex items-center justify-center gap-8">
            <button class="text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors">
              <span class="material-symbols-outlined text-xl">shuffle</span>
            </button>
            <button class="text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors">
              <span class="material-symbols-outlined text-xl">skip_previous</span>
            </button>
            <button
              class="w-12 h-12 rounded-full bg-blue-800 dark:bg-blue-500 text-white flex items-center justify-center hover:bg-blue-700 dark:hover:bg-blue-600 transition-colors"
              @click="togglePlay"
            >
              <span class="material-symbols-outlined text-2xl ml-0.5">
                {{ isPlaying ? 'pause' : 'play_arrow' }}
              </span>
            </button>
            <button class="text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors">
              <span class="material-symbols-outlined text-xl">skip_next</span>
            </button>
            <button class="text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors">
              <span class="material-symbols-outlined text-xl">repeat</span>
            </button>
          </div>

          <!-- Audio element (hidden) -->
          <audio
            ref="audioRef"
            class="hidden"
            @timeupdate="onTimeUpdate"
            @loadedmetadata="onLoadedMetadata"
            @ended="onEnded"
          />
        </div>

        <!-- History Records -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-[0_40px_52.5px_-15px_rgba(25,28,30,0.1)] p-6 flex-1 overflow-hidden flex flex-col">
          <div class="flex items-center justify-between mb-4">
            <h4 class="text-sm font-bold text-gray-900 dark:text-gray-100">历史记录 (History)</h4>
            <button
              class="text-xs text-blue-800 dark:text-blue-400 hover:underline"
              @click="historyDrawerVisible = true"
            >
              查看全部
            </button>
          </div>

          <div class="flex-1 overflow-auto">
            <div class="flex flex-col gap-3">
              <div
                v-for="item in historyList.slice(0, 4)"
                :key="item.id"
                class="flex items-center gap-3 p-3 bg-white/80 dark:bg-gray-800/80 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors cursor-pointer group"
                @click="playFromHistory(item)"
              >
                <!-- Thumbnail -->
                <div class="w-12 h-12 bg-gray-200 dark:bg-gray-700 rounded flex items-center justify-center shrink-0 relative overflow-hidden">
                  <span class="material-symbols-outlined text-gray-500 dark:text-gray-400">music_note</span>
                  <div class="absolute inset-0 bg-blue-800/10 dark:bg-blue-500/10 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity">
                    <span class="material-symbols-outlined text-white text-lg">play_arrow</span>
                  </div>
                </div>

                <!-- Info -->
                <div class="flex-1 min-w-0">
                  <p class="text-xs font-bold text-gray-900 dark:text-gray-100 truncate">{{ item.displayTitle }}</p>
                  <p class="text-[10px] text-gray-600 dark:text-gray-400 truncate">"{{ item.prompt.substring(0, 20) }}..."</p>
                  <span
                    v-if="item.isInstrumental"
                    class="inline-block mt-1 px-1.5 py-0.5 text-[8px] font-medium rounded bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-400"
                  >
                    纯音乐
                  </span>
                  <span
                    v-else
                    class="inline-block mt-1 px-1.5 py-0.5 text-[8px] font-medium rounded bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400"
                  >
                    {{ item.model }}
                  </span>
                </div>

                <!-- More icon -->
                <button class="text-gray-400 dark:text-gray-600 hover:text-gray-600 dark:hover:text-gray-400 transition-colors">
                  <span class="material-symbols-outlined text-lg">more_vert</span>
                </button>
              </div>

              <div v-if="historyList.length === 0 && !loadingMore" class="text-center py-8 text-gray-600 dark:text-gray-400 text-sm">
                暂无历史记录
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- History Drawer -->
      <el-drawer
        v-model="historyDrawerVisible"
        title=""
        direction="rtl"
        size="480px"
        :with-header="false"
        :z-index="50"
      >
        <div class="h-full flex flex-col overflow-hidden">
          <div class="shrink-0 flex items-center justify-between px-6 py-5 border-b border-gray-300/20 dark:border-gray-700">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-lg bg-blue-800/10 dark:bg-blue-500/10 flex items-center justify-center">
                <span class="material-symbols-outlined text-blue-800 dark:text-blue-400">history</span>
              </div>
              <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100">Generation History</h2>
            </div>
            <div class="flex items-center gap-1">
              <button
                class="w-8 h-8 rounded-full hover:bg-gray-300/10 dark:hover:bg-gray-700/10 flex items-center justify-center transition-colors disabled:opacity-50"
                :disabled="refreshing"
                @click="handleRefresh"
              >
                <span class="material-symbols-outlined text-lg text-gray-600 dark:text-gray-400" :class="{ 'animate-spin': refreshing }">refresh</span>
              </button>
              <button
                class="w-8 h-8 rounded-full hover:bg-gray-300/10 dark:hover:bg-gray-700/10 flex items-center justify-center transition-colors"
                @click="historyDrawerVisible = false"
              >
                <span class="material-symbols-outlined text-lg text-gray-600 dark:text-gray-400">close</span>
              </button>
            </div>
          </div>

          <div class="flex-1 overflow-auto p-6" @scroll="handleHistoryScroll">
            <div class="flex flex-col gap-4">
              <div
                v-for="item in historyList"
                :key="item.id"
                class="bg-white dark:bg-gray-800 rounded-lg border border-gray-300/20 dark:border-gray-700 p-4"
              >
                <div class="flex items-start justify-between gap-2 mb-2">
                  <span class="text-xs font-bold text-blue-800 dark:text-blue-400 tracking-wide truncate">{{ item.displayTitle }}</span>
                  <span class="text-xs text-gray-600 dark:text-gray-400 shrink-0">{{ item.displayDate }}</span>
                </div>
                <div class="flex items-center gap-2 text-xs text-gray-600 dark:text-gray-400 mb-3">
                  <span>{{ item.model }}</span>
                  <span v-if="item.isInstrumental" class="text-blue-800 dark:text-blue-400">· 纯音乐</span>
                </div>
                <div class="flex items-center gap-2">
                  <button
                    v-if="item.audioUrl"
                    class="px-3 py-1 text-xs font-semibold text-blue-800 dark:text-blue-400 hover:bg-blue-800/5 dark:hover:bg-blue-500/5 rounded transition-colors"
                    @click="playFromHistory(item)"
                  >
                    播放
                  </button>
                  <button
                    class="px-3 py-1 text-xs font-semibold text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded transition-colors"
                    @click="handleDeleteHistory(item.id)"
                  >
                    删除
                  </button>
                </div>
              </div>

              <div v-if="loadingMore" class="text-center py-4 text-gray-600 dark:text-gray-400 text-sm">加载中...</div>
              <div v-if="!hasMore && historyList.length > 0" class="text-center py-4 text-gray-600 dark:text-gray-400 text-sm">没有更多了</div>
              <div v-if="!loadingMore && historyList.length === 0" class="text-center py-8 text-gray-600 dark:text-gray-400 text-sm">暂无历史记录</div>
            </div>
          </div>
        </div>
      </el-drawer>
    </div>
</template>
