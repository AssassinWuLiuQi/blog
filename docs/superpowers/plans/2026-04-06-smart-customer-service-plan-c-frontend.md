# Plan C: Vue Frontend

## Overview

Add chat UI components to the Vue frontend: chat window, message display, voice input, and streaming response support. Integrates with existing Spring Boot backend and reuses the TTS service for voice output.

**Files to create/modify:**

```
frontend/src/
├── components/chat/
│   ├── ChatWindow.vue        (create)
│   ├── ChatMessage.vue       (create)
│   └── VoiceInput.vue        (create)
├── views/
│   └── ChatView.vue          (create)
├── stores/
│   └── chat.js               (create)
├── api/
│   └── chat.js               (create)
└── router/
│   └── index.js              (modify)
```

---

## Task 1: Chat API Module

**File:** `frontend/src/api/chat.js`

**Purpose:** API calls to Spring Boot chat endpoints.

- [ ] **Step 1: Create chat.js**

```javascript
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
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/api/chat.js
git commit -m "feat(chat): add chat API module"
```

---

## Task 2: Chat Store (Pinia)

**File:** `frontend/src/stores/chat.js`

**Purpose:** State management for chat sessions and messages.

- [ ] **Step 1: Create chat.js**

```javascript
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
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/stores/chat.js
git commit -m "feat(chat): add Pinia chat store"
```

---

## Task 3: ChatMessage Component

**File:** `frontend/src/components/chat/ChatMessage.vue`

**Purpose:** Display a single chat message (user or assistant).

- [ ] **Step 1: Create ChatMessage.vue**

```vue
<template>
  <div
    :class="[
      'flex gap-3 p-4 rounded-lg',
      message.role === 'user' ? 'bg-primary/10 flex-row-reverse' : 'bg-surface'
    ]"
  >
    <div class="flex-shrink-0">
      <span
        v-if="message.role === 'user'"
        class="material-symbols-outlined text-primary"
      >
        person
      </span>
      <span
        v-else
        class="material-symbols-outlined text-accent"
      >
        smart_toy
      </span>
    </div>

    <div class="flex-1 min-w-0">
      <div class="flex items-center gap-2 mb-1">
        <span class="text-sm font-medium text-on-surface">
          {{ message.role === 'user' ? '你' : '小博' }}
        </span>
        <span class="text-xs text-on-surface/50">
          {{ formatTime(message.createdAt) }}
        </span>
      </div>

      <div
        :class="[
          'prose prose-sm max-w-none',
          message.role === 'user' ? 'text-right' : ''
        ]"
      >
        <p class="whitespace-pre-wrap break-words">{{ message.content }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  message: {
    type: Object,
    required: true
  }
})

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/chat/ChatMessage.vue
git commit -m "feat(chat): add ChatMessage component"
```

---

## Task 4: VoiceInput Component

**File:** `frontend/src/components/chat/VoiceInput.vue`

**Purpose:** Voice input using Web Speech API with recording indicator.

- [ ] **Step 1: Create VoiceInput.vue**

```vue
<template>
  <div class="flex items-center gap-2">
    <button
      @click="toggleRecording"
      :class="[
        'p-2 rounded-full transition-all',
        isRecording
          ? 'bg-red-500 text-white animate-pulse'
          : 'bg-surface hover:bg-primary/10 text-on-surface'
      ]"
      :title="isRecording ? '停止录音' : '开始语音输入'"
    >
      <span class="material-symbols-outlined">
        {{ isRecording ? 'stop' : 'mic' }}
      </span>
    </button>

    <div v-if="isRecording" class="flex items-center gap-1">
      <span class="w-2 h-2 bg-red-500 rounded-full animate-bounce" />
      <span class="text-sm text-red-500">录音中...</span>
    </div>

    <div v-if="transcript" class="flex-1 text-sm text-on-surface/70">
      {{ transcript }}
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'

const emit = defineEmits(['transcript', 'error'])

const isRecording = ref(false)
const transcript = ref('')
let recognition = null

function initSpeechRecognition() {
  if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
    emit('error', '当前浏览器不支持语音识别')
    return null
  }

  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  const rec = new SpeechRecognition()

  rec.continuous = false
  rec.interimResults = true
  rec.lang = 'zh-CN'

  rec.onresult = (event) => {
    const results = event.results
    const finalTranscript = results[results.length - 1][0].transcript
    transcript.value = finalTranscript
  }

  rec.onerror = (event) => {
    emit('error', event.error)
    isRecording.value = false
  }

  rec.onend = () => {
    isRecording.value = false
    if (transcript.value) {
      emit('transcript', transcript.value)
    }
  }

  return rec
}

function toggleRecording() {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

function startRecording() {
  if (!recognition) {
    recognition = initSpeechRecognition()
  }

  if (recognition) {
    transcript.value = ''
    isRecording.value = true
    recognition.start()
  }
}

function stopRecording() {
  if (recognition) {
    recognition.stop()
    isRecording.value = false
  }
}

onUnmounted(() => {
  if (recognition) {
    recognition.abort()
  }
})
</script>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/chat/VoiceInput.vue
git commit -m "feat(chat): add VoiceInput component with Web Speech API"
```

