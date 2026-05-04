import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加认证token
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 统一处理成功响应
    if (response.data && response.data.code === 200) {
      return response.data
    }
    return response.data
  },
  (error) => {
    // 统一处理错误响应
    const { status, data } = error.response || {}
    
    // 认证错误
    if (status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    
    // 权限错误
    if (status === 403) {
      ElMessage.error('没有权限执行此操作')
      return Promise.reject(error)
    }
    
    // 服务器错误
    if (status >= 500) {
      ElMessage.error('服务器错误，请稍后重试')
      return Promise.reject(error)
    }
    
    // 其他错误
    const message = data?.message || error.message || '请求失败'
    ElMessage.error(message)
    
    return Promise.reject(error)
  }
)

export default request