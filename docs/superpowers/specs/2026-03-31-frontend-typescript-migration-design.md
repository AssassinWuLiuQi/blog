# Frontend TypeScript 完全严格迁移设计

## 概述

将前端从 JavaScript 迁移至 TypeScript，开启全部严格类型检查。

## 配置变更

### 依赖安装
```bash
npm install typescript vue-tsc --save-dev
```

### tsconfig.json 配置
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
  "include": ["src/**/*.ts", "src/**/*.tsx", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

## 类型定义

### 目录结构
```
frontend/src/types/
├── api.ts        # API 通用类型
├── article.ts    # 文章相关类型
├── auth.ts       # 认证相关类型
├── tts.ts        # TTS 相关类型
└── index.ts      # 统一导出
```

### api.ts
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

### auth.ts
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
```

### article.ts
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
```

### tts.ts
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
```

## 迁移顺序

1. **types/** - 定义所有共享类型
2. **utils/** - api.ts, sse.js
3. **hooks/** - useTTS.js
4. **stores/** - auth.js, post.js, ui.js
5. **router/** - index.js
6. **components/** - .vue 的 script 部分
7. **views/** - 页面组件

## 严格类型要求

- 禁用 `any`
- 禁止 `as any` 类型断言
- 可选属性用 `?:`
- API 响应结构全部定义 interface
- 函数参数和返回值类型明确
- Vue 组件 props 使用 `defineProps<T>` 泛型

## 迁移文件清单

### utils (2 files)
- `src/utils/api.js` → `src/utils/api.ts`
- `src/utils/sse.js` → `src/utils/sse.ts`

### hooks (1 file)
- `src/hooks/useTTS.js` → `src/hooks/useTTS.ts`

### stores (3 files)
- `src/stores/auth.js` → `src/stores/auth.ts`
- `src/stores/post.js` → `src/stores/post.ts`
- `src/stores/ui.js` → `src/stores/ui.ts`

### router (1 file)
- `src/router/index.js` → `src/router/index.ts`

### components + views (14 files)
- `.vue` 文件保留后缀，script 部分转为 TypeScript

## 预期结果

- 所有 `.js` 文件转为 `.ts`
- 所有 `.vue` 文件使用 `<script setup lang="ts">`
- 零 `any` 类型
- 完整类型覆盖
