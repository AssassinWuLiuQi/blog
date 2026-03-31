# Frontend TypeScript 完全严格迁移实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将前端所有 .js 文件转为 .ts，.vue 文件使用 TypeScript，完全严格类型检查，零 any 类型。

**Architecture:** 渐进式迁移，从类型定义 → 工具类 → 组合式函数 → Store → 路由 → 组件 → 视图，保持每步可运行。

**Tech Stack:** TypeScript 5.x, vue-tsc, Vite (已有)

---

## 文件清单

### 新建类型文件 (5 files)
- `frontend/src/types/api.ts`
- `frontend/src/types/article.ts`
- `frontend/src/types/auth.ts`
- `frontend/src/types/tts.ts`
- `frontend/src/types/index.ts`

### 迁移文件 (26 files)
- `frontend/src/utils/api.js` → `frontend/src/utils/api.ts`
- `frontend/src/utils/sse.js` → `frontend/src/utils/sse.ts`
- `frontend/src/hooks/useTTS.js` → `frontend/src/hooks/useTTS.ts`
- `frontend/src/stores/auth.js` → `frontend/src/stores/auth.ts`
- `frontend/src/stores/post.js` → `frontend/src/stores/post.ts`
- `frontend/src/stores/ui.js` → `frontend/src/stores/ui.ts`
- `frontend/src/router/index.js` → `frontend/src/router/index.ts`
- `frontend/src/main.js` → `frontend/src/main.ts`
- `frontend/src/App.vue` (script → script setup lang="ts")
- 12 components/*.vue (script → script setup lang="ts")
- 7 views/*.vue (script → script setup lang="ts")

### 配置文件 (2 files)
- `frontend/tsconfig.json` (新建)
- `frontend/tsconfig.node.json` (新建)

---

## Task 1: 配置 TypeScript 环境

**Files:**
- Create: `frontend/tsconfig.json`
- Create: `frontend/tsconfig.node.json`
- Modify: `frontend/package.json`

- [ ] **Step 1: 安装依赖**

```bash
cd frontend
npm install typescript vue-tsc --save-dev
```

- [ ] **Step 2: 创建 tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "strictFunctionTypes": true,
    "strictBindCallApply": true,
    "strictPropertyInitialization": true,
    "noImplicitThis": true,
    "alwaysStrict": true,
    "noImplicitReturns": true,
    "forceConsistentCasingInFileNames": true
  },
  "include": ["src/**/*.ts", "src/**/*.tsx", "src/**/*.vue"]
}
```

- [ ] **Step 3: 创建 tsconfig.node.json**

```json
{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true
  },
  "include": ["vite.config.js"]
}
```

- [ ] **Step 4: 验证 TypeScript 配置**

```bash
cd frontend
npx tsc --noEmit
```
Expected: 无输出或仅有预期的类型错误

---

## Task 2: 创建类型定义文件

**Files:**
- Create: `frontend/src/types/api.ts`
- Create: `frontend/src/types/article.ts`
- Create: `frontend/src/types/auth.ts`
- Create: `frontend/src/types/tts.ts`
- Create: `frontend/src/types/index.ts`

- [ ] **Step 1: 创建 api.ts**

```typescript
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
```

- [ ] **Step 2: 创建 article.ts**

```typescript
export interface ArticleRequest {
  title: string
  content: string
  excerpt: string
  status: 'DRAFT' | 'PUBLISHED'
  categoryIds?: number[]
}

export interface ArticleResponse {
  id: number
  title: string
  excerpt: string
  status: string
  author: string
  createdAt: string
  publishedAt: string | null
  categories: string[]
  readTime: string
}

export interface ArticleDetailResponse extends ArticleResponse {
  content: string
  authorId: number
  updatedAt: string
}

export interface Post {
  id: number
  title: string
  summary: string
  content: string
  category: string
  author: string
  date: string
  readTime: string
  featured: boolean
}

export interface Category {
  id: string
  name: string
  subItems: string[]
}
```

- [ ] **Step 3: 创建 auth.ts**

```typescript
export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  refreshToken: string
}

export interface User {
  id: number
  username: string
  email: string
  role: string
  avatar: string
}

