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

  // Theme State
  const theme: Ref<'light' | 'dark' | 'system'> = ref('system')
  const effectiveTheme: Ref<'light' | 'dark'> = ref('light')
  let systemThemeListenerAdded = false

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

  // Theme Actions
  function getSystemTheme(): 'light' | 'dark' {
    if (typeof window !== 'undefined' && window.matchMedia) {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    return 'light'
  }

  function getEffectiveTheme(): 'light' | 'dark' {
    if (theme.value === 'system') {
      return getSystemTheme()
    }
    return theme.value
  }

  function applyTheme(): void {
    effectiveTheme.value = getEffectiveTheme()
    if (typeof document !== 'undefined') {
      document.documentElement.setAttribute('data-theme', effectiveTheme.value)
    }
  }

  function initTheme(): void {
    const saved = localStorage.getItem('theme') as 'light' | 'dark' | 'system' | null
    if (saved && ['light', 'dark', 'system'].includes(saved)) {
      theme.value = saved
    }
    applyTheme()

    // Listen for system theme changes
    if (!systemThemeListenerAdded && typeof window !== 'undefined' && window.matchMedia) {
      systemThemeListenerAdded = true
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
        if (theme.value === 'system') {
          applyTheme()
        }
      })
    }
  }

  function setTheme(newTheme: 'light' | 'dark' | 'system'): void {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
    applyTheme()
  }

  function toggleTheme(): void {
    if (effectiveTheme.value === 'light') {
      setTheme('dark')
    } else {
      setTheme('light')
    }
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
    setTTSRate,
    theme,
    effectiveTheme,
    initTheme,
    setTheme,
    toggleTheme,
  }
})
