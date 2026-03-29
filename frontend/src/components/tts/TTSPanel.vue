<script setup>
import VoiceProfile from './VoiceProfile.vue'
import PlaybackControls from './PlaybackControls.vue'
import TextAnalysis from './TextAnalysis.vue'
</script>

<template>
  <aside class="flex-1 min-w-[320px] flex flex-col gap-6">
    <!-- Voice Profile Card -->
    <VoiceProfile />

    <!-- Playback Control Panel -->
    <PlaybackControls />

    <!-- Text Analysis Card -->
    <TextAnalysis />
  </aside>
</template>
