import { fileURLToPath, URL } from 'node:url'
import { resolve } from 'node:path'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import compression from 'vite-plugin-compression'

const __dirname = fileURLToPath(new URL('.', import.meta.url))

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    // Element Plus 按需导入：只打包实际用到的组件
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    // Brotli 预压缩：构建时生成 .br 文件，Nginx 直接返回，省 CPU
    compression({
      algorithm: 'brotliCompress',
      ext: '.br',
      threshold: 1024,  // 大于 1KB 才压缩
      deleteOriginals: false,  // 保留原始文件作为 fallback
    }),
    compression({
      algorithm: 'gzip',
      ext: '.gz',
      threshold: 1024,
      deleteOriginals: false,
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    assetsInlineLimit: 4096,
    // 多入口：着陆页 landing.html 独立打包，避免拖入主应用 347KB 主包
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),
        landing: resolve(__dirname, 'landing.html'),
      },
      output: {
        // 不做手动分包：重型依赖已通过 defineAsyncComponent 拆分（MarkdownRenderer, CommentList, TableOfContents）
        // manualChunks 容易导致循环依赖和不可预期的合并（如 mermaid 被打进首页 chunk）
      },
    },
  },
  base: '/',
})