export interface UserSettings {
  theme: 'light' | 'dark'
  fontSize: 'small' | 'medium' | 'large'
  autoPlayTTS: boolean
  voiceSpeed: number
}

export interface UserPreferences {
  theme: 'light' | 'dark'
  fontSize: 'small' | 'medium' | 'large'
  autoPlayTTS: boolean
  voiceSpeed: number
}
```

- [ ] **Step 4: 创建 tts.ts**

```typescript
export interface TtsRequest {
  text: string
  voiceId: string
  speed?: number
  vol?: number
  pitch?: number
  emotion?: string
  sampleRate?: number
  bitrate?: number
  format?: string
  channel?: number
}

export interface VoiceResponse {
  voiceId: string
  voiceName: string
  description: string[]
  createdTime: string
  type: string
}

export interface SseMessage {
  step: number
  message?: string
  data?: string
}

export interface TTSOptions {
  sampleRate?: number
  bufferSize?: number
  channels?: number
}
```

- [ ] **Step 5: 创建 index.ts**

```typescript
export * from './api'
export * from './article'
export * from './auth'
export * from './tts'
```

- [ ] **Step 6: 提交**

```bash
git add frontend/src/types/ frontend/tsconfig.json frontend/tsconfig.node.json
git commit -m "feat(frontend): add TypeScript configuration and type definitions"
```

---

## Task 3: 迁移 utils (api.ts, sse.ts)

**Files:**
- Modify: `frontend/src/utils/api.js` → `frontend/src/utils/api.ts`
- Modify: `frontend/src/utils/sse.js` → `frontend/src/utils/sse.ts`

- [ ] **Step 1: 迁移 api.ts**

```typescript
export function authHeaders(): Record<string, string> {
  const token = localStorage.getItem('accessToken')
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

export async function fetchWithAuth(url: string, options: RequestInit = {}): Promise<Response> {
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      ...authHeaders()
    }
  })

  if (response.status === 403) {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    window.location.href = '/login'
  }

  return response
}
```

- [ ] **Step 2: 迁移 sse.ts**

```typescript
import { fetchEventSource } from '@microsoft/fetch-event-source'

function getAuthHeader(): Record<string, string> {
  const token = localStorage.getItem('accessToken')
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

interface SSEOptions {
  method?: string
  headers?: Record<string, string>
  body?: unknown
  onMessage?: (data: unknown) => void
  onOpen?: () => void
  onClose?: () => void
  onError?: (err: Error) => void
}

export function createSSEStream(url: string, options: SSEOptions = {}): AbortController {
  const {
    method = 'POST',
    headers = {},
    body,
    onMessage = () => { /* noop */ },
    onOpen = () => { console.log("sse open") },
    onClose = () => { /* noop */ },
    onError = () => { /* noop */ }
  } = options

  const ctrl = new AbortController()

  fetchEventSource(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeader(),
      ...headers
    },
    signal: ctrl.signal,
    body: JSON.stringify(body),
    onMessage: (msg: { data?: string }) => {
      if (msg.data) {
        try {
          const data = JSON.parse(msg.data)
          onMessage(data)
        } catch {
          onMessage(msg.data)
        }
      }
    },
    onOpen,
    onClose,
    onError
  })

  return ctrl
}

export function createSSERawStream(url: string, options: SSEOptions = {}): AbortController {
  const {
    method = 'POST',
    headers = {},
    body,
    onMessage = () => { /* noop */ },
    onOpen = () => { /* noop */ },
    onClose = () => { /* noop */ },
    onError = () => { /* noop */ }
  } = options

  const ctrl = new AbortController()

  fetchEventSource(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeader(),
      ...headers
    },
    signal: ctrl.signal,
    body: typeof body === 'string' ? body : JSON.stringify(body),
    onMessage,
    onOpen,
    onClose,
    onError
  })

  return ctrl
}
```

- [ ] **Step 3: 验证 utils 编译**

```bash
cd frontend && npx tsc --noEmit
```
Expected: 无错误

- [ ] **Step 4: 提交**

```bash
git add frontend/src/utils/api.ts frontend/src/utils/sse.ts
git rm frontend/src/utils/api.js frontend/src/utils/sse.js
git commit -m "feat(frontend): migrate utils to TypeScript"
```

---

## Task 4: 迁移 hooks (useTTS.ts)

**Files:**
- Modify: `frontend/src/hooks/useTTS.js` → `frontend/src/hooks/useTTS.ts`

- [ ] **Step 1: 迁移 useTTS.ts**

```typescript
import { ref, onUnmounted } from 'vue'
import { createSSERawStream } from '@/utils/sse'
import type { TTSOptions } from '@/types'

