import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const getAppPrefix = () => {
  const appType = import.meta.env.VITE_APP_TYPE || 'user'
  return `${appType}_`
}

const TOKEN_KEY = () => `${getAppPrefix()}token`

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

// 请求拦截器 - 添加认证token
api.interceptors.request.use(
  (config) => {
    try {
      console.log('=== 请求拦截器 ===')
      console.log('原始URL:', config.url)
      console.log('方法:', config.method)

      // 清理URL中的:1后缀
      if (config.url && config.url.includes('/comments:')) {
        config.url = config.url.replace(/\/comments:.*$/, '/comments')
        console.log('清理后的URL:', config.url)
      }

      // 在组件外使用auth store时，直接从localStorage获取token
      const token = localStorage.getItem(TOKEN_KEY())
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    } catch (error) {
      console.warn('获取认证token失败:', error)
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理认证错误
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 导出所有API模块
export * from './auth'
export * from './cat'
export * from './rescue'
export * from './adoption'
export * from './community'
export * from './admin'

export default api