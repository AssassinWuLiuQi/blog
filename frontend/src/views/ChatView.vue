<script setup lang="ts">
import { ref } from 'vue'
import ChatWindow from '@/components/chat/ChatWindow.vue'
import ChatSessionDrawer from '@/components/chat/ChatSessionDrawer.vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()
const drawerOpen = ref(true)

async function createNewSession() {
  const session = await chatStore.createSession()
  await chatStore.fetchSession(session.id)
  drawerOpen.value = false
}

function selectSession(session) {
  if (session) {
    chatStore.fetchSession(session.id)
  }
  drawerOpen.value = false
}

async function deleteSession(id) {
  if (confirm('确定要删除这个对话吗？')) {
    await chatStore.deleteSession(id)
  }
}
</script>

<template>
  <!-- Toggle Button -->
  <button
    @click="drawerOpen = !drawerOpen"
    :class="[
      'fixed top-20 z-40 w-8 h-16 flex items-center justify-center bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-r-lg shadow-md hover:bg-gray-50 dark:hover:bg-gray-700 transition-all',
      drawerOpen ? 'left-[17rem]' : 'left-0'
    ]"
  >
    <span class="material-symbols-outlined text-gray-600 dark:text-gray-400 text-xl transition-transform duration-300" :class="drawerOpen ? 'rotate-180' : ''">
      chevron_right
    </span>
  </button>

  <!-- Session Drawer -->
  <ChatSessionDrawer
    :open="drawerOpen"
    @close="drawerOpen = false"
    @select="selectSession"
    @delete="deleteSession"
  />

  <!-- Overlay when drawer is open on mobile -->
  <transition name="fade">
    <div
      v-if="drawerOpen"
      class="fixed inset-0 bg-black/20 z-30 lg:hidden"
      @click="drawerOpen = false"
    />
  </transition>

  <!-- Chat Area -->
  <div class="flex-1 flex flex-col">
    <ChatWindow
      v-if="chatStore.currentSession"
      :sessionId="chatStore.currentSession.id"
    />

    <div v-else class="flex-1 flex items-center justify-center bg-gray-50 dark:bg-gray-900">
      <div class="text-center">
        <span class="material-symbols-outlined text-6xl text-gray-400/20 dark:text-gray-600/20">forum</span>
        <p class="mt-4 text-gray-600 dark:text-gray-400">选择一个对话或创建新对话</p>
        <button
          @click="createNewSession"
          class="mt-4 px-4 py-2 bg-blue-800 dark:bg-blue-500 text-white rounded-lg hover:bg-blue-800/90 dark:hover:bg-blue-500/90 transition-colors"
        >
          新建对话
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
