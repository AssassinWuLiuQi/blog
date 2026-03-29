<script setup>
import { computed } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { usePostStore } from '@/stores/post'
import SurfaceCard from '@/components/common/SurfaceCard.vue'
import GradientButton from '@/components/common/GradientButton.vue'

const postStore = usePostStore()

const recentPosts = computed(() => postStore.recentPosts)
</script>

<template>
  <AppLayout section-title="首页">
    <div class="p-8">
    <!-- Welcome Section -->
    <section class="mb-12">
      <h1 class="text-display-md text-on-surface mb-4">欢迎回来</h1>
      <p class="text-body-lg text-on-surface-variant">探索最新的技术文章和学术文献</p>
    </section>

    <!-- Recent Posts Section -->
    <section>
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-xl font-semibold text-on-surface">最新文章</h2>
        <GradientButton label="查看全部" />
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <SurfaceCard
          v-for="post in recentPosts"
          :key="post.id"
          class="hover:shadow-md transition-shadow cursor-pointer"
        >
          <div class="flex flex-col h-full">
            <span class="text-xs text-primary font-medium mb-2">{{ post.category }}</span>
            <h3 class="text-lg font-semibold text-on-surface mb-2 line-clamp-2">{{ post.title }}</h3>
            <p class="text-sm text-on-surface-variant mb-4 line-clamp-3 flex-1">{{ post.summary }}</p>
            <div class="flex items-center justify-between text-xs text-on-surface-variant">
              <span>{{ post.date }}</span>
              <span>{{ post.readTime }}</span>
            </div>
          </div>
        </SurfaceCard>
      </div>
    </section>
    </div>
  </AppLayout>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
