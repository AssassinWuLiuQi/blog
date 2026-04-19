# Dark Mode Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement dark mode with system auto-detection + manual toggle, persisted to localStorage.

**Architecture:** CSS custom properties (variables) with `[data-theme="dark"]` selector override. Theme state managed in Pinia `ui.ts` store, initialized at app startup. Manual toggle via TopAppBar button and SettingsView.

**Tech Stack:** Vue 3, Pinia, Tailwind CSS, Element Plus, Heroicons

---

## File Map

| File | Responsibility |
|------|----------------|
| `frontend/src/stores/ui.ts` | Theme state (`theme`, `effectiveTheme`), actions (`initTheme`, `setTheme`, `toggleTheme`) |
| `frontend/src/assets/main.css` | CSS variables for light + dark themes via `[data-theme="dark"]` selector |
| `frontend/src/main.ts` | Mount app, call `uiStore.initTheme()` |
| `frontend/src/components/layout/TopAppBar.vue` | Sun/Moon icon toggle button in top-right |
| `frontend/src/views/SettingsView.vue` | Theme preference selector wired to `uiStore.theme` |

---

## Task 1: Add theme state and actions to ui.ts

**Files:**
- Modify: `frontend/src/stores/ui.ts`

- [ ] **Step 1: Read current ui.ts to understand existing structure**

```typescript
// Current state at line 10-16:
const sidebarOpen: Ref<boolean> = ref(true)
const currentSection: Ref<string> = ref('tech-preview')
const currentCategory: Ref<string> = ref('all')
const isLoading: Ref<boolean> = ref(false)
const notification: Ref<Notification | null> = ref(null)
```

- [ ] **Step 2: Add theme state after existing state (around line 16)**

```typescript
// Theme State
const theme: Ref<'light' | 'dark' | 'system'> = ref('system')
const effectiveTheme: Ref<'light' | 'dark'> = ref('light')
```

- [ ] **Step 3: Add theme actions before the return statement (around line 53)**

```typescript
// Theme Actions
function getSystemTheme(): 'light' | 'dark' {
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }
  return 'light'
}

function getEffectiveTheme(): 'light' | 'dark' {
  if (theme.value === 'system') {
    return getSystemTheme()
  }
  return theme.value
}

function applyTheme(): void {
  effectiveTheme.value = getEffectiveTheme()
  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('data-theme', effectiveTheme.value)
  }
}

function initTheme(): void {
  const saved = localStorage.getItem('theme') as 'light' | 'dark' | 'system' | null
  if (saved && ['light', 'dark', 'system'].includes(saved)) {
    theme.value = saved
  }
  applyTheme()

  // Listen for system theme changes
  if (typeof window !== 'undefined' && window.matchMedia) {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
      if (theme.value === 'system') {
        applyTheme()
      }
    })
  }
}

function setTheme(newTheme: 'light' | 'dark' | 'system'): void {
  theme.value = newTheme
  localStorage.setItem('theme', newTheme)
  applyTheme()
}

function toggleTheme(): void {
  if (effectiveTheme.value === 'light') {
    setTheme('dark')
  } else {
    setTheme('light')
  }
}
```

- [ ] **Step 4: Add new actions to return object (around line 55)**

```typescript
return {
  // ... existing exports
  theme,
  effectiveTheme,
  initTheme,
  setTheme,
  toggleTheme,
}
```

- [ ] **Step 5: Commit**

```bash
git add frontend/src/stores/ui.ts
git commit -m "feat(dark-mode): add theme state and actions to ui store"
```

---

## Task 2: Add dark theme CSS variables to main.css

**Files:**
- Modify: `frontend/src/assets/main.css`

- [ ] **Step 1: Read current :root section (lines 6-50)**

- [ ] **Step 2: Add dark theme CSS variables after the :root block and before "/* Base Styles */" (after line 50, before line 52)**

