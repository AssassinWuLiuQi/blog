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
