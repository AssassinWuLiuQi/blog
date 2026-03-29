import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import TechPreviewView from '../views/TechPreviewView.vue'
import ArticleView from '../views/ArticleView.vue'
import ArchiveView from '../views/ArchiveView.vue'
import SettingsView from '../views/SettingsView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/tech-preview',
      name: 'tech-preview',
      component: TechPreviewView,
      meta: { requiresAuth: true }
    },
    {
      path: '/tech-preview/:category',
      name: 'tech-preview-category',
      component: TechPreviewView,
      meta: { requiresAuth: true }
    },
    {
      path: '/article/:id',
      name: 'article',
      component: ArticleView,
      meta: { requiresAuth: true }
    },
    {
      path: '/archive',
      name: 'archive',
      component: ArchiveView,
      meta: { requiresAuth: true }
    },
    {
      path: '/settings',
      name: 'settings',
      component: SettingsView,
      meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'login' })
  } else if (to.name === 'login' && authStore.isAuthenticated) {
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router