```css
/* Dark Theme */
[data-theme="dark"] {
  /* Colors - Surface */
  --surface: #1a1a1a;
  --surface-container-low: #242424;
  --surface-container: #2a2a2a;
  --surface-container-lowest: #1f1f1f;
  --surface-container-high: #363636;

  /* Colors - On Surface */
  --on-surface: #e8e8e8;
  --on-surface-variant: #a0a0a0;

  /* Colors - Primary - slightly brighter for dark mode */
  --primary: #4a9eff;
  --primary-container: #0056b3;
  --on-primary: #ffffff;

  /* Colors - Tertiary */
  --tertiary: #ff8a5c;
  --tertiary-container: #5c2a15;
  --on-tertiary: #ffffff;

  /* Colors - Outline */
  --outline: #5a5a5a;
  --outline-variant: #3a3a3a;

  /* Glass Effect - darker for dark mode */
  .glass {
    background-color: rgba(30, 30, 30, 0.85);
  }

  /* Shadow - lighter for dark mode */
  .shadow-ambient {
    box-shadow: 0 40px 80px rgba(0, 0, 0, 0.3);
  }
}
```

- [ ] **Step 3: Add Element Plus dark mode overrides inside the [data-theme="dark"] block**

```css
  /* Element Plus Message Dark Mode */
  .el-message {
    --el-message-bg-color: #2a2a2a;
    --el-message-border-color: #3a3a3a;
    --el-message-text-color: #e8e8e8;
  }

  .el-message--error {
    --el-message-bg-color: #3d1f1f;
    --el-message-border-color: #5c3030;
    --el-message-text-color: #fca5a5;
  }

  .el-message--success {
    --el-message-bg-color: #1f3d1f;
    --el-message-border-color: #305c30;
    --el-message-text-color: #86efac;
  }

  .el-message--warning {
    --el-message-bg-color: #3d3d1f;
    --el-message-border-color: #5c5c30;
    --el-message-text-color: #fde047;
  }

  /* Element Plus Collapse Dark Mode */
  .el-collapse {
    --el-collapse-border-color: #3a3a3a;
    --el-collapse-header-bg-color: #1a1a1a;
    --el-collapse-content-bg-color: transparent;
  }

  /* Element Plus Input Dark Mode */
  .el-input__wrapper {
    background-color: #242424 !important;
  }

  .el-input__inner {
    color: #e8e8e8;
  }

  .el-input__inner::placeholder {
    color: #5a5a5a;
  }

  /* Element Plus Button Dark Mode */
  .el-button {
    --el-button-bg-color: #2a2a2a;
    --el-button-border-color: #5a5a5a;
    --el-button-text-color: #e8e8e8;
    --el-button-hover-bg-color: #363636;
    --el-button-hover-border-color: #5a5a5a;
    --el-button-hover-text-color: #ffffff;
  }
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/assets/main.css
git commit -m "feat(dark-mode): add dark theme CSS variables and Element Plus overrides"
```

---

## Task 3: Initialize theme in main.ts

**Files:**
- Modify: `frontend/src/main.ts`

- [ ] **Step 1: Read current main.ts**

- [ ] **Step 2: Import uiStore and call initTheme after app is mounted**

```typescript
// Add import at top
import { useUIStore } from '@/stores/ui'

// After app.mount('#app') call, add:
const uiStore = useUIStore()
uiStore.initTheme()
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/main.ts
git commit -m "feat(dark-mode): initialize theme on app startup"
```

---

## Task 4: Add theme toggle button to TopAppBar.vue

**Files:**
- Modify: `frontend/src/components/layout/TopAppBar.vue`

- [ ] **Step 1: Read current TopAppBar.vue to find where to add button**

- [ ] **Step 2: Add import for uiStore**

```typescript
import { useUIStore } from '@/stores/ui'
const uiStore = useUIStore()
```

- [ ] **Step 3: Add Sun/Moon button in the right section of the top bar (next to existing buttons or user menu)**

