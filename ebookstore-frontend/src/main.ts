import { createApp } from 'vue'
import { createPinia } from 'pinia'
// Element Plus 组件/样式按需引入,这里只保留全局基础样式
import 'element-plus/theme-chalk/base.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)

app.mount('#app')