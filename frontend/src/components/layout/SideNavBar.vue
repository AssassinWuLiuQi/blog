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
  { path: '/tech-preview', icon: 'science', label: '技术预览', exact: false },
  { path: '/image-generation', icon: 'image', label: '图像生成', exact: false },
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
    class="fixed left-0 top-0 h-screen flex flex-col bg-white border-r border-outline-variant/10 transition-all duration-300 ease-in-out z-50"
    :class="isCollapsed ? 'w-16' : 'w-64'"
  >
    <!-- Toggle Button -->
    <button
      class="absolute -right-3 top-20 w-6 h-6 bg-white border border-outline-variant/20 rounded-full flex items-center justify-center shadow-sm hover:shadow-md hover:scale-110 transition-all duration-200"
      @click="toggleCollapse"
    >
      <span
        class="material-symbols-outlined text-sm text-on-surface-variant transition-transform duration-300"
        :class="isCollapsed ? 'rotate-180' : ''"
      >chevron_right</span>
    </button>

    <!-- Logo Section -->
    <div class="h-16 flex items-center px-4 border-b border-outline-variant/10">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 bg-gradient-to-br from-primary to-primary-container rounded-xl flex items-center justify-center shadow-md shrink-0">
          <span class="material-symbols-outlined text-sm text-white" style="font-variation-settings: 'FILL' 1;">menu_book</span>
        </div>
        <transition name="fade-slide">
          <div v-if="!isCollapsed" class="overflow-hidden">
            <h1 class="text-lg font-bold text-on-surface tracking-tight leading-none">MxJin</h1>
            <p class="text-[10px] text-on-surface-variant tracking-wider">Mxjin's Blog</p>
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
                ? 'bg-primary/8 text-primary font-medium'
                : 'text-on-surface-variant hover:bg-surface-container-low hover:text-on-surface',
              isCollapsed ? 'justify-center px-0 w-12 mx-auto' : 'px-3'
            ]"
            :href="'#'"
            @click.prevent="item.hasChildren ? toggleGadgets() : navigate(item.path)"
          >
            <span
              class="material-symbols-outlined text-xl shrink-0 transition-colors duration-200"
              :class="isItemActive(item) && !item.hasChildren ? 'text-primary' : 'text-on-surface-variant group-hover:text-on-surface'"
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
            class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-6 bg-primary rounded-r-full"
          />
        </div>

        <!-- Sub-items (Gadgets) -->
        <transition name="expand">
          <div v-show="gadgetsOpen && !isCollapsed && item.hasChildren" class="ml-4 py-1 space-y-0.5">
            <a
              class="sub-item flex items-center gap-2 py-2 pl-3 pr-2 rounded-lg text-sm transition-all duration-200"
              :class="isActive('/gadgets/cyber-burning') ? 'text-primary font-medium bg-primary/5' : 'text-on-surface-variant hover:text-primary hover:bg-surface-container-low'"
              href="#"
              @click.prevent="navigate('/gadgets/cyber-burning')"
            >
              <span
                class="w-1.5 h-1.5 rounded-full shrink-0 transition-colors duration-200"
                :class="isActive('/gadgets/cyber-burning') ? 'bg-primary' : 'bg-outline'"
              />
              <span>赛博烧纸</span>
            </a>
          </div>
        </transition>
      </template>
    </nav>

    <!-- Bottom Section -->
    <div class="p-2 border-t border-outline-variant/10 space-y-1">
      <!-- Settings -->
      <a
        class="nav-item group flex items-center gap-3 py-2.5 rounded-xl transition-all duration-200"
        :class="[
          isActive('/settings') ? 'bg-primary/8 text-primary font-medium' : 'text-on-surface-variant hover:bg-surface-container-low hover:text-on-surface',
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