```html
<!-- Add before or after existing right-side elements -->
<button
  @click="uiStore.toggleTheme()"
  class="p-2 rounded-lg hover:bg-surface-container-high transition-colors"
  :title="uiStore.effectiveTheme === 'light' ? '切换到深色模式' : '切换到浅色模式'"
>
  <!-- Sun icon (shown in light mode, click to go dark) -->
  <svg v-if="uiStore.effectiveTheme === 'light'" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5">
    <path stroke-linecap="round" stroke-linejoin="round" d="M21.752 15.002A9.72 9.72 0 0 1 18 15.75c-5.385 0-9.75-4.365-9.75-9.75 0-1.33.266-2.597.748-3.752A9.753 9.753 0 0 0 3 11.25C3 16.635 7.365 21 12.75 21a9.753 9.753 0 0 0 9.002-5.998Z" />
  </svg>
  <!-- Moon icon (shown in dark mode, click to go light) -->
  <svg v-else xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5">
    <path stroke-linecap="round" stroke-linejoin="round" d="M3.972 12.97a9 9 0 0 1 6.055-3.995 9.751 9.751 0 0 1 6.1 3.958 9.751 9.751 0 0 1-6.1 3.958 9 9 0 0 1-6.055 3.995 9.751 9.751 0 0 1-7.402-4.318 9.751 9.751 0 0 1 7.402-4.318Z" />
  </svg>
</button>
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/layout/TopAppBar.vue
git commit -m "feat(dark-mode): add sun/moon toggle button to TopAppBar"
```

---

## Task 5: Wire SettingsView.vue to global theme state

**Files:**
- Modify: `frontend/src/views/SettingsView.vue`

- [ ] **Step 1: Read current SettingsView.vue (lines 1-19 for script setup)**

- [ ] **Step 2: Remove local theme ref, use uiStore.theme instead**

```typescript
// REMOVE line 15: const theme = ref<string>('light')
// CHANGE: use uiStore.theme directly, but add a computed for display
import { computed } from 'vue'

// Add computed to sync with uiStore
const themePreference = computed({
  get: () => uiStore.theme,
  set: (val: 'light' | 'dark' | 'system') => uiStore.setTheme(val)
})
```

- [ ] **Step 3: Update template to use themePreference instead of theme**

```html
<!-- Change :class bindings and v-model -->
<div class="flex gap-3">
  <button
    @click="themePreference = 'light'"
    class="px-4 py-2 rounded-lg text-sm transition-all"
    :class="themePreference === 'light'
      ? 'bg-primary text-white'
      : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'"
  >
    浅色
  </button>
  <button
    @click="themePreference = 'dark'"
    class="px-4 py-2 rounded-lg text-sm transition-all"
    :class="themePreference === 'dark'
      ? 'bg-primary text-white'
      : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'"
  >
    深色
  </button>
  <!-- Add system option -->
  <button
    @click="themePreference = 'system'"
    class="px-4 py-2 rounded-lg text-sm transition-all"
    :class="themePreference === 'system'
      ? 'bg-primary text-white'
      : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'"
  >
    跟随系统
  </button>
</div>
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/SettingsView.vue
git commit -m "feat(dark-mode): wire SettingsView theme to global ui store"
```

---

## Verification Checklist

After all tasks complete:

- [ ] Refresh page - theme should initialize correctly (check `<html data-theme>` attribute)
- [ ] Click TopAppBar sun/moon button - theme should toggle immediately
- [ ] Open Settings - theme selector should show current selection
- [ ] Change theme in Settings - both TopAppBar button and page should update
- [ ] Close and reopen browser - theme should persist (check localStorage)
- [ ] On macOS/Windows dark mode system - reload page with `theme=system` - should auto-select dark
- [ ] Check all pages have correct dark mode colors (Home, Article, Settings, Login, etc.)
- [ ] Check Element Plus components (messages, inputs, buttons) in dark mode
