# Tailwind Dark Mode Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace CSS variable-based dark theme with native Tailwind `dark:` prefix mode.

**Architecture:** Convert all custom CSS variable colors (primary, surface, on-surface, etc.) to direct Tailwind color classes with `dark:` variants. Use `darkMode: 'media'` to follow system preference.

**Tech Stack:** Vue 3, Tailwind CSS 3.x, Vite, Pinia

---

## Scope Summary

| Category | Count | Scope |
|----------|-------|-------|
| bg- colors | 185 | 27 files |
| text- colors | 333 | 28 files |
| border- colors | 61 | 15 files |
| fill/stroke/ring/shadow/gradient | 84 | 23 files |
| **Total** | **663** | **35 files** |

---

## Task 1: Configure Tailwind for Dark Mode

**Files:**
- Modify: `frontend/tailwind.config.js`

- [ ] **Step 1: Change darkMode from 'class' to 'media'**

```javascript
// frontend/tailwind.config.js
export default {
  darkMode: 'media',  // was: 'class'
  // ... rest of config
}
```

- [ ] **Step 2: Remove custom color mappings that reference CSS variables**

Remove these from `theme.extend.colors` block since we'll use Tailwind defaults directly:
- `primary`, `primary-container`, `surface`, `background`
- `surface-container-low`, `surface-container`, `surface-container-lowest`, `surface-container-high`
- `on-surface`, `on-surface-variant`
- `tertiary`, `tertiary-container`, `on-tertiary`
- `outline`, `outline-variant`
- `error`, `success`

```javascript
// Keep only generic Tailwind-compatible colors in tailwind.config.js
export default {
  darkMode: 'media',
  content: ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        display: ['Inter', 'PingFang SC', 'Noto Sans SC', 'sans-serif'],
        body: ['Inter', 'PingFang SC', 'Noto Sans SC', 'sans-serif'],
      },
      spacing: {
        '18': '4.5rem',
        '24': '6rem',
      },
      borderRadius: {
        'sm': '0.125rem',
        'md': '0.375rem',
        'lg': '0.5rem',
      },
    },
  },
  plugins: [require('@tailwindcss/typography')],
}
```

---

## Task 2: Rewrite main.css - Remove CSS Variables

**Files:**
- Modify: `frontend/src/assets/main.css`

- [ ] **Step 1: Remove all `:root` CSS variable definitions** (lines 6-50)
- [ ] **Step 2: Remove all `.dark {}` CSS variable overrides** (lines 52-143)
- [ ] **Step 3: Keep base body styles but convert CSS variables to Tailwind**

Replace:
```css
/* REMOVE entire :root block */
/* REMOVE entire .dark block */
/* KEEP body styles converted */
body {
  font-family: var(--font-family-body);  /* Keep - uses system fonts */
  background-color: var(--surface);       /* CONVERT to bg-gray-50 */
  color: var(--on-surface);               /* CONVERT to text-gray-900 */
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
```

Converted to:
```css
body {
  font-family: var(--font-family-body);
  @apply bg-gray-50 text-gray-900;
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.dark body {
  @apply bg-gray-900 text-gray-100;
}
```

- [ ] **Step 4: Convert utility classes (text-display-*, text-body-*, text-label-*)**

These stay mostly the same, just verify CSS variables in colors are converted.

- [ ] **Step 5: Convert Element Plus component overrides**

Remove `.el-message`, `.el-collapse`, `.el-input__wrapper`, `.el-switch`, `.el-button` CSS variable references.

For dark mode, add `@layer components` with `dark:` variants:

```css
@layer components {
  /* Element Plus Dark Mode */
  .dark .el-message {
    --el-message-bg-color: theme('colors.gray.800');
    --el-message-border-color: theme('colors.gray.700');
    --el-message-text-color: theme('colors.gray.100');
  }

  .dark .el-input__wrapper {
    background-color: theme('colors.gray.800') !important;
  }

  .dark .el-input__inner {
    color: theme('colors.gray.100');
  }
}
```

**Note:** Element Plus has its own dark mode via `el-theme--dark` class. Consider adding that class to `<html>` when dark mode is active instead of manually overriding variables.

---

## Task 3: Update UI Store - Remove Theme Class Logic

**Files:**
- Modify: `frontend/src/stores/ui.ts`

- [ ] **Step 1: Remove `applyTheme()` function's classList.toggle('dark') logic**

The `darkMode: 'media'` handles dark mode automatically via `@media (prefers-color-scheme: dark)`.

```typescript
// REMOVE or comment out these lines in applyTheme():
function applyTheme(): void {
  effectiveTheme.value = getEffectiveTheme()
  if (typeof document !== 'undefined') {
    // REMOVE: document.documentElement.classList.toggle('dark', effectiveTheme.value === 'dark')
  }
}
```

- [ ] **Step 2: Optionally remove theme store entirely** (light/dark/system toggle in settings will no longer work with `darkMode: 'media'`)

If manual theme toggle is needed, you must use `darkMode: 'class'` and keep the class toggle logic.

---

## Task 4: Convert Vue Component Colors - Batch by Category

Since 663 color instances span 35 files, organize by file type for parallel work.

