import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import chatApi from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  const sessions = ref([])
  const currentSession = ref(null)
  const messages = ref([])
  const isLoading = ref(false)
  const isStreaming = ref(false)
  const useVoiceMode = ref(false)

  const sortedSessions = computed(() => {
    return [...sessions.value].sort((a, b) =>
      new Date(b.updatedAt) - new Date(a.updatedAt)
    )
  })

  async function fetchSessions() {
    const res = await chatApi.getSessions()
    sessions.value = res.data.data || []
  }

  async function fetchSession(id) {
    const res = await chatApi.getSession(id)
    currentSession.value = res.data.data
    messages.value = res.data.data.messages || []
  }

  async function createSession(name) {
    const res = await chatApi.createSession(name)
    const newSession = res.data.data
    sessions.value.unshift(newSession)
    return newSession
  }

  async function deleteSession(id) {
    await chatApi.deleteSession(id)
    sessions.value = sessions.value.filter(s => s.id !== id)
    if (currentSession.value?.id === id) {
      currentSession.value = null
      messages.value = []
    }
  }

  function addMessage(message) {
    messages.value.push(message)
  }

  function clearMessages() {
    messages.value = []
  }

  function setStreaming(value) {
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
