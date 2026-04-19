# 夜间模式设计方案

## 概述

为 The Scholar's Manuscript 项目实现夜间模式，采用 CSS 变量 + 自动/手动切换机制。

## 设计决策

| 项目 | 决策 |
|------|------|
| 配色风格 | 柔和深色（类似 iOS/Material You） |
| 切换机制 | 自动检测系统偏好 + 手动切换 + localStorage 持久化 |
| 图标形式 | 太阳/月亮图标切换 |
| 持久化 | localStorage 保存用户偏好 |

## 主题状态流

```
启动 → 读取 localStorage('theme')
         ↓
    有值？→ 使用保存值
         ↓ 无
    检测系统偏好 (prefers-color-scheme)
         ↓
    应用主题 → 写入 <html data-theme="light|dark">
                    ↓
    通知所有 CSS 变量更新
```

## 需要修改的文件

### 1. `frontend/src/stores/ui.ts`

新增状态：
- `theme: 'light' | 'dark' | 'system'` — 主题偏好
- `effectiveTheme: 'light' | 'dark'` — 实际生效的主题

新增 action：
- `setTheme(theme)` — 设置主题并保存到 localStorage
- `initTheme()` — 初始化主题（启动时调用）
- `toggleTheme()` — 快速切换主题

### 2. `frontend/src/assets/main.css`

在 `:root` 后添加深色变量覆盖：

```css
[data-theme="dark"] {
  /* Surface colors */
  --surface: #1a1a1a;
  --surface-container-low: #242424;
  --surface-container: #2a2a2a;
  --surface-container-lowest: #1f1f1f;
  --surface-container-high: #363636;

  /* On Surface */
  --on-surface: #e8e8e8;
  --on-surface-variant: #a0a0a0;

  /* Primary - 保持品牌色但调亮 */
  --primary: #4a9eff;
  --primary-container: #0056b3;
  --on-primary: #ffffff;

  /* Tertiary */
  --tertiary: #ff8a5c;
  --tertiary-container: #5c2a15;
  --on-tertiary: #ffffff;

  /* Outline */
  --outline: #5a5a5a;
  --outline-variant: #3a3a3a;

  /* Glass effect */
  .glass {
    background-color: rgba(30, 30, 30, 0.85);
  }

  /* Element Plus overrides */
  .el-message {
    --el-message-bg-color: #2a2a2a;
    --el-message-border-color: #3a3a3a;
    --el-message-text-color: #e8e8e8;
  }
}
```

### 3. `frontend/src/components/layout/TopAppBar.vue`

在右上角添加主题切换按钮：
- 使用 Heroicons `sun` 和 `moon` 图标
- 点击切换主题
- 手动点击后保存到 localStorage

### 4. `frontend/src/views/SettingsView.vue`

修改现有的主题设置：
- 将本地 `theme` ref 替换为 `uiStore.theme`
- 添加 `watch` 监听变化，同步到全局状态
- 添加系统选项（跟随系统）

### 5. `frontend/src/main.ts`

在应用挂载时调用 `uiStore.initTheme()`

## 深色配色方案

| 变量 | 浅色值 | 深色值 |
|------|--------|--------|
| `--surface` | #f7f9fb | #1a1a1a |
| `--surface-container-low` | #eceef0 | #242424 |
| `--surface-container` | #eceef0 | #2a2a2a |
| `--surface-container-high` | #dde1e6 | #363636 |
| `--on-surface` | #191c1e | #e8e8e8 |
| `--on-surface-variant` | #424752 | #a0a0a0 |
| `--primary` | #003f87 | #4a9eff |
| `--outline` | #c2c6d4 | #5a5a5a |

## 第三方组件适配

### Element Plus
Element Plus 使用 CSS 变量，可以通过覆盖 `--el-*` 变量适配深色模式。需在 `[data-theme="dark"]` 中覆盖关键变量。

### Tiptap 编辑器
Tiptap 使用 CSS 变量控制颜色，需添加对应深色变量。

## 实施步骤

1. 修改 `ui.ts` — 添加主题状态和 action
2. 修改 `main.css` — 添加深色 CSS 变量
3. 修改 `main.ts` — 初始化主题
4. 修改 `TopAppBar.vue` — 添加切换按钮
5. 修改 `SettingsView.vue` — 对接全局状态
