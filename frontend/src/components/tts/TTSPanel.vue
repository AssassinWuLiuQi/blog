<script setup lang="ts">
import { ref } from 'vue'
import VoiceProfile from './VoiceProfile.vue'
import PlaybackControls from './PlaybackControls.vue'
import TextAnalysis from './TextAnalysis.vue'

interface Props {
  text?: string
}

withDefaults(defineProps<Props>(), {
  text: ''
})

const selectedVoiceId = ref('')

const handleVoiceChange = (voiceId: string): void => {
  selectedVoiceId.value = voiceId
}
</script>

<template>
  <aside class="flex-1 min-w-[320px] flex flex-col gap-6">
    <!-- Playback Control Panel -->
    <PlaybackControls :text="props.text" :voice-id="selectedVoiceId" />

    <!-- Voice Profile Card -->
    <VoiceProfile @voice-change="handleVoiceChange" />

    <!-- Text Analysis Card -->
    <TextAnalysis :text="props.text" />
  </aside>
</template>
