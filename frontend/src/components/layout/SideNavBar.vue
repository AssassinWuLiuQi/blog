<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const techPreviewOpen = ref(true)

const isActive = (path) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const navigate = (path) => {
  router.push(path)
}

const toggleTechPreview = () => {
  techPreviewOpen.value = !techPreviewOpen.value
}
</script>

<template>
  <aside class="fixed left-0 top-0 bottom-0 w-64 flex flex-col py-8 px-6 gap-6 bg-white/80 backdrop-blur-md z-50 border-r border-outline-variant/10">
    <!-- Logo Section -->
    <div class="flex items-center gap-3 mb-4">
      <div class="w-10 h-10 bg-primary rounded-xl flex items-center justify-center text-white shadow-lg shadow-primary/20">
        <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">menu_book</span>
      </div>
      <div>
        <h1 class="text-2xl font-bold text-blue-900 tracking-tight leading-none">Manuscript</h1>
        <p class="text-[10px] uppercase tracking-widest text-on-surface-variant font-medium mt-1">Technical Blog</p>
      </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 space-y-1">
      <!-- 首页 -->
      <div class="relative">
        <div v-if="isActive('/')" class="nav-indicator"></div>
        <a
          class="nav-item flex items-center gap-4 py-3 pl-5 rounded-lg transition-all duration-200"
          :class="isActive('/') ? 'text-blue-800 font-medium bg-blue-50/50' : 'text-slate-500 hover:text-blue-900 hover:bg-slate-50'"
          href="#"
          @click.prevent="navigate('/')"
        >
          <span class="material-symbols-outlined" :style="isActive('/') ? 'font-variation-settings: \'FILL\' 1;' : ''">home</span>
          <span class="font-medium">首页</span>
        </a>
      </div>

      <!-- 技术预览 (可展开) -->
      <div class="relative">
        <div v-if="isActive('/tech-preview')" class="nav-indicator"></div>
        <a
          class="nav-item flex items-center justify-between py-3 pl-4 rounded-lg transition-all duration-200 cursor-pointer"
          :class="isActive('/tech-preview') ? 'text-blue-800 font-medium bg-blue-50/50' : 'text-slate-500 hover:text-blue-900 hover:bg-slate-50'"
          href="#"
          @click.prevent="toggleTechPreview"
        >
          <div class="flex items-center gap-4">
            <span class="material-symbols-outlined" :style="isActive('/tech-preview') ? 'font-variation-settings: \'FILL\' 1;' : ''">menu_book</span>
            <span class="font-medium">技术预览</span>
          </div>
          <span
            class="material-symbols-outlined transition-transform duration-200"
            :class="techPreviewOpen ? 'rotate-180' : ''"
          >expand_more</span>
        </a>
        <!-- Sub-items (可展开) -->
        <transition name="expand">
          <div v-show="techPreviewOpen" class="pl-4 py-2 space-y-1 overflow-hidden">
            <a
              class="sub-item flex items-center gap-3 py-2 px-3 rounded-lg text-sm transition-all duration-200"
              :class="isActive('/tech-preview/text') ? 'text-primary font-bold bg-primary/5' : 'text-slate-400 hover:text-primary hover:bg-slate-50'"
              href="#"
              @click.prevent="navigate('/tech-preview/text')"
            >
              <span class="w-1.5 h-1.5 rounded-full transition-all duration-200" :class="isActive('/tech-preview/text') ? 'bg-primary' : 'bg-slate-300'"></span>
              文本处理
            </a>
          </div>
        </transition>
      </div>
    </nav>

    <!-- Settings Link at Bottom -->
    <div class="pt-6 border-t border-outline-variant/10 relative">
      <div v-if="isActive('/settings')" class="nav-indicator"></div>
      <a
        class="nav-item flex items-center gap-4 py-3 pl-5 rounded-lg transition-all duration-200"
        :class="isActive('/settings') ? 'text-blue-800 font-medium bg-blue-50/50' : 'text-slate-500 hover:text-blue-900 hover:bg-slate-50'"
        href="#"
        @click.prevent="navigate('/settings')"
      >
        <span class="material-symbols-outlined">settings</span>
        <span class="font-medium">设置</span>
      </a>
    </div>
  </aside>
</template>

<style scoped>
.nav-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 60%;
  background-color: #003f87;
  border-radius: 0 4px 4px 0;
  animation: slideIn 0.3s ease-out;
}

.nav-item {
  transition: all 0.3s ease;
}

.sub-item {
  transition: all 0.2s ease;
}

@keyframes slideIn {
  from {
    height: 0;
    opacity: 0;
  }
  to {
    height: 60%;
    opacity: 1;
  }
}

/* 展开/收起动画 */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 200px;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
  padding-top: 0;
  padding-bottom: 0;
}
</style>
