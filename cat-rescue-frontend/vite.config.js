import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// 多应用配置 - 前端多端口，后端单端口
const apps = {
  user: {
    port: 3000,
    backendPort: 8080,
    title: '流浪猫救助平台 - 用户端',
    htmlFile: 'user.html',
    mainFile: '/src/main-user.js'
  },
  rescuer: {
    port: 3001,
    backendPort: 8080,
    title: '流浪猫救助平台 - 救助端',
    htmlFile: 'rescuer.html',
    mainFile: '/src/main-rescuer.js'
  },
  admin: {
    port: 3002,
    backendPort: 8080,
    title: '流浪猫救助平台 - 管理端',
    htmlFile: 'admin.html',
    mainFile: '/src/main-admin.js'
  }
}

// 获取当前应用配置（通过环境变量或默认用户端）
const currentApp = process.env.VITE_APP_TYPE || 'user'
const appConfig = apps[currentApp]

export default defineConfig({
  plugins: [vue()],
  // 配置base URL
  base: '/',
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  define: {
    'import.meta.env.VITE_APP_TYPE': JSON.stringify(currentApp),
    'import.meta.env.VITE_APP_TITLE': JSON.stringify(appConfig.title)
  },
  server: {
    host: '0.0.0.0',
    port: appConfig.port,
    // 开发模式下使用对应的HTML文件
    open: appConfig.htmlFile,
    // 配置静态资源服务
    fs: {
      allow: ['..']
    },
    // 配置静态文件服务
    middlewareMode: false,
    proxy: {
      '/api': {
        target: `http://localhost:${appConfig.backendPort}`,
        changeOrigin: true
      },
      '/uploads': {
        target: `http://localhost:${appConfig.backendPort}`,
        changeOrigin: true
      }
    }
  },
  // 配置public目录作为静态资源目录
  publicDir: 'src/public',
  build: {
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),
        user: resolve(__dirname, 'user.html'),
        rescuer: resolve(__dirname, 'rescuer.html'),
        admin: resolve(__dirname, 'admin.html')
      }
    }
  }
})