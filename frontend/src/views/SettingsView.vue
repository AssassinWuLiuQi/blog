<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import SurfaceCard from '@/components/common/SurfaceCard.vue'
import GradientButton from '@/components/common/GradientButton.vue'
import { encrypt, getPublicKey } from '@/utils/crypto'
import type { UserPreferences } from '@/types'

const authStore = useAuthStore()
const uiStore = useUIStore()

// Add computed to sync with uiStore
const themePreference = computed({
  get: () => uiStore.theme,
  set: (val: 'light' | 'dark' | 'system') => uiStore.setTheme(val)
})

const fontSize = ref<string>('medium')
const autoPlayTTS = ref<boolean>(false)
const voiceSpeed = ref<number>(1.0)

// Password change
const showPasswordForm = ref<boolean>(false)
const oldPassword = ref<string>('')
const newPassword = ref<string>('')
const confirmPassword = ref<string>('')
const passwordError = ref<string>('')
const isPasswordLoading = ref<boolean>(false)

const handleSave = (): void => {
  const preferences: UserPreferences = {
    theme: themePreference.value as 'light' | 'dark',
    fontSize: fontSize.value as 'small' | 'medium' | 'large',
    autoPlayTTS: autoPlayTTS.value,
    voiceSpeed: voiceSpeed.value
  }
  authStore.updatePreferences(preferences)
  uiStore.showNotification('设置已保存', 'success')
}

const handlePasswordChange = async (): Promise<void> => {
  passwordError.value = ''

  if (!oldPassword.value || !newPassword.value) {
    passwordError.value = '请填写所有密码字段'
    return
  }

  if (newPassword.value.length < 6) {
    passwordError.value = '新密码长度至少为6位'
    return
  }

  if (newPassword.value !== confirmPassword.value) {
    passwordError.value = '两次输入的密码不一致'
    return
  }

  if (!getPublicKey()) {
    passwordError.value = '加密密钥未加载，请刷新页面'
    return
  }

  isPasswordLoading.value = true

  try {
    const response = await fetch('/api/users/password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      },
      body: JSON.stringify({
        oldPassword: encrypt(oldPassword.value),
        newPassword: encrypt(newPassword.value)
      })
    })

    if (response.ok) {
      oldPassword.value = ''
      newPassword.value = ''
      confirmPassword.value = ''
      showPasswordForm.value = false
      uiStore.showNotification('密码修改成功', 'success')
    } else {
      const data = await response.json()
      passwordError.value = data.message || '密码修改失败'
    }
  } catch {
    passwordError.value = '网络错误，请稍后重试'
  } finally {
    isPasswordLoading.value = false
  }
}
</script>