const STEP_INIT = 0
const STEP_DATA = 100
const STEP_ERROR = 400
const STEP_COMPLETE = 1000

export function useTTS(options: TTSOptions = {}) {
  const {
    sampleRate = 24000,
    bufferSize = 4096,
    channels = 1
  }: Required<TTSOptions> = {
    sampleRate: options.sampleRate ?? 24000,
    bufferSize: options.bufferSize ?? 4096,
    channels: options.channels ?? 1
  }

  const audioContext = ref<AudioContext | null>(null)
  const processor = ref<ScriptProcessorNode | null>(null)
  const pcmQueue = ref<Float32Array[]>([])
  const isPlaying = ref(false)
  const isStreamEnded = ref(false)
  const currentController = ref<AbortController | null>(null)

  const initAudio = (): void => {
    if (audioContext.value) return

    audioContext.value = new AudioContext({ sampleRate })

    processor.value = audioContext.value.createScriptProcessor(bufferSize, channels, channels)
    processor.value.connect(audioContext.value.destination)

    processor.value.onaudioprocess = (e: AudioProcessingEvent) => {
      const output = e.outputBuffer.getChannelData(0)

      if (pcmQueue.value.length === 0) {
        output.fill(0)
        return
      }

      let offset = 0
      while (offset < bufferSize && pcmQueue.value.length > 0) {
        const chunk = pcmQueue.value[0]
        const remaining = bufferSize - offset

        if (chunk.length <= remaining) {
          output.set(chunk, offset)
          offset += chunk.length
          pcmQueue.value.shift()
        } else {
          output.set(chunk.subarray(0, remaining), offset)
          pcmQueue.value[0] = chunk.subarray(remaining)
          offset += remaining
        }
      }
    }
  }

  const stop = (): void => {
    if (currentController.value) {
      currentController.value.abort()
      currentController.value = null
    }

    pcmQueue.value = []
    isPlaying.value = false
    isStreamEnded.value = false
  }

  const hexToFloat32 = (hexString: string, isLittleEndian = false): Float32Array => {
    const bytes = new Uint8Array(hexString.length / 2)
    for (let i = 0; i < hexString.length; i += 2) {
      bytes[i / 2] = parseInt(hexString.substr(i, 2), 16)
    }

    const int16 = new Int16Array(bytes.buffer)
    const float32 = new Float32Array(int16.length)

    if (isLittleEndian) {
      for (let i = 0; i < int16.length; i++) {
        float32[i] = int16[i] / 32768
      }
    } else {
      for (let i = 0; i < int16.length; i++) {
        const b0 = bytes[i * 2]
        const b1 = bytes[i * 2 + 1]
        int16[i] = (b0 << 8) | b1
        float32[i] = int16[i] / 32768
      }
    }

    return float32
  }

  const play = async (text: string, voiceId: string): Promise<void> => {
    console.log('TTS play called', { text, voiceId })

    stop()
    initAudio()

    if (audioContext.value?.state === 'suspended') {
      await audioContext.value.resume()
    }

    isPlaying.value = true

    currentController.value = createSSERawStream('/api/tts/speech', {
      body: { text, voiceId },
      onOpen: () => {
        console.log('TTS onOpen: 连接成功')
      },
      onMessage: (msg: unknown) => {
        console.log('TTS onMessage:', msg)
        const response = typeof msg === 'string' ? JSON.parse(msg) : msg as { step?: number; message?: string; data?: string }

        switch (response.step) {
          case STEP_INIT:
            console.log('TTS init:', response.message)
            break
          case STEP_DATA:
            if (response.data) {
              const float32 = hexToFloat32(response.data, false)
              pcmQueue.value.push(float32)
            }
            break
          case STEP_ERROR:
            console.error('TTS error:', response.message)
            break
          case STEP_COMPLETE:
            console.log('TTS complete')
            isStreamEnded.value = true
            break
        }
      },
      onClose: () => {
        console.log('TTS onClose')
        isStreamEnded.value = true
        waitForQueueEmpty()
      },
      onError: (err: Error) => {
        console.error('TTS stream error:', err)
        isStreamEnded.value = true
        stop()
      }
    })
  }

  const waitForQueueEmpty = (): void => {
    const check = (): void => {
      if (pcmQueue.value.length === 0) {
        stop()
      } else {
        setTimeout(check, 100)
      }
    }
    setTimeout(check, 100)
  }

  const pause = (): void => {
    if (audioContext.value?.state === 'running') {
      audioContext.value.suspend()
    }
    isPlaying.value = false
  }

  const resume = (): void => {
    if (audioContext.value?.state === 'suspended') {
      audioContext.value.resume()
    }
    isPlaying.value = true
  }

  onUnmounted(() => {
    stop()

    if (processor.value) {
      processor.value.disconnect()
      processor.value = null
    }

    if (audioContext.value) {
      audioContext.value.close()
      audioContext.value = null
    }
  })

  return {
    isPlaying,
    play,
    stop,
    pause,
    resume
  }
}
```

- [ ] **Step 2: 验证 hooks 编译**

```bash
cd frontend && npx tsc --noEmit
```
Expected: 无错误

- [ ] **Step 3: 提交**

```bash
git add frontend/src/hooks/useTTS.ts
git rm frontend/src/hooks/useTTS.js
git commit -m "feat(frontend): migrate useTTS hook to TypeScript"
```

---

## Task 5: 迁移 stores (auth.ts, post.ts, ui.ts)

**Files:**
- Modify: `frontend/src/stores/auth.js` → `frontend/src/stores/auth.ts`
- Modify: `frontend/src/stores/post.js` → `frontend/src/stores/post.ts`
- Modify: `frontend/src/stores/ui.js` → `frontend/src/stores/ui.ts`

- [ ] **Step 1: 迁移 auth.ts**

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserPreferences } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User>({
    id: 0,
    name: 'Scholar Admin',
    email: 'admin@scholar.com',
    role: 'Premium Access',
    avatar: 'account_circle'
  })

  const isPremium = ref(true)

  const preferences = ref<UserPreferences>({
    theme: 'light',
    fontSize: 'medium',
    autoPlayTTS: false,
    voiceSpeed: 1.0
  })

  const userName = computed(() => user.value?.name || 'Guest')
  const userRole = computed(() => user.value?.role || 'Free Access')

  function login(credentials: { username?: string; email?: string }): void {
    isAuthenticated.value = true
    user.value = {
      id: 0,
      name: credentials.username || 'Scholar Admin',
      email: credentials?.email || 'admin@scholar.com',
      role: 'Premium Access',
      avatar: 'account_circle'
    }
  }

  function logout(): void {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    user.value = { id: 0, name: '', email: '', role: '', avatar: '' }
  }

  function updatePreferences(newPrefs: Partial<UserPreferences>): void {
    preferences.value = { ...preferences.value, ...newPrefs }
  }

  return {
    user,
    isPremium,
    preferences,
    userName,
    userRole,
    login,
    logout,
    updatePreferences
  }
})

export const isAuthenticated = (): boolean => !!localStorage.getItem('accessToken')
```

