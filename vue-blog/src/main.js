import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import { createPinia } from 'pinia'
import vueDanmaku from 'vue3-danmaku'
import App from './App.vue'
import router from './router'

// 生產環境關閉所有 console 輸出以避免資訊外洩
// if (import.meta.env && import.meta.env.PROD) {
//   const noop = () => {}
//   ;['log', 'info', 'warn', 'error', 'debug'].forEach((method) => {
//     try {
//       // eslint-disable-next-line no-console
//       console[method] = noop
//     } catch (_) {
//       /* ignore */
//     }
//   })
// }

const app = createApp(App)

app.use(createPinia())
app.use(ElementPlus)
app.use(router)

// 将 vue3-danmaku 组件注册为全局组件
app.component('vue-danmaku', vueDanmaku)

app.mount('#app')
