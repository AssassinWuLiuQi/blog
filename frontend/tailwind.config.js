/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: 'var(--primary)',
        'primary-container': 'var(--primary-container)',
        'primary/8': 'rgba(0, 63, 135, 0.08)',
        surface: 'var(--surface)',
        background: 'var(--surface)',
        'surface-container-low': 'var(--surface-container-low)',
        'surface-container': 'var(--surface-container)',
        'surface-container-lowest': 'var(--surface-container-lowest)',
        'surface-container-high': 'var(--surface-container-high)',
        'on-surface': 'var(--on-surface)',
        'on-surface-variant': 'var(--on-surface-variant)',
        tertiary: 'var(--tertiary)',
        'tertiary-container': 'var(--tertiary-container)',
        outline: 'var(--outline)',
        'outline-variant': 'var(--outline-variant)',
        error: '#dc2626',
        'error/5': 'rgba(220, 38, 38, 0.05)',
        success: '#16a34a',
        'success/10': 'rgba(22, 163, 74, 0.1)',
      },
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
