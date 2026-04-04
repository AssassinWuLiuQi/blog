<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
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
  <AppLayout section-title="操作日志">
    <div class="log-viewer h-full flex flex-col bg-white rounded-xl border border-outline-variant/10 overflow-hidden">
      <!-- Header -->
      <div class="flex-none px-4 py-3 border-b border-outline-variant/10 bg-surface-container-low/50">
        <h3 class="text-sm font-semibold text-on-surface">操作日志</h3>
      <p class="text-xs text-on-surface-variant mt-0.5">共 {{ totalElements }} 条记录</p>
    </div>

    <!-- Filters -->
    <div class="flex-none px-4 py-3 border-b border-outline-variant/10 space-y-3">
      <div class="flex gap-2">
        <div class="relative flex-1">
          <span class="material-symbols-outlined text-on-surface-variant/60 absolute left-2.5 top-1/2 -translate-y-1/2 text-base pointer-events-none">search</span>
          <input
            v-model="filters.username"
            class="w-full pl-8 pr-3 py-1.5 bg-surface border border-outline-variant/20 rounded-lg text-sm placeholder:text-on-surface-variant/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
            placeholder="搜索用户名..."
            @keyup.enter="page = 0; fetchLogs()"
          />
        </div>
        <button
          class="px-3 py-1.5 bg-primary text-white text-sm rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-1"
          @click="page = 0; loadLogs()"
        >
          <span class="material-symbols-outlined text-base">search</span>
        </button>
        <button
          class="px-3 py-1.5 text-on-surface-variant text-sm rounded-lg hover:bg-surface-container-low transition-colors flex items-center gap-1"
          @click="resetFilters"
        >
          <span class="material-symbols-outlined text-base">clear</span>
        </button>
      </div>

      <!-- Status Filter -->
      <div class="flex gap-1">
        <button
          class="px-3 py-1 text-xs rounded-full transition-colors"
          :class="filters.success === null ? 'bg-primary text-white' : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'"
          @click="filters.success = null"
        >
          全部
        </button>
        <button
          class="px-3 py-1 text-xs rounded-full transition-colors"
          :class="filters.success === true ? 'bg-success text-white' : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'"
          @click="filters.success = true"
        >
          成功
        </button>
        <button
          class="px-3 py-1 text-xs rounded-full transition-colors"
          :class="filters.success === false ? 'bg-error text-white' : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'"
          @click="filters.success = false"
        >
          失败
        </button>
      </div>
    </div>

    <!-- Log List -->
    <div class="flex-1 overflow-y-auto">
      <div v-if="isLoading" class="flex items-center justify-center h-full">
        <span class="material-symbols-outlined text-3xl text-on-surface-variant animate-spin">progress_activity</span>
      </div>

      <div v-else-if="logs.length === 0" class="flex flex-col items-center justify-center h-full text-on-surface-variant">
        <span class="material-symbols-outlined text-4xl">receipt_long</span>
        <p class="text-sm mt-2">暂无日志记录</p>
      </div>

      <div v-else class="divide-y divide-outline-variant/10">
        <div
          v-for="log in logs"
          :key="log.id"
          class="px-4 py-3 hover:bg-surface-container-low/50 cursor-pointer transition-colors"
          @click="showDetail(log)"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2">
                <span
                  class="px-1.5 py-0.5 text-[10px] font-medium rounded"
                  :class="log.httpMethod === 'GET' ? 'bg-blue-100 text-blue-700' : log.httpMethod === 'POST' ? 'bg-green-100 text-green-700' : 'bg-purple-100 text-purple-700'"
                >
                  {{ log.httpMethod }}
                </span>
                <span class="text-xs text-on-surface truncate">{{ log.requestUri }}</span>
              </div>
              <div class="flex items-center gap-3 mt-1.5 text-xs text-on-surface-variant">
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
              <span class="text-xs text-on-surface-variant">{{ log.executionTime }}ms</span>
              <span
                class="w-2 h-2 rounded-full"
                :class="log.success ? 'bg-success' : 'bg-error'"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="flex-none px-4 py-3 border-t border-outline-variant/10 bg-surface-container-low/50">
      <div class="flex items-center justify-between">
        <button
          class="px-3 py-1.5 text-xs rounded-lg transition-colors flex items-center gap-1"
          :class="page === 0 ? 'text-on-surface-variant/40 cursor-not-allowed' : 'text-on-surface-variant hover:bg-surface-container-low'"
          :disabled="page === 0"
          @click="prevPage"
        >
          <span class="material-symbols-outlined text-base">chevron_left</span>
          上一页
        </button>
        <span class="text-xs text-on-surface-variant">
          {{ page + 1 }} / {{ totalPages || 1 }}
        </span>
        <button
          class="px-3 py-1.5 text-xs rounded-lg transition-colors flex items-center gap-1"
          :class="page >= totalPages - 1 ? 'text-on-surface-variant/40 cursor-not-allowed' : 'text-on-surface-variant hover:bg-surface-container-low'"
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
          <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[80vh] overflow-hidden flex flex-col">
            <!-- Modal Header -->
            <div class="flex items-center justify-between px-5 py-4 border-b border-outline-variant/10">
              <div>
                <h3 class="text-base font-semibold text-on-surface">日志详情</h3>
                <p class="text-xs text-on-surface-variant mt-0.5">{{ formatDate(selectedLog.createdAt) }}</p>
              </div>
              <button
                class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-surface-container-low transition-colors"
                @click="closeDetail"
              >
                <span class="material-symbols-outlined text-on-surface-variant">close</span>
              </button>
            </div>

            <!-- Modal Content -->
            <div class="flex-1 overflow-y-auto p-5">
              <!-- Status Badge -->
              <div class="flex items-center gap-2 mb-4">
                <span
                  class="px-2.5 py-1 text-xs font-medium rounded-full"
                  :class="selectedLog.success ? 'bg-success/10 text-success' : 'bg-error/10 text-error'"
                >
                  {{ selectedLog.success ? '成功' : '失败' }}
                </span>
                <span class="text-xs text-on-surface-variant">耗时 {{ selectedLog.executionTime }}ms</span>
              </div>

              <!-- Info Grid -->
              <div class="grid grid-cols-2 gap-3 mb-4">
                <div class="bg-surface-container-low/50 rounded-xl p-3">
                  <p class="text-[10px] uppercase tracking-wider text-on-surface-variant mb-1">用户</p>
                  <p class="text-sm font-medium text-on-surface">{{ selectedLog.username }}</p>
                </div>
                <div class="bg-surface-container-low/50 rounded-xl p-3">
                  <p class="text-[10px] uppercase tracking-wider text-on-surface-variant mb-1">IP地址</p>
                  <p class="text-sm font-medium text-on-surface">{{ selectedLog.ip || 'N/A' }}</p>
                </div>
                <div class="bg-surface-container-low/50 rounded-xl p-3">
                  <p class="text-[10px] uppercase tracking-wider text-on-surface-variant mb-1">类名</p>
                  <p class="text-sm font-medium text-on-surface truncate" :title="selectedLog.className">{{ selectedLog.className }}</p>
                </div>
                <div class="bg-surface-container-low/50 rounded-xl p-3">
                  <p class="text-[10px] uppercase tracking-wider text-on-surface-variant mb-1">方法</p>
                  <p class="text-sm font-medium text-on-surface">{{ selectedLog.methodName }}</p>
                </div>
              </div>

              <!-- URI -->
              <div class="bg-surface-container-low/50 rounded-xl p-3 mb-4">
                <p class="text-[10px] uppercase tracking-wider text-on-surface-variant mb-1">请求URI</p>
                <div class="flex items-center gap-2">
                  <span
                    class="px-1.5 py-0.5 text-[10px] font-medium rounded"
                    :class="selectedLog.httpMethod === 'GET' ? 'bg-blue-100 text-blue-700' : selectedLog.httpMethod === 'POST' ? 'bg-green-100 text-green-700' : 'bg-purple-100 text-purple-700'"
                  >
                    {{ selectedLog.httpMethod }}
                  </span>
                  <p class="text-sm font-medium text-on-surface break-all">{{ selectedLog.requestUri }}</p>
                </div>
              </div>

              <!-- Parameters -->
              <div v-if="selectedLog.parameters" class="mb-4">
                <p class="text-xs font-medium text-on-surface mb-2">请求参数</p>
                <pre class="bg-surface-container-low/50 rounded-xl p-3 text-xs text-on-surface overflow-x-auto">{{ formatJson(selectedLog.parameters) }}</pre>
              </div>

              <!-- Error Message -->
              <div v-if="selectedLog.errorMessage" class="mb-4">
                <p class="text-xs font-medium text-error mb-2">错误信息</p>
                <pre class="bg-error/5 rounded-xl p-3 text-xs text-error overflow-x-auto">{{ selectedLog.errorMessage }}</pre>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
  </AppLayout>
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
