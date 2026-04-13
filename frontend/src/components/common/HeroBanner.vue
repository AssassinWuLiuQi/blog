<script setup lang="ts">
import TechTag from '@/components/common/TechTag.vue'

interface Props {
  techStack?: string[]
}

withDefaults(defineProps<Props>(), {
  techStack: () => ['Vue3', 'Spring Boot', 'MySQL', 'AI', 'TypeScript', '云原生'],
})
</script>

<template>
  <div
    class="bg-white/80 backdrop-blur-md p-8 rounded-xl mb-12"
  >
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-display-md font-bold text-on-surface">
          MxJin's Blog
        </h1>
        <p class="text-body-lg text-on-surface-variant mt-2">
          技术沉淀与前沿探索
        </p>
      </div>
      <div class="flex flex-wrap gap-2">
        <TechTag
          v-for="tech in techStack"
          :key="tech"
          :label="tech"
        />
      </div>
    </div>
  </div>
</template>
