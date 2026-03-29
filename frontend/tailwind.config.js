/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#003f87',
        'primary-container': '#0056b3',
        surface: '#f7f9fb',
        'surface-container-low': '#eceef0',
        'surface-container': '#eceef0',
        'surface-container-lowest': '#ffffff',
        'surface-container-high': '#dde1e6',
        'on-surface': '#191c1e',
        'on-surface-variant': '#424752',
        tertiary: '#722b00',
        'tertiary-container': '#ffdbcc',
        outline: '#c2c6d4',
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