- [ ] **Step 2: 迁移 post.ts**

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Post, Category } from '@/types'

export const usePostStore = defineStore('post', () => {
  const posts = ref<Post[]>([
    {
      id: 1,
      title: '深度学习在自然语言处理中的应用',
      summary: '本文探讨了深度学习技术在自然语言处理领域的重要进展，包括Transformer架构和注意力机制的核心原理。',
      content: '深度学习已彻底改变了自然语言处理的面貌。',
      category: 'AI',
      author: 'Scholar Admin',
      date: '2026-03-28',
      readTime: '8分钟',
      featured: true
    },
    {
      id: 2,
      title: 'Vue 3 Composition API 最佳实践',
      summary: '深入理解Vue 3的Composition API和响应式系统。',
      content: 'Vue 3引入的Composition API是一种全新的逻辑组织方式。',
      category: '前端',
      author: 'Scholar Admin',
      date: '2026-03-27',
      readTime: '12分钟',
      featured: false
    }
  ])

  const categories = ref<Category[]>([
    { id: 'all', name: '全部', subItems: [] },
    { id: 'frontend', name: '前端', subItems: ['Vue', 'React', 'TypeScript'] },
    { id: 'backend', name: '后端', subItems: ['Node.js', 'Python', 'Go'] },
    { id: 'architecture', name: '架构', subItems: ['微服务', '云原生', '设计模式'] },
    { id: 'ai', name: 'AI', subItems: ['深度学习', 'NLP', '计算机视觉'] },
    { id: 'database', name: '数据库', subItems: ['关系型', 'NoSQL', 'NewSQL'] }
  ])

  const recentPosts = computed(() => posts.value.slice(0, 5))

  const featuredPosts = computed(() => posts.value.filter(post => post.featured))

  function getPostById(id: number | string): Post | undefined {
    return posts.value.find(post => post.id === Number(id))
  }

  function getPostsByCategory(category: string): Post[] {
    if (!category || category === 'all') return posts.value
    return posts.value.filter(post => post.category.toLowerCase() === category.toLowerCase())
  }

  function searchPosts(query: string): Post[] {
    if (!query) return posts.value
    const lowerQuery = query.toLowerCase()
    return posts.value.filter(post =>
      post.title.toLowerCase().includes(lowerQuery) ||
      post.summary.toLowerCase().includes(lowerQuery)
    )
  }

  return {
    posts,
    categories,
    recentPosts,
    featuredPosts,
    getPostById,
    getPostsByCategory,
    searchPosts
  }
})
```

- [ ] **Step 3: 迁移 ui.ts**

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { VoiceResponse } from '@/types'

interface Notification {
  message: string
  type: string
}

export const useUIStore = defineStore('ui', () => {
  const sidebarOpen = ref(true)
  const currentSection = ref('tech-preview')
  const currentCategory = ref('all')
  const isLoading = ref(false)
  const notification = ref<Notification | null>(null)

  const ttsPlaying = ref(false)
  const ttsRate = ref(1.0)
  const ttsVoice = ref<VoiceResponse | null>(null)

  function toggleSidebar(): void {
    sidebarOpen.value = !sidebarOpen.value
  }

  function setSection(section: string): void {
    currentSection.value = section
  }

  function setCategory(category: string): void {
    currentCategory.value = category
  }

  function setLoading(loading: boolean): void {
    isLoading.value = loading
  }

  function showNotification(message: string, type = 'info'): void {
    notification.value = { message, type }
    setTimeout(() => {
      notification.value = null
    }, 3000)
  }

  function setTTSPlaying(playing: boolean): void {
    ttsPlaying.value = playing
  }

  function setTTSRate(rate: number): void {
    ttsRate.value = rate
  }

  return {
    sidebarOpen,
    currentSection,
    currentCategory,
    isLoading,
    notification,
    ttsPlaying,
    ttsRate,
    ttsVoice,
    toggleSidebar,
    setSection,
    setCategory,
    setLoading,
    showNotification,
    setTTSPlaying,
    setTTSRate
  }
})
```

