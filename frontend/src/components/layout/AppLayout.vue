<script setup lang="ts">
import SideNavBar from './SideNavBar.vue'
import TopAppBar from './TopAppBar.vue'

interface Props {
  sectionTitle?: string
}

withDefaults(defineProps<Props>(), {
  sectionTitle: '首页'
})
</script>

<template>
  <div class="flex min-h-screen bg-surface">
    <!-- Sidebar -->
    <SideNavBar />

    <!-- Main Content Area -->
    <div class="flex-1 ml-64 flex flex-col">
      <!-- Top App Bar -->
      <TopAppBar :section-title="sectionTitle" />

      <!-- Page Content Slot -->
      <main class="flex-1 mt-16 overflow-hidden">
        <slot />
      </main>
    </div>
  </div>
</template>
