<script setup>
</script>

<template>
  <section class="flex-[3] flex flex-col bg-surface-container-lowest rounded-xl shadow-sm border border-outline-variant/10 overflow-hidden">
    <!-- Toolbar Slot -->
    <slot name="toolbar"></slot>

    <!-- Content Area -->
    <div class="p-12 overflow-y-auto chinese-manuscript text-on-surface">
      <slot name="content"></slot>
    </div>
  </section>
</template>

<style scoped>
.chinese-manuscript {
  line-height: 1.8;
  letter-spacing: 0.02em;
}
</style>