### Task 4a: Convert common/ layout components

**Files (12 files):**
- `components/common/SurfaceCard.vue` - bg-surface-container-lowest, border-outline-variant
- `components/common/GradientButton.vue` - bg-primary gradient, text-on-primary
- `components/common/TechTag.vue` - bg-primary/8, text-primary
- `components/common/HeroBanner.vue` - bg-surface, text-on-surface
- `components/common/ToolCard.vue` - multiple surface/primary colors
- `components/common/PostCard.vue` - surface colors
- `components/common/SearchInput.vue` - bg-surface, text-on-surface-variant
- `components/common/SvgIcon.vue`
- `components/layout/TopAppBar.vue` - bg-surface, text-on-surface
- `components/layout/SideNavBar.vue` - bg-surface-container-lowest, border-outline
- `components/layout/AppLayout.vue`

**Pattern:** `bg-surface` → `bg-gray-50 dark:bg-gray-900`

### Task 4b: Convert views (11 files)

**Files:**
- `views/HomeView.vue`
- `views/LoginView.vue`
- `views/RegisterView.vue`
- `views/ArticleView.vue`
- `views/ArchiveView.vue`
- `views/SettingsView.vue` - 59 class attrs, most complex
- `views/TechPreviewView.vue`
- `views/LogViewerView.vue`
- `views/ImageGenerationView.vue`
- `views/MusicGenerationView.vue`
- `views/ChatView.vue`
- `views/gadgets/CyberBurningView.vue`

### Task 4c: Convert tts components (5 files)

**Files:**
- `components/tts/TTSPanel.vue`
- `components/tts/PlaybackControls.vue`
- `components/tts/TextAnalysis.vue`
- `components/tts/VoiceProfile.vue`

### Task 4d: Convert editor components (2 files)

**Files:**
- `components/editor/TextEditor.vue`
- `components/editor/RichTextEditor.vue`

### Task 4e: Convert chat components (3 files)

**Files:**
- `components/chat/ChatWindow.vue`
- `components/chat/ChatMessage.vue`
- `components/chat/VoiceInput.vue`

### Task 4f: Convert music/ image/ gadgets components

**Files:**
- `components/music/MusicPanel.vue`
- `components/image/ImagePanel.vue`
- `components/gadgets/CyberBurning.vue`

---

## Task 5: Convert App.vue and SideNavBar.vue scrollbar

**Files:**
- Modify: `App.vue` - `background-color: var(--surface)` → `class="bg-gray-50 dark:bg-gray-900"`
- Modify: `SideNavBar.vue` - scrollbar CSS variables
- Modify: `LogViewerView.vue` - scrollbar CSS variables

---

## Task 6: Verify Build

- [ ] **Step 1: Run `npm run build` in frontend directory**

Expected: Build succeeds with no errors

- [ ] **Step 2: Test in browser**
- [ ] **Step 3: Verify dark mode works via OS preference**

---

## Color Mapping Reference

| CSS Variable | Light Value | Light Tailwind | Dark Value | Dark Tailwind |
|--------------|-------------|----------------|------------|---------------|
| --primary | #003f87 | blue-800 | #4a9eff | blue-500 |
| --primary-container | #0056b3 | blue-700 | #0056b3 | blue-700 |
| --on-primary | #ffffff | white | #ffffff | white |
| --surface | #f7f9fb | gray-50 | #1a1a1a | gray-900 |
| --surface-container-low | #eceef0 | gray-100 | #242424 | gray-800 |
| --surface-container | #eceef0 | gray-100 | #2a2a2a | gray-800 |
| --surface-container-lowest | #ffffff | white | #1f1f1a | gray-800 |
| --surface-container-high | #dde1e6 | gray-200 | #363636 | gray-700 |
| --on-surface | #191c1e | gray-900 | #e8e8e8 | gray-100 |
| --on-surface-variant | #424752 | gray-600 | #a0a0a0 | gray-400 |
| --tertiary | #722b00 | orange-800 | #ff8a5c | orange-400 |
| --tertiary-container | #ffdbcc | orange-100 | #5c2a15 | orange-900 |
| --on-tertiary | #ffffff | white | #ffffff | white |
| --outline | #c2c6d4 | gray-300 | #5a5a5a | gray-500 |
| --outline-variant | #c2c6d4 | gray-300 | #3a3a3a | gray-700 |
| --error | #dc2626 | red-600 | - | - |
| --success | #16a34a | green-600 | - | - |

---

## Notes

- **Element Plus Dark Mode:** Consider using Element Plus built-in dark mode (`import 'element-plus/theme-chalk/dark/css-vars.css'`) instead of manual overrides
- **Custom colors like `bg-primary/8`:** Convert to `bg-blue-800/20` (opacity syntax)
- **Gradient buttons:** `bg-gradient-to-r from-primary to-primary-container` → `bg-gradient-to-r from-blue-800 to-blue-700`

---

## Execution Options

**1. Subagent-Driven (recommended)** - Dispatch 4-6 parallel subagents, one per file batch (Task 4a through 4f), plus config tasks in parallel

**2. Inline Execution** - Execute tasks sequentially in this session

---

*Plan created: 2026-04-23*
