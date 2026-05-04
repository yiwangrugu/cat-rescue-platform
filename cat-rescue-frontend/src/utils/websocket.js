class WebSocketService {
  constructor() {
    this.ws = null
    this.isConnected = false
    this.messageHandlers = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = null
    this.heartbeatInterval = null
    this.heartbeatTimeout = null
    this.heartbeatIntervalMs = 30000 // 30秒心跳间隔
    this.heartbeatTimeoutMs = 10000   // 10秒心跳超时
  }

  getUserId() {
    try {
      // 根据应用类型获取正确的用户信息键名
      const appType = import.meta.env.VITE_APP_TYPE || 'user'
      const userKey = `${appType}_user`
      console.log('应用类型:', appType, '用户信息键名:', userKey)

      // 从localStorage获取用户信息
      const userInfo = localStorage.getItem(userKey)
      console.log('从localStorage获取的用户信息:', userInfo)

      if (userInfo) {
        const user = JSON.parse(userInfo)
        console.log('解析后的用户信息:', user)
        const userId = user.id || null
        console.log('获取到的用户ID:', userId)
        return userId
      }
      console.log('未找到用户信息')
      return null
    } catch (error) {
      console.error('获取用户ID失败:', error)
      return null
    }
  }

  connect() {
    if (this.ws && this.isConnected) {
      console.log('WebSocket已连接，跳过重复连接')
      return Promise.resolve()
    }

    return new Promise((resolve, reject) => {
      try {
        console.log('正在建立WebSocket连接...')
        this.ws = new WebSocket('ws://localhost:8080/ws')

        this.ws.onopen = () => {
          console.log('✅ WebSocket连接成功')
          this.isConnected = true
          this.reconnectAttempts = 0
          if (this.reconnectInterval) {
            clearInterval(this.reconnectInterval)
            this.reconnectInterval = null
          }

          // 发送用户ID进行身份识别
          const userId = this.getUserId()
          if (userId) {
            this.send(`USER_ID:${userId}`)
            console.log('已发送用户ID进行身份识别:', userId)
          }

          // 启动心跳机制
          this.startHeartbeat()
          resolve()
        }

        this.ws.onmessage = (event) => {
          console.log('📨 收到WebSocket消息:', event.data)

          // 处理心跳响应
          if (event.data === 'PONG') {
            console.log('💓 收到心跳PONG')
            this.resetHeartbeatTimeout()
            return
          }

          // 处理连接成功消息
          if (event.data === 'CONNECTED') {
            console.log('✅ WebSocket连接确认')
            return
          }

          // 触发所有消息处理器
          this.messageHandlers.forEach((handler, handlerId) => {
            try {
              handler(event.data)
            } catch (error) {
              console.error(`消息处理器 ${handlerId} 错误:`, error)
            }
          })
        }

        this.ws.onclose = (event) => {
          console.log('🔌 WebSocket连接关闭: 代码=' + event.code + ', 原因=' + event.reason)
          this.isConnected = false

          // 停止心跳机制
          this.stopHeartbeat()

          // 如果是正常关闭，不尝试重连
          if (event.code === 1000) {
            console.log('WebSocket正常关闭，不进行重连')
            return
          }

          this.attemptReconnect()
        }

        this.ws.onerror = (error) => {
          console.error('❌ WebSocket连接错误:', error)
          reject(new Error('WebSocket连接失败'))
        }

      } catch (error) {
        console.error('创建WebSocket失败:', error)
        reject(error)
      }
    })
  }

  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000) // 指数退避策略

      console.log(`🔄 尝试重新连接WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts}), ${delay}ms后重试`)

      this.reconnectInterval = setTimeout(() => {
        this.connect().catch(error => {
          console.error('重新连接失败:', error)
        })
      }, delay)
    } else {
      console.error('🚫 WebSocket重连次数已达上限，停止重连')

      // 通知所有消息处理器连接已断开
      this.messageHandlers.forEach((handler, handlerId) => {
        try {
          handler('CONNECTION_LOST')
        } catch (error) {
          console.error(`连接丢失通知处理器 ${handlerId} 错误:`, error)
        }
      })
    }
  }

  // 添加消息处理器
  addMessageHandler(handler) {
    const handlerId = Date.now().toString() + Math.random().toString(36).substring(2, 9)
    this.messageHandlers.set(handlerId, handler)
    console.log('注册消息处理器，ID:', handlerId, '当前处理器数量:', this.messageHandlers.size)
    return handlerId
  }

  // 移除消息处理器
  removeMessageHandler(handlerId) {
    this.messageHandlers.delete(handlerId)
  }

  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
      this.isConnected = false
    }
    if (this.reconnectInterval) {
      clearInterval(this.reconnectInterval)
      this.reconnectInterval = null
    }

    // 停止心跳机制
    this.stopHeartbeat()

    this.messageHandlers.clear()
  }

  // 启动心跳机制
  startHeartbeat() {
    this.stopHeartbeat()

    console.log('💓 启动WebSocket心跳机制')

    // 发送心跳
    this.heartbeatInterval = setInterval(() => {
      if (this.isConnected && this.ws) {
        try {
          this.ws.send('PING')
          console.log('💓 发送心跳PING')

          // 设置心跳超时检测
          this.heartbeatTimeout = setTimeout(() => {
            console.error('💔 心跳超时，连接可能已断开')
            if (this.ws) {
              this.ws.close()
            }
          }, this.heartbeatTimeoutMs)

        } catch (error) {
          console.error('发送心跳失败:', error)
        }
      }
    }, this.heartbeatIntervalMs)
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

  // 重置心跳超时（收到PONG时调用）
  resetHeartbeatTimeout() {
    if (this.heartbeatTimeout) {
      clearTimeout(this.heartbeatTimeout)
      this.heartbeatTimeout = null
    }
  }

  // 发送消息到服务器
  send(message) {
    if (!this.isConnected || !this.ws) {
      console.warn('WebSocket未连接，无法发送消息')
      return false
    }

    try {
      this.ws.send(JSON.stringify(message))
      return true
    } catch (error) {
      console.error('发送WebSocket消息失败:', error)
      return false
    }
  }

  // 添加特定类型的消息处理器
  addPostApplicationHandler(handler) {
    return this.addMessageHandler((message) => {
      console.log('帖子申请处理器检查消息:', message)
      if (message.startsWith('POST_APPLICATION_UPDATE:')) {
        console.log('帖子申请处理器匹配到消息，开始处理...')
        const data = message.substring('POST_APPLICATION_UPDATE:'.length)
        console.log('帖子申请处理器提取的数据:', data)
        handler(data)
        console.log('帖子申请处理器处理完成')
      }
    })
  }

  addPostReviewHandler(handler) {
    return this.addMessageHandler((message) => {
      if (message.startsWith('POST_REVIEW_UPDATE:')) {
        const data = message.substring('POST_REVIEW_UPDATE:'.length)
        handler(data)
      }
    })
  }

  addAdoptionApplicationHandler(handler) {
    return this.addMessageHandler((message) => {
      if (message.startsWith('ADOPTION_APPLICATION_UPDATE:')) {
        const data = message.substring('ADOPTION_APPLICATION_UPDATE:'.length)
        handler(data)
      }
    })
  }

  addAdoptionReviewHandler(handler) {
    return this.addMessageHandler((message) => {
      if (message.startsWith('ADOPTION_REVIEW_UPDATE:')) {
        const data = message.substring('ADOPTION_REVIEW_UPDATE:'.length)
        handler(data)
      }
    })
  }

  // 发送实时更新通知到后端
  notifyPostApplicationUpdate(data) {
    return this.send({ type: 'POST_APPLICATION_UPDATE', data })
  }

  notifyPostReviewUpdate(data) {
    return this.send({ type: 'POST_REVIEW_UPDATE', data })
  }

  notifyAdoptionApplicationUpdate(data) {
    return this.send({ type: 'ADOPTION_APPLICATION_UPDATE', data })
  }

  notifyAdoptionReviewUpdate(data) {
    return this.send({ type: 'ADOPTION_REVIEW_UPDATE', data })
  }
}

// 创建全局WebSocket服务实例
export const webSocketService = new WebSocketService()

// 默认导出
export default webSocketService