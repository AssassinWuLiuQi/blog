import { createRouter, createWebHistory, type Router, type RouteRecordRaw } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import { isAuthenticated } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  // Auth pages - flat (no layout)
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

  // App pages - nested under AppLayout
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
        meta: { sectionTitle: '首页' }
      },
      {
        path: 'tech-preview',
        name: 'tech-preview',
        component: () => import('@/views/TechPreviewView.vue'),
        meta: { sectionTitle: 'Editor-TTS' }
      },
      {
        path: 'tech-preview/:category',
        name: 'tech-preview-category',
        component: () => import('@/views/TechPreviewView.vue'),
        meta: { sectionTitle: 'Editor-TTS' }
      },
      {
        path: 'article/:id',
        name: 'article',
        component: () => import('@/views/ArticleView.vue'),
        meta: { sectionTitle: '文章' }
      },
      {
        path: 'archive',
        name: 'archive',
        component: () => import('@/views/ArchiveView.vue'),
        meta: { sectionTitle: '归档' }
      },
      {
        path: 'settings',
        name: 'settings',
        component: () => import('@/views/SettingsView.vue'),
        meta: { sectionTitle: '设置' }
      },
      {
        path: 'logs',
        name: 'logs',
        component: () => import('@/views/LogViewerView.vue'),
        meta: { sectionTitle: '操作日志' }
      },
      {
        path: 'gadgets/cyber-burning',
        name: 'cyber-burning',
        component: () => import('@/views/gadgets/CyberBurningView.vue'),
        meta: { sectionTitle: '赛博烧纸' }
      },
      {
        path: 'image-generation',
        name: 'image-generation',
        component: () => import('@/views/ImageGenerationView.vue'),
        meta: { sectionTitle: '图像生成' }
      },
      {
        path: 'chat',
        name: 'chat',
        component: () => import('@/views/ChatView.vue'),
        meta: { sectionTitle: '客服' }
      },
      {
        path: 'music-generation',
        name: 'music-generation',
        component: () => import('@/views/MusicGenerationView.vue'),
        meta: { sectionTitle: '音乐生成' }
      }
    ]
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
