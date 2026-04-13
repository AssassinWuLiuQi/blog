import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import chatApi, { type ChatSession, type ChatMessage } from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<ChatSession[]>([])
  const currentSession = ref<ChatSession | null>(null)
  const messages = ref<ChatMessage[]>([])
  const isLoading = ref(false)
  const isStreaming = ref(false)
  const useVoiceMode = ref(false)

  const sortedSessions = computed(() => {
    return [...sessions.value].sort((a, b) =>
      new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    )
  })

  async function fetchSessions() {
    sessions.value = await chatApi.getSessions()
  }

  async function fetchSession(id: number) {
    const session = await chatApi.getSession(id)
    currentSession.value = session
    messages.value = session.messages || []
  }

  async function createSession(name?: string) {
    const newSession = await chatApi.createSession(name)
    sessions.value.unshift(newSession)
    return newSession
  }

  async function deleteSession(id: number) {
    await chatApi.deleteSession(id)
    sessions.value = sessions.value.filter(s => s.id !== id)
    if (currentSession.value?.id === id) {
      currentSession.value = null
      messages.value = []
    }
  }

  function addMessage(message: ChatMessage) {
    messages.value.push(message)
  }

  function clearMessages() {
    messages.value = []
  }

  function setStreaming(value: boolean) {
    isStreaming.value = value
  }

  function toggleVoiceMode() {
    useVoiceMode.value = !useVoiceMode.value
  }

  return {
    sessions,
    currentSession,
    messages,
    isLoading,
    isStreaming,
    useVoiceMode,
    sortedSessions,
    fetchSessions,
    fetchSession,
    createSession,
    deleteSession,
    addMessage,
    clearMessages,
    setStreaming,
    toggleVoiceMode
  }
})
