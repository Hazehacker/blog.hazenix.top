import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  // 構建配置
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false, // 生產環境關閉 sourcemap
    // 確保構建後的資源路徑正確
    assetsInlineLimit: 4096, // 小於 4kb 的資源內聯為 base64
  },
  // 基礎路徑配置（如果部署在子路徑下，修改此配置）
  // 例如：如果部署在 https://example.com/blog/，則設置 base: '/blog/'
  base: '/',
})