- [ ] **Step 4: 验证 stores 编译**

```bash
cd frontend && npx tsc --noEmit
```
Expected: 无错误

- [ ] **Step 5: 提交**

```bash
git add frontend/src/stores/auth.ts frontend/src/stores/post.ts frontend/src/stores/ui.ts
git rm frontend/src/stores/auth.js frontend/src/stores/post.js frontend/src/stores/ui.js
git commit -m "feat(frontend): migrate stores to TypeScript"
```

---

## Task 6: 迁移 router (index.ts)

**Files:**
- Modify: `frontend/src/router/index.js` → `frontend/src/router/index.ts`

- [ ] **Step 1: 迁移 router/index.ts**

```typescript
import { createRouter, createWebHistory, type Router, type RouteRecordRaw } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import TechPreviewView from '../views/TechPreviewView.vue'
import ArticleView from '../views/ArticleView.vue'
import ArchiveView from '../views/ArchiveView.vue'
import SettingsView from '../views/SettingsView.vue'
import { useAuthStore } from '../stores/auth'
import { isAuthenticated } from '../stores/auth'

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
```

- [ ] **Step 2: 验证 router 编译**

```bash
cd frontend && npx tsc --noEmit
```
Expected: 无错误

- [ ] **Step 3: 提交**

```bash
git add frontend/src/router/index.ts
git rm frontend/src/router/index.js
git commit -m "feat(frontend): migrate router to TypeScript"
```

