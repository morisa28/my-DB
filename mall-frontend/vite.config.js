import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('@element-plus/icons-vue')) {
              return 'vendor-element-icons'
            }
            if (id.includes('element-plus/es/components')) {
              const component = id.split('element-plus/es/components/')[1]?.split('/')[0]
              return component ? `vendor-ep-${component}` : 'vendor-element-plus'
            }
            if (id.includes('element-plus')) {
              return 'vendor-ep-core'
            }
            if (id.includes('/vue') || id.includes('pinia') || id.includes('vue-router')) {
              return 'vendor-vue'
            }
            if (id.includes('axios')) {
              return 'vendor-http'
            }
            return 'vendor'
          }
        }
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
