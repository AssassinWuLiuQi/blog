<script setup lang="ts">
import SideNavBar from './SideNavBar.vue'
import TopAppBar from './TopAppBar.vue'
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const sidebarCollapsed = ref(false)

const sectionTitle = computed(() => {
  return route.meta.sectionTitle as string || '首页'
})

const handleSidebarToggle = (collapsed: boolean) => {
  sidebarCollapsed.value = collapsed
}
</script>

<template>
  <div class="flex min-h-screen">
    <!-- Sidebar -->
    <SideNavBar
      class="fixed left-0 top-0 h-screen"
      @update:collapsed="handleSidebarToggle"
    />

    <!-- Main Container -->
    <div
      class="flex-1 flex flex-col transition-all duration-300 ease-in-out"
      :class="sidebarCollapsed ? 'ml-16' : 'ml-64'"
    >
      <!-- TopAppBar -->
      <TopAppBar class="shrink-0 h-16" :section-title="sectionTitle" />

      <!-- Page Content -->
      <main class="flex-1 overflow-auto">
        <router-view />
      </main>
    </div>
  </div>
</template>
