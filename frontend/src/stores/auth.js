import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const user = ref({
    name: 'Scholar Admin',
    email: 'admin@scholar.com',
    role: 'Premium Access',
    avatar: 'account_circle'
  })

  const isPremium = ref(true)

  const preferences = ref({
    theme: 'light',
    fontSize: 'medium',
    autoPlayTTS: false,
    voiceSpeed: 1.0
  })

  const userName = computed(() => user.value?.name || 'Guest')
  const userRole = computed(() => user.value?.role || 'Free Access')

  function login(credentials) {
    isAuthenticated.value = true
    user.value = {
      name: 'Scholar Admin',
      email: credentials?.email || 'admin@scholar.com',
      role: 'Premium Access',
      avatar: 'account_circle'
    }
  }

  function logout() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    user.value = null
  }

  function updatePreferences(newPrefs) {
    preferences.value = { ...preferences.value, ...newPrefs }
  }

  return {
    user,
    isPremium,
    preferences,
    userName,
    userRole,
    login,
    logout,
    updatePreferences
  }
})

export const isAuthenticated = () => !!localStorage.getItem('accessToken')