---

## Task 7: 迁移 main.js → main.ts + App.vue

**Files:**
- Modify: `frontend/src/main.js` → `frontend/src/main.ts`
- Modify: `frontend/src/App.vue`

- [ ] **Step 1: 迁移 main.ts**

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
```

- [ ] **Step 2: 迁移 App.vue**

将 `<script>` 改为 `<script setup lang="ts">`，保持逻辑不变。

- [ ] **Step 3: 验证编译**

```bash
cd frontend && npx tsc --noEmit
```
Expected: 无错误

- [ ] **Step 4: 提交**

```bash
git add frontend/src/main.ts frontend/src/App.vue
git rm frontend/src/main.js
git commit -m "feat(frontend): migrate main entry and App.vue to TypeScript"
```

---

## Task 8: 迁移 components (12 files)

**Files:** (script → script setup lang="ts")

- `frontend/src/components/common/SearchInput.vue`
- `frontend/src/components/common/SurfaceCard.vue`
- `frontend/src/components/common/GradientButton.vue`
- `frontend/src/components/editor/TextEditor.vue`
- `frontend/src/components/editor/RichTextEditor.vue`
- `frontend/src/components/layout/AppLayout.vue`
- `frontend/src/components/layout/SideNavBar.vue`
- `frontend/src/components/layout/TopAppBar.vue`
- `frontend/src/components/tts/VoiceProfile.vue`
- `frontend/src/components/tts/PlaybackControls.vue`
- `frontend/src/components/tts/TTSPanel.vue`
- `frontend/src/components/tts/TextAnalysis.vue`

- [ ] **Step 1: 逐个迁移 components**

每个组件：`<script>` → `<script setup lang="ts">`，为 props 和 emit 添加类型注解。

示例 GradientButton.vue:

```vue
<script setup lang="ts">
interface Props {
  label: string
  type?: 'button' | 'submit' | 'reset'
  disabled?: boolean
  class?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'button',
  disabled: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const handleClick = (event: MouseEvent): void => {
  if (!props.disabled) {
    emit('click', event)
  }
}
</script>
```

- [ ] **Step 2: 批量验证编译**

```bash
cd frontend && npx tsc --noEmit
```
Expected: 无错误

- [ ] **Step 3: 提交**

```bash
git add frontend/src/components/
git commit -m "feat(frontend): migrate all components to TypeScript"
```

---

## Task 9: 迁移 views (7 files)

**Files:**

- `frontend/src/views/HomeView.vue`
- `frontend/src/views/ArticleView.vue`
- `frontend/src/views/ArchiveView.vue`
- `frontend/src/views/SettingsView.vue`
- `frontend/src/views/LoginView.vue`
- `frontend/src/views/RegisterView.vue`
- `frontend/src/views/TechPreviewView.vue`

- [ ] **Step 1: 逐个迁移 views**

每个视图：`<script>` → `<script setup lang="ts">`，为 data、methods、computed 添加类型注解。

- [ ] **Step 2: 批量验证编译**

```bash
cd frontend && npx tsc --noEmit
```
Expected: 无错误

- [ ] **Step 3: 提交**

```bash
git add frontend/src/views/
git commit -m "feat(frontend): migrate all views to TypeScript"
```

---

## Task 10: 最终验证

- [ ] **Step 1: 运行构建验证**

```bash
cd frontend
npm run build
```
Expected: 构建成功，无错误

- [ ] **Step 2: 运行开发服务器验证**

```bash
npm run dev
```
Expected: 开发服务器正常启动

- [ ] **Step 3: 最终提交**

```bash
git add -A
git commit -m "feat(frontend): complete TypeScript migration with strict mode"
```

---

## 验收标准

- [ ] `npx tsc --noEmit` 无错误
- [ ] `npm run build` 构建成功
- [ ] 所有 .js 文件已删除
- [ ] 所有 .vue 文件使用 `<script setup lang="ts">`
- [ ] 无 `any` 类型（搜索 `:\s*any` 无结果）
- [ ] 无 `as any` 类型断言
