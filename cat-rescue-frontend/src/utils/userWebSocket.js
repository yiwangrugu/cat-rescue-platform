import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

// WebSocket服务单例
let instance = null

class UserWebSocketService {
  constructor() {
    if (instance) {
      return instance
    }
    instance = this

    this.ws = null
    this.isConnected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 10  // 增加重连次数
    this.reconnectInterval = null
    this.messageHandlers = new Map()
    this.heartbeatInterval = null
    this.heartbeatTimeout = null

    // 页面可见性变化时重新连接
    this.setupVisibilityChangeHandler()
  }

  // 设置页面可见性变化处理器
  setupVisibilityChangeHandler() {
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible' && !this.isConnected) {
        this.attemptReconnect()
      }
    })
  }

  // 连接WebSocket
  connect() {
    return new Promise((resolve, reject) => {
      if (this.isConnected && this.ws) {
        resolve()
        return
      }

      const authStore = useAuthStore()
      const token = authStore.token

      if (!token) {
        reject(new Error('用户未登录，无法连接WebSocket'))
        return
      }

      try {
        this.ws = new WebSocket(`ws://localhost:8080/ws/user?token=${token}`)

        this.ws.onopen = () => {
          this.isConnected = true
          this.reconnectAttempts = 0
          this.startHeartbeat()
          resolve()
        }

        this.ws.onmessage = (event) => {
          this.handleMessage(event.data)
        }

        this.ws.onclose = (event) => {
          this.isConnected = false
          this.stopHeartbeat()

          if (event.code !== 1000) {
            this.attemptReconnect()
          }
        }

        this.ws.onerror = (error) => {
          this.isConnected = false
          reject(new Error('WebSocket连接失败'))
        }

        // 设置连接超时
        setTimeout(() => {
          if (!this.isConnected) {
            this.ws.close()
            reject(new Error('WebSocket连接超时'))
          }
        }, 5000)

      } catch (error) {
        reject(error)
      }
    })
  }

  // 启动心跳机制
  startHeartbeat() {
    this.stopHeartbeat()

    // 每30秒发送一次心跳
    this.heartbeatInterval = setInterval(() => {
      if (this.ws && this.isConnected) {
        this.ws.send('PING')

        // 设置心跳超时检测
        this.heartbeatTimeout = setTimeout(() => {
          this.ws.close()
        }, 10000) // 10秒超时
      }
    }, 30000)
  }

  // 停止心跳机制
  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
    if (this.heartbeatTimeout) {
      clearTimeout(this.heartbeatTimeout)
      this.heartbeatTimeout = null
    }
  }

  // 尝试重新连接
  attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      return
    }

    if (this.reconnectInterval) {
      return // 已经在重连中
    }

    this.reconnectInterval = setInterval(() => {
      this.reconnectAttempts++

      this.connect()
        .then(() => {
          clearInterval(this.reconnectInterval)
          this.reconnectInterval = null
        })
        .catch((error) => {
          if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            clearInterval(this.reconnectInterval)
            this.reconnectInterval = null
          }
        })
    }, 5000) // 5秒重连一次
  }

  // 处理消息
  handleMessage(data) {
    // 处理心跳响应
    if (data === 'PONG') {
      if (this.heartbeatTimeout) {
        clearTimeout(this.heartbeatTimeout)
        this.heartbeatTimeout = null
      }
      return
    }

    // 处理连接确认
    if (data === 'USER_CONNECTED') {
      return
    }

    // 处理用户被禁用消息
    if (typeof data === 'string' && data.startsWith('USER_BANNED|')) {
      const parts = data.split('|')
      if (parts.length >= 3) {
        const userId = parts[1]
        const endTime = parts[2]
        this.handleUserBanned(userId, endTime)
      }
      return
    }

    // 处理用户启用消息
    if (typeof data === 'string' && data.startsWith('USER_UNBANNED|')) {
      const parts = data.split('|')
      if (parts.length >= 2) {
        const userId = parts[1]
        this.handleUserUnbanned(userId)
      }
      return
    }

    // 处理状态更新消息
    if (data === 'USER_STATUS_UPDATED') {
      return
    }

    // 调用注册的消息处理器
    this.messageHandlers.forEach((handler) => {
      try {
        handler(data)
      } catch (error) {
      }
    })
  }

  // 处理纯文本消息
  handleTextMessage(message) {
    console.log('📨 处理纯文本消息:', message)

    if (message.startsWith('USER_BANNED|')) {
      // 解析格式: USER_BANNED|userId|endTime
      const parts = message.split('|')
      if (parts.length >= 3) {
        const data = {
          userId: parts[1],
          endTime: parts[2]
        }
        this.handleUserBanned(data)
      }
    } else if (message.startsWith('USER_UNBANNED|')) {
      // 解析格式: USER_UNBANNED|userId
      const parts = message.split('|')
      if (parts.length >= 2) {
        const data = {
          userId: parts[1]
        }
        this.handleUserUnbanned(data)
      }
    } else if (message === 'USER_STATUS_UPDATED') {
      // 用户状态更新通知
      this.handleUserStatusChanged({})
    } else if (message === 'USER_CONNECTED') {
      console.log('✅ 用户状态WebSocket连接已确认')
    } else if (message === 'PONG') {
      console.log('🏓 收到心跳响应')
      // 清除心跳超时
      if (this.heartbeatTimeout) {
        clearTimeout(this.heartbeatTimeout)
        this.heartbeatTimeout = null
      }
    } else if (message === 'PING') {
      // 收到心跳请求，回复PONG
      if (this.ws && this.isConnected) {
        this.ws.send('PONG')
        console.log('🏓 回复心跳')
      }
    } else {
      console.log('未知的纯文本消息:', message)
    }
  }

  // 处理用户被禁用消息
  handleUserBanned(data) {
    console.log('🔴 处理用户被禁用消息:', data)
    const authStore = useAuthStore()

    console.log('🔍 当前登录用户信息:', authStore.user)
    console.log('🔍 用户ID匹配检查:', {
      '前端用户ID': authStore.user?.id,
      '后端发送用户ID': data.userId,
      '匹配结果': authStore.user && String(authStore.user.id) === String(data.userId)
    })

    // 更新前端用户状态为INACTIVE
    if (authStore.user && String(authStore.user.id) === String(data.userId)) {
      console.log('🔍 更新前端用户状态为INACTIVE')
      authStore.user.status = 'INACTIVE'
      authStore.user.banEndTime = data.endTime

      // 更新localStorage中的用户信息
      const userKey = `${import.meta.env.VITE_APP_TYPE || 'user'}_user`
      localStorage.setItem(userKey, JSON.stringify(authStore.user))

      console.log('🔍 前端用户状态已更新:', authStore.user)
    }

    // 检查是否是当前用户被禁用
    if (authStore.user && String(authStore.user.id) === String(data.userId)) {
      console.log('🔍 开始处理用户被禁用消息，数据:', data)

      const endTime = new Date(data.endTime)
      console.log('🔍 原始结束时间:', data.endTime)
      console.log('🔍 解析后的结束时间:', endTime)
      console.log('🔍 时间是否有效:', !isNaN(endTime.getTime()))

      const formattedTime = endTime.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
      console.log('🔍 格式化后的时间:', formattedTime)

      const reason = data.reason || '违反猫猫社区规章制度'
      const message = `您因${reason}被限制登录，请于 ${formattedTime} 后重新尝试。\n\n如有疑问，请联系客服电话：10086`
      console.log('🔍 最终提示消息:', message)

      // 立即强制退出登录
      authStore.logout()

      // 存储禁用信息到sessionStorage，以便登录页面显示
      sessionStorage.setItem('userBannedInfo', JSON.stringify({
        message: message,
        timestamp: new Date().getTime()
      }))

      // 跳转到登录页面
      window.location.href = '/login'
    }

    // 触发消息处理器
    this.messageHandlers.forEach((handler) => {
      try {
        handler('USER_BANNED', data)
      } catch (error) {
        console.error('消息处理器错误:', error)
      }
    })
  }

  // 处理用户解禁消息
  handleUserUnbanned(data) {
    const { userId, username } = data
    const authStore = useAuthStore()

    // 更新前端用户状态为ACTIVE
    if (authStore.user && String(authStore.user.id) === String(userId)) {
      console.log('🔍 更新前端用户状态为ACTIVE')
      authStore.user.status = 'ACTIVE'
      authStore.user.banEndTime = null

      // 更新localStorage中的用户信息
      const userKey = `${import.meta.env.VITE_APP_TYPE || 'user'}_user`
      localStorage.setItem(userKey, JSON.stringify(authStore.user))

      console.log('🔍 前端用户状态已更新:', authStore.user)
    }

    // 触发消息处理器
    this.messageHandlers.forEach((handler) => {
      try {
        handler('USER_UNBANNED', data)
      } catch (error) {
        console.error('消息处理器错误:', error)
      }
    })
  }

  // 处理用户状态变化消息
  handleUserStatusChanged(data) {
    const { userId, status } = data

    // 触发消息处理器
    this.messageHandlers.forEach((handler) => {
      try {
        handler('USER_STATUS_CHANGED', data)
      } catch (error) {
        console.error('消息处理器错误:', error)
      }
    })
  }

  // 发送消息
  send(message) {
    if (!this.isConnected || !this.ws) {
      console.error('WebSocket未连接，无法发送消息')
      return false
    }

    try {
      const messageStr = typeof message === 'string' ? message : JSON.stringify(message)
      this.ws.send(messageStr)
      return true
    } catch (error) {
      console.error('发送WebSocket消息失败:', error)
      return false
    }
  }

  // 添加消息处理器
  addMessageHandler(handlerId, handler) {
    this.messageHandlers.set(handlerId, handler)
  }

  // 移除消息处理器
  removeMessageHandler(handlerId) {
    this.messageHandlers.delete(handlerId)
  }

  // 关闭连接
  disconnect() {
    if (this.ws) {
      this.ws.close(1000, '正常关闭')
      this.ws = null
    }
    this.isConnected = false

    if (this.reconnectInterval) {
      clearInterval(this.reconnectInterval)
      this.reconnectInterval = null
    }
  }
}

// 创建全局实例
export const userWebSocketService = new UserWebSocketService()

// 默认导出
// eslint-disable-next-line import/no-anonymous-default-export
export default userWebSocketService