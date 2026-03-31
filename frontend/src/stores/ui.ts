import { defineStore } from 'pinia'
import { ref, type Ref } from 'vue'
import type { VoiceResponse } from '@/types'

export interface Notification {
  message: string
  type: string
}

export const useUIStore = defineStore('ui', () => {
  // State
  const sidebarOpen: Ref<boolean> = ref(true)
  const currentSection: Ref<string> = ref('tech-preview')
  const currentCategory: Ref<string> = ref('all')
  const isLoading: Ref<boolean> = ref(false)
  const notification: Ref<Notification | null> = ref(null)

  // TTS State
  const ttsPlaying: Ref<boolean> = ref(false)
  const ttsRate: Ref<number> = ref(1.0)
  const ttsVoice: Ref<VoiceResponse | null> = ref(null)

  // Actions
  function toggleSidebar(): void {
    sidebarOpen.value = !sidebarOpen.value
  }

  function setSection(section: string): void {
    currentSection.value = section
  }

  function setCategory(category: string): void {
    currentCategory.value = category
  }

  function setLoading(loading: boolean): void {
    isLoading.value = loading
  }

  function showNotification(message: string, type: string = 'info'): void {
    notification.value = { message, type }
    setTimeout(() => {
      notification.value = null
    }, 3000)
  }

  function setTTSPlaying(playing: boolean): void {
    ttsPlaying.value = playing
  }

  function setTTSRate(rate: number): void {
    ttsRate.value = rate
  }

  return {
    sidebarOpen,
    currentSection,
    currentCategory,
    isLoading,
    notification,
    ttsPlaying,
    ttsRate,
    ttsVoice,
    toggleSidebar,
    setSection,
    setCategory,
    setLoading,
    showNotification,
    setTTSPlaying,
    setTTSRate
  }
})
