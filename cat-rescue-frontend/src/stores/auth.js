import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

const getAppPrefix = () => {
  const appType = import.meta.env.VITE_APP_TYPE || 'user'
  return `${appType}_`
}

const TOKEN_KEY = () => `${getAppPrefix()}token`
const USER_KEY = () => `${getAppPrefix()}user`

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY()))
  const user = ref(JSON.parse(localStorage.getItem(USER_KEY()) || 'null'))

  const isAuthenticated = computed(() => !!token.value)

  // 计算属性：检查用户角色
  const isRescuer = computed(() => {
    // 如果用户信息中有明确的角色字段，使用角色字段（不区分大小写）
    if (user.value?.role) {
      const role = user.value.role.toLowerCase()
      return role === 'rescuer' || role === 'admin'
    }

    // 如果没有角色字段，根据邮箱或用户名判断
    const email = user.value?.email || ''
    const username = user.value?.username || ''

    // 如果邮箱包含 "rescuer" 或用户名是救助人员特征
    const isRescuerByEmail = email.toLowerCase().includes('rescuer')
    const isRescuerByUsername = username === '222' // 这个用户名是救助人员

    return isRescuerByEmail || isRescuerByUsername
  })

  const isAdmin = computed(() => {
    // 如果用户信息中有明确的角色字段，使用角色字段（不区分大小写）
    if (user.value?.role) {
      const role = user.value.role.toLowerCase()
      return role === 'admin'
    }

    // 如果没有角色字段，根据邮箱或用户名判断
    const email = user.value?.email || ''
    const username = user.value?.username || ''

    // 如果邮箱包含 "admin" 或用户名是管理员特征
    const isAdminByEmail = email.toLowerCase().includes('admin')
    const isAdminByUsername = username.toLowerCase().includes('admin')

    return isAdminByEmail || isAdminByUsername
  })
  const isLoggedIn = computed(() => {
    // 检查token是否存在
    if (!token.value) return false

    // 检查用户状态是否为ACTIVE
    if (user.value?.status !== 'ACTIVE') {
      // 如果用户状态不是ACTIVE，自动登出
      logout()
      return false
    }

    return true
  })

  const login = async (credentials) => {
    try {
      const response = await authApi.login(credentials)

      token.value = response.data.token
      user.value = response.data.user
      // 权限状态现在通过计算属性自动更新，不需要手动设置

      // 登录后检查用户是否被禁用
      const banStatus = checkUserBanStatus()
      if (banStatus.isBanned) {
        // 如果用户被禁用，清除登录状态并抛出错误
        logout()
        throw new Error(banStatus.message)
      }

      localStorage.setItem(TOKEN_KEY(), token.value)
      localStorage.setItem(USER_KEY(), JSON.stringify(user.value))

      return response
    } catch (error) {
      throw error
    }
  }

  const register = async (userData) => {
    try {
      const response = await authApi.register(userData)

      token.value = response.data.token
      user.value = response.data.user
      // 权限状态现在通过计算属性自动更新，不需要手动设置

      localStorage.setItem(TOKEN_KEY(), token.value)
      localStorage.setItem(USER_KEY(), JSON.stringify(user.value))

      return response
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    token.value = null
    user.value = null
    // 权限状态现在通过计算属性自动更新，不需要手动设置

    localStorage.removeItem(TOKEN_KEY())
    localStorage.removeItem(USER_KEY())
  }

  // 检查用户是否被禁用
  const checkUserBanStatus = () => {
    if (!user.value) return false

    // 检查用户状态
    if (user.value.status === 'INACTIVE') {
      // 检查是否有禁用结束时间
      if (user.value.banEndTime) {
        const now = new Date()
        const endTime = new Date(user.value.banEndTime)

        if (now < endTime) {
          // 用户仍在禁用期内
          return {
            isBanned: true,
            endTime: endTime,
            message: `您因违反猫猫社区规章制度被限制登录，请于 ${formatTime(endTime)} 后重新尝试`
          }
        } else {
          // 禁用期已过，自动启用用户
          user.value.status = 'ACTIVE'
          user.value.banEndTime = null
          localStorage.setItem(USER_KEY(), JSON.stringify(user.value))
          return { isBanned: false }
        }
      } else {
        // 永久禁用
        return {
          isBanned: true,
          message: '您的账户已被永久禁用，请联系管理员'
        }
      }
    }

    return { isBanned: false }
  }

  // 格式化时间显示
  const formatTime = (date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  }

  // 尝试自动登录（从localStorage恢复登录状态）
  const tryAutoLogin = () => {
    const storedToken = localStorage.getItem(TOKEN_KEY())
    const storedUser = localStorage.getItem(USER_KEY())

    if (storedToken && storedUser) {
      try {
        token.value = storedToken
        user.value = JSON.parse(storedUser)

        // 自动登录时检查用户是否被禁用
        const banStatus = checkUserBanStatus()
        if (banStatus.isBanned) {
          console.log('用户被禁用，禁止自动登录:', banStatus.message)
          logout()
          return { success: false, message: banStatus.message }
        }

        // 权限状态现在通过计算属性自动更新，不需要手动设置
        console.log('自动登录成功，用户:', user.value?.username)
        return { success: true }
      } catch (error) {
        console.error('自动登录失败:', error)
        // 清除无效的存储数据
        localStorage.removeItem(TOKEN_KEY())
        localStorage.removeItem(USER_KEY())
        return { success: false, message: '自动登录失败' }
      }
    } else {
      console.log('没有找到有效的登录信息')
      return { success: false, message: '没有找到有效的登录信息' }
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    isRescuer,
    isAdmin,
    isLoggedIn,
    login,
    register,
    logout,
    tryAutoLogin,
    checkUserBanStatus
  }
})