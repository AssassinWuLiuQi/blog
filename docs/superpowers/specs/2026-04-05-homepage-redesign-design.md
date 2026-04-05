# 首页重新设计技术方案

## 概述

重新设计博客首页，突出技术相关内容和特色工具能力，采用三段式布局。

## 设计风格

- **主题**：技术感 + 学术气质
- **背景**：玻璃拟态（glassmorphism）增强层次感

## 页面结构

### Section 1: Hero Banner

| 属性 | 值 |
|------|------|
| 背景 | `bg-white/80 backdrop-blur-md` 玻璃拟态 |
| 内边距 | `p-8` |
| 布局 | 左侧文字区 + 右侧标签云 |

**左侧内容：**
- 主标题：「MxJin's Blog」
- 副标题：「技术沉淀与前沿探索」

**右侧内容：**
- 技术栈标签（无缝排列）
- 标签样式：`bg-primary/8 text-primary px-3 py-1 rounded-full text-sm`

**技术栈列表：** Vue3 · Spring Boot · MySQL · AI · TypeScript · 云原生

### Section 2: 特色工具入口

| 属性 | 值 |
|------|------|
| 布局 | 3列网格 `grid-cols-3` |
| 列间距 | `gap-6` |
| 内边距 | `p-8` |

**工具卡片：**

| 工具 | 图标 | 简介 |
|------|------|------|
| Tiptap 编辑器 | `edit_note` | 富文本编辑，支持 Markdown 导出 |
| TTS 语音合成 | `text_to_speech` | 文本转语音，多音色可选 |
| AI 图像生成 | `image` | 图像生成与风格迁移 |

**卡片样式：**
- 尺寸：高度固定 `h-48`
- 背景：`bg-surface-container-lowest rounded-xl`
- 边框：无边框，靠背景色区分
- 图标大小：`text-4xl`
- 文字：标题 `font-semibold`，简介 `text-sm text-on-surface-variant`

**悬停交互：**
- 悬停显示简介浮层（tooltip-style popover）
- 浮层样式：`bg-on-surface text-surface px-4 py-3 rounded-lg shadow-lg`
- 浮层内容：工具名 + 详细简介

### Section 3: 最新文章

| 属性 | 值 |
|------|------|
| 布局 | 3列网格 `grid-cols-1 md:grid-cols-2 lg:grid-cols-3` |
| 列间距 | `gap-6` |
| 内边距 | `p-8` |

**Section Header：**
- 标题：「最新文章」
- 右侧：「查看全部」按钮 → 跳转 `/archive`

**文章卡片（SurfaceCard）：**
- 分类标签：`<span class="text-xs text-primary font-medium mb-2">{{ post.category }}</span>`
- 标题：`.line-clamp-2` 限制2行
- 摘要：`.line-clamp-3` 限制3行
- 底部：日期 + 阅读时长
- 悬停：`hover:shadow-md transition-shadow cursor-pointer`

## 组件清单

| 组件 | 复用/新建 | 说明 |
|------|----------|------|
| SurfaceCard | 复用 | 现有组件 |
| GradientButton | 复用 | 现有组件 |
| TechTag | 新建 | 技术栈标签原子组件 |
| ToolCard | 新建 | 工具入口卡片组件 |
| PostCard | 新建 | 文章卡片（基于 SurfaceCard 封装） |
| HeroBanner | 新建 | Hero 区容器组件 |

## 文件变更

```
frontend/src/views/HomeView.vue          # 重构
frontend/src/components/common/TechTag.vue     # 新建
frontend/src/components/common/ToolCard.vue    # 新建
frontend/src/components/common/PostCard.vue    # 新建
frontend/src/components/common/HeroBanner.vue  # 新建
```

## 实现顺序

1. 新建原子组件：TechTag、ToolCard、PostCard、HeroBanner
2. 重构 HomeView.vue 组合各区块
3. 添加悬停浮层交互
4. 测试响应式布局
