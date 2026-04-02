<script setup lang="ts">
import AppLayout from '@/components/layout/AppLayout.vue'
import ImagePanel from '@/components/image/ImagePanel.vue'
</script>

<template>
  <AppLayout section-title="AI 图像生成">
    <div class="p-8 flex gap-8 flex-1 overflow-hidden h-[calc(100vh-8rem)]">
      <!-- Main Content Area -->
      <div class="flex-[3] flex flex-col items-center justify-center">
        <div class="text-center text-gray-500 dark:text-gray-400">
          <span class="material-symbols-outlined text-6xl mb-4">image_generation</span>
          <p class="text-lg">在右侧面板中输入描述词生成图片</p>
          <p class="text-sm mt-2">Generated images will appear below the input form</p>
        </div>
      </div>

      <!-- Right Column: Image Generation Panel -->
      <ImagePanel />
    </div>
  </AppLayout>
</template>
