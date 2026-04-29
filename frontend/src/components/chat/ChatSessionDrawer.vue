<script setup>
import { useChatStore } from '@/stores/chat'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'select', 'delete'])

const chatStore = useChatStore()

function selectSession(session) {
  emit('select', session)
}
</script>

<template>
  <div
    :class="[
      'fixed left-0 top-16 h-[calc(100vh-64px)] w-72 flex flex-col z-50 transition-transform duration-300',
      open ? 'translate-x-0' : '-translate-x-full'
    ]"
  >
    <!-- Drawer Content -->
    <div class="flex-1 flex flex-col bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700">
      <!-- Header -->
      <div class="p-4 border-b border-gray-200 dark:border-gray-700">
        <button
          @click="$emit('close')"
          class="absolute top-4 right-2 p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
        >
          <span class="material-symbols-outlined text-gray-600 dark:text-gray-400">close</span>
        </button>
        <button
          @click="$emit('select', null)"
          class="w-full flex items-center justify-center gap-2 px-4 py-2 bg-blue-800 dark:bg-blue-500 text-white rounded-lg hover:bg-blue-800/90 dark:hover:bg-blue-500/90 transition-colors"
        >
          <span class="material-symbols-outlined">add</span>
          新建对话
        </button>
      </div>

      <!-- Session List -->
      <div class="flex-1 overflow-y-auto">
        <div class="p-2 space-y-1">
          <div
            v-for="session in chatStore.sortedSessions"
            :key="session.id"
            :class="[
              'flex items-center justify-between p-3 rounded-lg cursor-pointer group transition-colors',
              chatStore.currentSession?.id === session.id
                ? 'bg-blue-800/10 dark:bg-blue-500/10'
                : 'hover:bg-gray-100 dark:hover:bg-gray-700'
            ]"
            @click="selectSession(session)"
          >
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 dark:text-gray-100 truncate">
                {{ session.sessionName }}
              </p>
              <p class="text-xs text-gray-600 dark:text-gray-400">
                {{ session.messageCount }} 条消息
              </p>
            </div>

            <button
              @click.stop="$emit('delete', session.id)"
              class="p-1 opacity-0 group-hover:opacity-100 hover:bg-red-100 dark:hover:bg-red-900/20 rounded transition-all"
            >
              <span class="material-symbols-outlined text-red-500 text-sm">delete</span>
            </button>
          </div>

          <div v-if="chatStore.sessions.length === 0" class="text-center py-8 text-gray-600 dark:text-gray-400">
            <span class="material-symbols-outlined text-3xl">chat</span>
            <p class="mt-2 text-sm">暂无对话记录</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
