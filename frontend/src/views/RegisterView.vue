<script setup lang="ts">
import { ref } from 'vue'
import type { Ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import GradientButton from '@/components/common/GradientButton.vue'
import { encrypt, getPublicKey } from '@/utils/crypto'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

interface RegisterForm {
  username: string
  email: string
  password: string
  confirmPassword: string
}

const form = ref<RegisterForm>({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const isLoading = ref<boolean>(false)
const error = ref<string>('')

const handleRegister = async (): Promise<void> => {
  error.value = ''

  if (!form.value.username || !form.value.email || !form.value.password) {
    ElMessage.error('请填写所有必填项')
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  if (form.value.password.length < 6) {
    ElMessage.error('密码长度至少为6位')
    return
  }

  if (!getPublicKey()) {
    ElMessage.error('加密密钥未加载，请刷新页面')
    return
  }

  isLoading.value = true

  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: form.value.username,
        email: form.value.email,
        encryptedPassword: encrypt(form.value.password)
      })
    })

    if (response.ok) {
      const data = await response.json()
      localStorage.setItem('accessToken', data.data.token)
      localStorage.setItem('refreshToken', data.data.refreshToken)
      await authStore.login({ email: form.value.email })
      router.push('/')
    } else {
      const data = await response.json()
      ElMessage.error(data.message || '注册失败')
    }
  } catch {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-background flex flex-col">
    <main class="flex-grow flex items-center justify-center px-6 py-20">
      <div class="w-full max-w-md bg-surface-container-lowest overflow-hidden rounded-xl border border-outline-variant/10 p-8 md:p-12 register-card">
        <header class="mb-10 text-center">
          <h1 class="text-3xl font-headline font-bold text-primary tracking-wide mb-3">MxJin</h1>
          <p class="text-on-surface-variant text-sm font-light">开启您的思想记录之旅</p>
        </header>

        <form @submit.prevent="handleRegister" class="space-y-6">

          <div class="space-y-5">
            <div class="relative">
              <label class="block text-[11px] uppercase tracking-widest text-on-surface-variant mb-2 ml-1">用户名</label>
              <div class="relative group">
                <span class="material-symbols-outlined absolute left-0 top-1/2 -translate-y-1/2 text-outline group-focus-within:text-primary transition-colors duration-300">person</span>
                <input
                  v-model="form.username"
                  class="w-full bg-transparent border-none border-b-2 border-outline-variant/30 focus:ring-0 focus:border-primary pl-8 pb-3 text-sm transition-all duration-300 placeholder:text-outline/50"
                  placeholder="设置您的用户名"
                  type="text"
                />
              </div>
            </div>

            <div class="relative">
              <label class="block text-[11px] uppercase tracking-widest text-on-surface-variant mb-2 ml-1">邮箱</label>
              <div class="relative group">
                <span class="material-symbols-outlined absolute left-0 top-1/2 -translate-y-1/2 text-outline group-focus-within:text-primary transition-colors duration-300">mail</span>
                <input
                  v-model="form.email"
                  class="w-full bg-transparent border-none border-b-2 border-outline-variant/30 focus:ring-0 focus:border-primary pl-8 pb-3 text-sm transition-all duration-300 placeholder:text-outline/50"
                  placeholder="输入您的邮箱地址"
                  type="email"
                />
              </div>
            </div>

            <div class="relative">
              <label class="block text-[11px] uppercase tracking-widest text-on-surface-variant mb-2 ml-1">密码</label>
              <div class="relative group">
                <span class="material-symbols-outlined absolute left-0 top-1/2 -translate-y-1/2 text-outline group-focus-within:text-primary transition-colors duration-300">lock</span>
                <input
                  v-model="form.password"
                  class="w-full bg-transparent border-none border-b-2 border-outline-variant/30 focus:ring-0 focus:border-primary pl-8 pb-3 text-sm transition-all duration-300 placeholder:text-outline/50"
                  placeholder="设置您的密码"
                  type="password"
                />
              </div>
            </div>

            <div class="relative">
              <label class="block text-[11px] uppercase tracking-widest text-on-surface-variant mb-2 ml-1">确认密码</label>
              <div class="relative group">
                <span class="material-symbols-outlined absolute left-0 top-1/2 -translate-y-1/2 text-outline group-focus-within:text-primary transition-colors duration-300">lock</span>
                <input
                  v-model="form.confirmPassword"
                  class="w-full bg-transparent border-none border-b-2 border-outline-variant/30 focus:ring-0 focus:border-primary pl-8 pb-3 text-sm transition-all duration-300 placeholder:text-outline/50"
                  placeholder="再次输入密码"
                  type="password"
                />
              </div>
            </div>
          </div>

          <GradientButton label="立即注册" type="submit" class="w-full" :disabled="isLoading" />
        </form>

        <div class="mt-10 text-center">
          <p class="text-sm text-on-surface-variant font-light">
            已有账号？
            <router-link class="text-primary font-medium hover:underline underline-offset-4 ml-1 transition-all" to="/login">立即登录</router-link>
          </p>
        </div>
      </div>
    </main>

    <footer class="w-full py-12 border-t border-slate-100">
      <div class="max-w-[1080px] mx-auto px-6 flex flex-col md:flex-row justify-between items-center gap-4">
        <div class="flex flex-col items-center md:items-start gap-1">
          <span class="font-bold text-slate-700">MxJin</span>
          <p class="text-sm leading-relaxed text-slate-500">MxJin</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.register-card {
  box-shadow: 0 40px 100px -20px rgba(25, 28, 30, 0.04);
}
</style>
