import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref({
    name: 'Scholar Admin',
    email: 'admin@scholar.com',
    role: 'Premium Access',
    avatar: 'account_circle'
  })

  const isAuthenticated = ref(false)
  const isPremium = ref(true)

  // Preferences
  const preferences = ref({
    theme: 'light',
    fontSize: 'medium',
    autoPlayTTS: false,
    voiceSpeed: 1.0
  })

  // Getters
  const userName = computed(() => user.value?.name || 'Guest')
  const userRole = computed(() => user.value?.role || 'Free Access')

  // Actions
  function login(credentials) {
    // Mock implementation
    isAuthenticated.value = true
    user.value = {
      name: 'Scholar Admin',
      email: credentials?.email || 'admin@scholar.com',
      role: 'Premium Access',
      avatar: 'account_circle'
    }
  }

  function logout() {
    isAuthenticated.value = false
    user.value = null
  }

  function updatePreferences(newPrefs) {
    preferences.value = { ...preferences.value, ...newPrefs }
  }

  return {
    user,
    isAuthenticated,
    isPremium,
    preferences,
    userName,
    userRole,
    login,
    logout,
    updatePreferences
  }
})