---

## Task 5: ChatWindow Component

**File:** `frontend/src/components/chat/ChatWindow.vue`

**Purpose:** Main chat interface with message list, input, and streaming support.

- [ ] **Step 1: Create ChatWindow.vue**

```vue
<template>
  <div class="flex flex-col h-full bg-white rounded-xl shadow-lg overflow-hidden">
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3 border-b border-surface">
      <div class="flex items-center gap-3">
        <span class="material-symbols-outlined text-primary text-2xl">smart_toy</span>
        <div>
          <h3 class="font-medium text-on-surface">小博</h3>
          <p class="text-xs text-on-surface/50">智能客服助手</p>
        </div>
      </div>

      <button
        @click="chatStore.toggleVoiceMode()"
        :class="[
          'p-2 rounded-lg transition-colors',
          chatStore.useVoiceMode ? 'bg-primary text-white' : 'hover:bg-surface'
        ]"
        title="语音模式"
      >
        <span class="material-symbols-outlined">
          {{ chatStore.useVoiceMode ? 'volume_up' : 'volume_off' }}
        </span>
      </button>
    </div>

    <!-- Messages -->
    <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 space-y-4">
      <div v-if="chatStore.messages.length === 0" class="flex flex-col items-center justify-center h-full text-center">
        <span class="material-symbols-outlined text-4xl text-on-surface/20 mb-2">chat</span>
        <p class="text-on-surface/50">发送消息开始对话</p>
      </div>

      <ChatMessage
        v-for="msg in chatStore.messages"
        :key="msg.messageId"
        :message="msg"
      />

      <div v-if="chatStore.isStreaming" class="flex gap-3 p-4 bg-surface rounded-lg">
        <span class="material-symbols-outlined text-accent animate-pulse">smart_toy</span>
        <div class="flex-1">
          <div class="flex items-center gap-2 mb-1">
            <span class="text-sm font-medium text-on-surface">小博</span>
            <span class="text-xs text-on-surface/50">输入中...</span>
          </div>
          <span class="text-on-surface/70">{{ streamingContent }}</span>
          <span class="inline-block w-2 h-4 bg-primary animate-pulse ml-1" />
        </div>
      </div>
    </div>

    <!-- Input Area -->
    <div class="border-t border-surface p-4 space-y-3">
      <VoiceInput
        v-if="chatStore.useVoiceMode"
        @transcript="handleVoiceInput"
        @error="handleVoiceError"
      />

      <div class="flex gap-2">
        <input
          v-model="inputText"
          @keyup.enter="sendMessage"
          type="text"
          placeholder="输入消息..."
          class="flex-1 px-4 py-2 bg-surface rounded-lg border-0 focus:ring-2 focus:ring-primary outline-none"
          :disabled="chatStore.isStreaming"
        />

        <button
          @click="sendMessage"
          :disabled="!inputText.trim() || chatStore.isStreaming"
          :class="[
            'px-4 py-2 rounded-lg font-medium transition-colors',
            inputText.trim() && !chatStore.isStreaming
              ? 'bg-primary text-white hover:bg-primary/90'
              : 'bg-surface text-on-surface/50 cursor-not-allowed'
          ]"
        >
          <span class="material-symbols-outlined">
            {{ chatStore.isStreaming ? 'hourglass_empty' : 'send' }}
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import ChatMessage from './ChatMessage.vue'
import VoiceInput from './VoiceInput.vue'

const props = defineProps({
  sessionId: {
    type: Number,
    required: true
  }
})

const chatStore = useChatStore()
const inputText = ref('')
const streamingContent = ref('')

watch(() => props.sessionId, async (newId) => {
  if (newId) {
    await chatStore.fetchSession(newId)
    scrollToBottom()
  }
}, { immediate: true })

watch(() => chatStore.messages, () => {
  nextTick(() => scrollToBottom())
}, { deep: true })

function scrollToBottom() {
  const container = document.querySelector('.overflow-y-auto')
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || chatStore.isStreaming) return

  inputText.value = ''

  chatStore.addMessage({
    messageId: Date.now(),
    role: 'user',
    content: text,
    createdAt: new Date().toISOString()
  })

  chatStore.setStreaming(true)
  streamingContent.value = ''

  try {
    await streamResponse(props.sessionId, text)
  } catch (error) {
    console.error('Chat error:', error)
    chatStore.addMessage({
      messageId: Date.now(),
      role: 'assistant',
      content: '抱歉，发生了错误。请稍后再试。',
      createdAt: new Date().toISOString()
    })
  } finally {
    chatStore.setStreaming(false)
    streamingContent.value = ''
  }
}

async function streamResponse(sessionId, text) {
  const eventSource = new EventSource(`/api/chat/sessions/${sessionId}/stream`)

  eventSource.addEventListener('message', (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.content) {
        streamingContent.value += data.content
      }
    } catch (e) {
      console.error('Parse error:', e)
    }
  })

  eventSource.addEventListener('done', () => {
    if (streamingContent.value) {
      chatStore.addMessage({
        messageId: Date.now(),
        role: 'assistant',
        content: streamingContent.value,
        createdAt: new Date().toISOString()
      })
    }
    eventSource.close()
  })

  await fetch(`/api/chat/sessions/${sessionId}/messages`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content: text, useVoice: chatStore.useVoiceMode })
  })
}

function handleVoiceInput(text) {
  inputText.value = text
  sendMessage()
}

function handleVoiceError(error) {
  console.error('Voice error:', error)
}
</script>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/chat/ChatWindow.vue
git commit -m "feat(chat): add ChatWindow component with streaming support"
```

