import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { existsSync, mkdirSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs'
import { join, relative, resolve } from 'node:path'

export default defineConfig({
  publicDir: false,
  plugins: [vue(), publicAssetsPlugin()],
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

function publicAssetsPlugin() {
  let config

  function copyDir(source, target) {
    if (!existsSync(source)) {
      return
    }
    mkdirSync(target, { recursive: true })
    for (const entry of readdirSync(source)) {
      const sourcePath = join(source, entry)
      const targetPath = join(target, entry)
      if (statSync(sourcePath).isDirectory()) {
        copyDir(sourcePath, targetPath)
      } else {
        writeFileSync(targetPath, readFileSync(sourcePath))
      }
    }
  }

  return {
    name: 'mall-public-assets',
    configResolved(resolvedConfig) {
      config = resolvedConfig
    },
    configureServer(server) {
      const publicRoot = resolve(server.config.root, 'public')
      server.middlewares.use((request, response, next) => {
        if (!request.url?.startsWith('/images/')) {
          next()
          return
        }
        const pathname = decodeURIComponent(new URL(request.url, 'http://localhost').pathname)
        const filePath = resolve(publicRoot, pathname.slice(1))
        if (relative(publicRoot, filePath).startsWith('..') || !existsSync(filePath) || statSync(filePath).isDirectory()) {
          next()
          return
        }
        if (filePath.endsWith('.webp')) {
          response.setHeader('Content-Type', 'image/webp')
        }
        response.end(readFileSync(filePath))
      })
    },
    closeBundle() {
      const publicRoot = resolve(config.root, 'public')
      const outRoot = resolve(config.root, config.build.outDir)
      copyDir(publicRoot, outRoot)
    }
  }
}
