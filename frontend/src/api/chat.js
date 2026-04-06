import axios from 'axios'

const API_BASE = '/api/chat'

const chatApi = {
  getSessions() {
    return axios.get(`${API_BASE}/sessions`)
  },

  getSession(id) {
    return axios.get(`${API_BASE}/sessions/${id}`)
  },

  createSession(name) {
    return axios.post(`${API_BASE}/sessions`, null, { params: { name } })
  },

  deleteSession(id) {
    return axios.delete(`${API_BASE}/sessions/${id}`)
  },

  sendMessage(sessionId, content, useVoice = false) {
    return axios.post(`${API_BASE}/sessions/${sessionId}/messages`, {
      content,
      useVoice
    })
  },

  streamChat(sessionId, content, useVoice = false) {
    return `${API_BASE}/sessions/${sessionId}/stream`
  }
}

export default chatApi
