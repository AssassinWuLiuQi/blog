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
import { ref, watch, nextTick, onUnmounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import { createSSEStream } from '@/utils/sse'
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
const streamController = ref(null)

onUnmounted(() => {
  if (streamController.value) {
    streamController.value.abort()
  }
})

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
  let fullResponse = ''

  streamController.value = createSSEStream(`/api/chat/sessions/${sessionId}/stream`, {
    method: 'POST',
    body: {
      sessionId: sessionId,
      content: text
    },
    onMessage: (data) => {
      if (data.content) {
        fullResponse += data.content
        streamingContent.value = fullResponse
      }
    },
    onClose: () => {
      if (fullResponse) {
        chatStore.addMessage({
          messageId: Date.now(),
          role: 'assistant',
          content: fullResponse,
          createdAt: new Date().toISOString()
        })
      }
    },
    onError: (err) => {
      console.error('SSE error:', err)
      chatStore.addMessage({
        messageId: Date.now(),
        role: 'assistant',
        content: '抱歉，发生了错误。请稍后再试。',
        createdAt: new Date().toISOString()
      })
    }
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
