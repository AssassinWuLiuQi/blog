import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { setPublicKey } from './utils/crypto'
import componentPlugins from './plugins/components'
import './assets/main.css'
// Import SVG sprite
import 'virtual:svg-icons-register'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(componentPlugins)

// Initialize auth and encryption key AFTER pinia is installed
fetch('/api/auth/public-key')
  .then(res => res.json())
  .then(data => { if (data.data?.publicKey) setPublicKey(data.data.publicKey) })
  .catch(console.error)

app.mount('#app')

// Fetch current user after app is mounted
const authStore = useAuthStore()
authStore.fetchCurrentUser()
