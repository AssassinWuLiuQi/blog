<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

interface Props {
  sectionTitle?: string
}

withDefaults(defineProps<Props>(), {
  sectionTitle: '首页'
})

const authStore = useAuthStore()
const router = useRouter()
const showDropdown = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const closeDropdown = () => {
  showDropdown.value = false
}

const handleLogout = () => {
  authStore.logout()
  showDropdown.value = false
  router.push({ name: 'login' })
}

const handleClickOutside = (e: MouseEvent) => {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target as Node)) {
    closeDropdown()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <header class="bg-white/80 backdrop-blur-md border-b border-outline-variant/10 transition-all duration-300 flex items-center justify-between px-6">
    <!-- Left: Section Title -->
    <div class="flex items-center gap-3">
      <h2 class="text-lg font-semibold text-on-surface tracking-tight">{{ sectionTitle }}</h2>
    </div>

    <!-- Right: User Profile -->
    <div class="flex items-center gap-4">
      <!-- User Profile -->
      <div class="relative" ref="dropdownRef">
        <button
          class="flex items-center gap-3 p-1.5 rounded-xl hover:bg-surface-container-low transition-colors duration-200"
          @click="toggleDropdown"
        >
          <div class="text-right hidden sm:block">
            <p class="text-sm font-medium text-on-surface leading-none">{{ authStore.userName }}</p>
            <p class="text-xs text-on-surface-variant mt-0.5">{{ authStore.userRole || '用户' }}</p>
          </div>
          <div class="w-9 h-9 rounded-full bg-gradient-to-br from-primary to-primary-container flex items-center justify-center text-white text-sm font-medium shadow-sm">
            {{ authStore.userName?.charAt(0).toUpperCase() || 'U' }}
          </div>
        </button>

        <!-- Dropdown Menu -->
        <transition name="dropdown">
          <div
            v-if="showDropdown"
            class="absolute right-0 top-full mt-2 w-56 bg-white rounded-xl shadow-xl border border-outline-variant/10 py-1.5 z-50"
          >
            <!-- User Info Header -->
            <div class="px-4 py-3 border-b border-outline-variant/10">
              <p class="text-sm font-medium text-on-surface">{{ authStore.userName }}</p>
              <p class="text-xs text-on-surface-variant mt-0.5">{{ authStore.userRole || '用户' }}</p>
            </div>

            <!-- Menu Items -->
            <div class="py-1">
              <router-link
                to="/settings"
                class="flex items-center gap-3 px-4 py-2.5 text-sm text-on-surface hover:bg-surface-container-low transition-colors duration-150"
                @click="closeDropdown"
              >
                <span class="material-symbols-outlined text-lg text-on-surface-variant">settings</span>
                <span>账号设置</span>
              </router-link>
            </div>

            <!-- Divider -->
            <div class="border-t border-outline-variant/10 py-1">
              <button
                class="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-error hover:bg-error/5 transition-colors duration-150"
                @click="handleLogout"
              >
                <span class="material-symbols-outlined text-lg">logout</span>
                <span>退出登录</span>
              </button>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </header>
</template>

<style scoped>
/* Dropdown Animation */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}
</style>
