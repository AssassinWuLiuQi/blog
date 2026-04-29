<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const emit = defineEmits<{
  'update:collapsed': [collapsed: boolean]
}>()

const router = useRouter()
const route = useRoute()

const isCollapsed = ref(false)
const gadgetsOpen = ref(true)

const isActive = (path: string): boolean => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const navigate = (path: string): void => {
  router.push(path)
}

const toggleGadgets = (): void => {
  gadgetsOpen.value = !gadgetsOpen.value
}

const toggleCollapse = (): void => {
  isCollapsed.value = !isCollapsed.value
  emit('update:collapsed', isCollapsed.value)
}

const navItems = [
  { path: '/', icon: 'home', label: '首页', exact: true },
  { path: '/tech-preview', icon: 'science', label: 'Editor-TTS', exact: false },
  { path: '/image-generation', icon: 'image', label: '图像生成', exact: false },
  { path: '/music-generation', icon: 'music_note', label: '音乐生成', exact: false },
  { path: '/chat', icon: 'chat', label: '客服', exact: false },
  { path: '/logs', icon: 'history', label: '日志', exact: false },
  { path: '/gadgets', icon: 'widgets', label: '小工具', exact: false, hasChildren: true },
]

const isItemActive = (item: typeof navItems[0]): boolean => {
  if (item.exact) return route.path === item.path
  return route.path.startsWith(item.path)
}
</script>

<template>
  <aside
    class="fixed left-0 top-0 h-screen flex flex-col bg-white dark:bg-gray-800 border-r border-gray-300/20 dark:border-gray-700 transition-all duration-300 ease-in-out z-30"
    :class="isCollapsed ? 'w-16' : 'w-64'"
  >
    <!-- Toggle Button -->
    <button
      class="absolute -right-3 top-20 w-6 h-6 bg-white dark:bg-gray-800 border border-gray-300/20 dark:border-gray-700 rounded-full flex items-center justify-center shadow-sm hover:shadow-md hover:scale-110 transition-all duration-200"
      @click="toggleCollapse"
    >
      <span
        class="material-symbols-outlined text-sm text-gray-600 dark:text-gray-400 transition-transform duration-300"
        :class="isCollapsed ? 'rotate-180' : ''"
      >chevron_right</span>
    </button>

    <!-- Logo Section -->
    <div class="h-16 flex items-center px-4 border-b border-gray-300/20 dark:border-gray-700">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 bg-gradient-to-br from-blue-800 to-blue-700 dark:from-blue-500 dark:to-blue-700 rounded-xl flex items-center justify-center shadow-md shrink-0">
          <span class="material-symbols-outlined text-sm text-white" style="font-variation-settings: 'FILL' 1;">menu_book</span>
        </div>
        <transition name="fade-slide">
          <div v-if="!isCollapsed" class="overflow-hidden">
            <h1 class="text-lg font-bold text-gray-900 dark:text-gray-100 tracking-tight leading-none">MxJin</h1>
            <p class="text-[10px] text-gray-600 dark:text-gray-400 tracking-wider">Mxjin's Blog</p>
          </div>
        </transition>
      </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 overflow-y-auto py-4 px-2 space-y-1">
      <template v-for="item in navItems" :key="item.path">
        <!-- Parent Item -->
        <div class="relative">
          <a
            class="nav-item group flex items-center gap-3 py-2.5 rounded-xl transition-all duration-200 cursor-pointer"
            :class="[
              isItemActive(item) && !item.hasChildren
                ? 'bg-blue-800/20 dark:bg-blue-500/20 text-blue-800 dark:text-blue-400 font-medium'
                : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-gray-100',
              isCollapsed ? 'justify-center px-0 w-12 mx-auto' : 'px-3'
            ]"
            :href="'#'"
            @click.prevent="item.hasChildren ? toggleGadgets() : navigate(item.path)"
          >
            <span
              class="material-symbols-outlined text-xl shrink-0 transition-colors duration-200"
              :class="isItemActive(item) && !item.hasChildren ? 'text-blue-800 dark:text-blue-400' : 'text-gray-600 dark:text-gray-400 group-hover:text-gray-900 dark:group-hover:text-gray-100'"
              :style="isItemActive(item) && !item.hasChildren ? 'font-variation-settings: \'FILL\' 1;' : ''"
            >{{ item.icon }}</span>
            <transition name="fade-slide">
              <div v-if="!isCollapsed" class="flex-1 flex items-center justify-between overflow-hidden">
                <span class="text-sm whitespace-nowrap">{{ item.label }}</span>
                <span
                  v-if="item.hasChildren"
                  class="material-symbols-outlined text-base transition-transform duration-200"
                  :class="gadgetsOpen ? 'rotate-180' : ''"
                >expand_more</span>
              </div>
            </transition>
          </a>

          <!-- Active Indicator -->
          <div
            v-if="isItemActive(item) && !item.hasChildren && !isCollapsed"
            class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-6 bg-blue-800 dark:bg-blue-500 rounded-r-full"
          />
        </div>

        <!-- Sub-items (Gadgets) -->
        <transition name="expand">
          <div v-show="gadgetsOpen && !isCollapsed && item.hasChildren" class="ml-4 py-1 space-y-0.5">
            <a
              class="sub-item flex items-center gap-2 py-2 pl-3 pr-2 rounded-lg text-sm transition-all duration-200"
              :class="isActive('/gadgets/cyber-burning') ? 'text-blue-800 dark:text-blue-400 font-medium bg-blue-800/5 dark:bg-blue-500/5' : 'text-gray-600 dark:text-gray-400 hover:text-blue-800 dark:hover:text-blue-400 hover:bg-gray-100 dark:hover:bg-gray-700'"
              href="#"
              @click.prevent="navigate('/gadgets/cyber-burning')"
            >
              <span
                class="w-1.5 h-1.5 rounded-full shrink-0 transition-colors duration-200"
                :class="isActive('/gadgets/cyber-burning') ? 'bg-blue-800 dark:bg-blue-500' : 'bg-gray-300 dark:bg-gray-600'"
              />
              <span>赛博烧纸</span>
            </a>
          </div>
        </transition>
      </template>
    </nav>

    <!-- Bottom Section -->
    <div class="p-2 border-t border-gray-300/20 dark:border-gray-700 space-y-1">
      <!-- Settings -->
      <a
        class="nav-item group flex items-center gap-3 py-2.5 rounded-xl transition-all duration-200"
        :class="[
          isActive('/settings') ? 'bg-blue-800/20 dark:bg-blue-500/20 text-blue-800 dark:text-blue-400 font-medium' : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-gray-100',
          isCollapsed ? 'justify-center px-0 w-12 mx-auto' : 'px-3'
        ]"
        href="#"
        @click.prevent="navigate('/settings')"
      >
        <span class="material-symbols-outlined text-xl shrink-0">settings</span>
        <transition name="fade-slide">
          <span v-if="!isCollapsed" class="text-sm">设置</span>
        </transition>
      </a>
    </div>
  </aside>
</template>

<style scoped>
/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-6px);
}

.expand-enter-active,
.expand-leave-active {
  transition: max-height 0.2s ease, opacity 0.15s ease;
  max-height: 200px;
  overflow: hidden;
}
.expand-enter-from,
.expand-leave-to {
  max-height: 0;
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
::-webkit-scrollbar-thumb:hover {
  background-color: var(--outline);
}
</style>