<template>
  <div class="p-8 max-w-3xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-display-sm text-gray-900 dark:text-gray-100 mb-2">设置</h1>
        <p class="text-body-md text-gray-600 dark:text-gray-400">管理您的偏好设置</p>
      </div>

      <!-- User Settings -->
      <SurfaceCard class="mb-6">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">用户信息</h3>
        <div class="flex items-center gap-4 mb-4">
          <span class="material-symbols-outlined text-4xl text-gray-600 dark:text-gray-400">account_circle</span>
          <div>
            <p class="font-medium text-gray-900 dark:text-gray-100">{{ authStore.user?.name }}</p>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ authStore.user?.role }}</p>
          </div>
        </div>
        <GradientButton label="编辑资料" />
      </SurfaceCard>

      <!-- Appearance Settings -->
      <SurfaceCard class="mb-6">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">外观</h3>

        <div class="mb-4">
          <label class="text-sm text-gray-600 dark:text-gray-400 mb-2 block">主题</label>
          <div class="flex gap-3">
            <button
              @click="themePreference = 'light'"
              class="px-4 py-2 rounded-lg text-sm transition-all"
              :class="themePreference === 'light'
                ? 'bg-blue-800 dark:bg-blue-500 text-white'
                : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700'"
            >
              浅色
            </button>
            <button
              @click="themePreference = 'dark'"
              class="px-4 py-2 rounded-lg text-sm transition-all"
              :class="themePreference === 'dark'
                ? 'bg-blue-800 dark:bg-blue-500 text-white'
                : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700'"
            >
              深色
            </button>
            <!-- Add system option -->
            <button
              @click="themePreference = 'system'"
              class="px-4 py-2 rounded-lg text-sm transition-all"
              :class="themePreference === 'system'
                ? 'bg-blue-800 dark:bg-blue-500 text-white'
                : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700'"
            >
              跟随系统
            </button>
          </div>
        </div>

        <div>
          <label class="text-sm text-gray-600 dark:text-gray-400 mb-2 block">字体大小</label>
          <select
            v-model="fontSize"
            class="w-full px-4 py-2 bg-gray-100 dark:bg-gray-800 border-none rounded-lg text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-800/20 dark:focus:ring-blue-500/20"
          >
            <option value="small">小</option>
            <option value="medium">中</option>
            <option value="large">大</option>
          </select>
        </div>
      </SurfaceCard>

      <!-- Voice Settings -->
      <SurfaceCard class="mb-6">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">语音设置</h3>

        <div class="mb-4">
          <label class="flex items-center gap-3 cursor-pointer">
            <input
              type="checkbox"
              v-model="autoPlayTTS"
              class="w-5 h-5 accent-blue-800 dark:accent-blue-400"
            />
            <span class="text-sm text-gray-900 dark:text-gray-100">自动播放语音朗读</span>
          </label>
        </div>

        <div>
          <label class="text-sm text-gray-600 dark:text-gray-400 mb-2 block">语音速度: {{ voiceSpeed }}x</label>
          <input
            type="range"
            v-model="voiceSpeed"
            min="0.5"
            max="2"
            step="0.1"
            class="w-full accent-blue-800 dark:accent-blue-400"
          />
          <div class="flex justify-between text-xs text-gray-600 dark:text-gray-400 mt-1">
            <span>0.5x</span>
            <span>1x</span>
            <span>1.5x</span>
            <span>2x</span>
          </div>
        </div>
      </SurfaceCard>

      <!-- Notification Settings -->
      <SurfaceCard class="mb-6">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">通知</h3>

        <div class="space-y-3">
          <label class="flex items-center gap-3 cursor-pointer">
            <input type="checkbox" class="w-5 h-5 accent-blue-800 dark:accent-blue-400" checked />
            <span class="text-sm text-gray-900 dark:text-gray-100">新文章通知</span>
          </label>
          <label class="flex items-center gap-3 cursor-pointer">
            <input type="checkbox" class="w-5 h-5 accent-blue-800 dark:accent-blue-400" checked />
            <span class="text-sm text-gray-900 dark:text-gray-100">评论回复通知</span>
          </label>
          <label class="flex items-center gap-3 cursor-pointer">
            <input type="checkbox" class="w-5 h-5 accent-blue-800 dark:accent-blue-400" />
            <span class="text-sm text-gray-900 dark:text-gray-100">系统公告</span>
          </label>
        </div>
      </SurfaceCard>

      <!-- Password Change -->
      <SurfaceCard class="mb-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">修改密码</h3>
          <button
            v-if="!showPasswordForm"
            @click="showPasswordForm = true"
            class="text-sm text-blue-800 dark:text-blue-400 hover:underline"
          >
            修改密码
          </button>
          <button
            v-else
            @click="showPasswordForm = false; passwordError = ''"
            class="text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100"
          >
            取消
          </button>
        </div>

        <div v-if="showPasswordForm">
          <div v-if="passwordError" class="bg-red-600/10 text-red-600 text-sm px-4 py-3 rounded-lg mb-4">
            {{ passwordError }}
          </div>

          <div class="space-y-4">
            <div>
              <label class="text-sm text-gray-600 dark:text-gray-400 mb-2 block">当前密码</label>
              <input
                v-model="oldPassword"
                type="password"
                class="w-full px-4 py-2 bg-gray-100 dark:bg-gray-800 border-none rounded-lg text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-800/20 dark:focus:ring-blue-500/20"
                placeholder="输入当前密码"
              />
            </div>
            <div>
              <label class="text-sm text-gray-600 dark:text-gray-400 mb-2 block">新密码</label>
              <input
                v-model="newPassword"
                type="password"
                class="w-full px-4 py-2 bg-gray-100 dark:bg-gray-800 border-none rounded-lg text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-800/20 dark:focus:ring-blue-500/20"
                placeholder="输入新密码"
              />
            </div>
            <div>
              <label class="text-sm text-gray-600 dark:text-gray-400 mb-2 block">确认新密码</label>
              <input
                v-model="confirmPassword"
                type="password"
                class="w-full px-4 py-2 bg-gray-100 dark:bg-gray-800 border-none rounded-lg text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-800/20 dark:focus:ring-blue-500/20"
                placeholder="再次输入新密码"
              />
            </div>
            <div class="flex justify-end">
              <GradientButton
                label="确认修改"
                @click="handlePasswordChange"
                :disabled="isPasswordLoading"
              />
            </div>
          </div>
        </div>
      </SurfaceCard>

      <!-- Save Button -->
      <div class="flex justify-end gap-4">
        <GradientButton label="保存设置" @click="handleSave" />
      </div>
    </div>
</template>
