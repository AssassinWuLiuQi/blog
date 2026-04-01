<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useTTS } from '@/hooks/useTTS'
import type { TtsRequest } from '@/types/tts'

interface Props {
  text?: string
  voiceId?: string
  ttsRequest?: TtsRequest | null
}

const props = withDefaults(defineProps<Props>(), {
  text: '',
  voiceId: ''
})

const isPlaying = ref(false)
const progress = ref(0)

const { isPlaying: ttsPlaying, play, stop } = useTTS()

watch(ttsPlaying, (val: boolean) => {
  isPlaying.value = val
})

const togglePlay = async (): Promise<void> => {
  if (!props.text) {
    ElMessage.warning('请先输入要转换的文本')
    return
  }

  if (!props.voiceId) {
    ElMessage.warning('请先选择音色')
    return
  }

  if (ttsPlaying.value) {
    stop()
    isPlaying.value = false
  } else {
    // 使用完整参数
    const request: TtsRequest = {
      text: props.text,
      voiceId: props.voiceId,
      speed: props.ttsRequest?.speed ?? 1.0,
      emotion: props.ttsRequest?.emotion ?? 'happy',
      format: props.ttsRequest?.format ?? 'pcm',
      vol: props.ttsRequest?.vol ?? 1,
      pitch: props.ttsRequest?.pitch ?? 0,
      channel: props.ttsRequest?.channel ?? 1,
      forceCbr: props.ttsRequest?.forceCbr ?? false,
      textNormalization: props.ttsRequest?.textNormalization ?? false,
      latexRead: props.ttsRequest?.latexRead ?? false,
      voicePitch: props.ttsRequest?.voicePitch ?? 0,
      voiceIntensity: props.ttsRequest?.voiceIntensity ?? 0,
      voiceTimbre: props.ttsRequest?.voiceTimbre ?? 0,
      soundEffects: props.ttsRequest?.soundEffects ?? '',
      subtitleEnable: props.ttsRequest?.subtitleEnable ?? false,
      aigcWatermark: props.ttsRequest?.aigcWatermark ?? false
    }
    await play(request)
    isPlaying.value = true
  }
}

const replay10 = (): void => {
  // TODO: 实现后退10秒
}

const forward30 = (): void => {
  // TODO: 实现前进30秒
}
</script>

<template>
  <div class="bg-gradient-to-br from-blue-900 to-blue-950 p-8 rounded-xl shadow-xl shadow-blue-900/10 text-white flex flex-col items-center">
    <!-- EQ Icon with Pulse Effect -->
    <div class="w-16 h-16 bg-white/10 backdrop-blur-md rounded-full flex items-center justify-center mb-6 relative">
      <div class="absolute inset-0 border-2 border-white/20 rounded-full" :class="{ 'animate-pulse': isPlaying }"></div>
      <span class="material-symbols-outlined text-4xl" style="font-variation-settings: 'FILL' 1;">graphic_eq</span>
    </div>

    <!-- Title -->
    <div class="text-center mb-8">
      <h4 class="text-lg font-semibold tracking-tight">{{ isPlaying ? '正在朗读正文' : '点击播放' }}</h4>
      <p class="text-blue-200/60 text-xs mt-1">{{ text ? `${text.length} 字符` : '请输入文本' }}</p>
    </div>

    <!-- Progress Bar -->
    <div class="w-full mb-8">
      <div class="flex justify-between text-[10px] text-blue-200/50 mb-2 uppercase tracking-tighter">
        <span>00:00</span>
        <span>--:--</span>
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
      <button @click="replay10" class="text-blue-200/80 hover:text-white transition-colors">
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
      <button @click="forward30" class="text-blue-200/80 hover:text-white transition-colors">
        <span class="material-symbols-outlined text-3xl">forward_30</span>
      </button>
    </div>
  </div>
</template>
