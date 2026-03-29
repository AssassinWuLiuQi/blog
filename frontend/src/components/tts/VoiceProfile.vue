<script setup>
import { ref } from 'vue'

const selectedVoice = ref('学者男声 (高保真)')
const playbackSpeed = ref(1.2)
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
        <div class="flex items-center justify-between p-3 bg-surface-container rounded-lg border border-outline-variant/20">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-primary">
              <span class="material-symbols-outlined text-lg">face</span>
            </div>
            <span class="text-sm font-semibold">{{ selectedVoice }}</span>
          </div>
          <span class="material-symbols-outlined text-on-surface-variant text-sm">expand_more</span>
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
