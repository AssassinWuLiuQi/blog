<script setup lang="ts">
import { ref } from 'vue'
import VoiceProfile from './VoiceProfile.vue'
import PlaybackControls from './PlaybackControls.vue'
import TextAnalysis from './TextAnalysis.vue'
import type { TtsRequest } from '@/types/tts'

interface Props {
  text?: string
}

withDefaults(defineProps<Props>(), {
  text: ''
})

const selectedVoiceId = ref('')
const voiceSettings = ref<Partial<TtsRequest> | null>(null)

const handleVoiceChange = (voiceId: string): void => {
  selectedVoiceId.value = voiceId
}

const handleSettingsChange = (settings: Partial<TtsRequest>): void => {
  voiceSettings.value = settings
}

const buildTtsRequest = (): TtsRequest | null => {
  if (!voiceSettings.value || !selectedVoiceId.value) return null
  return {
    text: '',
    ...voiceSettings.value
  }
}
</script>

<template>
  <aside class="flex-1 min-w-[320px] flex flex-col gap-6">
    <!-- Playback Control Panel -->
    <PlaybackControls
      :text="text"
      :voice-id="selectedVoiceId"
      :tts-request="buildTtsRequest()"
    />

    <!-- Voice Profile Card -->
    <VoiceProfile
      @voice-change="handleVoiceChange"
      @settings-change="handleSettingsChange"
    />

    <!-- Text Analysis Card -->
    <TextAnalysis :text="text" />
  </aside>
</template>
