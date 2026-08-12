/// <reference types="vitest/config" />
import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
// 测试环境(Vitest)下不注入 Element Plus 样式,避免 Node 解析 .css 失败
const isTest = process.env.VITEST === 'true'

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    // Element Plus 按需引入(组件 + 样式 + ElMessage 等函数)
    AutoImport({
      resolvers: [ElementPlusResolver({ importStyle: isTest ? false : 'css' })],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [ElementPlusResolver({ importStyle: isTest ? false : 'css' })],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    css: false,
    environmentOptions: {
      jsdom: {
        url: 'http://localhost/',
      },
    },
  },
})
