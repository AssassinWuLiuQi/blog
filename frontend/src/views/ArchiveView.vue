<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Ref, ComputedRef } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { usePostStore } from '@/stores/post'
import SearchInput from '@/components/common/SearchInput.vue'
import SurfaceCard from '@/components/common/SurfaceCard.vue'
import GradientButton from '@/components/common/GradientButton.vue'
import type { Post } from '@/types'

const postStore = usePostStore()

const searchQuery = ref<string>('')
const selectedYear = ref<string>('all')

const allPosts = computed<Post[]>(() => postStore.posts)

// Group posts by year
const postsByYear = computed<Record<string, Post[]>>(() => {
  const grouped: Record<string, Post[]> = {}
  allPosts.value.forEach(post => {
    const year = post.date.split('-')[0]
    if (!grouped[year]) {
      grouped[year] = []
    }
    grouped[year].push(post)
  })
  return grouped
})

const filteredPosts = computed<Post[]>(() => {
  let posts = allPosts.value
  if (searchQuery.value) {
    posts = postStore.searchPosts(searchQuery.value)
  }
  return posts
})

const years = computed<string[]>(() => {
  return Object.keys(postsByYear.value).sort().reverse()
})
</script>

<template>
  <AppLayout section-title="归档">
    <div class="p-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-display-sm text-on-surface mb-2">文章归档</h1>
        <p class="text-body-md text-on-surface-variant">按时间线浏览所有文章</p>
      </div>

      <!-- Search -->
      <div class="flex gap-4 mb-8 max-w-xl">
        <SearchInput v-model="searchQuery" placeholder="搜索文章标题或内容..." class="flex-1" />
        <GradientButton label="搜索" />
      </div>

      <!-- Year Filter -->
      <div class="flex gap-2 mb-8">
        <button
          @click="selectedYear = 'all'"
          class="px-4 py-1.5 rounded-full text-sm transition-all"
          :class="selectedYear === 'all'
            ? 'bg-primary text-white'
            : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'"
        >
          全部
        </button>
        <button
          v-for="year in years"
          :key="year"
          @click="selectedYear = year"
          class="px-4 py-1.5 rounded-full text-sm transition-all"
          :class="selectedYear === year
            ? 'bg-primary text-white'
            : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'"
        >
          {{ year }}
        </button>
      </div>

      <!-- Posts List -->
      <div class="space-y-8">
        <div v-for="year in years" :key="year" v-show="selectedYear === 'all' || selectedYear === year">
          <h2 class="text-xl font-semibold text-primary mb-4 sticky top-0 bg-surface z-10 py-2">
            {{ year }}年
          </h2>
          <div class="space-y-4">
            <SurfaceCard
              v-for="post in postsByYear[year]"
              :key="post.id"
              class="cursor-pointer hover:shadow-md transition-shadow"
            >
              <div class="flex items-start justify-between gap-4">
                <div class="flex-1">
                  <div class="flex items-center gap-3 mb-2">
                    <span class="px-2 py-0.5 bg-primary/10 text-primary text-xs rounded">{{ post.category }}</span>
                    <span class="text-xs text-on-surface-variant">{{ post.date }}</span>
                  </div>
                  <h3 class="text-lg font-medium text-on-surface mb-2">{{ post.title }}</h3>
                  <p class="text-sm text-on-surface-variant line-clamp-2">{{ post.summary }}</p>
                </div>
                <div class="flex flex-col items-end text-xs text-on-surface-variant">
                  <span>{{ post.readTime }}</span>
                  <span class="mt-1">{{ post.author }}</span>
                </div>
              </div>
            </SurfaceCard>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="filteredPosts.length === 0" class="text-center py-12">
        <span class="material-symbols-outlined text-6xl text-on-surface-variant mb-4">search_off</span>
        <p class="text-lg text-on-surface-variant">没有找到相关文章</p>
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
