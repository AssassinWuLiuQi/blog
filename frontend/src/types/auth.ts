export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  refreshToken: string
}

export interface User {
  id: number
  username: string
  email: string
  role: string
  avatar: string
}

export interface UserSettings {
  theme: 'light' | 'dark'
  fontSize: 'small' | 'medium' | 'large'
  autoPlayTTS: boolean
  voiceSpeed: number
}

export interface UserPreferences {
  theme: 'light' | 'dark'
  fontSize: 'small' | 'medium' | 'large'
  autoPlayTTS: boolean
  voiceSpeed: number
}
