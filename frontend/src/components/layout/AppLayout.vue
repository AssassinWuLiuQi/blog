<script setup lang="ts">
import SideNavBar from './SideNavBar.vue'
import TopAppBar from './TopAppBar.vue'
import { ref } from 'vue'

interface Props {
  sectionTitle?: string
}

withDefaults(defineProps<Props>(), {
  sectionTitle: '首页'
})

const sidebarCollapsed = ref(false)

const handleSidebarToggle = (collapsed: boolean) => {
  sidebarCollapsed.value = collapsed
}
</script>

<template>
  <div class="flex min-h-screen bg-background">
    <!-- Sidebar -->
    <SideNavBar @update:collapsed="handleSidebarToggle" />

    <!-- Main Content Area -->
    <div
      class="flex-1 flex flex-col transition-all duration-300 ease-in-out"
      :class="sidebarCollapsed ? 'ml-16' : 'ml-64'"
    >
      <!-- Top App Bar -->
      <TopAppBar :section-title="sectionTitle" />

      <!-- Page Content Slot -->
      <main class="flex-1 overflow-auto">
        <slot />
      </main>
    </div>
  </div>
</template>
