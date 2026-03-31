import { defineStore } from 'pinia'
import { ref, computed, type Ref, type ComputedRef } from 'vue'
import type { User, UserPreferences } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user: Ref<User> = ref({
    id: 0,
    name: 'Scholar Admin',
    email: 'admin@scholar.com',
    role: 'Premium Access',
    avatar: 'account_circle'
  })

  const isPremium: Ref<boolean> = ref(true)

  const preferences: Ref<UserPreferences> = ref({
    theme: 'light',
    fontSize: 'medium',
    autoPlayTTS: false,
    voiceSpeed: 1.0
  })

  // Getters
  const userName: ComputedRef<string> = computed(() => user.value?.name || 'Guest')
  const userRole: ComputedRef<string> = computed(() => user.value?.role || 'Free Access')

  // Actions
  function login(credentials: { username?: string; email?: string }): void {
    isAuthenticated()
    user.value = {
      id: 0,
      name: 'Scholar Admin',
      email: credentials?.email || 'admin@scholar.com',
      role: 'Premium Access',
      avatar: 'account_circle'
    }
  }

  function logout(): void {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    user.value = {
      id: 0,
      name: '',
      email: '',
      role: 'Free Access',
      avatar: 'account_circle'
    }
  }

  function updatePreferences(newPrefs: Partial<UserPreferences>): void {
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

export const isAuthenticated = (): boolean => !!localStorage.getItem('accessToken')
