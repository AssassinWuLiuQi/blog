<script setup lang="ts">
import { ref, watch } from 'vue'
import ImagePanel from '@/components/image/ImagePanel.vue'
import { fetchImageHistory, type ImageHistoryItem } from '@/utils/imageApi'

const searchQuery = ref('')
const generatedImages = ref<string[]>([])
const historyDrawerVisible = ref(false)
const imagePanelRef = ref<InstanceType<typeof ImagePanel> | null>(null)

const selectedIds = ref<Set<number>>(new Set())

const currentPage = ref(0)
const pageSize = ref(10)
const hasMore = ref(true)
const loadingMore = ref(false)

const historyList = ref<ImageHistoryItem[]>([])
const refreshing = ref(false)

const loadHistory = async (): Promise<void> => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    const items = await fetchImageHistory(currentPage.value, pageSize.value)
    if (items.length < pageSize.value) {
      hasMore.value = false
    }
    historyList.value.push(...items)
    currentPage.value++
  } catch (error) {
    console.error('Failed to load history:', error)
  } finally {
    loadingMore.value = false
  }
}

const handleRefresh = async (): Promise<void> => {
  if (refreshing.value) return
  refreshing.value = true
  currentPage.value = 0
  hasMore.value = true
  historyList.value = []
  try {
    const items = await fetchImageHistory(0, pageSize.value)
    if (items.length < pageSize.value) {
      hasMore.value = false
    }
    historyList.value = items
    currentPage.value = 1
  } catch (error) {
    console.error('Failed to refresh history:', error)
  } finally {
    refreshing.value = false
  }
}

const handleDownload = (url: string, index: number): void => {
  const link = document.createElement('a')
  link.href = url
  link.download = `generated-image-${index + 1}.png`
  link.target = '_blank'
  link.click()
}

const detailsDialogVisible = ref(false)
const currentDetailsItem = ref<ImageHistoryItem | null>(null)

const openDetails = (item: ImageHistoryItem): void => {
  currentDetailsItem.value = item
  detailsDialogVisible.value = true
}

const handleReuse = (item: ImageHistoryItem): void => {
  historyDrawerVisible.value = false
  detailsDialogVisible.value = false
  imagePanelRef.value?.fillForm({
    description: item.description,
    model: item.model,
    aspectRatio: item.aspectRatio,
    style: item.style
  })
}

const handleHistoryScroll = (event: Event): void => {
  const el = event.target as HTMLElement
  const { scrollTop, scrollHeight, clientHeight } = el
  if (scrollHeight - scrollTop - clientHeight < 50 && !loadingMore.value && hasMore.value) {
    loadHistory()
  }
}

const contextmenuVisible = ref(false)
const contextmenuPosition = ref({ x: 0, y: 0 })

const handleContextMenu = (event: MouseEvent, item: ImageHistoryItem): void => {
  event.preventDefault()
  selectedIds.value.clear()
  selectedIds.value.add(item.id)
  contextmenuPosition.value = { x: event.clientX, y: event.clientY }
  contextmenuVisible.value = true
}

const handleSelectAll = (): void => {
  selectedIds.value = new Set(historyList.value.map(item => item.id))
  contextmenuVisible.value = false
}

const handleDeleteSelected = async (): Promise<void> => {
  if (selectedIds.value.size === 0) return
  try {
    const token = localStorage.getItem('token')
    await fetch('/api/image/history/batch-delete', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      body: JSON.stringify({ ids: Array.from(selectedIds.value) })
    })
    historyList.value = historyList.value.filter(item => !selectedIds.value.has(item.id))
    selectedIds.value.clear()
    contextmenuVisible.value = false
  } catch (error) {
    console.error('Delete failed:', error)
  }
}

const isSelected = (id: number): boolean => selectedIds.value.has(id)

watch(historyDrawerVisible, (visible) => {
  if (visible) {
    currentPage.value = 0
    hasMore.value = true
    historyList.value = []
    loadHistory()
  }
})
</script>

