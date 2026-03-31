import { createRouter, createWebHistory, type Router, type RouteRecordRaw } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import TechPreviewView from '../views/TechPreviewView.vue'
import ArticleView from '../views/ArticleView.vue'
import ArchiveView from '../views/ArchiveView.vue'
import SettingsView from '../views/SettingsView.vue'
import { isAuthenticated } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView
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

const router: Router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !isAuthenticated()) {
    next({ name: 'login' })
  } else if ((to.name === 'login' || to.name === 'register') && isAuthenticated()) {
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router
