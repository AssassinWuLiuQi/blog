<!-- frontend/src/views/MusicGenerationView.vue -->
<script setup lang="ts">
import { ref, watch } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import MusicPanel from '@/components/music/MusicPanel.vue'
import { fetchMusicHistory, deleteMusicHistory } from '@/utils/musicApi'
import type { MusicHistoryItem } from '@/types/music'

// Result state
type ResultMode = 'empty' | 'lyrics' | 'audio'
const resultMode = ref<ResultMode>('empty')
const currentLyrics = ref('')
const currentAudioUrl = ref('')
const isLoading = ref(false)

// History drawer
const historyDrawerVisible = ref(false)
const historyList = ref<MusicHistoryItem[]>([])
const currentPage = ref(0)
const pageSize = ref(10)
const hasMore = ref(true)
const loadingMore = ref(false)
const refreshing = ref(false)

const handleLyricsResult = (lyrics: string) => {
  currentLyrics.value = lyrics
  resultMode.value = 'lyrics'
}

const handleMusicResult = (audioUrl: string) => {
  currentAudioUrl.value = audioUrl
  resultMode.value = 'audio'
}

const copyLyrics = async () => {
  await navigator.clipboard.writeText(currentLyrics.value)
}

const downloadAudio = () => {
  const a = document.createElement('a')
  a.href = currentAudioUrl.value
  a.download = 'generated-music.mp3'
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
    resultMode.value = 'audio'
    historyDrawerVisible.value = false
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
  <AppLayout section-title="音乐生成">
    <div class="flex flex-1 h-[calc(100vh-4rem)]">
      <!-- Left: MusicPanel (1/3) -->
      <MusicPanel
        class="w-1/3"
        @lyrics-result="handleLyricsResult"
        @music-result="handleMusicResult"
        @loading="isLoading = $event"
      />

      <!-- Right: Results (2/3) -->
      <div class="flex-1 w-2/3 bg-white p-8 flex flex-col">
        <!-- Header -->
        <div class="flex items-center justify-between mb-6 shrink-0">
          <div class="flex items-center gap-3">
            <div class="w-2 h-8 rounded-full bg-gradient-to-b from-[#003f87] to-[#0056b3]"></div>
            <h2 class="text-2xl font-semibold text-[#003f87]">生成结果</h2>
          </div>
          <button
            class="flex items-center gap-1 text-[#003f87] text-sm font-medium hover:opacity-80 transition-opacity"
            @click="historyDrawerVisible = true"
          >
            <span>View All History</span>
            <span class="material-symbols-outlined text-base">chevron_right</span>
          </button>
        </div>

        <!-- Result Area -->
        <div class="flex-1 overflow-auto relative">
          <!-- Loading overlay -->
          <div
            v-if="isLoading"
            class="absolute inset-0 flex items-center justify-center bg-white/70 z-10 rounded-2xl"
          >
            <div class="flex flex-col items-center gap-3">
              <span class="material-symbols-outlined text-5xl text-[#003f87] animate-spin">progress_activity</span>
              <p class="text-sm text-[#424752]">正在生成，请稍候...</p>
            </div>
          </div>

          <!-- Empty state -->
          <div
            v-if="resultMode === 'empty' && !isLoading"
            class="h-full flex flex-col items-center justify-center rounded-2xl bg-[#f2f4f6]/5 border-2 border-[#c2c6d4]/10"
          >
            <div class="relative mb-6">
              <div class="w-24 h-24 rounded-2xl bg-gradient-to-br from-[#003f87]/10 to-[#0056b3]/10 flex items-center justify-center">
                <span class="material-symbols-outlined text-6xl text-[#003f87]/20">music_note</span>
              </div>
              <div class="absolute -top-2 -left-2 w-4 h-4 border-l-2 border-t-2 border-[#003f87]/20 rounded-tl-lg"></div>
              <div class="absolute -bottom-2 -right-2 w-4 h-4 border-r-2 border-b-2 border-[#003f87]/20 rounded-br-lg"></div>
            </div>
            <h3 class="text-2xl font-medium text-[#191c1e] mb-3">No music yet</h3>
            <p class="text-base text-[#424752] max-w-md text-center mb-8">
              使用左侧面板生成歌词或音乐。
            </p>
            <div class="flex items-center gap-3">
              <div class="w-16 h-1 rounded-full bg-[#c2c6d4]/30"></div>
              <div class="w-8 h-1 rounded-full bg-[#003f87]/20"></div>
              <div class="w-16 h-1 rounded-full bg-[#c2c6d4]/30"></div>
            </div>
          </div>

          <!-- Lyrics result -->
          <div v-if="resultMode === 'lyrics'" class="h-full flex flex-col">
            <div class="flex items-center justify-between mb-4">
              <span class="text-sm font-semibold text-[#424752]">Generated Lyrics</span>
              <button
                class="flex items-center gap-1 text-xs font-semibold text-[#003f87] hover:bg-[#003f87]/5 px-3 py-1.5 rounded transition-colors"
                @click="copyLyrics"
              >
                <span class="material-symbols-outlined text-base">content_copy</span>
                复制
              </button>
            </div>
            <div class="flex-1 overflow-auto bg-[#f7f9fb] rounded-xl p-6">
              <pre class="text-sm text-[#191c1e] whitespace-pre-wrap leading-relaxed font-sans">{{ currentLyrics }}</pre>
            </div>
          </div>

          <!-- Audio result -->
          <div v-if="resultMode === 'audio'" class="h-full flex flex-col items-center justify-center gap-6">
            <div class="w-32 h-32 rounded-2xl bg-gradient-to-br from-[#003f87]/10 to-[#0056b3]/10 flex items-center justify-center">
              <span class="material-symbols-outlined text-6xl text-[#003f87]/60">music_note</span>
            </div>
            <p class="text-sm text-[#424752]">音乐生成完成</p>
            <audio
              :src="currentAudioUrl"
              controls
              class="w-full max-w-lg"
            />
            <button
              class="flex items-center gap-2 px-4 py-2 text-sm font-semibold text-[#003f87] border border-[#003f87]/20 rounded-lg hover:bg-[#003f87]/5 transition-colors"
              @click="downloadAudio"
            >
              <span class="material-symbols-outlined text-base">download</span>
              下载 MP3
            </button>
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
      >
        <div class="h-full flex flex-col overflow-hidden">
          <div class="shrink-0 flex items-center justify-between px-6 py-5 border-b border-[#c2c6d4]/10">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-lg bg-[#003f87]/10 flex items-center justify-center">
                <span class="material-symbols-outlined text-[#003f87]">history</span>
              </div>
              <h2 class="text-xl font-semibold text-[#191c1e]">Generation History</h2>
            </div>
            <div class="flex items-center gap-1">
              <button
                class="w-8 h-8 rounded-full hover:bg-[#c2c6d4]/10 flex items-center justify-center transition-colors disabled:opacity-50"
                :disabled="refreshing"
                @click="handleRefresh"
              >
                <span class="material-symbols-outlined text-lg text-[#424752]" :class="{ 'animate-spin': refreshing }">refresh</span>
              </button>
              <button
                class="w-8 h-8 rounded-full hover:bg-[#c2c6d4]/10 flex items-center justify-center transition-colors"
                @click="historyDrawerVisible = false"
              >
                <span class="material-symbols-outlined text-lg text-[#424752]">close</span>
              </button>
            </div>
          </div>

          <div class="flex-1 overflow-auto p-6" @scroll="handleHistoryScroll">
            <div class="flex flex-col gap-4">
              <div
                v-for="item in historyList"
                :key="item.id"
                class="bg-white rounded-lg border border-[#c2c6d4]/10 p-4"
              >
                <div class="flex items-start justify-between gap-2 mb-2">
                  <span class="text-xs font-bold text-[#003f87] tracking-wide truncate">{{ item.displayTitle }}</span>
                  <span class="text-xs text-[#424752] shrink-0">{{ item.displayDate }}</span>
                </div>
                <div class="flex items-center gap-2 text-xs text-[#424752] mb-3">
                  <span>{{ item.model }}</span>
                  <span v-if="item.isInstrumental" class="text-[#003f87]">· 纯音乐</span>
                </div>
                <div class="flex items-center gap-2">
                  <button
                    v-if="item.audioUrl"
                    class="px-3 py-1 text-xs font-semibold text-[#003f87] hover:bg-[#003f87]/5 rounded transition-colors"
                    @click="playFromHistory(item)"
                  >
                    播放
                  </button>
                  <button
                    class="px-3 py-1 text-xs font-semibold text-red-500 hover:bg-red-50 rounded transition-colors"
                    @click="handleDeleteHistory(item.id)"
                  >
                    删除
                  </button>
                </div>
              </div>

              <div v-if="loadingMore" class="text-center py-4 text-[#424752] text-sm">加载中...</div>
              <div v-if="!hasMore && historyList.length > 0" class="text-center py-4 text-[#424752] text-sm">没有更多了</div>
              <div v-if="!loadingMore && historyList.length === 0" class="text-center py-8 text-[#424752] text-sm">暂无历史记录</div>
            </div>
          </div>
        </div>
      </el-drawer>
    </div>
  </AppLayout>
</template>