---

## Task 6: ChatView Page

**File:** `frontend/src/views/ChatView.vue`

**Purpose:** Full chat page with session list sidebar and chat window.

- [ ] **Step 1: Create ChatView.vue**

```vue
<template>
  <div class="h-[calc(100vh-64px)] flex">
    <!-- Sidebar -->
    <div class="w-72 border-r border-surface flex flex-col bg-white">
      <div class="p-4 border-b border-surface">
        <button
          @click="createNewSession"
          class="w-full flex items-center justify-center gap-2 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors"
        >
          <span class="material-symbols-outlined">add</span>
          新建对话
        </button>
      </div>

      <div class="flex-1 overflow-y-auto">
        <div class="p-2 space-y-1">
          <div
            v-for="session in chatStore.sortedSessions"
            :key="session.id"
            :class="[
              'flex items-center justify-between p-3 rounded-lg cursor-pointer group transition-colors',
              chatStore.currentSession?.id === session.id
                ? 'bg-primary/10'
                : 'hover:bg-surface'
            ]"
            @click="selectSession(session)"
          >
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-on-surface truncate">
                {{ session.sessionName }}
              </p>
              <p class="text-xs text-on-surface/50">
                {{ session.messageCount }} 条消息
              </p>
            </div>

            <button
              @click.stop="deleteSession(session.id)"
              class="p-1 opacity-0 group-hover:opacity-100 hover:bg-red-100 rounded transition-all"
            >
              <span class="material-symbols-outlined text-red-500 text-sm">delete</span>
            </button>
          </div>

          <div v-if="chatStore.sessions.length === 0" class="text-center py-8 text-on-surface/50">
            <span class="material-symbols-outlined text-3xl">chat</span>
            <p class="mt-2 text-sm">暂无对话记录</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Chat Area -->
    <div class="flex-1 flex flex-col">
      <ChatWindow
        v-if="chatStore.currentSession"
        :sessionId="chatStore.currentSession.id"
      />

      <div v-else class="flex-1 flex items-center justify-center bg-surface">
        <div class="text-center">
          <span class="material-symbols-outlined text-6xl text-on-surface/20">forum</span>
          <p class="mt-4 text-on-surface/50">选择一个对话或创建新对话</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import ChatWindow from '@/components/chat/ChatWindow.vue'

const chatStore = useChatStore()

onMounted(async () => {
  await chatStore.fetchSessions()
})

async function createNewSession() {
  const session = await chatStore.createSession()
  await chatStore.fetchSession(session.id)
}

function selectSession(session) {
  chatStore.fetchSession(session.id)
}

async function deleteSession(id) {
  if (confirm('确定要删除这个对话吗？')) {
    await chatStore.deleteSession(id)
  }
}
</script>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/ChatView.vue
git commit -m "feat(chat): add ChatView page with session management"
```

---

## Task 7: Router Configuration

**File:** `frontend/src/router/index.js`

**Purpose:** Add chat route to the Vue Router.

- [ ] **Step 1: Read current router file**

Run: `cat frontend/src/router/index.js`

- [ ] **Step 2: Add chat route**

```javascript
{
  path: '/chat',
  name: 'Chat',
  component: () => import('@/views/ChatView.vue'),
  meta: { requiresAuth: true }
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/router/index.js
git commit -m "feat(chat): add /chat route"
```

---

## Task 8: Nav Bar Update (Optional)

**File:** `frontend/src/components/common/NavBar.vue` (or wherever nav is)

**Purpose:** Add chat icon to navigation if not already present.

- [ ] **Step 1: Read NavBar component**

Run: `cat frontend/src/components/common/NavBar.vue`

- [ ] **Step 2: Add chat link if missing**

```vue
<router-link to="/chat" class="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-surface">
  <span class="material-symbols-outlined">chat</span>
  <span>客服</span>
</router-link>
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/common/NavBar.vue
git commit -m "feat(chat): add chat link to navigation"
```

---

## Verification

After all tasks complete:

```bash
cd frontend
npm install
npm run build
```

Expected: BUILD SUCCESS

---

## Integration with Backend

Ensure the backend is running on port 8080 and proxy is configured in `vite.config.js`:

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```
