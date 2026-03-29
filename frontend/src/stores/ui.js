import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  // State
  const sidebarOpen = ref(true)
  const currentSection = ref('tech-preview')
  const currentCategory = ref('all')
  const isLoading = ref(false)
  const notification = ref(null)

  // TTS State
  const ttsPlaying = ref(false)
  const ttsRate = ref(1.0)
  const ttsVoice = ref(null)

  // Actions
  function toggleSidebar() {
    sidebarOpen.value = !sidebarOpen.value
  }

  function setSection(section) {
    currentSection.value = section
  }

  function setCategory(category) {
    currentCategory.value = category
  }

  function setLoading(loading) {
    isLoading.value = loading
  }

  function showNotification(message, type = 'info') {
    notification.value = { message, type }
    setTimeout(() => {
      notification.value = null
    }, 3000)
  }

  function setTTSPlaying(playing) {
    ttsPlaying.value = playing
  }

  function setTTSRate(rate) {
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