<template>
  <header class="app-header rescuer-header">
    <div class="header-container">
      <!-- Logo和网站名称 -->
      <div class="logo-section" @click="$router.push('/')">
        <div class="logo">🐱</div>
        <span class="site-name">流浪猫救助平台</span>
      </div>

      <!-- 主导航菜单 -->
      <nav class="main-nav">
        <el-menu 
          :default-active="activeMenu" 
          mode="horizontal" 
          class="nav-menu"
          :ellipsis="false"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/rescuer/cats">
            <el-icon><Star /></el-icon>
            <span>猫咪管理</span>
          </el-menu-item>
          <el-menu-item index="/rescues" style="position: relative;">
            <el-icon><Help /></el-icon>
            <span>救助需求</span>
            <div 
              v-if="pendingRescueCount > 0" 
              class="nav-badge"
              style="position: absolute; top: 8px; right: 8px; width: 13px; height: 13px; background-color: #f56c6c; border-radius: 50%; border: 2px solid #fff; z-index: 10;"
            ></div>
          </el-menu-item>
          <el-menu-item index="/adoption-review">
            <el-icon><Document /></el-icon>
            <span>领养审核</span>
          </el-menu-item>
          <el-menu-item index="/statistics">
            <el-icon><Monitor /></el-icon>
            <span>救助统计</span>
          </el-menu-item>
        </el-menu>
      </nav>

      <!-- 用户操作区域 -->
      <div class="user-section">
        <template v-if="authStore.isAuthenticated">
          <!-- 用户头像和菜单 -->
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="authStore.user?.avatar || '/img/default-avatar.png'" />
              <span class="username">{{ authStore.user?.username || '救助人员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="posts">
                  <el-icon><ChatDotRound /></el-icon>
                  我的帖子
                </el-dropdown-item>
                <el-dropdown-item command="favorites">
                  <el-icon><Star /></el-icon>
                  我的收藏
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <!-- 未登录状态 -->
          <div class="auth-buttons">
            <el-button link @click="$router.push('/login')">登录</el-button>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { rescueApi } from '@/api/rescue'
import eventBus from '@/utils/eventBus'
import webSocketService from '@/utils/websocket'
import {
  House,
  Star,
  Help,
  Document,
  Monitor,
  User,
  SwitchButton,
  ArrowDown,
  ChatDotRound
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 待处理救助需求数量
const pendingRescueCount = ref(0)

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

// 加载待处理救助需求数量
const loadPendingRescueCount = async () => {
  try {
    const config = {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    }
    const response = await rescueApi.getRescueList({ 
      status: '', // 不筛选状态，获取所有数据
      page: 1,
      size: 1000 // 获取所有数据来统计数量
    }, config)
    if (response.data && response.data.records) {
      // 手动筛选状态为"待处理"的救助需求
      const pendingRescues = response.data.records.filter(rescue => 
        rescue.status === '待处理' || rescue.status === 'PENDING'
      )
      pendingRescueCount.value = pendingRescues.length
      console.log('待处理救助需求数量:', pendingRescueCount.value)
      console.log('所有救助需求:', response.data.records.map(r => ({id: r.id, status: r.status})))
    }
  } catch (error) {
    console.error('加载待处理救助需求数量失败:', error)
    console.error('错误详情:', error.response?.data)
  }
}

// WebSocket消息处理器ID
let wsHandlerId = null

// 组件卸载时清理WebSocket连接
onUnmounted(() => {
  if (wsHandlerId) {
    webSocketService.removeMessageHandler(wsHandlerId)
    wsHandlerId = null
  }
})

// 组件挂载时加载数据
onMounted(async () => {
  if (authStore.isAuthenticated) {
    loadPendingRescueCount()
    
    // 使用WebSocket实时更新而不是定时轮询
    try {
      await webSocketService.connect()
      
      // 添加消息处理器
      wsHandlerId = webSocketService.addMessageHandler((message) => {
        console.log('收到WebSocket通知:', message)
        
        // 只在收到救助数据更新通知时才刷新数据
        if (message === 'RESCUE_DATA_UPDATED') {
          console.log('WebSocket触发更新待处理救助需求数量')
          loadPendingRescueCount()
        }
      })
      
      console.log('救援端导航栏WebSocket监听已设置')
    } catch (error) {
      console.error('救援端导航栏WebSocket连接失败:', error)
      console.log('WebSocket连接失败，将使用手动刷新方式')
    }
  }
})

// 监听路由变化，当切换到救助需求页面时刷新数据
watch(() => route.path, (newPath) => {
  if (newPath === '/rescues' && authStore.isAuthenticated) {
    loadPendingRescueCount()
  }
})

// 监听救助需求状态变化事件
const handleRescueStatusChange = () => {
  console.log('收到救助需求状态变化事件，刷新小红点数据')
  loadPendingRescueCount()
}

// 组件挂载时注册事件监听
onMounted(() => {
  eventBus.on('rescueStatusChanged', handleRescueStatusChange)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  eventBus.off('rescueStatusChanged', handleRescueStatusChange)
})

// 处理菜单选择
const handleMenuSelect = (index) => {
  router.push(index)
}

// 处理用户命令
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'posts':
      router.push('/posts')
      break
    case 'favorites':
      router.push('/favorites')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '确认退出', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    authStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出登录失败')
    }
  }
}
</script>

<style scoped>
.app-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.rescuer-header {
  border-bottom: 3px solid #dee1e5;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 64px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.logo-section:hover {
  opacity: 0.8;
}

.logo {
  font-size: 28px;
}

.site-name {
  font-size: 20px;
  font-weight: bold;
  color: #67C23A;
}

.main-nav {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  border-bottom: none;
}

.nav-menu .el-menu-item {
  font-size: 16px;
  padding: 0 20px;
  height: 60px;
  line-height: 60px;
}

.user-section {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #333;
}

.auth-buttons {
  display: flex;
  gap: 12px;
}

.auth-buttons .el-button {
  font-size: 14px;
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }
  
  .site-name {
    font-size: 18px;
  }
  
  .nav-menu .el-menu-item {
    padding: 0 12px;
    font-size: 14px;
  }
  
  .username {
    display: none;
  }
}
</style>