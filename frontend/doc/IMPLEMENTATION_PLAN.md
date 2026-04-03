# Frontend Implementation Plan

## UI Analysis Summary

Based on `ui/code.html`, the design includes:

| Section | Components |
|---------|------------|
| **SideNavBar** | Logo, Navigation items (首页, Editor-TTS, 子菜单), Settings |
| **TopAppBar** | Section title, Search input, User profile |
| **Main Content** | Split layout: Editor (3/4) + TTS Panel (1/4) |
| **Editor** | Toolbar, Auto-save status, Export button, Article content |
| **TTS Panel** | Voice profile, Playback controls, Text analysis |
| **Footer** | System status |

---

## Phase 1: Project Foundation

### 1.1 Router Configuration
- [ ] Configure Vue Router with routes:
  - `/` - Home (首页)
  - `/tech-preview` - Technical Preview list (Editor-TTS)
  - `/tech-preview/:category` - Category view (e.g., 文本处理)
  - `/article/:id` - Article detail
  - `/archive` - Archive
  - `/settings` - Settings

### 1.2 Pinia Stores
- [ ] `usePostStore` - Posts data, categories
- [ ] `useUIStore` - Sidebar state, current section
- [ ] `useAuthStore` - User authentication state (mock)

### 1.3 Layout Components
- [ ] `AppLayout.vue` - Main layout wrapper
- [ ] `SideNavBar.vue` - Left sidebar navigation
- [ ] `TopAppBar.vue` - Top header bar

---

## Phase 2: Core Components

### 2.1 Navigation Components
- [ ] `NavItem.vue` - Single nav item with active state
- [ ] `NavGroup.vue` - Expandable nav group with sub-items
- [ ] `UserProfile.vue` - Avatar and user info

### 2.2 Editor Components (from code.html)
- [ ] `TextEditor.vue` - Rich text editor container
- [ ] `EditorToolbar.vue` - Formatting toolbar (bold, italic, lists, etc.)
- [ ] `EditorContent.vue` - Article content display

### 2.3 TTS Components
- [ ] `TTSPanel.vue` - TTS reader container
- [ ] `VoiceProfile.vue` - Voice selection card
- [ ] `PlaybackControls.vue` - Play/pause, progress, skip controls
- [ ] `TextAnalysis.vue` - Text metadata display

### 2.4 Common Components
- [ ] `SearchInput.vue` - Styled search input
- [ ] `GradientButton.vue` - Primary CTA button
- [ ] `SurfaceCard.vue` - Card with surface-container styling

---

## Phase 3: Pages / Views

### 3.1 Home Page (`/`)
- Hero section with welcome message
- Recent posts list
- Category highlights

### 3.2 Technical Preview (`/tech-preview`)
- Category navigation
- Article grid/list
- Sub-category filtering (文本处理, etc.)

### 3.3 Article Detail (`/article/:id`)
- Full article view with rich text
- Embedded TTS reader panel
- Related articles

### 3.4 Archive (`/archive`)
- Chronological post listing
- Tag cloud
- Search/filter

### 3.5 Settings (`/settings`)
- User preferences
- Voice settings
- Theme toggle (future)

---

## Phase 4: Styling & Design Tokens

### 4.1 CSS Integration
- [ ] Extend `main.css` with component-specific styles
- [ ] Ensure Tailwind config aligns with DESIGN.md tokens
- [ ] Add Material Symbols integration

### 4.2 Responsive Design
- [ ] Sidebar collapsible on mobile
- [ ] TTS panel moves below editor on narrow screens
- [ ] Touch-friendly controls

---

## File Structure (Target)

```
frontend/src/
├── main.js
├── App.vue
├── assets/
│   └── main.css
├── router/
│   └── index.js
├── stores/
│   ├── post.js
│   ├── ui.js
│   └── auth.js
├── components/
│   ├── layout/
│   │   ├── AppLayout.vue
│   │   ├── SideNavBar.vue
│   │   └── TopAppBar.vue
│   ├── nav/
│   │   ├── NavItem.vue
│   │   ├── NavGroup.vue
│   │   └── UserProfile.vue
│   ├── editor/
│   │   ├── TextEditor.vue
│   │   ├── EditorToolbar.vue
│   │   └── EditorContent.vue
│   ├── tts/
│   │   ├── TTSPanel.vue
│   │   ├── VoiceProfile.vue
│   │   ├── PlaybackControls.vue
│   │   └── TextAnalysis.vue
│   └── common/
│       ├── SearchInput.vue
│       ├── GradientButton.vue
│       └── SurfaceCard.vue
└── views/
    ├── HomeView.vue
    ├── TechPreviewView.vue
    ├── ArticleView.vue
    ├── ArchiveView.vue
    └── SettingsView.vue
```

---

## Implementation Order

1. **Router + Stores** - Foundation
2. **Layout components** - SideNavBar, TopAppBar
3. **Common components** - Buttons, Cards, SearchInput
4. **Editor components** - Toolbar, Content
5. **TTS components** - Panel, Controls
6. **Views/Pages** - Assemble all components
7. **Styling polish** - Responsive, animations

---

## Notes

- Material Symbols use: `<span class="material-symbols-outlined">icon_name</span>`
- Gradient buttons: `from-primary to-primary-container`
- Glass effect: `bg-white/80 backdrop-blur-md`
- Chinese text: `chinese-manuscript` class with `line-height: 1.8`
