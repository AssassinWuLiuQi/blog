<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import HeroBanner from '@/components/common/HeroBanner.vue'
import ToolCard from '@/components/common/ToolCard.vue'
import PostCard from '@/components/common/PostCard.vue'
import GradientButton from '@/components/common/GradientButton.vue'
import { usePostStore } from '@/stores/post'
import type { Post } from '@/types'

const router = useRouter()
const postStore = usePostStore()

const recentPosts = computed<Post[]>(() => postStore.recentPosts)
</script>

<template>
  <div class="p-8">
      <!-- Section 1: HeroBanner -->
      <HeroBanner />

      <!-- Section 2: Tool Cards -->
      <section class="mb-12">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-6">特色工具入口</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <ToolCard title="Tiptap 编辑器" icon="edit_note" description="富文本编辑，支持 Markdown 导出" />
          <ToolCard title="TTS 语音合成" icon="text_to_speech" description="文本转语音，多音色可选" />
          <ToolCard title="AI 图像生成" icon="image" description="图像生成与风格迁移" />
        </div>
      </section>

      <!-- Section 3: Post Cards -->
      <section>
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100">最新文章</h2>
          <GradientButton label="查看全部" @click="router.push('/archive')" />
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <PostCard
            v-for="post in recentPosts"
            :key="post.id"
            v-bind="post"
            @click="router.push(`/article/${post.id}`)"
          />
        </div>
      </section>
    </div>
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
