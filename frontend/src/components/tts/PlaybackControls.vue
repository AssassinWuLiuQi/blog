<script setup>
import { ref } from 'vue'

const isPlaying = ref(true)
const currentTime = ref('01:45')
const totalTime = ref('05:57')
const progress = ref(33)

const togglePlay = () => {
  isPlaying.value = !isPlaying.value
}
</script>

<template>
  <div class="bg-gradient-to-br from-blue-900 to-blue-950 p-8 rounded-xl shadow-xl shadow-blue-900/10 text-white flex flex-col items-center">
    <!-- EQ Icon with Pulse Effect -->
    <div class="w-16 h-16 bg-white/10 backdrop-blur-md rounded-full flex items-center justify-center mb-6 relative">
      <div class="absolute inset-0 border-2 border-white/20 rounded-full animate-pulse"></div>
      <span class="material-symbols-outlined text-4xl" style="font-variation-settings: 'FILL' 1;">graphic_eq</span>
    </div>

    <!-- Title -->
    <div class="text-center mb-8">
      <h4 class="text-lg font-semibold tracking-tight">正在朗读正文</h4>
      <p class="text-blue-200/60 text-xs mt-1">预计剩余时长: 04:12</p>
    </div>

    <!-- Progress Bar -->
    <div class="w-full mb-8">
      <div class="flex justify-between text-[10px] text-blue-200/50 mb-2 uppercase tracking-tighter">
        <span>{{ currentTime }}</span>
        <span>{{ totalTime }}</span>
      </div>
      <div class="h-1 w-full bg-white/10 rounded-full overflow-hidden">
        <div class="h-full bg-blue-400 rounded-full relative" :style="{ width: progress + '%' }">
          <div class="absolute right-0 top-1/2 -translate-y-1/2 w-3 h-3 bg-white rounded-full shadow-lg"></div>
        </div>
      </div>
    </div>

    <!-- Controls -->
    <div class="flex items-center gap-8">
      <!-- Replay 10 seconds -->
      <button class="text-blue-200/80 hover:text-white transition-colors">
        <span class="material-symbols-outlined text-3xl">replay_10</span>
      </button>

      <!-- Play/Pause Button -->
      <button
        @click="togglePlay"
        class="w-14 h-14 bg-white text-blue-900 rounded-full flex items-center justify-center shadow-lg hover:scale-105 active:scale-95 transition-transform"
      >
        <span class="material-symbols-outlined text-4xl">
          {{ isPlaying ? 'pause' : 'play_arrow' }}
        </span>
      </button>

      <!-- Forward 30 seconds -->
      <button class="text-blue-200/80 hover:text-white transition-colors">
        <span class="material-symbols-outlined text-3xl">forward_30</span>
      </button>
    </div>
  </div>
</template>
