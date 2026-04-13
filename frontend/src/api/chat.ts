import { fetchWithAuth } from '../utils/api'

const API_BASE = '/api/chat'

export interface ChatSession {
  id: number
  sessionName: string
  messageCount: number
  updatedAt: string
  createdAt: string
  messages?: ChatMessage[]
}

export interface ChatMessage {
  messageId?: number
  id?: number
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt: string
  tokensUsed?: number
}

const chatApi = {
  async getSessions(): Promise<ChatSession[]> {
    const res = await fetchWithAuth(`${API_BASE}/sessions`)
    const data = await res.json()
    return data.data || []
  },

  async getSession(id: number): Promise<ChatSession> {
    const res = await fetchWithAuth(`${API_BASE}/sessions/${id}`)
    const data = await res.json()
    return data.data
  },

  async createSession(name?: string): Promise<ChatSession> {
    const res = await fetchWithAuth(`${API_BASE}/sessions`, {
      method: 'POST',
      body: JSON.stringify({ name })
    })
    const data = await res.json()
    return data.data
  },

  async deleteSession(id: number): Promise<void> {
    await fetchWithAuth(`${API_BASE}/sessions/${id}`, { method: 'DELETE' })
  },

  async sendMessage(sessionId: number, content: string, useVoice = false): Promise<ChatMessage> {
    const res = await fetchWithAuth(`${API_BASE}/sessions/${sessionId}/messages`, {
      method: 'POST',
      body: JSON.stringify({ content, useVoice })
    })
    const data = await res.json()
    return data.data
  },

  getStreamUrl(sessionId: number, content: string, useVoice = false): string {
    return `${API_BASE}/sessions/${sessionId}/stream`
  }
}

export default chatApi
