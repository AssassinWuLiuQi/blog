<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Ref, ComputedRef } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import { usePostStore } from '@/stores/post'
import { useUIStore } from '@/stores/ui'
import SurfaceCard from '@/components/common/SurfaceCard.vue'
import GradientButton from '@/components/common/GradientButton.vue'
import type { Post } from '@/types'

const route = useRoute()
const postStore = usePostStore()
const uiStore = useUIStore()

const post = computed<Post | undefined>(() => postStore.getPostById(route.params.id as string))
const ttsPlaying = ref<boolean>(false)

const toggleTTS = (): void => {
  ttsPlaying.value = !ttsPlaying.value
  uiStore.setTTSPlaying(ttsPlaying.value)
}

const similarPosts = computed<Post[]>(() => {
  if (!post.value) return []
  return postStore.getPostsByCategory(post.value.category).filter(p => p.id !== post.value.id).slice(0, 3)
})
</script>

<template>
  <AppLayout section-title="文章详情">
    <div class="flex h-full" v-if="post">
      <!-- Article Content -->
      <div class="flex-[3] p-8 overflow-auto">
        <!-- Back Button -->
        <a href="#" @click.prevent="$router.back()" class="inline-flex items-center gap-2 text-on-surface-variant hover:text-primary mb-6 transition-colors">
          <span class="material-symbols-outlined">arrow_back</span>
          <span class="text-sm">返回</span>
        </a>

        <!-- Article Header -->
        <header class="mb-8">
          <div class="flex items-center gap-3 mb-4">
            <span class="px-3 py-1 bg-primary/10 text-primary text-sm rounded-full">{{ post.category }}</span>
            <span class="text-sm text-on-surface-variant">{{ post.readTime }}</span>
          </div>
          <h1 class="text-display-sm text-on-surface mb-4">{{ post.title }}</h1>
          <div class="flex items-center gap-4 text-sm text-on-surface-variant">
            <span class="font-medium text-on-surface">{{ post.author }}</span>
            <span>|</span>
            <span>{{ post.date }}</span>
          </div>
        </header>

        <!-- Article Body -->
        <article class="prose prose-lg max-w-none text-body-lg text-on-surface">
          <p class="mb-6 text-on-surface-variant leading-relaxed">{{ post.summary }}</p>
          <div class="whitespace-pre-line text-on-surface leading-loose">
            {{ post.content }}
          </div>
        </article>

        <!-- Similar Posts -->
        <section v-if="similarPosts.length > 0" class="mt-12">
          <h3 class="text-xl font-semibold text-on-surface mb-4">相关推荐</h3>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <SurfaceCard
              v-for="similar in similarPosts"
              :key="similar.id"
              class="cursor-pointer hover:shadow-md transition-shadow"
            >
              <h4 class="font-medium text-on-surface mb-2 line-clamp-2">{{ similar.title }}</h4>
              <p class="text-xs text-on-surface-variant">{{ similar.readTime }} | {{ similar.date }}</p>
            </SurfaceCard>
          </div>
        </section>
      </div>

      <!-- TTS Controls Sidebar -->
      <div class="flex-1 p-6 bg-surface-container-low border-l border-outline-variant">
        <h3 class="text-lg font-semibold text-on-surface mb-4">朗读控制</h3>

        <SurfaceCard class="mb-4">
          <div class="flex flex-col items-center gap-4">
            <button
              @click="toggleTTS"
              class="w-16 h-16 rounded-full flex items-center justify-center transition-all"
              :class="ttsPlaying
                ? 'bg-primary text-white'
                : 'bg-surface-container-high text-on-surface'"
            >
              <span class="material-symbols-outlined text-3xl">
                {{ ttsPlaying ? 'pause' : 'play_arrow' }}
              </span>
            </button>
            <span class="text-sm text-on-surface-variant">
              {{ ttsPlaying ? '正在朗读' : '点击开始' }}
            </span>
          </div>
        </SurfaceCard>

        <div class="mb-4">
          <label class="text-sm text-on-surface-variant mb-2 block">语速</label>
          <input
            type="range"
            min="0.5"
            max="2"
            step="0.1"
            :value="uiStore.ttsRate"
            @input="uiStore.setTTSRate($event.target.value)"
            class="w-full accent-primary"
          />
          <div class="flex justify-between text-xs text-on-surface-variant mt-1">
            <span>0.5x</span>
            <span>{{ uiStore.ttsRate }}x</span>
            <span>2x</span>
          </div>
        </div>

        <div class="mt-6">
          <GradientButton label="收藏文章" class="w-full" />
        </div>

        <div class="mt-4">
          <GradientButton label="分享" class="w-full" />
        </div>
      </div>
    </div>

    <!-- Loading/Not Found State -->
    <div v-else class="flex items-center justify-center h-full">
      <div class="text-center">
        <span class="material-symbols-outlined text-6xl text-on-surface-variant mb-4">article</span>
        <p class="text-lg text-on-surface-variant">文章不存在</p>
      </div>
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
</style>
