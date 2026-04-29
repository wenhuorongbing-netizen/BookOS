import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ElLoadingDirective, ElMessage } from 'element-plus'
import 'element-plus/es/components/loading/style/css'
import 'element-plus/es/components/message/style/css'
import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)

app.config.errorHandler = (error) => {
  console.error('[BookOS frontend error]', error)
  ElMessage.error('Something went wrong in the interface. Check the console for details.')
}

window.addEventListener('unhandledrejection', (event) => {
  console.error('[BookOS unhandled promise rejection]', event.reason)
})

app.use(createPinia())
app.use(router)
app.directive('loading', ElLoadingDirective)
app.mount('#app')
