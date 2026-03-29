# 前端技术架构文档

## 1. 项目概述

- **项目名称**: The Scholar's Manuscript（学者手稿）
- **项目类型**: Vue 3 博客平台
- **技术栈**: Vue 3 + Vite + Tailwind CSS + Pinia + Vue Router

## 2. 技术架构

### 2.1 核心框架

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4.31 | 核心框架 |
| Vite | 5.4.0 | 构建工具 |
| Vue Router | 4.4.3 | 路由管理 |
| Pinia | 2.2.2 | 状态管理 |

### 2.2 UI 与样式

| 技术 | 版本 | 用途 |
|------|------|------|
| Tailwind CSS | 3.4.14 | CSS 框架 |
| @tailwindcss/typography | 0.5.19 | 文章排版 |
| Material Symbols | - | 图标库 |

### 2.3 富文本编辑器

| 技术 | 版本 | 用途 |
|------|------|------|
| @tiptap/vue-3 | 3.21.0 | Tiptap Vue 集成 |
| @tiptap/starter-kit | 3.21.0 | 基础扩展包 |
| @tiptap/extension-placeholder | 3.21.0 | 占位符扩展 |
| @tiptap/extension-text-style | 3.21.0 | 文字样式扩展 |
| @tiptap/extension-color | 3.21.0 | 文字颜色扩展 |
| @tiptap/extension-text-align | 3.21.0 | 文本对齐扩展 |

## 3. 目录结构

```
frontend/
├── src/
│   ├── components/
│   │   ├── common/           # 通用组件
│   │   │   ├── GradientButton.vue    # 渐变按钮
│   │   │   ├── SearchInput.vue       # 搜索输入框
│   │   │   └── SurfaceCard.vue       # 卡片组件
│   │   ├── editor/           # 编辑器相关
│   │   │   ├── RichTextEditor.vue    # 富文本编辑器
│   │   │   └── TextEditor.vue        # 文本编辑器
│   │   ├── layout/           # 布局组件
│   │   │   ├── AppLayout.vue         # 应用布局
│   │   │   ├── SideNavBar.vue        # 侧边导航栏
│   │   │   └── TopAppBar.vue         # 顶部导航栏
│   │   └── tts/              # TTS 相关
│   │       ├── PlaybackControls.vue   # 播放控制
│   │       ├── TextAnalysis.vue      # 文本分析
│   │       ├── TTSPanel.vue          # TTS 面板
│   │       └── VoiceProfile.vue      # 语音配置
│   ├── views/                # 页面视图
│   │   ├── HomeView.vue             # 首页
│   │   ├── LoginView.vue            # 登录页
│   │   ├── ArticleView.vue          # 文章详情页
│   │   ├── ArchiveView.vue          # 归档页
│   │   ├── SettingsView.vue         # 设置页
│   │   └── TechPreviewView.vue      # 技术预览页
│   ├── stores/               # Pinia 状态库
│   │   ├── auth.js                  # 认证状态
│   │   ├── post.js                  # 文章数据
│   │   └── ui.js                    # UI 状态
│   ├── router/
│   │   └── index.js                 # 路由配置
│   ├── App.vue                     # 根组件
│   └── main.js                     # 入口文件
├── index.html
├── vite.config.js
├── tailwind.config.js
└── package.json
```

## 4. 设计系统

### 4.1 颜色体系

```css
--primary: #003f87           /* 主色 - 深蓝 */
--primary-container: #0056b3 /* 主色容器 */
--surface: #f7f9fb           /* 表面背景 */
--surface-container-lowest: #ffffff  /* 最底层容器 */
--on-surface: #191c1e        /* 表面文字 */
--on-surface-variant: #424752 /* 表面变体文字 */
--tertiary: #722b00          /* 第三色 */
--outline: #c2c6d4           /* 边框色 */
```

### 4.2 字体

```css
font-family: Inter, "PingFang SC", "Noto Sans SC", sans-serif;
```

## 5. 已实现功能

### 5.1 页面功能

| 页面 | 路由 | 功能描述 |
|------|------|----------|
| 登录页 | `/login` | 用户登录，含表单验证 |
| 首页 | `/` | 展示最新文章卡片 |
| 技术预览 | `/tech-preview` | 技术分类展示，可按分类筛选 |
| 文章详情 | `/article/:id` | 文章内容阅读，含 TTS 朗读功能 |
| 归档页 | `/archive` | 按年份归档文章，支持搜索 |
| 设置页 | `/settings` | 用户偏好设置（主题、字体、语音） |

### 5.2 组件功能

| 组件 | 功能 |
|------|------|
| GradientButton | 渐变色按钮，支持点击反馈 |
| SearchInput | 带图标的搜索输入框 |
| SurfaceCard | 卡片容器，统一卡片样式 |
| RichTextEditor | 富文本编辑器，支持加粗、斜体、删除线、标题、代码块、引用、列表 |
| SideNavBar | 可展开/收起的侧边导航栏，带动画 |
| TopAppBar | 顶部应用栏 |
| TTSPanel | 语音朗读控制面板 |
| PlaybackControls | TTS 播放控制组件 |
| TextAnalysis | TTS 文本分析展示 |

### 5.3 状态管理

| Store | 职责 |
|-------|------|
| auth | 用户认证状态、登录/登出、用户信息、偏好设置 |
| post | 文章数据、分类、搜索、筛选 |
| ui | 侧边栏状态、当前区块、分类、加载状态、通知、TTS 状态 |

### 5.4 路由守卫

- 所有功能页面（`/` 路径）需要登录访问
- 未登录访问自动跳转到 `/login`
- 已登录访问 `/login` 自动跳转到首页

## 6. 路由配置

```javascript
routes: [
  { path: '/login', name: 'login', component: LoginView },
  { path: '/', name: 'home', component: HomeView, meta: { requiresAuth: true } },
  { path: '/tech-preview', name: 'tech-preview', component: TechPreviewView, meta: { requiresAuth: true } },
  { path: '/tech-preview/:category', name: 'tech-preview-category', component: TechPreviewView },
  { path: '/article/:id', name: 'article', component: ArticleView },
  { path: '/archive', name: 'archive', component: ArchiveView },
  { path: '/settings', name: 'settings', component: SettingsView }
]
```

## 7. 待完善功能

- [ ] 富文本编辑器的颜色和字体大小选择器（UI 已预留，逻辑待完善）
- [ ] 用户注册功能
- [ ] 文章增删改查 API 对接
- [ ] 真实 TTS 语音合成对接
- [ ] 深色模式支持
- [ ] 响应式布局优化
