<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { fetchLogs, type OperationLog } from '@/utils/logApi'

const logs = ref<OperationLog[]>([])
const page = ref(0)
const size = ref(20)
const totalPages = ref(0)
const totalElements = ref(0)
const filters = ref({
  success: null as boolean | null
})
const selectedLog = ref<OperationLog | null>(null)
const isLoading = ref(false)

const loadLogs = async () => {
  isLoading.value = true
  try {
    const response = await fetchLogs(page.value, size.value, filters.value.success)
    logs.value = response.content || []
    totalPages.value = response.totalPages || 0
    totalElements.value = response.totalElements || 0
  } catch (error) {
    console.error('Failed to fetch logs:', error)
    logs.value = []
  } finally {
    isLoading.value = false
  }
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const formatJson = (str: string) => {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

const showDetail = (log: OperationLog) => {
  selectedLog.value = log
}

const closeDetail = () => {
  selectedLog.value = null
}

const prevPage = () => {
  if (page.value > 0) {
    page.value--
    loadLogs()
  }
}

const nextPage = () => {
  if (page.value < totalPages.value - 1) {
    page.value++
    loadLogs()
  }
}

const resetFilters = () => {
  filters.value.success = null
  page.value = 0
  loadLogs()
}

watch(() => filters.value.success, () => {
  page.value = 0
  loadLogs()
})

onMounted(loadLogs)

defineExpose({ loadLogs })
</script>

<template>
  <div class="log-viewer h-full flex flex-col bg-white dark:bg-gray-800 rounded-xl border border-gray-300/20 dark:border-gray-700 overflow-hidden">
      <!-- Header -->
      <div class="flex-none px-4 py-3 border-b border-gray-300/20 dark:border-gray-700 bg-gray-50 dark:bg-gray-900/50">
        <h3 class="text-sm font-semibold text-gray-900 dark:text-gray-100">操作日志</h3>
      <p class="text-xs text-gray-600 dark:text-gray-400 mt-0.5">共 {{ totalElements }} 条记录</p>
    </div>

    <!-- Filters -->
    <div class="flex-none px-4 py-3 border-b border-gray-300/20 dark:border-gray-700 space-y-3">
      <div class="flex gap-2">
        <div class="relative flex-1">
          <span class="material-symbols-outlined text-gray-600/60 dark:text-gray-400/60 absolute left-2.5 top-1/2 -translate-y-1/2 text-base pointer-events-none">search</span>
          <input
            v-model="filters.username"
            class="w-full pl-8 pr-3 py-1.5 bg-gray-100 dark:bg-gray-800 border border-gray-300/20 dark:border-gray-700 rounded-lg text-sm placeholder:text-gray-600/60 dark:placeholder:text-gray-400/60 focus:outline-none focus:ring-2 focus:ring-blue-800/20 dark:focus:ring-blue-500/20 focus:border-blue-800 dark:focus:border-blue-400 transition-all"
            placeholder="搜索用户名..."
            @keyup.enter="page = 0; fetchLogs()"
          />
        </div>
        <button
          class="px-3 py-1.5 bg-blue-800 dark:bg-blue-500 text-white text-sm rounded-lg hover:bg-blue-800/90 dark:hover:bg-blue-500/90 transition-colors flex items-center gap-1"
          @click="page = 0; loadLogs()"
        >
          <span class="material-symbols-outlined text-base">search</span>
        </button>
        <button
          class="px-3 py-1.5 text-gray-600 dark:text-gray-400 text-sm rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors flex items-center gap-1"
          @click="resetFilters"
        >
          <span class="material-symbols-outlined text-base">clear</span>
        </button>
      </div>

      <!-- Status Filter -->
      <div class="flex gap-1">
        <button
          class="px-3 py-1 text-xs rounded-full transition-colors"
          :class="filters.success === null ? 'bg-blue-800 dark:bg-blue-500 text-white' : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700'"
          @click="filters.success = null"
        >
          全部
        </button>
        <button
          class="px-3 py-1 text-xs rounded-full transition-colors"
          :class="filters.success === true ? 'bg-green-600 text-white' : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700'"
          @click="filters.success = true"
        >
          成功
        </button>
        <button
          class="px-3 py-1 text-xs rounded-full transition-colors"
          :class="filters.success === false ? 'bg-red-600 text-white' : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700'"
          @click="filters.success = false"
        >
          失败
        </button>
      </div>
    </div>

    <!-- Log List -->
    <div class="flex-1 overflow-y-auto">
      <div v-if="isLoading" class="flex items-center justify-center h-full">
        <span class="material-symbols-outlined text-3xl text-gray-600 dark:text-gray-400 animate-spin">progress_activity</span>
      </div>

      <div v-else-if="logs.length === 0" class="flex flex-col items-center justify-center h-full text-gray-600 dark:text-gray-400">
        <span class="material-symbols-outlined text-4xl">receipt_long</span>
        <p class="text-sm mt-2">暂无日志记录</p>
      </div>

      <div v-else class="divide-y divide-gray-300/20 dark:divide-gray-700">
        <div
          v-for="log in logs"
          :key="log.id"
          class="px-4 py-3 hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer transition-colors"
          @click="showDetail(log)"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2">
                <span
                  class="px-1.5 py-0.5 text-[10px] font-medium rounded"
                  :class="log.httpMethod === 'GET' ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400' : log.httpMethod === 'POST' ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400' : 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400'"
                >
                  {{ log.httpMethod }}
                </span>
                <span class="text-xs text-gray-900 dark:text-gray-100 truncate">{{ log.requestUri }}</span>
              </div>
              <div class="flex items-center gap-3 mt-1.5 text-xs text-gray-600 dark:text-gray-400">
                <span class="flex items-center gap-1">
                  <span class="material-symbols-outlined text-xs">person</span>
                  {{ log.username }}
                </span>
                <span class="flex items-center gap-1">
                  <span class="material-symbols-outlined text-xs">schedule</span>
                  {{ formatDate(log.createdAt) }}
                </span>
              </div>
            </div>
            <div class="flex items-center gap-2 shrink-0">
              <span class="text-xs text-gray-600 dark:text-gray-400">{{ log.executionTime }}ms</span>
              <span
                class="w-2 h-2 rounded-full"
                :class="log.success ? 'bg-green-600' : 'bg-red-600'"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="flex-none px-4 py-3 border-t border-gray-300/20 dark:border-gray-700 bg-gray-50 dark:bg-gray-900/50">
      <div class="flex items-center justify-between">
        <button
          class="px-3 py-1.5 text-xs rounded-lg transition-colors flex items-center gap-1"
          :class="page === 0 ? 'text-gray-400 cursor-not-allowed' : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'"
          :disabled="page === 0"
          @click="prevPage"
        >
          <span class="material-symbols-outlined text-base">chevron_left</span>
          上一页
        </button>
        <span class="text-xs text-gray-600 dark:text-gray-400">
          {{ page + 1 }} / {{ totalPages || 1 }}
        </span>
        <button
          class="px-3 py-1.5 text-xs rounded-lg transition-colors flex items-center gap-1"
          :class="page >= totalPages - 1 ? 'text-gray-400 cursor-not-allowed' : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'"
          :disabled="page >= totalPages - 1"
          @click="nextPage"
        >
          下一页
          <span class="material-symbols-outlined text-base">chevron_right</span>
        </button>
      </div>
    </div>

    <!-- Detail Modal -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="selectedLog" class="fixed inset-0 bg-black/40 backdrop-blur-sm z-[100] flex items-center justify-center p-4" @click.self="closeDetail">
          <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-w-2xl max-h-[80vh] overflow-hidden flex flex-col">
            <!-- Modal Header -->
            <div class="flex items-center justify-between px-5 py-4 border-b border-gray-300/20 dark:border-gray-700">
              <div>
                <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">日志详情</h3>
                <p class="text-xs text-gray-600 dark:text-gray-400 mt-0.5">{{ formatDate(selectedLog.createdAt) }}</p>
              </div>
              <button
                class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                @click="closeDetail"
              >
                <span class="material-symbols-outlined text-gray-600 dark:text-gray-400">close</span>
              </button>
            </div>

            <!-- Modal Content -->
            <div class="flex-1 overflow-y-auto p-5">
              <!-- Status Badge -->
              <div class="flex items-center gap-2 mb-4">
                <span
                  class="px-2.5 py-1 text-xs font-medium rounded-full"
                  :class="selectedLog.success ? 'bg-green-600/10 text-green-600 dark:bg-green-500/20 dark:text-green-400' : 'bg-red-600/10 text-red-600 dark:bg-red-500/20 dark:text-red-400'"
                >
                  {{ selectedLog.success ? '成功' : '失败' }}
                </span>
                <span class="text-xs text-gray-600 dark:text-gray-400">耗时 {{ selectedLog.executionTime }}ms</span>
              </div>

              <!-- Info Grid -->
              <div class="grid grid-cols-2 gap-3 mb-4">
                <div class="bg-gray-100 dark:bg-gray-900/50 rounded-xl p-3">
                  <p class="text-[10px] uppercase tracking-wider text-gray-600 dark:text-gray-400 mb-1">用户</p>
                  <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ selectedLog.username }}</p>
                </div>
                <div class="bg-gray-100 dark:bg-gray-900/50 rounded-xl p-3">
                  <p class="text-[10px] uppercase tracking-wider text-gray-600 dark:text-gray-400 mb-1">IP地址</p>
                  <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ selectedLog.ip || 'N/A' }}</p>
                </div>
                <div class="bg-gray-100 dark:bg-gray-900/50 rounded-xl p-3">
                  <p class="text-[10px] uppercase tracking-wider text-gray-600 dark:text-gray-400 mb-1">类名</p>
                  <p class="text-sm font-medium text-gray-900 dark:text-gray-100 truncate" :title="selectedLog.className">{{ selectedLog.className }}</p>
                </div>
                <div class="bg-gray-100 dark:bg-gray-900/50 rounded-xl p-3">
                  <p class="text-[10px] uppercase tracking-wider text-gray-600 dark:text-gray-400 mb-1">方法</p>
                  <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ selectedLog.methodName }}</p>
                </div>
              </div>

              <!-- URI -->
              <div class="bg-gray-100 dark:bg-gray-900/50 rounded-xl p-3 mb-4">
                <p class="text-[10px] uppercase tracking-wider text-gray-600 dark:text-gray-400 mb-1">请求URI</p>
                <div class="flex items-center gap-2">
                  <span
                    class="px-1.5 py-0.5 text-[10px] font-medium rounded"
                    :class="selectedLog.httpMethod === 'GET' ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400' : selectedLog.httpMethod === 'POST' ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400' : 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400'"
                  >
                    {{ selectedLog.httpMethod }}
                  </span>
                  <p class="text-sm font-medium text-gray-900 dark:text-gray-100 break-all">{{ selectedLog.requestUri }}</p>
                </div>
              </div>

              <!-- Parameters -->
              <div v-if="selectedLog.parameters" class="mb-4">
                <p class="text-xs font-medium text-gray-900 dark:text-gray-100 mb-2">请求参数</p>
                <pre class="bg-gray-100 dark:bg-gray-900/50 rounded-xl p-3 text-xs text-gray-900 dark:text-gray-100 overflow-x-auto">{{ formatJson(selectedLog.parameters) }}</pre>
              </div>

              <!-- Error Message -->
              <div v-if="selectedLog.errorMessage" class="mb-4">
                <p class="text-xs font-medium text-red-600 dark:text-red-400 mb-2">错误信息</p>
                <pre class="bg-red-600/5 dark:bg-red-500/10 rounded-xl p-3 text-xs text-red-600 dark:text-red-400 overflow-x-auto">{{ selectedLog.errorMessage }}</pre>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-active .bg-white,
.modal-leave-active .bg-white {
  transition: transform 0.2s ease, opacity 0.2s ease;
}
.modal-enter-from .bg-white,
.modal-leave-to .bg-white {
  transform: scale(0.95);
  opacity: 0;
}

/* Scrollbar */
::-webkit-scrollbar {
  width: 4px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background-color: var(--outline-variant);
  border-radius: 4px;
}
</style>
