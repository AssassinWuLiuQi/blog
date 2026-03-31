<script setup lang="ts">
interface Props {
  sectionTitle?: string
}

withDefaults(defineProps<Props>(), {
  sectionTitle: '首页'
})
</script>

<template>
  <header class="fixed top-0 right-0 left-64 flex justify-between items-center px-8 h-16 bg-white/80 backdrop-blur-md z-40 shadow-sm shadow-blue-900/5">
    <!-- Left: Section Title -->
    <div class="flex items-center gap-2">
      <span class="text-xl font-semibold text-blue-900 tracking-wide">{{ sectionTitle }}</span>
    </div>

    <!-- Right: Search + User Profile -->
    <div class="flex items-center gap-6">
      <!-- Search Input Slot -->
      <div class="relative group">
        <span class="material-symbols-outlined text-slate-500 absolute left-3 top-1/2 -translate-y-1/2 text-xl pointer-events-none">search</span>
        <input
          class="pl-10 pr-4 py-1.5 bg-surface-container-low border-none rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 w-64 transition-all"
          placeholder="搜索文献..."
          type="text"
        />
      </div>

      <!-- User Profile -->
      <div class="flex items-center gap-3 pl-6 border-l border-outline-variant/20">
        <div class="text-right hidden sm:block">
          <p class="text-xs font-bold text-on-surface leading-none">Scholar Admin</p>
          <p class="text-[10px] text-on-surface-variant mt-1">Premium Access</p>
        </div>
        <span class="material-symbols-outlined text-blue-800 text-3xl">account_circle</span>
      </div>
    </div>
  </header>
</template>
