<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isCollapsed = ref(false)
const techPreviewOpen = ref(true)
const gadgetsOpen = ref(true)

const isActive = (path: string): boolean => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const navigate = (path: string): void => {
  router.push(path)
}

const toggleTechPreview = (): void => {
  techPreviewOpen.value = !techPreviewOpen.value
}

const toggleGadgets = (): void => {
  gadgetsOpen.value = !gadgetsOpen.value
}

const toggleCollapse = (): void => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<template>
  <aside
    class="relative flex-none h-screen flex flex-col py-6 gap-4 bg-white/80 backdrop-blur-md z-50 border-r border-outline-variant/10 transition-all duration-300 ease-in-out"
    :class="isCollapsed ? 'w-16' : 'w-64'"
  >
    <!-- Toggle Button -->
    <button
      class="absolute -right-3 top-6 w-6 h-6 bg-white border border-outline-variant/20 rounded-full flex items-center justify-center shadow-md hover:shadow-lg hover:scale-110 transition-all duration-200 z-10"
      @click="toggleCollapse"
    >
      <span
        class="material-symbols-outlined text-xs text-slate-500 transition-transform duration-300"
        :class="isCollapsed ? 'rotate-180' : ''"
      >chevron_right</span>
    </button>

    <!-- Logo Section -->
    <transition name="fade-slide">
      <div v-if="!isCollapsed" class="flex items-center gap-3 px-2">
        <div class="w-9 h-9 bg-primary rounded-lg flex items-center justify-center text-white shadow-md shrink-0">
          <span class="material-symbols-outlined text-sm" style="font-variation-settings: 'FILL' 1;">menu_book</span>
        </div>
        <div class="overflow-hidden">
          <h1 class="text-xl font-bold text-blue-900 tracking-tight leading-none whitespace-nowrap">Manuscript</h1>
          <p class="text-[9px] uppercase tracking-widest text-on-surface-variant font-medium mt-0.5 whitespace-nowrap">Technical Blog</p>
        </div>
      </div>
    </transition>

    <!-- Navigation -->
    <nav class="flex-1 overflow-y-auto px-2 space-y-1 scrollbar-hide">
      <!-- 首页 -->
      <div v-if="false" class="relative">
        <div v-if="isActive('/')" class="nav-indicator" />
        <a
          class="nav-item flex items-center gap-3 py-2.5 rounded-lg transition-colors duration-200"
          :class="[
            isActive('/') ? 'text-blue-800 font-medium bg-blue-50/70' : 'text-slate-500 hover:text-blue-900 hover:bg-slate-50/70',
            isCollapsed ? 'justify-center px-0 w-12 mx-auto' : 'px-3'
          ]"
          href="#"
          @click.prevent="navigate('/')"
        >
          <span class="material-symbols-outlined text-lg shrink-0" :style="isActive('/') ? 'font-variation-settings: \'FILL\' 1;' : ''">home</span>
          <transition name="fade-slide">
            <span v-if="!isCollapsed" class="font-medium text-sm whitespace-nowrap overflow-hidden">首页</span>
          </transition>
        </a>
      </div>

      <!-- 技术预览 -->
      <div v-if="false" class="relative">
        <div v-if="isActive('/tech-preview')" class="nav-indicator" />
        <a
          class="nav-item flex items-center gap-3 py-2.5 rounded-lg transition-colors duration-200 cursor-pointer"
          :class="[
            isActive('/tech-preview') ? 'text-blue-800 font-medium bg-blue-50/70' : 'text-slate-500 hover:text-blue-900 hover:bg-slate-50/70',
            isCollapsed ? 'justify-center px-0 w-12 mx-auto' : 'px-3'
          ]"
          href="#"
          @click.prevent="toggleTechPreview"
        >
          <span class="material-symbols-outlined text-lg shrink-0" :style="isActive('/tech-preview') ? 'font-variation-settings: \'FILL\' 1;' : ''">menu_book</span>
          <transition name="fade-slide">
            <div v-if="!isCollapsed" class="flex-1 flex items-center justify-between overflow-hidden">
              <span class="font-medium text-sm whitespace-nowrap">技术预览</span>
              <span
                class="material-symbols-outlined text-sm transition-transform duration-200"
                :class="techPreviewOpen ? 'rotate-180' : ''"
              >expand_more</span>
            </div>
          </transition>
        </a>

        <!-- Sub-items -->
        <transition name="expand">
          <div v-show="techPreviewOpen && !isCollapsed" class="ml-4 py-1 space-y-0.5 overflow-hidden">
            <a
              class="sub-item flex items-center gap-2 py-2 px-3 rounded-lg text-sm transition-colors duration-200"
              :class="isActive('/tech-preview/text') ? 'text-primary font-medium bg-primary/5' : 'text-slate-400 hover:text-primary hover:bg-slate-50'"
              href="#"
              @click.prevent="navigate('/tech-preview/text')"
            >
              <span class="w-1.5 h-1.5 rounded-full shrink-0" :class="isActive('/tech-preview/text') ? 'bg-primary' : 'bg-slate-300'" />
              <span class="whitespace-nowrap">文本处理</span>
            </a>
          </div>
        </transition>
      </div>

      <!-- 更多小工具 -->
      <div class="relative">
        <div v-if="isActive('/gadgets')" class="nav-indicator" />
        <a
          class="nav-item flex items-center gap-3 py-2.5 rounded-lg transition-colors duration-200 cursor-pointer"
          :class="[
            isActive('/gadgets') ? 'text-blue-800 font-medium bg-blue-50/70' : 'text-slate-500 hover:text-blue-900 hover:bg-slate-50/70',
            isCollapsed ? 'justify-center px-0 w-12 mx-auto' : 'px-3'
          ]"
          href="#"
          @click.prevent="toggleGadgets"
        >
          <span class="material-symbols-outlined text-lg shrink-0" :style="isActive('/gadgets') ? 'font-variation-settings: \'FILL\' 1;' : ''">widgets</span>
          <transition name="fade-slide">
            <div v-if="!isCollapsed" class="flex-1 flex items-center justify-between overflow-hidden">
              <span class="font-medium text-sm whitespace-nowrap">更多</span>
              <span
                class="material-symbols-outlined text-sm transition-transform duration-200"
                :class="gadgetsOpen ? 'rotate-180' : ''"
              >expand_more</span>
            </div>
          </transition>
        </a>

        <!-- Sub-items -->
        <transition name="expand">
          <div v-show="gadgetsOpen && !isCollapsed" class="ml-4 py-1 space-y-0.5 overflow-hidden">
            <a
              class="sub-item flex items-center gap-2 py-2 px-3 rounded-lg text-sm transition-colors duration-200"
              :class="isActive('/gadgets/cyber-burning') ? 'text-primary font-medium bg-primary/5' : 'text-slate-400 hover:text-primary hover:bg-slate-50'"
              href="#"
              @click.prevent="navigate('/gadgets/cyber-burning')"
            >
              <span class="w-1.5 h-1.5 rounded-full shrink-0" :class="isActive('/tech-preview/text') ? 'bg-primary' : 'bg-slate-300'" />
              <span class="whitespace-nowrap">赛博烧纸</span>
            </a>
          </div>
        </transition>
      </div>
    </nav>

    <!-- Settings Link -->
    <div class="pt-3 px-2 border-t border-outline-variant/10">
      <a
        class="nav-item flex items-center gap-3 py-2.5 rounded-lg transition-colors duration-200"
        :class="[
          isActive('/settings') ? 'text-blue-800 font-medium bg-blue-50/70' : 'text-slate-500 hover:text-blue-900 hover:bg-slate-50/70',
          isCollapsed ? 'justify-center px-0 w-12 mx-auto' : 'px-3'
        ]"
        href="#"
        @click.prevent="navigate('/settings')"
      >
        <span class="material-symbols-outlined text-lg shrink-0">settings</span>
        <transition name="fade-slide">
          <span v-if="!isCollapsed" class="font-medium text-sm whitespace-nowrap overflow-hidden">设置</span>
        </transition>
      </a>
    </div>
  </aside>
</template>

<style scoped>
/* 导航指示器 */
.nav-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 50%;
  background-color: #003f87;
  border-radius: 0 2px 2px 0;
}

/* 过渡动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

/* 子菜单展开 */
.expand-enter-active,
.expand-leave-active {
  transition: max-height 0.25s ease, opacity 0.2s ease;
  max-height: 200px;
}
.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

/* 隐藏滚动条 */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