<template>
  <div class="flex flex-1 h-[calc(100vh-4rem)]">
      <!-- Left Column: Image Generation Panel (1/3) -->
      <ImagePanel ref="imagePanelRef" v-model="generatedImages" class="w-1/3" />

      <!-- Right Column: Results Gallery (2/3) -->
      <div class="flex-1 w-2/3 bg-white dark:bg-gray-800 p-8 flex flex-col">
        <!-- Results Header -->
        <div class="flex items-center justify-between mb-6 shrink-0">
          <div class="flex items-center gap-3">
            <!-- Gradient Accent Bar -->
            <div class="w-2 h-8 rounded-full bg-gradient-to-b from-blue-800 to-blue-700 dark:from-blue-500 dark:to-blue-700"></div>
            <h2 class="text-2xl font-semibold text-blue-800 dark:text-blue-400">生成结果 (Gallery)</h2>
          </div>
          <!-- View All History Link -->
          <button
            class="flex items-center gap-1 text-blue-800 dark:text-blue-400 text-sm font-medium hover:opacity-80 transition-opacity"
            @click="historyDrawerVisible = true"
          >
            <span>View All History</span>
            <span class="material-symbols-outlined text-base">chevron_right</span>
          </button>
        </div>

        <!-- Results Gallery -->
        <div class="flex-1 overflow-auto">
          <!-- Sophisticated Empty State Panel -->
          <div
            v-if="generatedImages.length === 0"
            class="h-full flex flex-col items-center justify-center rounded-2xl bg-gray-50/5 dark:bg-gray-900/5 border-2 border-gray-300/20 dark:border-gray-700"
          >
            <!-- Icon with decorative background -->
            <div class="relative mb-6">
              <div class="w-24 h-24 rounded-2xl bg-blue-800/10 dark:bg-blue-500/10 flex items-center justify-center">
                <span class="material-symbols-outlined text-6xl text-blue-800/20 dark:text-blue-500/20">auto_awesome</span>
              </div>
              <!-- Decorative corner elements -->
              <div class="absolute -top-2 -left-2 w-4 h-4 border-l-2 border-t-2 border-blue-800/20 dark:border-blue-500/20 rounded-tl-lg"></div>
              <div class="absolute -bottom-2 -right-2 w-4 h-4 border-r-2 border-b-2 border-blue-800/20 dark:border-blue-500/20 rounded-br-lg"></div>
            </div>

            <!-- Heading -->
            <h3 class="text-2xl font-medium text-gray-900 dark:text-gray-100 mb-3">No creations yet</h3>

            <!-- Subtitle -->
            <p class="text-base text-gray-600 dark:text-gray-400 max-w-md text-center mb-8">
              Enter a prompt and click "Start Generating" to see your<br />artistic vision come to life.
            </p>

            <!-- Decorative Elements -->
            <div class="flex items-center gap-3 mb-8">
              <div class="w-16 h-1 rounded-full bg-gray-300/30 dark:bg-gray-700/30"></div>
              <div class="w-8 h-1 rounded-full bg-blue-800/20 dark:bg-blue-500/20"></div>
              <div class="w-16 h-1 rounded-full bg-gray-300/30 dark:bg-gray-700/30"></div>
            </div>

            <!-- Footer Decoration -->
            <div class="flex items-center gap-4 opacity-20">
              <div class="w-24 h-px bg-gray-300 dark:bg-gray-700"></div>
              <span class="text-xs font-bold tracking-[0.3em] text-gray-600 dark:text-gray-400">Scholarly Precision & AI Artistry</span>
              <div class="w-24 h-px bg-gray-300 dark:bg-gray-700"></div>
            </div>
          </div>

          <!-- Generated Images Grid -->
          <div
            v-else
            class="grid gap-4"
            :class="generatedImages.length === 1 ? 'grid-cols-1' : 'grid-cols-2'"
          >
            <div
              v-for="(url, index) in generatedImages"
              :key="index"
              class="relative group rounded-lg overflow-hidden"
            >
              <img
                :src="url"
                :alt="`Generated image ${index + 1}`"
                class="w-full h-auto object-cover"
              />
              <div
                class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100
                       transition-opacity flex items-center justify-center"
              >
                <button
                  class="px-4 py-2 bg-white text-gray-900 font-medium rounded-lg
                         hover:bg-gray-100 transition-colors"
                  @click="handleDownload(url, index)"
                >
                  下载
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- History Drawer -->
      <el-drawer
        v-model="historyDrawerVisible"
        title=""
        direction="rtl"
        size="600px"
        :with-header="false"
      >
        <div class="h-full flex flex-col overflow-hidden">
          <!-- Drawer Header -->
          <div class="shrink-0 flex items-center justify-between px-6 py-5 border-b border-gray-300/20 dark:border-gray-700">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-lg bg-blue-800/10 dark:bg-blue-500/10 flex items-center justify-center">
                <span class="material-symbols-outlined text-blue-800 dark:text-blue-400">history</span>
              </div>
              <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100">Generation History</h2>
            </div>
            <div class="flex items-center gap-1">
              <button
                class="w-8 h-8 rounded-full hover:bg-gray-300/10 dark:hover:bg-gray-700/10 flex items-center justify-center transition-colors disabled:opacity-50"
                :disabled="refreshing"
                @click="handleRefresh"
              >
                <span class="material-symbols-outlined text-lg text-gray-600 dark:text-gray-400" :class="{ 'animate-spin': refreshing }">refresh</span>
              </button>
              <button
                class="w-8 h-8 rounded-full hover:bg-gray-300/10 dark:hover:bg-gray-700/10 flex items-center justify-center transition-colors"
                @click="historyDrawerVisible = false"
              >
                <span class="material-symbols-outlined text-lg text-gray-600 dark:text-gray-400">close</span>
              </button>
            </div>
          </div>

          <!-- History List -->
          <div class="flex-1 overflow-auto p-6" @scroll="handleHistoryScroll">
            <div class="flex flex-col gap-6">
              <div
                v-for="item in historyList"
                :key="item.id"
                class="bg-white dark:bg-gray-800 rounded-lg border border-gray-300/20 dark:border-gray-700 p-4 cursor-pointer transition-colors"
                :class="{ 'bg-blue-800/10 dark:bg-blue-500/10 border-blue-800/30 dark:border-blue-500/30': isSelected(item.id) }"
                @contextmenu="handleContextMenu($event, item)"
              >
                <div class="flex gap-4">
                  <!-- Thumbnail -->
                  <div class="w-24 h-24 rounded bg-gray-100 dark:bg-gray-700 flex items-center justify-center shrink-0 overflow-hidden">
                    <img
                      v-if="item.thumbnail"
                      :src="item.thumbnail"
                      class="w-full h-full object-cover"
                      alt="thumbnail"
                    />
                    <span v-else class="material-symbols-outlined text-4xl text-gray-400 dark:text-gray-600">image</span>
                  </div>

                  <!-- Content -->
                  <div class="flex-1 flex flex-col justify-between min-w-0">
                    <!-- Title & Date -->
                    <div class="flex items-center justify-between gap-2">
                      <span class="text-xs font-bold text-blue-800 dark:text-blue-400 tracking-wide truncate">{{ item.title }}</span>
                      <div class="flex items-center gap-2 shrink-0">
                        <span class="text-xs text-green-600 dark:text-green-400">{{ item.successCount }} 成功</span>
                        <span class="text-gray-400 dark:text-gray-600">•</span>
                        <span class="text-xs text-red-500 dark:text-red-400">{{ item.failedCount }} 失败</span>
                        <span class="text-gray-400 dark:text-gray-600">•</span>
                        <span class="text-xs text-gray-600 dark:text-gray-400">{{ item.date }}</span>
                      </div>
                    </div>

                    <!-- Description -->
                    <p class="text-sm text-gray-900 dark:text-gray-100 line-clamp-2 leading-relaxed">
                      {{ item.description }}
                    </p>

                    <!-- Actions -->
                    <div class="flex items-center gap-2">
                      <button
                        class="px-3 py-1 text-xs font-semibold text-blue-800 dark:text-blue-400 hover:bg-blue-800/5 dark:hover:bg-blue-500/5 rounded transition-colors"
                        @click="handleReuse(item)"
                      >
                        Reuse
                      </button>
                      <span class="text-gray-400 dark:text-gray-600">•</span>
                      <button
                        class="px-3 py-1 text-xs font-semibold text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded transition-colors"
                        @click="openDetails(item)"
                      >
                        Details
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="loadingMore" class="text-center py-4 text-gray-600 dark:text-gray-400 text-sm">
                加载中...
              </div>
              <div v-if="!hasMore && historyList.length > 0" class="text-center py-4 text-gray-600 dark:text-gray-400 text-sm">
                没有更多了
              </div>
            </div>
          </div>
        </div>

      </el-drawer>

      <!-- Right-click Context Menu (outside drawer to avoid scroll issues) -->
      <!-- <el-contextmenu
        v-model:visible="contextmenuVisible"
        :x="contextmenuPosition.x"
        :y="contextmenuPosition.y"
        :key="`${contextmenuPosition.x}-${contextmenuPosition.y}`"
      >
        <el-contextmenu-item @click="handleSelectAll">
          全选
        </el-contextmenu-item>
        <el-contextmenu-item @click="handleDeleteSelected" :disabled="selectedIds.size === 0">
          删除选中 ({{ selectedIds.size }})
        </el-contextmenu-item>
      </el-contextmenu> -->

      <!-- Details Dialog -->
      <el-dialog
        v-model="detailsDialogVisible"
        title="生成详情"
        width="600px"
        :close-on-click-modal="true"
      >
        <div v-if="currentDetailsItem" class="space-y-4">
          <!-- Prompt -->
          <div>
            <h4 class="text-sm font-semibold text-gray-600 dark:text-gray-400 mb-1">Prompt</h4>
            <p class="text-sm text-gray-900 dark:text-gray-100 bg-gray-100 dark:bg-gray-800 p-3 rounded">{{ currentDetailsItem.description }}</p>
          </div>

          <!-- Images Grid -->
          <div v-if="currentDetailsItem.imageUrls && currentDetailsItem.imageUrls.length > 0">
            <h4 class="text-sm font-semibold text-gray-600 dark:text-gray-400 mb-2">生成图片</h4>
            <div class="grid grid-cols-2 gap-2">
              <img
                v-for="(url, idx) in currentDetailsItem.imageUrls"
                :key="idx"
                :src="url"
                class="w-full h-auto rounded object-contain bg-gray-100 dark:bg-gray-800"
                alt=""
              />
            </div>
          </div>

          <!-- Meta Info -->
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="text-gray-600 dark:text-gray-400">模型：</span>
              <span class="text-gray-900 dark:text-gray-100">{{ currentDetailsItem.model }}</span>
            </div>
            <div>
              <span class="text-gray-600 dark:text-gray-400">比例：</span>
              <span class="text-gray-900 dark:text-gray-100">{{ currentDetailsItem.aspectRatio }}</span>
            </div>
            <div>
              <span class="text-gray-600 dark:text-gray-400">风格：</span>
              <span class="text-gray-900 dark:text-gray-100">{{ currentDetailsItem.style || '-' }}</span>
            </div>
            <div>
              <span class="text-gray-600 dark:text-gray-400">时间：</span>
              <span class="text-gray-900 dark:text-gray-100">{{ currentDetailsItem.date }}</span>
            </div>
            <div>
              <span class="text-gray-600 dark:text-gray-400">成功：</span>
              <span class="text-green-600 dark:text-green-400">{{ currentDetailsItem.successCount }}</span>
            </div>
            <div>
              <span class="text-gray-600 dark:text-gray-400">失败：</span>
              <span class="text-red-500 dark:text-red-400">{{ currentDetailsItem.failedCount }}</span>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
</template>
