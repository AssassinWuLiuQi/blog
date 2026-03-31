import { defineStore } from 'pinia'
import { ref, computed, type Ref, type ComputedRef } from 'vue'
import type { User, UserPreferences } from '@/types'

const DEFAULT_ROLE = 'Free Access'
const PREMIUM_ROLE = 'Premium Access'

export const useAuthStore = defineStore('auth', () => {
  const user: Ref<User> = ref<User>({
    id: 0,
    name: '',
    email: '',
    role: DEFAULT_ROLE,
    avatar: 'account_circle'
  })

  const preferences: Ref<UserPreferences> = ref<UserPreferences>({
    theme: 'light',
    fontSize: 'medium',
    autoPlayTTS: false,
    voiceSpeed: 1.0
  })

  const isPremium: ComputedRef<boolean> = computed(() => user.value.role === PREMIUM_ROLE)

  const fetchCurrentUser = async (): Promise<void> => {
    const token = localStorage.getItem('accessToken')
    if (!token) return

    try {
      const response = await fetch('/api/auth/me', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        if (data.data) {
          user.value = {
            id: data.data.id || 0,
            name: data.data.username || 'User',
            email: data.data.email || '',
            role: data.data.role || DEFAULT_ROLE,
            avatar: 'account_circle'
          }
        }
      } else {
        logout()
      }
    } catch {
      // Network error, ignore
    }
  }

  const userName: ComputedRef<string> = computed(() => user.value?.name || 'Guest')
  const userRole: ComputedRef<string> = computed(() => user.value?.role || DEFAULT_ROLE)

  async function login(credentials: { username?: string; email?: string }): Promise<void> {
    const token = localStorage.getItem('accessToken')
    if (!token) {
      user.value = {
        id: 0,
        name: credentials?.username || 'User',
        email: credentials?.email || '',
        role: DEFAULT_ROLE,
        avatar: 'account_circle'
      }
    } else {
      // Immediately set username from credentials, then fetch actual user data
      user.value = {
        ...user.value,
        name: credentials?.username || user.value.name,
        email: credentials?.email || user.value.email
      }
      // Fetch actual user data in background
      fetchCurrentUser()
    }
  }

  function logout(): void {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    user.value = { id: 0, name: '', email: '', role: DEFAULT_ROLE, avatar: 'account_circle' }
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
    updatePreferences,
    fetchCurrentUser
  }
})

export const isAuthenticated = (): boolean => !!localStorage.getItem('accessToken')
