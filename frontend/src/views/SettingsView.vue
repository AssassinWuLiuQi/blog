<script setup>
import { ref } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import SurfaceCard from '@/components/common/SurfaceCard.vue'
import GradientButton from '@/components/common/GradientButton.vue'

const authStore = useAuthStore()
const uiStore = useUIStore()

const theme = ref('light')
const fontSize = ref('medium')
const autoPlayTTS = ref(false)
const voiceSpeed = ref(1.0)

const handleSave = () => {
  authStore.updatePreferences({
    theme: theme.value,
    fontSize: fontSize.value,
    autoPlayTTS: autoPlayTTS.value,
    voiceSpeed: voiceSpeed.value
  })
  uiStore.showNotification('设置已保存', 'success')
}
</script>

<template>
  <AppLayout section-title="设置">
    <div class="p-8 max-w-3xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-display-sm text-on-surface mb-2">设置</h1>
        <p class="text-body-md text-on-surface-variant">管理您的偏好设置</p>
      </div>

      <!-- User Settings -->
      <SurfaceCard class="mb-6">
        <h3 class="text-lg font-semibold text-on-surface mb-4">用户信息</h3>
        <div class="flex items-center gap-4 mb-4">
          <span class="material-symbols-outlined text-4xl text-on-surface-variant">account_circle</span>
          <div>
            <p class="font-medium text-on-surface">{{ authStore.user?.name }}</p>
            <p class="text-sm text-on-surface-variant">{{ authStore.user?.role }}</p>
          </div>
        </div>
        <GradientButton label="编辑资料" />
      </SurfaceCard>

      <!-- Appearance Settings -->
      <SurfaceCard class="mb-6">
        <h3 class="text-lg font-semibold text-on-surface mb-4">外观</h3>

        <div class="mb-4">
          <label class="text-sm text-on-surface-variant mb-2 block">主题</label>
          <div class="flex gap-3">
            <button
              @click="theme = 'light'"
              class="px-4 py-2 rounded-lg text-sm transition-all"
              :class="theme === 'light'
                ? 'bg-primary text-white'
                : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'"
            >
              浅色
            </button>
            <button
              @click="theme = 'dark'"
              class="px-4 py-2 rounded-lg text-sm transition-all"
              :class="theme === 'dark'
                ? 'bg-primary text-white'
                : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'"
            >
              深色
            </button>
          </div>
        </div>

        <div>
          <label class="text-sm text-on-surface-variant mb-2 block">字体大小</label>
          <select
            v-model="fontSize"
            class="w-full px-4 py-2 bg-surface-container-low border-none rounded-lg text-on-surface focus:outline-none focus:ring-2 focus:ring-primary/20"
          >
            <option value="small">小</option>
            <option value="medium">中</option>
            <option value="large">大</option>
          </select>
        </div>
      </SurfaceCard>

      <!-- Voice Settings -->
      <SurfaceCard class="mb-6">
        <h3 class="text-lg font-semibold text-on-surface mb-4">语音设置</h3>

        <div class="mb-4">
          <label class="flex items-center gap-3 cursor-pointer">
            <input
              type="checkbox"
              v-model="autoPlayTTS"
              class="w-5 h-5 accent-primary"
            />
            <span class="text-sm text-on-surface">自动播放语音朗读</span>
          </label>
        </div>

        <div>
          <label class="text-sm text-on-surface-variant mb-2 block">语音速度: {{ voiceSpeed }}x</label>
          <input
            type="range"
            v-model="voiceSpeed"
            min="0.5"
            max="2"
            step="0.1"
            class="w-full accent-primary"
          />
          <div class="flex justify-between text-xs text-on-surface-variant mt-1">
            <span>0.5x</span>
            <span>1x</span>
            <span>1.5x</span>
            <span>2x</span>
          </div>
        </div>
      </SurfaceCard>

      <!-- Notification Settings -->
      <SurfaceCard class="mb-6">
        <h3 class="text-lg font-semibold text-on-surface mb-4">通知</h3>

        <div class="space-y-3">
          <label class="flex items-center gap-3 cursor-pointer">
            <input type="checkbox" class="w-5 h-5 accent-primary" checked />
            <span class="text-sm text-on-surface">新文章通知</span>
          </label>
          <label class="flex items-center gap-3 cursor-pointer">
            <input type="checkbox" class="w-5 h-5 accent-primary" checked />
            <span class="text-sm text-on-surface">评论回复通知</span>
          </label>
          <label class="flex items-center gap-3 cursor-pointer">
            <input type="checkbox" class="w-5 h-5 accent-primary" />
            <span class="text-sm text-on-surface">系统公告</span>
          </label>
        </div>
      </SurfaceCard>

      <!-- Save Button -->
      <div class="flex justify-end gap-4">
        <GradientButton label="保存设置" @click="handleSave" />
      </div>
    </div>
  </AppLayout>
</template>
