# Frontend Technical Specification

## 1. Overview

**Project Name:** The Scholar's Manuscript
**Type:** Vue 3 Blog Application
**Design System:** The Scholar's Manuscript (Academic/Editorial aesthetic)
**Target:** Modern web browsers, responsive design

---

## 2. Technology Stack

| Category | Technology | Version | Purpose |
|----------|------------|---------|---------|
| Framework | Vue.js | ^3.4.31 | Progressive JavaScript framework |
| Build Tool | Vite | ^5.4.0 | Fast development server & bundler |
| Router | Vue Router | ^4.4.3 | Client-side routing |
| State Management | Pinia | ^2.2.2 | Official Vue state management |
| Styling | Tailwind CSS | ^3.4.14 | Utility-first CSS framework |
| PostCSS | PostCSS | ^8.4.49 | CSS transformation |
| Autoprefixer | Autoprefixer | ^10.4.20 | Vendor prefix automation |
| Vue Plugin | @vitejs/plugin-vue | ^5.1.4 | Vue 3 SFC support |

---

## 3. Project Structure

```
frontend/
├── index.html              # Entry HTML
├── package.json            # Dependencies
├── vite.config.js          # Vite configuration
├── tailwind.config.js      # Tailwind configuration
├── postcss.config.js       # PostCSS configuration
├── public/                 # Static assets
│   └── ...
├── src/
│   ├── main.js             # Application entry
│   ├── App.vue             # Root component
│   ├── assets/
│   │   └── main.css        # Global styles & design tokens
│   ├── components/         # Reusable Vue components
│   ├── views/              # Page-level components
│   ├── router/
│   │   └── index.js        # Vue Router configuration
│   └── stores/             # Pinia stores
└── dist/                   # Production build output
```

---

## 4. Design Tokens

### 4.1 Color Palette

```css
/* Primary */
--primary: #003f87;
--primary-container: #0056b3;
--on-primary: #ffffff;

/* Surface */
--surface: #f7f9fb;
--surface-container-low: #eceef0;
--surface-container: #eceef0;
--surface-container-lowest: #ffffff;
--surface-container-high: #dde1e6;

/* On Surface */
--on-surface: #191c1e;
--on-surface-variant: #424752;

/* Tertiary */
--tertiary: #722b00;
--tertiary-container: #ffdbcc;
--on-tertiary: #ffffff;

/* Outline */
--outline: #c2c6d4;
--outline-variant: #c2c6d4;
```

### 4.2 Typography

| Token | Font Family | Usage |
|-------|-------------|-------|
| display | Inter, PingFang SC, Noto Sans SC | Headlines, titles |
| body | Inter, PingFang SC, Noto Sans SC | Body text |

### 4.3 Spacing Scale

| Token | Value | Use Case |
|-------|-------|----------|
| spacing-2 | 0.5rem | Tight gaps |
| spacing-4 | 1rem | Default gaps |
| spacing-6 | 1.5rem | Section internal |
| spacing-8 | 2rem | Component separation |
| spacing-12 | 3rem | Section gaps |
| spacing-16 | 4rem | Large sections |
| spacing-18 | 4.5rem | Hero spacing |
| spacing-24 | 6rem | Editorial margins |

### 4.4 Border Radius

| Token | Value |
|-------|-------|
| sm | 0.125rem |
| md | 0.375rem |
| lg | 0.5rem |

---

## 5. Core Components (To Be Built)

### 5.1 Buttons

- **Primary**: Gradient fill (primary → primary-container), 135deg angle
- **Secondary**: surface-container-high fill with on-surface text
- **Tertiary**: Pure text with primary color

### 5.2 Input Fields

- 2px bottom-bar style (underline only, not full border)
- Default: outline_variant color
- Focus: primary color transition

### 5.3 Cards

- Background: surface-container-lowest
- Border-radius: md (0.375rem)
- No border lines (use background shifts for separation)

### 5.4 Special Components

- **Manuscript Note**: tertiary-container background with tertiary left-accent bar

---

## 6. Routing

Router configured with `createWebHistory` (HTML5 history mode).

Routes to be defined:
- `/` - Home/Blog list
- `/article/:id` - Article detail
- `/about` - About page
- `/archive` - Archive page

---

## 7. State Management (Pinia)

Stores to be created:
- `usePostStore` - Blog posts data
- `useUIStore` - UI state (sidebar, theme)
- `useAuthStore` - Authentication state (if needed)

---

## 8. Development Commands

```bash
# Install dependencies
npm install

# Development server (http://localhost:5173)
npm run dev

# Production build
npm run build

# Preview production build
npm run preview
```

---

## 9. Tailwind Integration

The `tailwind.config.js` extends the default theme with custom design tokens:

- Custom colors mapped to design tokens
- Custom font families
- Custom spacing scale
- Custom border radius

---

## 10. Design Principles

1. **No 1px Borders**: Use background shifts instead of lines
2. **Intentional Asymmetry**: Generous left margins (spacing-24)
3. **Glassmorphism**: 80% opacity + 20px blur for navigation
4. **Gradient CTAs**: Primary buttons use 135deg gradient
5. **Tonal Depth**: Layer surfaces instead of using shadows heavily
